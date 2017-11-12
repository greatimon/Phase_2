package com.example.jyn.remotemeeting.WebRTC.Util;

/**
 * Created by JYN on 2017-11-10.
 */

import android.os.Build;
import android.util.Log;

/**
 * AppRTCUtils provides helper functions for managing thread safety.
 * AppRTCUtils는 스레드 안전을 관리하기위한 helper functions를 제공합니다.
 */
public class AppRTCUtils {
    private AppRTCUtils() {}

    /** Helper method which throws an exception  when an assertion has failed. */
    /** 어설션이 실패 할 때 예외를 throw하는 Helper method입니다. */
    public static void assertIsTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    /** Helper method for building a string of thread information.*/
    /** thread 정보의 String화를 위한 Helper method. */
    public static String getThreadInfo() {
        return "@[name=" + Thread.currentThread().getName() + ", id=" + Thread.currentThread().getId()
                + "]";
    }

    /** Information about the current build, taken from system properties. */
    /** 시스템 속성에서 가져온 현재 빌드에 대한 정보입니다. */
    public static void logDeviceInfo(String tag) {
        Log.d(tag, "Android SDK: " + Build.VERSION.SDK_INT + ", "
                + "Release: " + Build.VERSION.RELEASE + ", "
                + "Brand: " + Build.BRAND + ", "
                + "Device: " + Build.DEVICE + ", "
                + "Id: " + Build.ID + ", "
                + "Hardware: " + Build.HARDWARE + ", "
                + "Manufacturer: " + Build.MANUFACTURER + ", "
                + "Model: " + Build.MODEL + ", "
                + "Product: " + Build.PRODUCT);
    }
}
