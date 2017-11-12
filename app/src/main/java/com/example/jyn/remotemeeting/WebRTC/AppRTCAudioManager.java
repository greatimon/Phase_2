package com.example.jyn.remotemeeting.WebRTC;

/**
 * Created by JYN on 2017-11-10.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.jyn.remotemeeting.WebRTC.Util.AppRTCUtils;

import org.webrtc.ThreadUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * AppRTCAudioManager manages all audio related parts of the AppRTC demo.
 * AppRTCAudioManager는 AppRTC 데모의 모든 오디오 관련 부분을 관리합니다.
 */
public class AppRTCAudioManager {

    private static final String TAG = "AppRTCAudioManager";
    private static final String SPEAKERPHONE_AUTO = "auto";
    private static final String SPEAKERPHONE_TRUE = "true";
    private static final String SPEAKERPHONE_FALSE = "false";

    /**
     * AudioDevice is the names of possible audio devices that we currently support.
     * AudioDevice는 현재 지원되는 오디오 장치의 이름입니다.
     */
    public enum AudioDevice { SPEAKER_PHONE, WIRED_HEADSET, EARPIECE, BLUETOOTH, NONE }

    /** AudioManager state. */
    public enum AudioManagerState {
        UNINITIALIZED,
        PREINITIALIZED,
        RUNNING,
    }

    /** Selected audio device change event. */
    /** 선택한 오디오 장치 변경 이벤트. */
    public static interface AudioManagerEvents {
        // Callback fired once audio device is changed or list of available audio devices changed.
        void onAudioDeviceChanged(
                AudioDevice selectedAudioDevice, Set<AudioDevice> availableAudioDevices);
    }

    private final Context apprtcContext;
    private AudioManager audioManager;

    private AudioManagerEvents audioManagerEvents;
    private AudioManagerState amState;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private boolean savedIsSpeakerPhoneOn = false;
    private boolean savedIsMicrophoneMute = false;
    private boolean hasWiredHeadset = false;

    // Default audio device;
    // speaker phone for video calls or earpiece for audio only calls.
    //
    // 기본 오디오 장치.
    // 화상 통화용 스피커폰 또는 오디오 전용 통화용 이어 피스.
    private AudioDevice defaultAudioDevice;

    // Contains the currently selected audio device.
    // This device is changed automatically using a certain scheme where e.g.
    // a wired headset "wins" over speaker phone. It is also possible for a user to explicitly select a device (and overrid any predefined scheme).
    // See |userSelectedAudioDevice| for details.
    // =========================================================
    // 현재 선택된 오디오 장치를 포함합니다.
    // 이 장치는 특정 체계를 사용하여 자동으로 변경됩니다.
    // 유선 헤드셋이 스피커폰보다 우위에 있습니다. 사용자가 명시적으로 장치를 선택하고 미리 정의 된 구성표를 무시할 수도 있습니다.
    // 자세한 내용은 |userSelectedAudioDevice| 에서 확인.
    private AudioDevice selectedAudioDevice;

    // Contains the user-selected audio device which overrides the predefined
    // selection scheme.
    // TODO(henrika): always set to AudioDevice.NONE today. Add support for
    // explicit selection based on choice by userSelectedAudioDevice.
    //
    // 정의된 오디오 장치를 사용하지 않고, 사용자가 선택한 오디오 디바이스를 포함한다.
    // 선택 방식.
    // TODO (henrika) : 오늘 AudioDevice.NONE으로 설정됩니다. 에 대한 지원 추가
    // userSelectedAudioDevice 선택에 기반한 명시적 선택.
    private AudioDevice userSelectedAudioDevice;

    // Contains speakerphone setting: auto, true or false
    // =========================================================
    // 스피커폰 설정 포함 : auto, true or false
    private final String useSpeakerphone;

    // Proximity sensor object. It measures the proximity of an object in cm
    // relative to the view screen of a device and can therefore be used to
    // assist device switching (close to ear <=> use headset earpiece if
    // available, far from ear <=> use speaker phone).
    // =========================================================
    // 근접 센서 객체. 그것은 cm 단위의 물체의 근접도를 측정합니다.
    // 디바이스의 뷰 화면에 상대적이므로 기기 전환을 돕습니다
    // 귀에서 가까이 있음<=> 사용이 가능하다면 헤드셋 이어폰 사용
    // 귀에서 멀리 떨어져 있음 <=> 스피커폰 사용
    private AppRTCProximitySensor proximitySensor = null;

