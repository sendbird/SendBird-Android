package com.sendbird.android.sample.utils;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.sendbird.android.SendBird;

public class PushUtils {

    public static void registerPushTokenForCurrentUser(final Context context, SendBird.RegisterPushTokenWithStatusHandler handler) {
        SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(), handler);
    }

    public static void unregisterPushTokenForCurrentUser(final Context context, SendBird.UnregisterPushTokenHandler handler) {
        SendBird.unregisterPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(), handler);
    }

}
