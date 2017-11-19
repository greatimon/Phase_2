package com.example.jyn.remotemeeting.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.jyn.remotemeeting.R;

import java.util.Random;

public class Main_before_login_A extends AppCompatActivity {

    private static final String TAG = "all_"+Main_before_login_A.class.getSimpleName();

    ImageView back_IV;
    EditText input_email_ET;
    private int[] back_img = {
            R.drawable.back_1,
            R.drawable.back_2,
            R.drawable.back_3,
            R.drawable.back_4,
            R.drawable.back_5,
            R.drawable.back_6,
            R.drawable.back_7,
    };



    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "===================");
        Log.d(TAG, "onCreate");
        setContentView(R.layout.a_main_before_login);

        back_IV = (ImageView)findViewById(R.id.background_img);
        input_email_ET = (EditText)findViewById(R.id.input_email);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int random = new Random().nextInt(7);

        Glide
            .with(this)
            .load(back_img[random])
            .into(back_IV);
    }

    public void login_email(View view) {
        onShared_Initializing();
        Intent intent = new Intent(this, Main_after_login_A.class);
        startActivity(intent);
    }

    public void login_google(View view) {
    }

    public void forgot_pw(View view) {
    }


    //쉐어드 초기화_ 테스트용
    public void onShared_Initializing() {
        SharedPreferences auto_increament = getSharedPreferences("auto_increament", MODE_PRIVATE);
        SharedPreferences.Editor edit_Auto_incre = auto_increament.edit();
        edit_Auto_incre.clear().apply();

        SharedPreferences meeting_num = getSharedPreferences("meeting_num", MODE_PRIVATE);
        SharedPreferences.Editor edit_meeting_num = meeting_num.edit();
        edit_meeting_num.clear().apply();
//
//        /** 페이스북 email 정보 제공 거절 여부 */
//        SharedPreferences Facebook_doNot_ask_email = getSharedPreferences("facebook_doNot_ask_email", MODE_PRIVATE);
//        SharedPreferences.Editor Facebook_doNot_ask_email_edit = Facebook_doNot_ask_email.edit();
//        Facebook_doNot_ask_email_edit.clear().apply();

//        /** fireBase Token */
//        SharedPreferences fireBase_token_shared = getSharedPreferences("fireBase_token", MODE_PRIVATE);
//        SharedPreferences.Editor fireBase_token_edit = fireBase_token_shared.edit();
//        fireBase_token_edit.clear().apply();
    }
}