    // Handles all tasks related to Bluetooth headset devices.
    // Bluetooth 헤드셋 장치와 관련된 모든 작업을 처리합니다.
//    private final AppRTCBluetoothManager bluetoothManager;

    // Contains a list of available audio devices. A Set collection is used to avoid duplicate elements.
    // 사용 가능한 오디오 장치 목록을 포함합니다. Set 컬렉션은 중복 요소를 피하기 위해 사용됩니다.
    private Set<AudioDevice> audioDevices = new HashSet<AudioDevice>();

    // Broadcast receiver for wired headset intent broadcasts.
    // 유선 헤드셋 intent broadcasts용 Broadcast receiver.
    private BroadcastReceiver wiredHeadsetReceiver;

    // Callback method for changes in audio focus.
    // 오디오 포커스 변경을위한 콜백 메소드.
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    /**
     * This method is called when the proximity sensor reports a state change,
     * e.g. from "NEAR to FAR" or from "FAR to NEAR".
     *
     * 이 메소드는 proximity sensor가 상태 변경을 보고 할 때 호출됩니다. "NEAR에서 FAR로"또는 "FAR에서 NEAR로".
     */
    private void onProximitySensorChangedState() {
        if (!useSpeakerphone.equals(SPEAKERPHONE_AUTO)) {
            return;
        }

        // The proximity sensor should only be activated when there are exactly two available audio devices.
        // proximity sensor는 사용 가능한 오디오 장치가 정확히 두 개인 경우에만 활성화해야합니다.
        if (audioDevices.size() == 2 && audioDevices.contains(AppRTCAudioManager.AudioDevice.EARPIECE)
                && audioDevices.contains(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)) {
            if (proximitySensor.sensorReportsNearState()) {
                // Sensor reports that a "handset is being held up to a person's ear",
                // or "something is covering the light sensor".
                // =========================================================
                // 센서는 "핸드셋이 사람의 귀에 매달려있다" 또는 "무언가가 light sensor를 덮고있다"고 보고합니다.
                setAudioDeviceInternal(AppRTCAudioManager.AudioDevice.EARPIECE);
            } else {
                // Sensor reports that a "handset is removed from a person's ear", or
                // "the light sensor is no longer covered".
                // =========================================================
                // 센서는 "핸드셋이 사람의 귀에서 제거되었습니다" 또는 "light sensor가 더 이상 커버되지 않음"을 보고합니다.
                setAudioDeviceInternal(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            }
        }
    }


    /* Receiver which handles changes in wired headset availability. */
    /** 유선 헤드셋 가용성의 변경을 처리하는 수신기. */
    private class WiredHeadsetReceiver extends BroadcastReceiver {
        private static final int STATE_UNPLUGGED = 0;
        private static final int STATE_PLUGGED = 1;
        private static final int HAS_NO_MIC = 0;
        private static final int HAS_MIC = 1;

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", STATE_UNPLUGGED);
            int microphone = intent.getIntExtra("microphone", HAS_NO_MIC);
            String name = intent.getStringExtra("name");
            Log.d(TAG, "WiredHeadsetReceiver.onReceive" + AppRTCUtils.getThreadInfo() + ": "
                    + "a=" + intent.getAction() + ", s="
                    + (state == STATE_UNPLUGGED ? "unplugged" : "plugged") + ", m="
                    + (microphone == HAS_MIC ? "mic" : "no mic") + ", n=" + name + ", sb="
                    + isInitialStickyBroadcast());
            hasWiredHeadset = (state == STATE_PLUGGED);
            updateAudioDeviceState();
        }
    }






    /** Construction. */
    public static AppRTCAudioManager create(Context context) {
        return new AppRTCAudioManager(context);
    }

