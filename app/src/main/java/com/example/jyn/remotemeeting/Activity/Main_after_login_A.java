package com.example.jyn.remotemeeting.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.jyn.remotemeeting.Adapter.Main_viewpager_adapter;
import com.example.jyn.remotemeeting.BackPressCloseHandler;
import com.example.jyn.remotemeeting.IsNetwork;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Static;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

/**
 * Created by JYN on 2017-11-10.
 */

public class Main_after_login_A extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final String TAG = "all_"+Main_after_login_A.class.getSimpleName();
    private static final int CONNECTION_REQUEST = 1;
    private SharedPreferences sharedPref;
    
    private Toast logToast;
    private static boolean commandLineRun = false;

    Toolbar toolbar;
    TabLayout tablayout;
    ViewPager viewpager;
    Main_viewpager_adapter adapter;
    FloatingActionButton fab;

    BackPressCloseHandler backPressCloseHandler;
    IsNetwork isNetwork;
    private int[] tabIcons = {
            R.drawable.project_act1,
            R.drawable.partner_act,
            R.drawable.chat_act,
            R.drawable.noti_act,
            R.drawable.profile_act,
    };
    private int[] tabIcons_non = {
            R.drawable.project_non1,
            R.drawable.partner_non,
            R.drawable.chat_non,
            R.drawable.noti_non,
            R.drawable.profile_non
    };

    PermissionListener permissionListener;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "===================");
        Log.d(TAG, "onCreate");
        setContentView(R.layout.a_main_after_login);

        // 네트워크 확인 객체 생성
        isNetwork = new IsNetwork();

        // 뷰 찾기
        tablayout = findViewById(R.id.tab);
        viewpager = findViewById(R.id.viewpager);
        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);

        // 툴바 설정
        toolbar.setTitleTextColor(Color.parseColor("#f5f4f4")); //제목의 칼라
