package com.sendbird.syncmanager.sample.main;

import android.content.Context;
import android.widget.Toast;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.syncmanager.SendBirdSyncManager;
import com.sendbird.syncmanager.handler.CompletionHandler;
import com.sendbird.syncmanager.sample.R;
import com.sendbird.syncmanager.sample.model.ConnectionEvent;
import com.sendbird.syncmanager.sample.utils.PreferenceUtils;
import com.sendbird.syncmanager.sample.utils.PushUtils;
import com.sendbird.syncmanager.sample.utils.SyncManagerUtils;

import org.greenrobot.eventbus.EventBus;

public class ConnectionManager {
    public static boolean isLogin() {
        return PreferenceUtils.getConnected();
    }

    public static void connect(final Context context, String userId, final String userNickname, final SendBird.ConnectHandler handler) {
        SendBird.connect(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(context, context.getString(R.string.sendbird_error_with_code, e.getCode(), e.getMessage()), Toast.LENGTH_SHORT).show();

                    if (handler != null) {
                        handler.onConnected(user, e);
                    }
                    EventBus.getDefault().post(new ConnectionEvent(false));
                    return;
                }

                SyncManagerUtils.setup(context, userId, new CompletionHandler() {
                    @Override
                    public void onCompleted(SendBirdException e) {
                        SendBirdSyncManager.getInstance().resumeSync();
                    }
                });

                PushUtils.registerPushTokenForCurrentUser();
                SendBird.updateCurrentUserInfo(userNickname, null, new SendBird.UserInfoUpdateHandler() {
                    @Override
                    public void onUpdated(SendBirdException e) {
                        if (e != null) {
                            Toast.makeText(context, context.getString(R.string.sendbird_error_with_code, e.getCode(), e.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                        PreferenceUtils.setNickname(userNickname);
                    }
                });

                if (handler != null) {
                    handler.onConnected(user, e);
                }
                EventBus.getDefault().post(new ConnectionEvent(true));
            }
        });
    }

    public static void disconnect(final SendBird.DisconnectHandler handler) {
        SendBird.disconnect(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                SendBirdSyncManager.getInstance().pauseSync();

                if (handler != null) {
                    handler.onDisconnected();
                }
            }
        });
    }
}