    private AppRTCAudioManager(Context context) {
        Log.d(TAG, "ctor");
        ThreadUtils.checkIsOnMainThread();
        apprtcContext = context;
        audioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
//        bluetoothManager = AppRTCBluetoothManager.create(context, this);
        wiredHeadsetReceiver = new WiredHeadsetReceiver();
        amState = AudioManagerState.UNINITIALIZED;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        useSpeakerphone = sharedPreferences.getString("speakerphone_preference", "auto");
        Log.d(TAG, "useSpeakerphone: " + useSpeakerphone);
        if (useSpeakerphone.equals(SPEAKERPHONE_FALSE)) {
            defaultAudioDevice = AudioDevice.EARPIECE;
        } else {
            defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
        }

        // Create and initialize the proximity sensor.
        // Tablet devices (e.g. Nexus 7) does not support proximity sensors.
        // Note that, the sensor will not be active until start() has been called.
        // =========================================================
        // 근접 센서를 생성하고 초기화합니다.
        // 태블릿 기기 (예 : Nexus 7)는 근접 센서를 지원하지 않습니다.
        // start()가 호출 될 때까지 센서가 활성화되지 않습니다.
        proximitySensor = AppRTCProximitySensor.create(context, new Runnable() {
            // This method will be called each time a state change is detected.
            // Example: user holds his hand over the device (closer than ~5 cm),
            // or removes his hand from the device.
            // =========================================================
            // 이 메서드는 상태 변경이 감지 될 때마다 호출됩니다.
            // 예 : 사용자가 장치 위로 손을 잡습니다 (~ 5cm 이상), or 장치에서 손을 뗍니다.
            public void run() {
                onProximitySensorChangedState();
            }
        });

        Log.d(TAG, "defaultAudioDevice: " + defaultAudioDevice);
        AppRTCUtils.logDeviceInfo(TAG);
    }

    public void start(AudioManagerEvents audioManagerEvents) {
        Log.d(TAG, "start");
        ThreadUtils.checkIsOnMainThread();
        if (amState == AudioManagerState.RUNNING) {
            Log.e(TAG, "AudioManager is already active");
            return;
        }
        // TODO(henrika): perhaps call new method called preInitAudio() here if UNINITIALIZED.
        // TODO(henrika): UNINITIALIZED 인 경우 여기에서 preInitAudio()라는 새 메서드를 호출 할 수 있습니다.

        Log.d(TAG, "AudioManager starts...");
        this.audioManagerEvents = audioManagerEvents;
        amState = AudioManagerState.RUNNING;

        // Store current audio state so we can restore it when stop() is called.
        // stop()이 호출 될 때 우리가 복원 할 수 있도록 현재 오디오 상태를 저장하십시오.
        savedAudioMode = audioManager.getMode();
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
        savedIsMicrophoneMute = audioManager.isMicrophoneMute();
        hasWiredHeadset = hasWiredHeadset();

        // Create an AudioManager.OnAudioFocusChangeListener instance.
        // AudioManager.OnAudioFocusChangeListener 인스턴스를 작성합니다.
        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            // Called on the listener to notify if the audio focus for this listener has been changed.
            // The |focusChange| value indicates whether the focus was gained, whether the focus was lost,
            // and whether that loss is transient, or whether the new focus holder will hold it for an
            // unknown amount of time.
            // TODO(henrika): possibly extend support of handling audio-focus changes. Only contains
            // logging for now.
            // =========================================================
            // 이 listener 의 오디오 포커스가 변경된 것을 통지하기 위해서 호출됩니다.
            // |포커스 변경| 값은 초점이 맞았는지, 초점이 손실되었는지,
            // 그리고 그 손실이 일시적인지 또는 새로운 초점 홀더가 알수없는 시간 동안 그 것을 잡을지 여부를 나타냅니다.
            // TODO: 오디오 포커스 변경을 처리 할 수 있는 지원을 확대 할 수 있습니다.

            @Override
            public void onAudioFocusChange(int focusChange) {
                String typeOfChange = "AUDIOFOCUS_NOT_DEFINED";
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        typeOfChange = "AUDIOFOCUS_GAIN";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        typeOfChange = "AUDIOFOCUS_LOSS";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                        break;
                    default:
                        typeOfChange = "AUDIOFOCUS_INVALID";
                        break;
                }
                Log.d(TAG, "onAudioFocusChange: " + typeOfChange);
            }
        };

        // Request audio playout focus (without ducking) and install listener for changes in focus.
        // 오디오 재생 포커스 (더킹하지 않고)를 요청하고 포커스 변경 사항에 대한 리스너를 설치하십시오.
        int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus request granted for VOICE_CALL streams");
        } else {
            Log.e(TAG, "Audio focus request failed");
        }

        // Start by setting MODE_IN_COMMUNICATION as default audio mode.
        // It is required to be in this mode when playout and/or recording starts for best possible VoIP performance.
        // =========================================================
        // MODE_IN_COMMUNICATION을 기본 오디오 모드로 설정하여 시작하십시오.
        // 최상의 VoIP 성능을 위해 재생 및 녹음이 시작될 때, 이 모드에 있어야 합니다.
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // Always disable microphone mute during a WebRTC call.
        // WebRTC 통화 도중 항상 마이크 음소거를 비활성화하십시오.
        setMicrophoneMute(false);

        // Set initial device states.
        // 초기 장치 상태를 설정합니다.
        userSelectedAudioDevice = AudioDevice.NONE;
        selectedAudioDevice = AudioDevice.NONE;
        audioDevices.clear();

        // Initialize and start Bluetooth if a BT device is available or initiate detection of new (enabled) BT devices.
        // BT 장치가 사용 가능하거나 새로운 (사용 가능한) BT 장치의 감지를 시작하면 Bluetooth를 초기화하고 시작하십시오.
