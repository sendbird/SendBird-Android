package com.sendbird.localcaching.sample.model;

public class LoginEvent {
    private final boolean isLoggedIn;

    public LoginEvent(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}
