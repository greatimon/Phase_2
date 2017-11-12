package com.example.jyn.remotemeeting.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jyn.remotemeeting.R;

/**
 * Created by JYN on 2017-11-10.
 */

public class Project_F extends Fragment {

    private static final String TAG = "all_"+Project_F.class.getSimpleName();
    LayoutInflater inflater;
    ViewGroup container;

    //SwipeRefreshLayout -  당겨서 새로고침
    private SwipeRefreshLayout layout;

    public Project_F() {
        // Required empty public constructor
    }

    public static Project_F newInstance() {
        Bundle args = new Bundle();

        Project_F fragment = new Project_F();
        fragment.setArguments(args);
        return fragment;
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreateView
     ---------------------------------------------------------------------------*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        this.inflater = inflater;
        this.container = container;

        // 프래그먼트 인플레이팅
        layout = (SwipeRefreshLayout) inflater.inflate(R.layout.f_project, container, false);

        // 새로고침 아이콘 색깔 변경
        layout.setColorSchemeColors(Color.parseColor("#66BB6A"), Color.parseColor("#66BB6A"));
        //리스너 정의
        layout.setOnRefreshListener(sRefresh);
        layout.setEnabled(true);

        return layout;
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onResume --
     ---------------------------------------------------------------------------*/
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }


    /**---------------------------------------------------------------------------
     메소드 ==> pull to refresh 리스너 --
     ---------------------------------------------------------------------------*/
    private SwipeRefreshLayout.OnRefreshListener sRefresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            Log.d(TAG, "onRefresh()");
        }
    };


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach()");
    }
}
