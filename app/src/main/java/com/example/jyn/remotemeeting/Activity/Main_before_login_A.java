package com.example.jyn.remotemeeting.Activity;

import android.content.Intent;
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
        Intent intent = new Intent(this, Main_after_login_A.class);
        startActivity(intent);
    }

    public void login_google(View view) {
    }

    public void forgot_pw(View view) {
    }
}