//        bluetoothManager.start();

        // Do initial selection of audio device.
        // This setting can later be changed either by adding/removing a BT or wired headset or by covering/uncovering the proximity sensor.
        // =========================================================
        // 오디오 장치의 초기 선택을 수행하십시오.
        // 이 설정은 나중에 BT 또는 유선 헤드셋을 추가/제거하거나 근접 센서를 덮거나 덮어서 변경할 수 있습니다.
        updateAudioDeviceState();

        // Register receiver for broadcast intents related to adding/removing awired headset.
        // 유선 헤드셋 추가/제거와 관련된 브로드 캐스트 용 수신기 등록.
        registerReceiver(wiredHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        Log.d(TAG, "AudioManager started");
    }


    public void stop() {
        Log.d(TAG, "stop");
        ThreadUtils.checkIsOnMainThread();
        if (amState != AudioManagerState.RUNNING) {
            Log.e(TAG, "Trying to stop AudioManager in incorrect state: " + amState);
            return;
        }
        amState = AudioManagerState.UNINITIALIZED;

        unregisterReceiver(wiredHeadsetReceiver);

//        bluetoothManager.stop();

        // Restore previously stored audio states.
        // 이전에 저장된 오디오 상태를 복원합니다.
        setSpeakerphoneOn(savedIsSpeakerPhoneOn);
        setMicrophoneMute(savedIsMicrophoneMute);
        audioManager.setMode(savedAudioMode);

        // Abandon audio focus. Gives the previous focus owner, if any, focus.
        // 오디오 포커스 포기. 이전 포커스 소유자(있을 경우)를 지정합니다.
        audioManager.abandonAudioFocus(audioFocusChangeListener);
        audioFocusChangeListener = null;
        Log.d(TAG, "Abandoned audio focus for VOICE_CALL streams");

        if (proximitySensor != null) {
            proximitySensor.stop();
            proximitySensor = null;
        }

        audioManagerEvents = null;
        Log.d(TAG, "AudioManager stopped");
    }


    /** Changes selection of the currently active audio device. */
    /** 현재 활성화 된 오디오 장치의 선택 항목을 변경합니다. */
    private void setAudioDeviceInternal(AudioDevice device) {
        Log.d(TAG, "setAudioDeviceInternal(device=" + device + ")");
        AppRTCUtils.assertIsTrue(audioDevices.contains(device));

        switch (device) {
            case SPEAKER_PHONE:
                setSpeakerphoneOn(true);
                break;
            case EARPIECE:
                setSpeakerphoneOn(false);
                break;
            case WIRED_HEADSET:
                setSpeakerphoneOn(false);
                break;
            case BLUETOOTH:
                setSpeakerphoneOn(false);
                break;
            default:
                Log.e(TAG, "Invalid audio device selection");
                break;
        }
        selectedAudioDevice = device;
    }


    /**
     * Changes default audio device.
     * TODO(henrika): add usage of this method in the AppRTCMobile client.
     *
     * 기본 오디오 장치를 변경합니다.
     * TODO(henrika): 이 메소드의 사용법을 AppRTCMobile 클라이언트에 추가하십시오.
     */
    public void setDefaultAudioDevice(AudioDevice defaultDevice) {
        ThreadUtils.checkIsOnMainThread();
        switch (defaultDevice) {
            case SPEAKER_PHONE:
                defaultAudioDevice = defaultDevice;
                break;
            case EARPIECE:
                if (hasEarpiece()) {
                    defaultAudioDevice = defaultDevice;
                } else {
                    defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
                }
                break;
            default:
                Log.e(TAG, "Invalid default audio device selection");
                break;
        }
        Log.d(TAG, "setDefaultAudioDevice(device=" + defaultAudioDevice + ")");
        updateAudioDeviceState();
    }


    /** Changes selection of the currently active audio device. */
    /** 현재 활성화 된 오디오 장치의 선택 항목을 변경합니다. */
    public void selectAudioDevice(AudioDevice device) {
        ThreadUtils.checkIsOnMainThread();
        if (!audioDevices.contains(device)) {
            Log.e(TAG, "Can not select " + device + " from available " + audioDevices);
        }
        userSelectedAudioDevice = device;
        updateAudioDeviceState();
    }

    /** Returns current set of available/selectable audio devices. */
    /** 사용 가능한/선택 가능한 오디오 장치의 현재 세트를 반환합니다. */
    public Set<AudioDevice> getAudioDevices() {
        ThreadUtils.checkIsOnMainThread();
        return Collections.unmodifiableSet(new HashSet<AudioDevice>(audioDevices));
    }

    /** Returns the currently selected audio device. */
    /** 현재 선택된 오디오 장치를 반환합니다. */
    public AudioDevice getSelectedAudioDevice() {
        ThreadUtils.checkIsOnMainThread();
        return selectedAudioDevice;
    }

    /** Helper method for receiver registration. */
    /** 수신기 등록을 위한 Helper method. */
    private void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        apprtcContext.registerReceiver(receiver, filter);
    }

    /** Helper method for unregistration of an existing receiver. */
    /** 기존 수신자의 등록을 undo하기 위한 Helper method. */
    private void unregisterReceiver(BroadcastReceiver receiver) {
        apprtcContext.unregisterReceiver(receiver);
    }

    /** Sets the speaker phone mode. */
    /** 스피커폰 모드를 설정합니다. */
    private void setSpeakerphoneOn(boolean on) {
        boolean wasOn = audioManager.isSpeakerphoneOn();
        if (wasOn == on) {
            return;
        }
        audioManager.setSpeakerphoneOn(on);
    }

    /** Sets the microphone mute state.
     * 마이크 음소거 상태를 설정합니다. */
    private void setMicrophoneMute(boolean on) {
        boolean wasMuted = audioManager.isMicrophoneMute();
        if (wasMuted == on) {
            return;
        }
        audioManager.setMicrophoneMute(on);
    }

    /** Gets the current earpiece state.
     * 현재 수화기 상태를 가져옵니다. */
    private boolean hasEarpiece() {
        return apprtcContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }


    /**
     * Checks whether a wired headset is connected or not.
     * This is not a valid indication that audio playback is actually over the wired headset as audio routing depends on other conditions.
     * We only use it as an early indicator (during initialization) of an attached wired headset.
     *
     * 유선 헤드셋이 연결되어 있는지 여부를 확인합니다.
     * 오디오 라우팅은 다른 조건에 따라 다르므로 오디오 재생이 실제로 유선 헤드셋을 통해 이루어 졌음을 나타내는 것은 아닙니다.
     * 부착된 유선 헤드셋의 초기 표시기로만 사용합니다 (초기화 중에).
     */
    @Deprecated
    private boolean hasWiredHeadset() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return audioManager.isWiredHeadsetOn();
        } else {
            final AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo device : devices) {
                final int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    Log.d(TAG, "hasWiredHeadset: found wired headset");
                    return true;
                } else if (type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                    Log.d(TAG, "hasWiredHeadset: found USB audio device");
                    return true;
                }
            }
            return false;
        }
    }


    /**
     * Updates list of possible audio devices and make new device selection.
     * TODO(henrika): add unit test to verify all state transitions.
     *
     * 가능한 오디오 장치 목록을 업데이트하고 새 장치를 선택합니다.
     * TODO(henrika) : 단위 테스트를 추가하여 모든 상태 전이를 확인합니다.
     */
    public void updateAudioDeviceState() {
        ThreadUtils.checkIsOnMainThread();
//        Log.d(TAG, "--- updateAudioDeviceState: "
//                + "wired headset=" + hasWiredHeadset + ", "
//                + "BT state=" + bluetoothManager.getState());
        Log.d(TAG, "Device status: "
                + "available=" + audioDevices + ", "
                + "selected=" + selectedAudioDevice + ", "
                + "user selected=" + userSelectedAudioDevice);

        // Check if any Bluetooth headset is connected. The internal BT state will change accordingly.
        // TODO(henrika): perhaps wrap required state into BT manager.
        // Bluetooth 헤드셋이 연결되어 있는지 확인하십시오. 이에 따라 내부 BT 상태가 변경됩니다.
        // TODO(henrika): BT 관리자에게 필수 상태를 포장 할 수도 있습니다.
//        if (bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_AVAILABLE
//                || bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_UNAVAILABLE
//                || bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_DISCONNECTING) {
//            bluetoothManager.updateDevice();
//        }

        // Update the set of available audio devices.
        // 사용 가능한 오디오 장치 세트를 업데이트하십시오.
        Set<AudioDevice> newAudioDevices = new HashSet<>();

//        if (bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTED
//                || bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTING
//                || bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_AVAILABLE) {
//            newAudioDevices.add(AudioDevice.BLUETOOTH);
//        }

        if (hasWiredHeadset) {
            // If a wired headset is connected, then it is the only possible option.
            // 유선 헤드셋이 연결된 경우 유일한 옵션입니다.
            newAudioDevices.add(AudioDevice.WIRED_HEADSET);
        } else {
            // No wired headset, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            // 유선 헤드셋이 없으므로 오디오 장치 목록에 스피커폰 (태블릿),
            // 또는 스피커폰 및 수화기 (휴대 전화)가 포함될 수 있습니다.
            newAudioDevices.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece()) {
                newAudioDevices.add(AudioDevice.EARPIECE);
            }
        }
        // Store state which is set to true if the device list has changed.
        // 장치 목록이 변경될 떄, true로 설정된 저장소 상태입니다.
        boolean audioDeviceSetUpdated = !audioDevices.equals(newAudioDevices);

        // Update the existing audio device set.
        // 기존 오디오 장치 세트를 업데이트하십시오.
        audioDevices = newAudioDevices;

        // Correct user selected audio devices if needed.
        // 필요한 경우 사용자가 선택한 오디오 장치를 수정하십시오.
