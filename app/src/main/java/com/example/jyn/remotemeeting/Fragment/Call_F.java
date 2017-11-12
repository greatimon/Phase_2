package com.example.jyn.remotemeeting.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Activity.Call_A;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Static;
import com.example.jyn.remotemeeting.WebRTC.CaptureQualityController;

import org.webrtc.RendererCommon;

/**
 * Created by JYN on 2017-11-10.
 */

public class Call_F extends Fragment {

    private LinearLayout exit_LIN;
    private RelativeLayout popup_menu_REL;
    private View controlView;
    private TextView contactView;
    private ImageView cameraSwitchButton;
    private ImageView mic_on_show_IV;
    private ImageView mic_off_show_IV;
//    private ImageButton videoScalingButton;
    private ImageView toggleMuteButton;
    private ImageView popup_menu_icon;
    private TextView toggleMuteText;
    private TextView captureFormatText;
    private TextView cameraSwitchText;
    private SeekBar captureFormatSlider;
    private OnCallEvents callEvents;
    private RendererCommon.ScalingType scalingType;
    private boolean videoCallEnabled = true;

    private static final String TAG = "all_"+Call_F.class.getSimpleName();
    final int REQUEST_OUT=1000;

    /**
     * Call control interface for container activity.
     */
    public interface OnCallEvents {
        void onCallHangUp();
        void onCameraSwitch();
        void onVideoScalingSwitch(RendererCommon.ScalingType scalingType);
        void onCaptureFormatChange(int width, int height, int framerate);
        boolean onToggleMic();
    }

    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        controlView = inflater.inflate(R.layout.f_call, container, false);


        // Create UI controls.
        contactView = controlView.findViewById(R.id.contact_name_call);
        exit_LIN = controlView.findViewById(R.id.button_call_disconnect);
        cameraSwitchButton = controlView.findViewById(R.id.button_call_switch_camera);
        cameraSwitchText = controlView.findViewById(R.id.Textview_call_switch_camera);
//        videoScalingButton = controlView.findViewById(R.id.button_call_scaling_mode);
        toggleMuteButton = controlView.findViewById(R.id.button_call_toggle_mic);
        toggleMuteText = controlView.findViewById(R.id.text_call_toggle_mic);
        captureFormatText = controlView.findViewById(R.id.capture_format_text_call);
        captureFormatSlider = controlView.findViewById(R.id.capture_format_slider_call);
        popup_menu_icon = controlView.findViewById(R.id.popup_menu_icon);
        popup_menu_REL = controlView.findViewById(R.id.popup_menu);
        mic_on_show_IV = controlView.findViewById(R.id.mic_on_show);
        mic_off_show_IV = controlView.findViewById(R.id.mic_off_show);


        /**---------------------------------------------------------------------------
         클릭이벤트 ==> 통화 종료확인 요청 -- static Handler 이용
         ---------------------------------------------------------------------------*/
        exit_LIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                callEvents.onCallHangUp();
                Call_A.hangup_confirm.sendEmptyMessage(1);
            }
        });


        /**---------------------------------------------------------------------------
         클릭이벤트 ==> 카메라 플립
         ---------------------------------------------------------------------------*/
        cameraSwitchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEvents.onCameraSwitch();
            }
        });
        cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEvents.onCameraSwitch();
            }
        });

//        videoScalingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (scalingType == RendererCommon.ScalingType.SCALE_ASPECT_FILL) {
//                    videoScalingButton.setBackgroundResource(R.drawable.ic_action_full_screen);
//                    scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT;
//                } else {
//                    videoScalingButton.setBackgroundResource(R.drawable.ic_action_return_from_full_screen);
//                    scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;
//                }
//                callEvents.onVideoScalingSwitch(scalingType);
//            }
//        });
        scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;


        /**---------------------------------------------------------------------------
         클릭이벤트 ==> 마이크 on,off
         ---------------------------------------------------------------------------*/
        // TODO: 음소거 버튼 이미지 바뀌는거 코딩하기`
        toggleMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enabled = callEvents.onToggleMic();
                Log.d(TAG, "toggleMuteButton_enabled: " + enabled);

                // 사용자에게 보여주는 마이크 상태 표시 변경
//                if(enabled) {
//                    mic_on_show_IV.setVisibility(View.VISIBLE);
//                    mic_off_show_IV.setVisibility(View.GONE);
//                } else if(!enabled) {
//                    mic_on_show_IV.setVisibility(View.GONE);
//                    mic_off_show_IV.setVisibility(View.VISIBLE);
//                }


                toggleMuteButton.setAlpha(enabled ? 1.0f : 0.3f);
                String test = String.valueOf(enabled);
                switch (test) {
                    case "true":
                    mic_on_show_IV.setVisibility(View.VISIBLE);
                    mic_off_show_IV.setVisibility(View.GONE);
                        break;
                    case "false":
                        mic_on_show_IV.setVisibility(View.GONE);
                        mic_off_show_IV.setVisibility(View.VISIBLE);
                        break;
                  }

            }
        });
        toggleMuteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enabled = callEvents.onToggleMic();
                Log.d(TAG, "toggleMuteText_enabled: " + enabled);

                // 사용자에게 보여주는 마이크 상태 표시 변경
//                if(enabled) {
//                    mic_on_show_IV.setVisibility(View.VISIBLE);
//                    mic_off_show_IV.setVisibility(View.GONE);
//                } else if(!enabled) {
//                    mic_on_show_IV.setVisibility(View.GONE);
//                    mic_off_show_IV.setVisibility(View.VISIBLE);
//                }
                toggleMuteButton.setAlpha(enabled ? 1.0f : 0.3f);
                String test = String.valueOf(enabled);
                switch (test) {
                    case "true":
                        mic_on_show_IV.setVisibility(View.VISIBLE);
                        mic_off_show_IV.setVisibility(View.GONE);
                        break;
                    case "false":
                        mic_on_show_IV.setVisibility(View.GONE);
                        mic_off_show_IV.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });


        /**---------------------------------------------------------------------------
         클릭이벤트 ==> 팝업 메뉴 VISIBLE, GONE
         ---------------------------------------------------------------------------*/
        popup_menu_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (popup_menu_REL.getVisibility()) {
                    case View.GONE:
                        popup_menu_REL.setVisibility(View.VISIBLE);
                        break;
                    case View.VISIBLE:
                        popup_menu_REL.setVisibility(View.GONE);
                        break;
                }
            }
        });

        return controlView;
    }

    @Override
    public void onStart() {
        super.onStart();

        boolean captureSliderEnabled = false;
        Bundle args = getArguments();

        if (args != null) {
            String contactName = args.getString(Static.EXTRA_ROOMID);
            contactView.setText(contactName);
            videoCallEnabled = args.getBoolean(Static.EXTRA_VIDEO_CALL, true);
            captureSliderEnabled = videoCallEnabled
                    && args.getBoolean(Static.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, false);
        }

        if (!videoCallEnabled) {
            cameraSwitchButton.setVisibility(View.INVISIBLE);
        }

        if (captureSliderEnabled) {
            captureFormatSlider.setOnSeekBarChangeListener(
                    new CaptureQualityController(captureFormatText, callEvents));
        }

        else {
            captureFormatText.setVisibility(View.GONE);
            captureFormatSlider.setVisibility(View.GONE);
        }
    }

    // TODO(sakal): Replace with onAttach(Context) once we only support API level 23+.
    // API 레벨 23 이상 만 지원하면 onAttach(Context)로 바꿉니다.
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callEvents = (OnCallEvents) activity;
    }
}
