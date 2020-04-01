package com.sendbird.uikit_messaging_android;


import android.app.Application;

import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.adapter.SendBirdUIKitAdapter;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit_messaging_android.fcm.MyFirebaseMessagingService;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.utils.PushUtils;

public class BaseApplication extends Application {

    private static final String APP_ID = "2D7B4CDB-932F-4082-9B09-A1153792DC8D"; // Tokyo

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceUtils.init(getApplicationContext());

        SendBirdUIKit.init(new SendBirdUIKitAdapter() {
            @Override
            public String getAppId() {
                return APP_ID;
            }

            @Override
            public String getAccessToken() {
                return "";
            }

            @Override
            public UserInfo getUserInfo() {
                return new UserInfo() {
                    @Override
                    public String getUserId() {
                        return PreferenceUtils.getUserId();
                    }

                    @Override
                    public String getNickname() {
                        return PreferenceUtils.getNickname();
                    }

                    @Override
                    public String getProfileUrl() {
                        return "";
                    }
                };
            }
        }, this);

        boolean useDarkTheme = PreferenceUtils.isUsingDarkTheme();
        SendBirdUIKit.setDefaultThemeMode(useDarkTheme ? SendBirdUIKit.ThemeMode.Dark : SendBirdUIKit.ThemeMode.Light);
        PushUtils.registerPushHandler(new MyFirebaseMessagingService());
    }
}
