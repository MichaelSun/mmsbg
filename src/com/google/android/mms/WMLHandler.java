package com.google.android.mms;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class WMLHandler extends DefaultHandler {
    private static final String TAG = "WMLHandler";
    private static final boolean DEBUG = false;    
    
    private StringBuilder mCurrentValue;
    
    public WMLHandler() {
        super();
        mCurrentValue = new StringBuilder();
    }
    
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
        LOGD("[[startElement]]  uri = " + uri + "  localName = " + localName + " name = " + name);
        if (attributes != null) {
            for (int index = 0; index < attributes.getLength(); ++index) {
                LOGD("[[startElement]] attr name = " + attributes.getLocalName(index) + " value = " + attributes.getValue(index)); 
            }
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        super.endElement(uri, localName, name);
        LOGD("[[endElement]]  uri = " + uri + "  localName = " + localName + " name = " + name);
        mCurrentValue.setLength(0);
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
    
    private void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}
