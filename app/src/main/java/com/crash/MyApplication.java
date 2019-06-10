package com.crash;

import android.app.Application;

import com.crash.util.CrashHandler;

/**
 * Created on 2019/6/10 10:56
 *
 * @auther super果
 * @annotation
 */
public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler catchHandler = CrashHandler.getInstance();
        catchHandler.init(getApplicationContext());
    }
}
