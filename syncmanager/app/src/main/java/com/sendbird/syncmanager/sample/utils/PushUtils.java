package com.sendbird.syncmanager.sample.utils;

import android.util.Log;

import com.sendbird.android.SendBird;
import com.sendbird.syncmanager.sample.fcm.MyFirebaseMessagingService;

public class PushUtils {

    public static void registerPushTokenForCurrentUser() {
        registerPushTokenForCurrentUser(null);
    }

    public static void registerPushTokenForCurrentUser(SendBird.RegisterPushTokenWithStatusHandler handler) {
        MyFirebaseMessagingService.getPushToken(pushToken -> {
            Log.d("Token", "++ pushToken : "+ pushToken);
            SendBird.registerPushTokenForCurrentUser(pushToken, handler);
        });
    }

    public static void unregisterPushTokenForCurrentUser(SendBird.UnregisterPushTokenHandler handler) {
        MyFirebaseMessagingService.getPushToken(pushToken -> SendBird.unregisterPushTokenForCurrentUser(pushToken, handler));
    }

    public static void unregisterPushTokenAllForCurrentUser(SendBird.UnregisterPushTokenHandler handler) {
        SendBird.unregisterPushTokenAllForCurrentUser(handler);
    }

}
