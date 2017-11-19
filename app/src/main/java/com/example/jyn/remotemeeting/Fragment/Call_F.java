package com.example.jyn.remotemeeting.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.example.jyn.remotemeeting.Adapter.RecyclerView_adapter;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Static;
import com.example.jyn.remotemeeting.WebRTC.CaptureQualityController;
import com.squareup.otto.Subscribe;

import org.webrtc.RendererCommon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

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
    public static boolean from_files_select;

    private static final String TAG = "all_"+Call_F.class.getSimpleName();
    final int REQUEST_OUT=1000;
    private static String current_view;

    // 리사이클러뷰 관련 클래스
    public RecyclerView_adapter recyclerAdapter_project;
    public RecyclerView_adapter recyclerAdapter_local;
    private RecyclerView.LayoutManager layoutManager;


    /** 버터나이프 적용시킨 이후 뷰 찾기*/
    private Unbinder unbinder;
    @BindView(R.id.popup_file_manager)      public RelativeLayout popup_file_manager_REL;
    @BindView(R.id.recyclerView)            public RecyclerView recyclerView;
    @BindView(R.id.file_box_title)          public TextView file_box_title;
    @BindView(R.id.back_to_menu)            public ImageView back_to_menu;
    public static ImageView add_files;

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
        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this, controlView);

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
        add_files = controlView.findViewById(R.id.add_files);

        /** 리사이클러뷰 - 프로젝트 파일 */
        recyclerView.setHasFixedSize(true);
        // 리사이클러뷰 - GridLayoutManager 사용
        layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 구분선 - 가로, 세로(디폴트)
//        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        // 리사이클러뷰 구분선 - 가로(클래스 생성)
//        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // 생성자 인수
        // 1. 액티비티
        // 2. 인플레이팅 되는 레이아웃
        // 3. extra 변수
        recyclerAdapter_project = new RecyclerView_adapter(getActivity(), R.layout.i_file, "project");
        recyclerView.setAdapter(recyclerAdapter_project);
        recyclerAdapter_project.notifyDataSetChanged();
        current_view = "none";

        // 리사이클러뷰 에니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());


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
                        popup_file_manager_REL.setVisibility(View.GONE);
                        current_view = "menu";
                        break;
                    case View.VISIBLE:
                        popup_menu_REL.setVisibility(View.GONE);
                        popup_file_manager_REL.setVisibility(View.GONE);
                        current_view = "none";
                        break;
                }
            }
        });

        return controlView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // otto 등록
        BusProvider.getBus().register(this);

        boolean captureSliderEnabled = false;
        Bundle args = getArguments();

        if (args != null) {
            String contactName = args.getString(Static.EXTRA_ROOMID);
            contactView.setText(contactName);fileFormat:
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


    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getBus().unregister(this);
    }

    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회의 파일함 Open
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.file_box_IV})
    public void go_file_box(View view) {
        recyclerView.setAdapter(recyclerAdapter_project);
        recyclerAdapter_project.notifyDataSetChanged();

        popup_menu_REL.setVisibility(View.GONE);
        popup_menu_icon.setVisibility(View.GONE);
        popup_file_manager_REL.setVisibility(View.VISIBLE);
        back_to_menu.setVisibility(View.VISIBLE);
        add_files.setVisibility(View.GONE);

        file_box_title.setText("회의 파일함");
        current_view = "project";

//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) popup_menu_icon.getLayoutParams();
//        params.addRule(RelativeLayout.RIGHT_OF, popup_file_manager_REL.getId());
//        popup_menu_icon.setLayoutParams(params);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> back_img 클릭 -- 회의 파일함->메뉴로 이동
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.back_to_menu)
    public void back_to_menu() {
        Log.d(TAG, "current_view: " + current_view);

        popup_menu_REL.setVisibility(View.VISIBLE);
        popup_menu_icon.setVisibility(View.VISIBLE);
        popup_file_manager_REL.setVisibility(View.GONE);
        current_view = "menu";
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> add_complete 클릭 -- 로컬파일함->회의 파일함으로 이동
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.add_files)
    public void add_complete() {
        Log.d(TAG, "current_view: " + current_view);

        recyclerAdapter_local.save_to_shared();

        // 이전 시도 방법
//        from_files_select = true;
//        recyclerView.setAdapter(recyclerAdapter_project);
//        recyclerAdapter_project.notifyDataSetChanged();

        // 리사이클러뷰 프로젝트 새로 생성
        recyclerAdapter_project = null;
        from_files_select = true;
        recyclerAdapter_project = new RecyclerView_adapter(getActivity(), R.layout.i_file, "project");
        recyclerView.setAdapter(recyclerAdapter_project);
        recyclerAdapter_project.notifyDataSetChanged();

        popup_menu_REL.setVisibility(View.GONE);
        popup_menu_icon.setVisibility(View.GONE);
        popup_file_manager_REL.setVisibility(View.VISIBLE);
        back_to_menu.setVisibility(View.VISIBLE);
        add_files.setVisibility(View.GONE);

        file_box_title.setText("회의 파일함");
        current_view = "project";

//        // 체크한 파일 shared에 저장하기
//        recyclerAdapter_local.save_to_shared();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 팝업 닫기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.close_popup)
    public void close_popup() {
        popup_menu_REL.setVisibility(View.GONE);
        popup_menu_icon.setVisibility(View.VISIBLE);
        popup_file_manager_REL.setVisibility(View.GONE);
        current_view = "none";
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 버터나이프 바인드 해제
        if(unbinder != null) {
            unbinder.unbind();
        }
    }


    /**---------------------------------------------------------------------------
     otto ==> Call_A로 부터 message 수신
     ---------------------------------------------------------------------------*/
    @Subscribe
    public void getMessage(Event.ActivityFragmentMessage activityFragmentMessage) {
        Log.d(TAG, "activityFragmentMessage.getMessage: " + activityFragmentMessage.getMessage());
        // 로컬 파일을 보여주기 위한 리사이클러뷰 생성 메소드 호출
        create_recyclerView_local_file(activityFragmentMessage.getMessage());
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 회의 파일함->로컬파일함으로 이동
     ---------------------------------------------------------------------------*/
    public void create_recyclerView_local_file(String format) {
        recyclerAdapter_local = new RecyclerView_adapter(getActivity(), R.layout.i_file, format);
        recyclerView.setAdapter(recyclerAdapter_local);
        recyclerAdapter_local.notifyDataSetChanged();

        back_to_menu.setVisibility(View.GONE);
        add_files.setVisibility(View.VISIBLE);

        String title = "";
        if(format.equals("pdf")) {
            title = "내장 PDF 파일";
        }
        else if(format.equals("img")) {
            title = "내장 Image 파일";
        }
        else if(format.equals("all")) {
            title = "내장 PDF / Image 파일";
        }

        file_box_title.setText(title);
        current_view = format;
    }
}
