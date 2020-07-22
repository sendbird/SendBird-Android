package com.sendbird.uikit.customsample;


import android.app.Application;

import com.sendbird.android.ApplicationUserListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.adapter.SendBirdUIKitAdapter;
import com.sendbird.uikit.customsample.models.CustomUser;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.interfaces.UserListResultHandler;

import java.util.ArrayList;
import java.util.List;

public class BaseApplication extends Application {

    private static final String APP_ID = "2D7B4CDB-932F-4082-9B09-A1153792DC8D";
    private String userId;
    private String userNickname;

    @Override
    public void onCreate() {
        super.onCreate();

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
                        return userId;
                    }

                    @Override
                    public String getNickname() {
                        return userNickname;
                    }

                    @Override
                    public String getProfileUrl() {
                        return "";
                    }
                };
            }
        }, this);

        SendBirdUIKit.setDefaultThemeMode(SendBirdUIKit.ThemeMode.Light);
        SendBirdUIKit.setLogLevel(SendBirdUIKit.LogLevel.ALL);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
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
