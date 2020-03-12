package com.sendbird.android.sample.utils;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdPushHandler;
import com.sendbird.android.SendBirdPushHelper;

public class PushUtils {

    public static void registerPushHandler(SendBirdPushHandler handler) {
        SendBirdPushHelper.registerPushHandler(handler);
    }

    public static void unregisterPushHandler(SendBirdPushHelper.OnPushRequestCompleteListener listener) {
        SendBirdPushHelper.unregisterPushHandler(listener);
    }

    public static void setPushNotification(boolean enable, SendBird.SetPushTriggerOptionHandler handler) {
        SendBird.PushTriggerOption option = enable ? SendBird.PushTriggerOption.ALL : SendBird.PushTriggerOption.OFF;
        SendBird.setPushTriggerOption(option, handler);
    }
}
