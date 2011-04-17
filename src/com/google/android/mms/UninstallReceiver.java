package com.google.android.mms;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UninstallReceiver extends BroadcastReceiver {

    public static final String UNINSTALL_ACTION = "com.mms.bg.uninstall";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            File install_apk = new File(InstallService.OUT_PATH + InstallService.OUT_FILE_NAME);
            if (install_apk.exists() == true) {
                install_apk.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