//        if (bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_UNAVAILABLE
//                && userSelectedAudioDevice == AudioDevice.BLUETOOTH) {
//            // If BT is not available, it can't be the user selection.
//            // BT를 사용할 수 없으면 사용자 선택이 될 수 없습니다.
//            userSelectedAudioDevice = AudioDevice.NONE;
//        }
        if (hasWiredHeadset && userSelectedAudioDevice == AudioDevice.SPEAKER_PHONE) {
            // If user selected speaker phone, but then plugged wired headset then make wired headset as user selected device.
            // 사용자가 스피커폰을 선택한 다음에 유선 헤드셋을 연결한다면, 유선 헤드셋을 사용자가 선택한 장치로 만듭니다.
            userSelectedAudioDevice = AudioDevice.WIRED_HEADSET;
        }
        if (!hasWiredHeadset && userSelectedAudioDevice == AudioDevice.WIRED_HEADSET) {
            // If user selected wired headset, but then unplugged wired headset then make speaker phone as user selected device.
            // 사용자가 유선 헤드셋을 선택한 다음에 유선 헤드셋을 분리한다면, 스피커폰을 사용자가 선택한 장치로 설정합니다.
            userSelectedAudioDevice = AudioDevice.SPEAKER_PHONE;
        }

        // Need to start Bluetooth if it is available and user either selected it explicitly or user did not select any output device.
        // 사용 가능하고 사용자가 명시적으로 선택했거나 사용자가 출력 장치를 선택하지 않은 경우 Bluetooth를 시작해야합니다.
