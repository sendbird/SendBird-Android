package com.sendbird.uikit.customsample.utils;

import com.sendbird.android.SendBirdPushHandler;
import com.sendbird.android.SendBirdPushHelper;

public class PushUtils {

    public static void registerPushHandler(SendBirdPushHandler handler) {
        SendBirdPushHelper.registerPushHandler(handler);
    }

    public static void unregisterPushHandler(SendBirdPushHelper.OnPushRequestCompleteListener listener) {
        SendBirdPushHelper.unregisterPushHandler(listener);
    }
}
