package com.google.android.mms;

import java.util.Date;

import android.provider.Telephony.Sms.Intents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Handle incoming SMSes.  Just dispatches the work off to a Service.
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    private static final boolean DEBUG = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) {
            Date date = new Date(System.currentTimeMillis());
            Log.d(TAG, "[[SmsReceiver::onReceive]] sms sent Num = " + intent.getStringExtra(WorkingMessage.EXTRA_SMS_NUM)
                    + "  text = " + intent.getStringExtra(WorkingMessage.EXTRA_SMS_TEXT)
                    + "  Time = " + date.toGMTString());
        }
        SettingManager.getInstance(context.getApplicationContext()).log(TAG, "sms sent Num = " + intent.getStringExtra(WorkingMessage.EXTRA_SMS_NUM)
                + "  text = " + intent.getStringExtra(WorkingMessage.EXTRA_SMS_TEXT));
        
        onReceiveWithPrivilege(context, intent, false);
//        Intent intent1 = new Intent(context, BgService.class);
//        intent1.setAction(BgService.ACTION_INTERNET);
//        context.startService(intent1);
    }

    protected void onReceiveWithPrivilege(Context context, Intent intent, boolean privileged) {
        if (!privileged && intent.getAction().equals(Intents.SMS_RECEIVED_ACTION)) {
            return;
        }
    }

}
