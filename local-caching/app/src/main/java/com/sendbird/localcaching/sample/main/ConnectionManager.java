package com.sendbird.localcaching.sample.main;

import android.content.Context;
import android.widget.Toast;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.localcaching.sample.R;
import com.sendbird.localcaching.sample.model.LoginEvent;
import com.sendbird.localcaching.sample.utils.PreferenceUtils;
import com.sendbird.localcaching.sample.utils.PushUtils;

import org.greenrobot.eventbus.EventBus;

public class ConnectionManager {
    public static boolean isLoggedIn() {
        return PreferenceUtils.getConnected();
    }

    public static void connect(final Context context, String userId, final String userNickname, final SendBird.ConnectHandler handler) {
        SendBird.connect(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                // Local Caching
                if (user != null) {
                    if (e != null) {
                        // offline mode

                        // wait for first connection to register push token and update user info.
                        final String identifier = "first_connection";
                        SendBird.addConnectionHandler(identifier, new SendBird.ConnectionHandler() {
                            @Override
                            public void onReconnectStarted() {
                            }

                            @Override
                            public void onReconnectSucceeded() {
                                onInitialConnect(context, userNickname);
                                SendBird.removeConnectionHandler(identifier);
                            }

                            @Override
                            public void onReconnectFailed() {
                            }
                        });
                    } else {
                        onInitialConnect(context, userNickname);
                    }

                    if (handler != null) {
                        handler.onConnected(user, e);
                    }
                    EventBus.getDefault().post(new LoginEvent(true));
                } else {
                    Toast.makeText(context, context.getString(R.string.sendbird_error_with_code, e.getCode(), e.getMessage()), Toast.LENGTH_SHORT).show();

                    if (handler != null) {
                        handler.onConnected(null, e);
                    }
                    EventBus.getDefault().post(new LoginEvent(false));
                }
            }
        });
    }

    private static void onInitialConnect(Context context, String userNickname) {
        PushUtils.registerPushTokenForCurrentUser();
        SendBird.updateCurrentUserInfo(userNickname, null, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    Toast.makeText(context, context.getString(R.string.sendbird_error_with_code, e.getCode(), e.getMessage()), Toast.LENGTH_SHORT).show();
                    return;
                }
                PreferenceUtils.setNickname(userNickname);
            }
        });
    }

    public static void disconnect(final SendBird.DisconnectHandler handler) {
        SendBird.disconnect(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                if (handler != null) {
                    handler.onDisconnected();
                }
            }
        });
    }
}