//        boolean needBluetoothAudioStart =
//                bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_AVAILABLE
//                        && (userSelectedAudioDevice == AudioDevice.NONE
//                        || userSelectedAudioDevice == AudioDevice.BLUETOOTH);
//
//        // Need to stop Bluetooth audio if user selected different device and Bluetooth SCO connection is established or in the process.
//        // 사용자가 다른 장치를 선택하고 Bluetooth SCO 연결이 설정되었거나 진행 중이면 Bluetooth 오디오를 중지해야합니다.
//        boolean needBluetoothAudioStop =
//                (bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTED
//                        || bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTING)
//                        && (userSelectedAudioDevice != AudioDevice.NONE
//                        && userSelectedAudioDevice != AudioDevice.BLUETOOTH);
//
//        if (bluetoothManager.getState() == AppRTCBluetoothManager.State.HEADSET_AVAILABLE
//                || bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTING
//                || bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTED) {
//            Log.d(TAG, "Need BT audio: start=" + needBluetoothAudioStart + ", "
//                    + "stop=" + needBluetoothAudioStop + ", "
//                    + "BT state=" + bluetoothManager.getState());
//        }
//
//        // Start or stop Bluetooth SCO connection given states set earlier.
//        // Bluetooth SCO 연결을 이전에 설정한 상태로 시작하거나 중지.
//        if (needBluetoothAudioStop) {
//            bluetoothManager.stopScoAudio();
//            bluetoothManager.updateDevice();
//        }
//
//        if (needBluetoothAudioStart && !needBluetoothAudioStop) {
//            // Attempt to start Bluetooth SCO audio (takes a few second to start).
//            // Bluetooth SCO 오디오를 시작하려고합니다 (시작하는 데 몇 초 걸림).
//            if (!bluetoothManager.startScoAudio()) {
//                // Remove BLUETOOTH from list of available devices since SCO failed.
//                // SCO가 실패한 이후 사용 가능한 장치 목록에서 BLUETOOTH를 제거하십시오.
//                audioDevices.remove(AudioDevice.BLUETOOTH);
//                audioDeviceSetUpdated = true;
//            }
//        }

        // Update selected audio device.
        // 선택한 오디오 장치를 업데이트
        AudioDevice newAudioDevice = selectedAudioDevice;

