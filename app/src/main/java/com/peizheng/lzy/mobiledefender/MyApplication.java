package com.peizheng.lzy.mobiledefender;

import android.app.Application;

import com.lzy.okhttputils.OkHttpUtils;

public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpUtils.init(this);
    }
}