//        toolbar.setSubtitle(R.string.subtitle); //부제목 넣기
//        toolbar.setNavigationIcon(R.mipmap.ic_launcher); //제목앞에 아이콘 넣기
        setSupportActionBar(toolbar); //툴바를 액션바와 같게 만들어 준다.

        // 탭레이아웃, 뷰페이져
        adapter = new Main_viewpager_adapter(getSupportFragmentManager(), this);
        viewpager.setAdapter(adapter);
        tablayout.setupWithViewPager(viewpager);
        setupTabIcons();

        /**---------------------------------------------------------------------------
         리스너 ==> 탭 레이아웃, 클릭리스너
         ---------------------------------------------------------------------------*/
        tablayout.post(new Runnable() {
            @Override
            public void run() {
//                tablayout.setupWithViewPager(viewpager);
//                tablayout.setTabsFromPagerAdapter(adapter);
                tablayout.setOnTabSelectedListener(Main_after_login_A.this);
            }
        });

        // 뒤로 두번 누르면 종료되게
        backPressCloseHandler = new BackPressCloseHandler(this);

        // 퍼미션 리스너(테드_ 라이브러리)
        permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(a_profile.this, "권한 허가", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                Toast.makeText(a_profile.this, "권한 거부", Toast.LENGTH_SHORT).show();
            }
        };

        // 퍼미션 체크
        permission_check();

        /**---------------------------------------------------------------------------
         액티비티 이동 ==> 영상 통화 걸기
         ---------------------------------------------------------------------------*/
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "플로팅 버튼 클릭");

                /**
                 * 통화 연결
                 * */
                connectToRoom("", false, false, false, 0);

            }
        });


        /**---------------------------------------------------------------------------
         리스너 ==> 뷰페이져, 페이징 관련
         ---------------------------------------------------------------------------*/
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled position: " + position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected: " + position);
                setTitle(position);
                setupTabIcons_moving(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    Log.d(TAG, "onPageScrollStateChanged: SCROLL_STATE_DRAGGING");
                }
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    Log.d(TAG, "onPageScrollStateChanged: SCROLL_STATE_SETTLING");
                }
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onPageScrollStateChanged: SCROLL_STATE_IDLE");
                }
            }
        });

        setTitle(0);
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onResume
     ---------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "===================");
        Log.d(TAG, "onResume");

        if(!isNetwork.available(this)) {
            logAndToast("인터넷이 연결되어 있지 않습니다");
        }
        if(isNetwork.available(this)) {
            // 뷰페이저 특정 아이템에 포커스 주기
            viewpager.setCurrentItem(0);
        }


    }

    /**---------------------------------------------------------------------------
     메소드 ==> pull to refresh 리스너 --
     ---------------------------------------------------------------------------*/
    private void logAndToast(String msg) {
        Log.d("logAndToast", msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> OnCreate 시, 뷰페이저 하단 탭, 아이콘 넣는 로직
     ---------------------------------------------------------------------------*/
    private void setupTabIcons() {
        tablayout.getTabAt(0).setIcon(tabIcons[0]);
        tablayout.getTabAt(1).setIcon(tabIcons_non[1]);
        tablayout.getTabAt(2).setIcon(tabIcons_non[2]);
        tablayout.getTabAt(3).setIcon(tabIcons_non[3]);
        tablayout.getTabAt(4).setIcon(tabIcons_non[4]);
    }

    /**---------------------------------------------------------------------------
     메소드 ==> 페이징 변경 시, 뷰페이저 하단 탭, 아이콘 넣는 로직
     ---------------------------------------------------------------------------*/
    private void setupTabIcons_moving(int position) {
        Log.d(TAG, "setupTabIcons_moving: " + position);

        tablayout.getTabAt(position).setIcon(tabIcons[position]);

        for(int i=0; i<Main_viewpager_adapter.PAGE_NUMBER; i++) {
            Log.d(TAG, "setupTabIcons_moving_i 값: " + i);
            if(i != position) {
                Log.d(TAG, "setupTabIcons_moving_for문: " + i);
                tablayout.getTabAt(i).setIcon(tabIcons_non[i]);
            }
        }
    }


    /**---------------------------------------------------------------------------
     클래스 ==> connectToRoom -- 영상통화 연결
     ---------------------------------------------------------------------------*/
    private void connectToRoom(String roomId, boolean commandLineRun, boolean loopback, boolean useValuesFromIntent, int runTimeMs) {
        this.commandLineRun = commandLineRun;

        // roomId는 랜덤값임
        // TODO: 룸ID는 나중에 알맞은 값으로 변경하기: 파트너의 user_in 등
//        if (loopback) {
//        roomId = Integer.toString((new Random()).nextInt(100000000));
        roomId = Static.ROOM_ID;
//        logAndToast(roomId);
        Log.d(TAG, roomId);
//        }

        /**
         * 인자값 intent 셋팅
         * */
        Intent intent = new Intent(this, Call_A.class);

        boolean videoCallEnabled = Static.VIDEO_CALL_ENABLED;
        boolean useScreencapture = Static.USE_SCREEN_CAPTURE;
        boolean useCamera2 = Static.USE_CAMERA_2;

        // Get video resolution from settings.
        int videoWidth = Static.DEFAULT_VIDEO_WIDTH;
        int videoHeight = Static.DEFAULT_VIDEO_HEIGHT;
//        if (useValuesFromIntent) {
//            videoWidth = getIntent().getIntExtra(Static.EXTRA_VIDEO_WIDTH, 0);
//            videoHeight = getIntent().getIntExtra(Static.EXTRA_VIDEO_HEIGHT, 0);
//        }
//        if (videoWidth == 0 && videoHeight == 0) {
//            String resolution =
//                    sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
//            String[] dimensions = resolution.split("[ x]+");
//            if (dimensions.length == 2) {
//                try {
//                    videoWidth = Integer.parseInt(dimensions[0]);
//                    videoHeight = Integer.parseInt(dimensions[1]);
//                } catch (NumberFormatException e) {
//                    videoWidth = 0;
//                    videoHeight = 0;
//                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
//                }
//            }
//        }

        // Get camera fps from settings.
        int cameraFps = Static.DEFAULT_CAMERA_FPS;
//        if (useValuesFromIntent) {
//            cameraFps = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_FPS, 0);
//        }
//        if (cameraFps == 0) {
//            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
//            String[] fpsValues = fps.split("[ x]+");
//            if (fpsValues.length == 2) {
//                try {
//                    cameraFps = Integer.parseInt(fpsValues[0]);
//                } catch (NumberFormatException e) {
//                    cameraFps = 0;
//                    Log.e(TAG, "Wrong camera fps setting: " + fps);
//                }
//            }
//        }

        // Get video and audio start bitrate.
        int videoStartBitrate = Static.VIDEO_START_BITRATE;
//        if (useValuesFromIntent) {
//            videoStartBitrate = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_BITRATE, 0);
//        }
//        if (videoStartBitrate == 0) {
//            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
//            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
//            if (!bitrateType.equals(bitrateTypeDefault)) {
//                String bitrateValue = sharedPref.getString(
//                        keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default));
//                videoStartBitrate = Integer.parseInt(bitrateValue);
//            }
//        }

        boolean captureQualitySlider = Static.CAPTURE_QUALITY_SLIDER;
        String videoCodec = Static.VIDEO_CODEC;
        String audioCodec = Static.AUDIO_CODEC;
        boolean hwCodec = Static.HW_CODEC;
        boolean captureToTexture = Static.CAPTURE_TO_TEXTURE;
        boolean flexfecEnabled = Static.FLEXFEC_ENABLED;
        boolean noAudioProcessing = Static.NO_AUDIO_PROCESSING;
        boolean aecDump = Static.AEC_DUMP;
        boolean useOpenSLES = Static.USE_OPENSLES;
        boolean disableBuiltInAEC = Static.DISABLE_BUILT_IN_AEC;
        boolean disableBuiltInAGC = Static.DISABLE_BUILT_IN_AGC;
        boolean disableBuiltInNS = Static.DISABLE_BUILT_IN_NS;
        boolean enableLevelControl = Static.ENABLE_LEVEL_CONTROL;
        boolean disableWebRtcAGCAndHPF = Static.DISABLE_WEBRTC_AGC_AND_HPE;

        int audioStartBitrate = Static.AUDIO_START_BITRATE;
//        if (useValuesFromIntent) {
//            audioStartBitrate = getIntent().getIntExtra(Static.EXTRA_AUDIO_BITRATE, 0);
//        }
//        if (audioStartBitrate == 0) {
//            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
//            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
//            if (!bitrateType.equals(bitrateTypeDefault)) {
//                String bitrateValue = sharedPref.getString(
//                        keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
//                audioStartBitrate = Integer.parseInt(bitrateValue);
//            }
//        }

        boolean displayHud = Static.DISPLAY_HUD;
        boolean tracing = Static.TRACING;
        boolean dataChannelEnabled = Static.DATA_CHANNEL_ENABLED;
        boolean ordered = Static.ORDERED;
        int maxRetrMs = Static.MAX_RETR_MS;
        int maxRetr = Static.MAX_RETR;
        String protocol = Static.PROTOCOL;
        boolean negotiated = Static.NEGOTIATED;
        int id = Static.ID;


        
        /**
         * 인자값 intent 넣기
         * */
        String roomUrl = Static.WEBRTC_URL;
        Log.d(TAG, roomUrl);
        Uri uri = Uri.parse(roomUrl);

        intent.setData(uri);
        intent.putExtra(Static.EXTRA_ROOMID, roomId);
        intent.putExtra(Static.EXTRA_LOOPBACK, loopback);
        intent.putExtra(Static.EXTRA_VIDEO_CALL, videoCallEnabled);
        intent.putExtra(Static.EXTRA_SCREENCAPTURE, useScreencapture);
        intent.putExtra(Static.EXTRA_CAMERA2, useCamera2);
        intent.putExtra(Static.EXTRA_VIDEO_WIDTH, videoWidth);
        intent.putExtra(Static.EXTRA_VIDEO_HEIGHT, videoHeight);
        intent.putExtra(Static.EXTRA_VIDEO_FPS, cameraFps);
        intent.putExtra(Static.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
        intent.putExtra(Static.EXTRA_VIDEO_BITRATE, videoStartBitrate);
        intent.putExtra(Static.EXTRA_VIDEOCODEC, videoCodec);
        intent.putExtra(Static.EXTRA_HWCODEC_ENABLED, hwCodec);
        intent.putExtra(Static.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
        intent.putExtra(Static.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
        intent.putExtra(Static.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
        intent.putExtra(Static.EXTRA_AECDUMP_ENABLED, aecDump);
        intent.putExtra(Static.EXTRA_OPENSLES_ENABLED, useOpenSLES);
        intent.putExtra(Static.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
        intent.putExtra(Static.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
        intent.putExtra(Static.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
        intent.putExtra(Static.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
        intent.putExtra(Static.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
        intent.putExtra(Static.EXTRA_AUDIO_BITRATE, audioStartBitrate);
        intent.putExtra(Static.EXTRA_AUDIOCODEC, audioCodec);
        intent.putExtra(Static.EXTRA_DISPLAY_HUD, displayHud);
        intent.putExtra(Static.EXTRA_TRACING, tracing);
        intent.putExtra(Static.EXTRA_CMDLINE, commandLineRun);
        intent.putExtra(Static.EXTRA_RUNTIME, runTimeMs);

        intent.putExtra(Static.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);


        if (dataChannelEnabled) {
            intent.putExtra(Static.EXTRA_ORDERED, ordered);
            intent.putExtra(Static.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
            intent.putExtra(Static.EXTRA_MAX_RETRANSMITS, maxRetr);
            intent.putExtra(Static.EXTRA_PROTOCOL, protocol);
            intent.putExtra(Static.EXTRA_NEGOTIATED, negotiated);
            intent.putExtra(Static.EXTRA_ID, id);
        }

        if (useValuesFromIntent) {
            if (getIntent().hasExtra(Static.EXTRA_VIDEO_FILE_AS_CAMERA)) {
                String videoFileAsCamera =
                        getIntent().getStringExtra(Static.EXTRA_VIDEO_FILE_AS_CAMERA);
                intent.putExtra(Static.EXTRA_VIDEO_FILE_AS_CAMERA, videoFileAsCamera);
            }

            if (getIntent().hasExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)) {
                String saveRemoteVideoToFile =
                        getIntent().getStringExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
                intent.putExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE, saveRemoteVideoToFile);
            }

            if (getIntent().hasExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH)) {
                int videoOutWidth =
                        getIntent().getIntExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
                intent.putExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, videoOutWidth);
            }

            if (getIntent().hasExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT)) {
                int videoOutHeight =
                        getIntent().getIntExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
                intent.putExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, videoOutHeight);
            }
        }
        
        startActivityForResult(intent, CONNECTION_REQUEST);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 퍼미션 체크
     ---------------------------------------------------------------------------*/
    public void permission_check() {
        // 퍼미션 확인(테드_ 라이브러리)
        new TedPermission(this)
                .setPermissionListener(permissionListener)
//                .setRationaleMessage("다음 작업을 허용하시겠습니까? 기기 사진, 미디어, 파일 액세스")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다")
                .setGotoSettingButton(true)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS)
                .check();
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> 탭레이아웃의 탭 클릭관련 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        Log.d(TAG, "TabSelected: " + tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        Log.d(TAG, "TabUnselected: " + tab.getPosition());
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        Log.d(TAG, "TabReselected: " + tab.getPosition());
        if(tab.getPosition() == 0) {
            setTitle(tab.getPosition());
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 툴바 텍스트 변경
     ---------------------------------------------------------------------------*/
    public void setTitle(int positon) {
        switch(positon) {
            case 0:
                toolbar.setTitle("프로젝트");
                break;
            case 1:
                toolbar.setTitle("비지니스 파트너");
                break;
            case 2:
                toolbar.setTitle("채팅");
                break;
            case 3:
                toolbar.setTitle("알림");
                break;
            case 4:
                toolbar.setTitle("프로필");
                break;
        }
    }

}
