package com.google.android.mms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;

public class LogUtil {
    
    private static final String TAG = "LogUtil";
    private static final boolean DEBUG = false;
    
    private String mPath;
    private XmlSerializer mSerializer;
    private FileWriter mFileWriter;
    private BufferedWriter mBufferedWriter;
    private SimpleDateFormat mDateFormat;
    private static LogUtil gLogUtil;
    
    public static LogUtil getInstance(String path) {
        if (gLogUtil == null) {
            gLogUtil = new LogUtil(path);
        }
        return gLogUtil;
    }
    
    private LogUtil(String path) {
        mPath = path;
        mDateFormat = new SimpleDateFormat("[[yyyy,MMM,dd,h:mmaa]]: ");
        mDateFormat.setTimeZone(Calendar.getInstance().getTimeZone());
        init(mPath);
    }
    
    public void appendLog(String log) {
        try {
            mBufferedWriter.write(mDateFormat.format(new Date(System.currentTimeMillis())));
            mBufferedWriter.write(log);
            mBufferedWriter.write("\n");
            mBufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void release() {
        try {
            mBufferedWriter.close();
            mFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File logFile = new File(mPath);
        if (logFile.exists() == true) {
            logFile.delete();
        }
    }
    
    private void init(String path) {
        File logFile = new File(path);
        try {
            if (logFile.exists() == false) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e("XmlLog", "[[init]] exception in createNewFile() method : " + e.getMessage());
        }
        
        try {
            mFileWriter = new FileWriter(path, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mBufferedWriter = new BufferedWriter(mFileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
