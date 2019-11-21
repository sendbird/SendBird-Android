package com.sendbird.syncmanager.sample.main;


import android.app.Application;

import com.sendbird.android.SendBird;
import com.sendbird.syncmanager.sample.utils.PreferenceUtils;

public class BaseApplication extends Application {

    private static final String APP_ID = "9DA1B1F4-0BE6-4DA8-82C5-2E81DAB56F23"; // US-1 Demo
    public static final String VERSION = "3.0.40";

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceUtils.init(getApplicationContext());

        SendBird.init(APP_ID, getApplicationContext());

    }

}
