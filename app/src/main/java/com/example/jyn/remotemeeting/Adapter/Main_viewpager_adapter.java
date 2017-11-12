package com.example.jyn.remotemeeting.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Fragment.Chat_F;
import com.example.jyn.remotemeeting.Fragment.Notice_F;
import com.example.jyn.remotemeeting.Fragment.Partner_F;
import com.example.jyn.remotemeeting.Fragment.Profile_F;
import com.example.jyn.remotemeeting.Fragment.Project_F;

/**
 * Created by JYN on 2017-11-10.
 */

public class Main_viewpager_adapter extends FragmentPagerAdapter {

    public static int PAGE_NUMBER = 5;
    Context context;

    public Main_viewpager_adapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return Project_F.newInstance();
            case 1:
                return Partner_F.newInstance();
            case 2:
                return Chat_F.newInstance();
            case 3:
                return Notice_F.newInstance();
            case 4:
                return Profile_F.newInstance();
            default:
                return null;
        }
    }

//    @Override
//    public CharSequence getPageTitle(int position) {
//        switch(position) {
//            case 0:
//                return "Project";
//            case 1:
//                return "Partner";
//            case 2:
//                return "Chat";
//            case 3:
//                return "Notice";
//            case 4:
//                return "Profile";
//            default:
//                return null;
//        }
//    }

    @Override
    public int getCount() {
        return PAGE_NUMBER;
    }

    public static class ViewUtil {
        public static int TEXT_SIZE  = -1;
        public static int TEXT_SIZE_BIG = -1;

        public static Drawable drawable(Context context, int id) {
            if (TEXT_SIZE == -1) {
                TEXT_SIZE = (int) new TextView(context).getTextSize();
                TEXT_SIZE_BIG = (int) (TEXT_SIZE * 1.5);
            }
            if (Build.VERSION.SDK_INT >= 21) {
                return context.getResources().getDrawable(id, context.getTheme());
            } else {
                return context.getResources().getDrawable(id);
            }
        }

        public static CharSequence iconText(Drawable icon, String text) {
            SpannableString iconText = new SpannableString(" "+text);
            icon.setBounds(0, 0, TEXT_SIZE_BIG, TEXT_SIZE_BIG);
            ImageSpan imageSpan = new ImageSpan(icon);

            iconText.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return iconText;
        }
    }
}
