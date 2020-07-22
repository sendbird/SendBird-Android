package com.sendbird.uikit.customsample.models;

import com.sendbird.android.User;
import com.sendbird.uikit.interfaces.UserInfo;

public class CustomUser implements UserInfo {
    User user;

    public CustomUser(User user) {
        this.user = user;
    }

    @Override
    public String getUserId() {
        return user.getUserId();
    }

    @Override
    public String getNickname() {
        return user.getNickname();
    }

    @Override
    public String getProfileUrl() {
        return user.getProfileUrl();
    }
}
