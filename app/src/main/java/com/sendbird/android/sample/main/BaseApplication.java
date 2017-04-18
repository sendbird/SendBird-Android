package com.sendbird.android.sample.main;


import android.app.Application;

import com.sendbird.android.SendBird;

public class BaseApplication extends Application {

    //private static final String APP_ID = "9DA1B1F4-0BE6-4DA8-82C5-2E81DAB56F23"; // US-1 Demo
    private static final String APP_ID = "21AC2AF0-7F63-46A6-93F2-A52E5C65D698"; // My unique id.
    //private static final String APP_ID = "21AC2AF0-7F63-46A6-93F2-A52E5C65D677"; // My second hack id. And this doesn't work.
    public static final String VERSION = "3.0.30";

    @Override
    public void onCreate() {
        super.onCreate();
        SendBird.init(APP_ID, getApplicationContext());
    }
}
