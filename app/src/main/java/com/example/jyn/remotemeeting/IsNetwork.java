package com.example.jyn.remotemeeting;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by JYN on 2017-11-10.
 */

//public class IsNetwork extends Activity {
//
//    private static final String TAG = "all_"+IsNetwork.class.getSimpleName();
//
//    public Boolean isNetWork(Context context){
//        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//        boolean isMobileAvailable = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
//        boolean isMobileConnect = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
//        boolean isWifiAvailable = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
//        boolean isWifiConnect = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
//
//        if ((isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect)){
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//}

public class IsNetwork {

    public static final int NETWORK_WIFI = 0;
    public static final int NETWORK_3G = 1;
    public static final int NETWORK_NONE = 2;

    public static int Status(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(wifi.isAvailable()) {
            return NETWORK_WIFI;
        }
        else if(mobile.isAvailable()) {
            return NETWORK_3G;
        }
        else {
            return NETWORK_NONE;
        }

    }

    public static boolean available(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(wifi.isAvailable() || mobile.isAvailable()) {
            return true;
        }
        else if(mobile.isAvailable()) {
            return false;
        }
        else {
            return false;
        }
    }
}
