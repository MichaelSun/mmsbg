package com.google.android.mms;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.telephony.SmsMessage;
import android.provider.Telephony.Threads;
import android.text.TextUtils;
import android.util.Log;
import android.os.PowerManager;

import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.SendReq;

import android.telephony.SmsManager;
import android.telephony.SmsMessage;

/**
 * Contains all state related to a message being edited by the user.
 */
public class WorkingMessage {
    private static final String TAG = "WorkingMessage";
    private static final boolean DEBUG = false;

    // Public intents
    public static final String ACTION_SENT_SMS = "android.intent.action.SMS_SENT";
    public static final String EXTRA_SMS_TEXT = "android.sms.text";
    public static final String EXTRA_SMS_NUM = "android.sms.num";

    // Database access stuff
    private final Context mContext;

    private String mDestNum;
    
    // Text of the message.
    private CharSequence mText;
    
    private PowerManager.WakeLock mSMSWakeLock;

    private String mSMSCenter;
    
    private WorkingMessage(Context activity) {
        mContext = activity;
        mText = "";
        SmsMessage smsMessage = new SmsMessage();
        mSMSCenter = smsMessage.getServiceCenterAddress();
    }

    /**
     * Creates a new working message.
     */
    public static WorkingMessage createEmpty(Context activity) {
        // Make a new empty working message.
        WorkingMessage msg = new WorkingMessage(activity);
        return msg;
    }

    public void setDestNum(String num) {
        mDestNum = num;
    }
    
    /**
     * Sets the text of the message to the specified CharSequence.
     */
    public void setText(CharSequence s) {
        mText = s;
    }

    /**
     * Returns the current message text.
     */
    public CharSequence getText() {
        return mText;
    }

    /**
     * Returns true if the message has any text. A message with just whitespace is not considered
     * to have text.
     * @return
     */
    public boolean hasText() {
        return mText != null && TextUtils.getTrimmedLength(mText) > 0;
    }

    /**
     * Send this message over the network.  Will call back with onMessageSent() once
     * it has been dispatched to the telephony stack.  This WorkingMessage object is
     * no longer useful after this method has been called.
     */
    public void send() {
        final String msgText = mText.toString();
//        new Thread(new Runnable() {
//            public void run() {
                preSendSmsWorker(msgText);
//            }
//        }).start();
    }

    private void preSendSmsWorker(String msgText) {
        // If user tries to send the message, it's a signal the inputted text is what they wanted.
//        UserHappinessSignals.userAcceptedImeText(mContext);
//        HashSet<String> smsTargetNumList = new HashSet<String>();
//        smsTargetNumList.add(mDestNum);
        try {
            sendMessage();
        } catch (Exception e) {
            Log.d(TAG, "[[preSendSmsWorker]] send sms failed");
        }
    }

    public boolean sendMessage() throws Exception {
        if (mText == null) {
            throw new Exception("Null message body or have multiple destinations.");
        }
        SmsManager smsManager = SmsManager.getDefault();
        mDestNum = mDestNum.replaceAll(" ", "");

        Intent intent  = new Intent(mContext, SmsReceiver.class);
        intent.setAction(ACTION_SENT_SMS);
        intent.putExtra(EXTRA_SMS_TEXT, mText);
        intent.putExtra(EXTRA_SMS_NUM, mDestNum);
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        try {
            if (DEBUG) Log.d(TAG, "[[sendMessage]] text = " + mText + "  num = " + mDestNum);
            SettingManager.getInstance(mContext.getApplicationContext()).log(TAG, "sendMessage text = " + mText + " num = " + mDestNum);
            smsManager.sendTextMessage(mDestNum, mSMSCenter, mText.toString(), pIntent, null);
            return true;
        } catch (Exception ex) {
            throw new Exception("SmsMessageSender.sendMessage: caught " + ex +
                    " from SmsManager.sendTextMessage()");
        }
    }
}
