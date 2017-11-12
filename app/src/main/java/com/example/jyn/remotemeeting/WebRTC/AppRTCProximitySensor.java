package com.example.jyn.remotemeeting.WebRTC;

/**
 * Created by JYN on 2017-11-10.
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import com.example.jyn.remotemeeting.WebRTC.Util.AppRTCUtils;

import org.webrtc.ThreadUtils;

/**
 * AppRTCProximitySensor manages functions related to the proximity sensor in the AppRTC demo.
 * On most device, the proximity sensor is implemented as a boolean-sensor.
 * It returns just two values "NEAR" or "FAR". Thresholding is done on the LUX
 * value i.e. the LUX value of the light sensor is compared with a threshold.
 * A LUX-value more than the threshold means the proximity sensor returns "FAR".
 * Anything less than the threshold value and the sensor  returns "NEAR".
 * <p>
 * AppRTCProximitySensor는 AppRTC 데모에서 근접 센서와 관련된 기능을 관리합니다.
 * 대부분의 장치에서 근접 센서는 boolean-sensor로 구현됩니다.
 * "NEAR"또는 "FAR"의 두 값만 반환합니다. 임계 값은 LUX 값에 대해 수행되고, 즉 광 센서의 LUX 값은 임계 값과 비교된다.
 * 임계 값보다 큰 LUX 값은 근접 센서가 "FAR"을 반환 함을 의미합니다.
 * 임계 값보다 작은 값이면 센서는 "NEAR"를 반환합니다.
 */
public class AppRTCProximitySensor implements SensorEventListener {

    private static final String TAG = "AppRTCProximitySensor";

    // This class should be created, started and stopped on one thread (e.g. the main thread).
    // We use |nonThreadSafe| to ensure that this is the case.
    // Only active when |DEBUG| is set to true.
    //
    // 이 클래스는 하나의 스레드 (예 : 메인 스레드)에서 생성, 시작 및 중지되어야합니다.
    // 우리는 |nonThreadSafe| 이 경우를 보장합니다.
    // 활성화 될 때만 |DEBUG| true로 설정됩니다.
    private final ThreadUtils.ThreadChecker threadChecker = new ThreadUtils.ThreadChecker();

    private final Runnable onSensorStateListener;
    private final SensorManager sensorManager;
    private Sensor proximitySensor = null;
    private boolean lastStateReportIsNear = false;

    /**
     * Construction
     */
    static AppRTCProximitySensor create(Context context, Runnable sensorStateListener) {
        return new AppRTCProximitySensor(context, sensorStateListener);
    }

    private AppRTCProximitySensor(Context context, Runnable sensorStateListener) {
        Log.d(TAG, "AppRTCProximitySensor" + AppRTCUtils.getThreadInfo());
        onSensorStateListener = sensorStateListener;
        sensorManager = ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
    }


    /**
     * Activate the proximity sensor. Also do initialization if called for the first time.
     * 근접 센서를 활성화하십시오. 또한 처음 호출 될 경우 초기화를 수행하십시오.
     */
    public boolean start() {
        threadChecker.checkIsOnValidThread();
        Log.d(TAG, "start" + AppRTCUtils.getThreadInfo());
        if (!initDefaultSensor()) {
            // Proximity sensor is not supported on this device.
            return false;
        }
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        return true;
    }


    /**
     * Deactivate the proximity sensor.
     * 근접 센서 비활성화
     */
    public void stop() {
        threadChecker.checkIsOnValidThread();
        Log.d(TAG, "stop" + AppRTCUtils.getThreadInfo());
        if (proximitySensor == null) {
            return;
        }
        sensorManager.unregisterListener(this, proximitySensor);
    }

    /**
     * Getter for last reported state. Set to true if "near" is reported.
     * 마지막으로보고 된 상태를 얻습니다. "near"가보고되면 true로 설정됩니다.
     */
    public boolean sensorReportsNearState() {
        threadChecker.checkIsOnValidThread();
        return lastStateReportIsNear;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        threadChecker.checkIsOnValidThread();
        AppRTCUtils.assertIsTrue(event.sensor.getType() == Sensor.TYPE_PROXIMITY);
        // As a best practice; do as little as possible within this method and avoid blocking.
        // 모범 사례로서; 가능한 한 이 메소드 내에서 수행하고 블로킹을 피하십시오.
        float distanceInCentimeters = event.values[0];
        if (distanceInCentimeters < proximitySensor.getMaximumRange()) {
            Log.d(TAG, "Proximity sensor => NEAR state");
            lastStateReportIsNear = true;
        } else {
            Log.d(TAG, "Proximity sensor => FAR state");
            lastStateReportIsNear = false;
        }

        // Report about new state to listening client. Client can then call
        // sensorReportsNearState() to query the current state (NEAR or FAR).
        // 수신 대기중인 클라이언트에 대한 새 상태에 대해 보고합니다.
        // 클라이언트는 sensorReportsNearState()를 호출하여 현재 상태 (NEAR 또는 FAR)를 질의 할 수 있습니다.
        if (onSensorStateListener != null) {
            onSensorStateListener.run();
        }

        Log.d(TAG, "onSensorChanged" + AppRTCUtils.getThreadInfo() + ": "
                + "accuracy=" + event.accuracy + ", timestamp=" + event.timestamp + ", distance="
                + event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        threadChecker.checkIsOnValidThread();
        AppRTCUtils.assertIsTrue(sensor.getType() == Sensor.TYPE_PROXIMITY);
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e(TAG, "The values returned by this sensor cannot be trusted");
        }
    }


    /**
     * Get default proximity sensor if it exists.
     * Tablet devices (e.g. Nexus 7)does not support this type of sensor and false will be returned in such cases.
     * <p>
     * 기본 근접 센서가 있으면 가져옵니다.
     * 태블릿 기기 (예 : Nexus 7)는이 유형의 센서를 지원하지 않으며 이러한 경우에는 false가 반환됩니다.
     */
    private boolean initDefaultSensor() {
        if (proximitySensor != null) {
            return true;
        }
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor == null) {
            return false;
        }
        logProximitySensorInfo();
        return true;
    }

    /** Helper method for logging information about the proximity sensor. */
    /**
     * 근접 센서에 대한 정보를 기록하는 Helper method
     */
    private void logProximitySensorInfo() {
        if (proximitySensor == null) {
            return;
        }
        StringBuilder info = new StringBuilder("Proximity sensor: ");
        info.append("name=").append(proximitySensor.getName());
        info.append(", vendor: ").append(proximitySensor.getVendor());
        info.append(", power: ").append(proximitySensor.getPower());
        info.append(", resolution: ").append(proximitySensor.getResolution());
        info.append(", max range: ").append(proximitySensor.getMaximumRange());
        info.append(", min delay: ").append(proximitySensor.getMinDelay());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            // Added in API level 20.
            info.append(", type: ").append(proximitySensor.getStringType());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Added in API level 21.
            info.append(", max delay: ").append(proximitySensor.getMaxDelay());
            info.append(", reporting mode: ").append(proximitySensor.getReportingMode());
            info.append(", isWakeUpSensor: ").append(proximitySensor.isWakeUpSensor());
        }
        Log.d(TAG, info.toString());
    }
}


