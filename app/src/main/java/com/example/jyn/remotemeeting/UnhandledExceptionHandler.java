package com.example.jyn.remotemeeting;

/**
 * Created by JYN on 2017-11-10.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Singleton helper: install a default unhandled exception handler which shows
 * an informative dialog and kills the app.
 * Useful for apps whose error-handling consists of throwing RuntimeExceptions.
 * NOTE: almost always more useful to
 * Thread.setDefaultUncaughtExceptionHandler() rather than
 * Thread.setUncaughtExceptionHandler(), to apply to background threads as well.
 *
 * 싱글턴 도우미 : 유익한 대화 상자를 표시하고 응용 프로그램을 죽이는 기본 처리되지 않은 예외 처리기를 설치하십시오.
 * 오류 처리가 RuntimeExceptions를 던지는 것으로 구성된 앱에 유용합니다.
 *
 * 참고 : 거의 항상 thread.setUncaughtExceptionHandler()보다는,
 * Thread.setDefaultUncaughtExceptionHandler()를 백그라운드 스레드에 적용하는 것이 더 유용합니다.
 */
public class UnhandledExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "AppRTCMobileActivity";
    private final Activity activity;

    public UnhandledExceptionHandler(final Activity activity) {
        this.activity = activity;
    }

    @Override
    public void uncaughtException(Thread unusedThread, final Throwable e) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String title = "Fatal error: " + getTopLevelCauseMessage(e);
                String msg = getRecursiveStackTrace(e);
                TextView errorView = new TextView(activity);
                errorView.setText(msg);
                errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                ScrollView scrollingContainer = new ScrollView(activity);
                scrollingContainer.addView(errorView);
                Log.e(TAG, title + "\n\n" + msg);
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        System.exit(1);
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(title)
                        .setView(scrollingContainer)
                        .setPositiveButton("Exit", listener)
                        .show();
            }
        });
    }

    // Returns the Message attached to the original Cause of |t|.
    // |t|의 원래 원인에 첨부 된 메시지를 리턴합니다.
    private static String getTopLevelCauseMessage(Throwable t) {
        Throwable topLevelCause = t;
        while (topLevelCause.getCause() != null) {
            topLevelCause = topLevelCause.getCause();
        }
        return topLevelCause.getMessage();
    }

    // Returns a human-readable String of the stacktrace in |t|, recursively through all Causes that led to |t|.
    // |t|에있는 stackTrace의 사람이 읽을 수 있는 문자열을 |t|에 연결된 모든 원인을 통해 재귀적으로 반환합니다.
    private static String getRecursiveStackTrace(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
