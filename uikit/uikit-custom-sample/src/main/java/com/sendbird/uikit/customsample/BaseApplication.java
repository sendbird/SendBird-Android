package com.sendbird.uikit.customsample;


import android.app.Application;

import androidx.annotation.NonNull;

import com.sendbird.android.ApplicationUserListQuery;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.android.OpenChannelParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.android.UserMessageParams;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.adapter.SendBirdUIKitAdapter;
import com.sendbird.uikit.customsample.fcm.MyFirebaseMessagingService;
import com.sendbird.uikit.customsample.models.CustomUser;
import com.sendbird.uikit.customsample.utils.PreferenceUtils;
import com.sendbird.uikit.customsample.utils.PushUtils;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.interfaces.UserListResultHandler;

import java.util.ArrayList;
import java.util.List;

public class BaseApplication extends Application {

    private static final String APP_ID = "2D7B4CDB-932F-4082-9B09-A1153792DC8D";

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
                        return PreferenceUtils.getProfileUrl();
                    }
                };
            }
        }, this);

        PushUtils.registerPushHandler(new MyFirebaseMessagingService());
        SendBirdUIKit.setDefaultThemeMode(SendBirdUIKit.ThemeMode.Light);
        SendBirdUIKit.setLogLevel(SendBirdUIKit.LogLevel.ALL);
        SendBirdUIKit.setUseDefaultUserProfile(false);
        SendBirdUIKit.setCustomParamsHandler(new CustomParamsHandler() {
            @Override
            public void onBeforeCreateGroupChannel(@NonNull GroupChannelParams groupChannelParams) {
                // You can set GroupChannelParams globally before creating a channel.
            }

            @Override
            public void onBeforeUpdateGroupChannel(@NonNull GroupChannelParams groupChannelParams) {
                // You can set GroupChannelParams globally before updating a channel.
            }

            @Override
            public void onBeforeSendUserMessage(@NonNull UserMessageParams userMessageParams) {
                // You can set UserMessageParams globally before sending a text message.
            }

            @Override
            public void onBeforeSendFileMessage(@NonNull FileMessageParams fileMessageParams) {
                // You can set FileMessageParams globally before sending a binary file message.
            }

            @Override
            public void onBeforeUpdateUserMessage(@NonNull UserMessageParams userMessageParams) {
                // You can set UserMessageParams globally before updating a text message.
            }

            @Override
            public void onBeforeUpdateOpenChannel(@NonNull OpenChannelParams openChannelParams) {
                // You can set OpenChannelParams globally before updating a channel.
            }
        });
    }

    public static CustomUserListQueryHandler getCustomUserListQuery() {
        final ApplicationUserListQuery userListQuery = SendBird.createApplicationUserListQuery();
        return new CustomUserListQueryHandler() {
            @Override
            public void loadInitial(UserListResultHandler handler) {
                userListQuery.setLimit(5);
                userListQuery.next((list, e) -> {
                    if (e != null) {
                        return;
                    }

                    List<CustomUser> customUserList = new ArrayList<>();
                    for (User user : list) {
                        customUserList.add(new CustomUser(user));
                    }
                    handler.onResult(customUserList, null);
                });
            }

            @Override
            public void loadNext(UserListResultHandler handler) {
                userListQuery.next((list, e) -> {
                    if (e != null) {
                        return;
                    }

                    List<CustomUser> customUserList = new ArrayList<>();
                    for (User user : list) {
                        customUserList.add(new CustomUser(user));
                    }
                    handler.onResult(customUserList, null);
                });
            }

            @Override
            public boolean hasMore() {
                return userListQuery.hasNext();
            }
        };
    }
}
