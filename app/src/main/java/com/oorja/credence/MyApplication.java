package com.oorja.credence;

import android.app.Application;

import credence.oorja.com.androidsdk.PushSdkClient;

/**
 * Created by gaurav on 22/11/17.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PushSdkClient.init(this, "testApiKey");
    }
}
