package com.sany.downloadfiletest;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * @ClassName: App
 * @Description:
 * @Author: wuzhi.peng
 * @Date: 2022/3/25
 */
public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