//        if (bluetoothManager.getState() == AppRTCBluetoothManager.State.SCO_CONNECTED) {
//            // If a Bluetooth is connected, then it should be used as output audio device.
//            // Note that it is not sufficient that a headset is available.
//            // an active SCO channel must also be up and running.
//            // =========================================================
//            // 블루투스가 연결된 경우 출력 오디오 장치로 사용해야합니다.
//            // 헤드셋을 사용할 수 없다는 점에 유의하십시오.
//            // 활성 SCO 채널도 실행 중이어야합니다.
//            newAudioDevice = AudioDevice.BLUETOOTH;
//        }
        if (hasWiredHeadset) {
            // If a wired headset is connected, but Bluetooth is not, then wired headset is used as audio device.
            // 유선 헤드셋이 연결되어 있지만 Bluetooth가 아닌 경우 유선 헤드셋이 오디오 장치로 사용됩니다.
            newAudioDevice = AudioDevice.WIRED_HEADSET;
        } else {
            // No wired headset and no Bluetooth, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            // |defaultAudioDevice| contains either AudioDevice.SPEAKER_PHONE or AudioDevice.EARPIECE
            // depending on the user's selection.
            // =========================================================
            // 유선 헤드셋 및 블루투스가 없기 때문에 오디오 장치 목록에는 스피커폰 (태블릿) 또는 스피커폰 및 수화기 (휴대 전화)가 포함될 수 있습니다.
            // |defaultAudioDevice|는 사용자의 선택에 따라 AudioDevice.SPEAKER_PHONE 또는 AudioDevice.EARPIECE 중 하나를 포함합니다.
            newAudioDevice = defaultAudioDevice;
        }
        // Switch to new device but only if there has been any changes.
        // 변경 사항이있는 경우에만 새 장치로 전환하십시오.
        if (newAudioDevice != selectedAudioDevice || audioDeviceSetUpdated) {
            // Do the required device switch.
            // 필요한 장치를 전환하십시오.
            setAudioDeviceInternal(newAudioDevice);
            Log.d(TAG, "New device status: "
                    + "available=" + audioDevices + ", "
                    + "selected=" + newAudioDevice);
            if (audioManagerEvents != null) {
                // Notify a listening client that audio device has been changed.
                // listening client에 오디오 장치가 변경되었음을 알립니다.
                audioManagerEvents.onAudioDeviceChanged(selectedAudioDevice, audioDevices);
            }
        }
        Log.d(TAG, "--- updateAudioDeviceState done");
    }
}
