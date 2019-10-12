package com.sendbird.syncmanager.sample.main;


import android.app.Application;

import com.sendbird.android.SendBird;
import com.sendbird.syncmanager.sample.utils.PreferenceUtils;
import com.sendbird.syncmanager.SendBirdSyncManager;

public class BaseApplication extends Application {

    private static final String APP_ID = "24CF77F2-1083-4A23-8FD1-4C63C108EE8E";
    public static final String VERSION = "3.0.40";
    private boolean mIsSyncManagerSetup = false;

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceUtils.init(getApplicationContext());

        SendBird.init(APP_ID, getApplicationContext());
        SendBirdSyncManager.setLoggerLevel(98765);
    }

    public boolean isSyncManagerSetup() {
        return mIsSyncManagerSetup;
    }

    public void setSyncManagerSetup(boolean syncManagerSetup) {
        mIsSyncManagerSetup = syncManagerSetup;
    }
}
