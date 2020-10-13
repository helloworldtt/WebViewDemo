package com.ting.webview;

import android.app.Application;
import android.os.Looper;
import android.util.Log;
import android.util.LogPrinter;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Looper.getMainLooper().setMessageLogging(new
                LogPrinter(Log.DEBUG, "mtThread"));
    }
}
