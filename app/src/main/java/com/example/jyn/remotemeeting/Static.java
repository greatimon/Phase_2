package com.example.jyn.remotemeeting;

/**
 * Created by JYN on 2017-11-10.
 */

public class Static {
    public static String ROOM_ID = "67953693"; // 테스트용
    public static String WEBRTC_URL = "https://appr.tc";

    /** 인텐트용 공유 키 */
    public static String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
    public static String EXTRA_URLPARAMETERS = "org.appspot.apprtc.URLPARAMETERS";
    public static String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
    public static String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
    public static String EXTRA_SCREENCAPTURE = "org.appspot.apprtc.SCREENCAPTURE";
    public static String EXTRA_CAMERA2 = "org.appspot.apprtc.CAMERA2";
    public static String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
    public static String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
    public static String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";
    public static String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED = "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static String EXTRA_VIDEO_BITRATE = "org.appspot.apprtc.VIDEO_BITRATE";
    public static String EXTRA_VIDEOCODEC = "org.appspot.apprtc.VIDEOCODEC";
    public static String EXTRA_HWCODEC_ENABLED = "org.appspot.apprtc.HWCODEC";
    public static String EXTRA_CAPTURETOTEXTURE_ENABLED = "org.appspot.apprtc.CAPTURETOTEXTURE";
    public static String EXTRA_FLEXFEC_ENABLED = "org.appspot.apprtc.FLEXFEC";
    public static String EXTRA_AUDIO_BITRATE = "org.appspot.apprtc.AUDIO_BITRATE";
    public static String EXTRA_AUDIOCODEC = "org.appspot.apprtc.AUDIOCODEC";
    public static String EXTRA_NOAUDIOPROCESSING_ENABLED = "org.appspot.apprtc.NOAUDIOPROCESSING";
    public static String EXTRA_AECDUMP_ENABLED = "org.appspot.apprtc.AECDUMP";
    public static String EXTRA_OPENSLES_ENABLED = "org.appspot.apprtc.OPENSLES";
    public static String EXTRA_DISABLE_BUILT_IN_AEC = "org.appspot.apprtc.DISABLE_BUILT_IN_AEC";
    public static String EXTRA_DISABLE_BUILT_IN_AGC = "org.appspot.apprtc.DISABLE_BUILT_IN_AGC";
    public static String EXTRA_DISABLE_BUILT_IN_NS = "org.appspot.apprtc.DISABLE_BUILT_IN_NS";
    public static String EXTRA_ENABLE_LEVEL_CONTROL = "org.appspot.apprtc.ENABLE_LEVEL_CONTROL";
    public static String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF = "org.appspot.apprtc.DISABLE_WEBRTC_GAIN_CONTROL";
    public static String EXTRA_DISPLAY_HUD = "org.appspot.apprtc.DISPLAY_HUD";
    public static String EXTRA_TRACING = "org.appspot.apprtc.TRACING";
    public static String EXTRA_CMDLINE = "org.appspot.apprtc.CMDLINE";
    public static String EXTRA_RUNTIME = "org.appspot.apprtc.RUNTIME";
    public static String EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA";
    public static String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE";
    public static String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static String EXTRA_USE_VALUES_FROM_INTENT = "org.appspot.apprtc.USE_VALUES_FROM_INTENT";
    public static String EXTRA_DATA_CHANNEL_ENABLED = "org.appspot.apprtc.DATA_CHANNEL_ENABLED";
    public static String EXTRA_ORDERED = "org.appspot.apprtc.ORDERED";
    public static String EXTRA_MAX_RETRANSMITS_MS = "org.appspot.apprtc.MAX_RETRANSMITS_MS";
    public static String EXTRA_MAX_RETRANSMITS = "org.appspot.apprtc.MAX_RETRANSMITS";
    public static String EXTRA_PROTOCOL = "org.appspot.apprtc.PROTOCOL";
    public static String EXTRA_NEGOTIATED = "org.appspot.apprtc.NEGOTIATED";
    public static String EXTRA_ID = "org.appspot.apprtc.ID";

    /** webrtc 기본값 */
    public static final int DEFAULT_VIDEO_WIDTH = 1280;
    public static final int DEFAULT_VIDEO_HEIGHT = 720;
//    <item>Default</item>
//    <item>3840 x 2160</item>      1.77
//    <item>1920 x 1080</item>      1.77
//    <item>1280 x 720</item>       1.77
//    <item>640 x 480</item>        1.33
//    <item>320 x 240</item>        1.33
    public static final int DEFAULT_CAMERA_FPS = 24;
    // TODO: 해상도 조절값
    public static final int BPS_IN_KBPS = 1000;
    public static final int VIDEO_START_BITRATE = 650;
    public static final boolean VIDEO_CALL_ENABLED = true;
    public static final boolean USE_SCREEN_CAPTURE = false;
    public static final boolean USE_CAMERA_2 = false;
    public static final boolean CAPTURE_QUALITY_SLIDER = false;
    public static final String VIDEO_CODEC = "VP9";
    public static final String AUDIO_CODEC = "OPUS";
    public static final boolean HW_CODEC = true;
    public static final boolean CAPTURE_TO_TEXTURE = true;
    public static final boolean FLEXFEC_ENABLED = false;
    public static final boolean NO_AUDIO_PROCESSING = false;
    public static final boolean AEC_DUMP = false;
    public static final boolean USE_OPENSLES = false;
    public static final boolean DISABLE_BUILT_IN_AEC = false;
    public static final boolean DISABLE_BUILT_IN_AGC = false;
    public static final boolean DISABLE_BUILT_IN_NS = false;
    public static final boolean ENABLE_LEVEL_CONTROL = false;
    public static final boolean DISABLE_WEBRTC_AGC_AND_HPE = false;
    public static final int AUDIO_START_BITRATE = 32;
    public static final boolean DISPLAY_HUD = false;
    public static final boolean TRACING = false;
    public static final boolean DATA_CHANNEL_ENABLED = true;
    public static final boolean ORDERED = true;
    public static final int MAX_RETR_MS = -1;
    public static final int MAX_RETR = -1;
    public static final String PROTOCOL = "";
    public static final boolean NEGOTIATED = false;
    public static final int ID = -1;




}
