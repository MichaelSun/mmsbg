package com.google.android.mms;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class VedioWebViewActivity extends Activity {
    private SettingManager mSM;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.vedio_web_view);
        mSM = SettingManager.getInstance(this);
        
        WebView view = (WebView) findViewById(R.id.webview);
        if (view != null) {
            view.loadUrl("http://211.136.165.53/wl/rmw1s/pp66.jsp");
        }
    }
}
