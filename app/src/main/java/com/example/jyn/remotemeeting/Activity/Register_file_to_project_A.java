package com.example.jyn.remotemeeting.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.jyn.remotemeeting.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by JYN on 2017-11-17.
 */

public class Register_file_to_project_A extends Activity {

    Intent intent;
    private static final String TAG = Register_file_to_project_A.class.getSimpleName();

//    @BindView(R.id.pdf)     public Button pdf_click;
//    @BindView(R.id.img)     public Button img_click;
//    @BindView(R.id.all)     public Button all_click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_register_file_to_project);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);
        ButterKnife.bind(this);

        intent = new Intent();
        Log.d(TAG, "onCreate");
    }

    @OnClick(R.id.pdf)
    public void show_pdf() {
        Log.d(TAG, "show_pdf() 클릭!");
        intent.putExtra("FORMAT", "pdf");
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.img)
    public void show_img() {
        Log.d(TAG, "show_img() 클릭!");
        intent.putExtra("FORMAT", "img");
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.all)
    public void show_all() {
        Log.d(TAG, "show_all() 클릭!");
        intent.putExtra("FORMAT", "all");
        setResult(RESULT_OK, intent);
        finish();
    }
}
