package com.sendbird.localcaching.sample.main;


import android.app.Application;
import android.util.Log;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.handlers.InitResultHandler;
import com.sendbird.localcaching.sample.model.InitEvent;
import com.sendbird.localcaching.sample.utils.PreferenceUtils;
import com.sendbird.localcaching.sample.widget.WaitingDialog;

import org.greenrobot.eventbus.EventBus;

public class BaseApplication extends Application {

    private static final String APP_ID = "9DA1B1F4-0BE6-4DA8-82C5-2E81DAB56F23"; // US-1 Demo
    public static final String VERSION = "3.0.40";
    public static boolean isInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceUtils.init(getApplicationContext());

        // Local caching
        isInitialized = false;
        SendBird.init(APP_ID, getApplicationContext(), true, new InitResultHandler() {
            @Override
            public void onMigrationStarted() {
                Log.i("Application", "Called when there's an upgrade in db.");
                WaitingDialog.show(BaseApplication.this);
            }

            @Override
            public void onInitFailed(SendBirdException e) {
                Log.i("Application", "Called when initialize failed.");
                WaitingDialog.dismiss();
                isInitialized = true;
                EventBus.getDefault().post(new InitEvent(true));
            }

            @Override
            public void onInitSucceed() {
                Log.i("Application", "Called when initialize is done.");
                WaitingDialog.dismiss();
                isInitialized = true;
                EventBus.getDefault().post(new InitEvent(true));
            }
        });
    }

}
