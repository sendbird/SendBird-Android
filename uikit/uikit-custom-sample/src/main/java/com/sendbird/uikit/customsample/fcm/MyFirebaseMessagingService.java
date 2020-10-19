/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sendbird.uikit.customsample.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdPushHandler;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.activities.MainActivity;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

public class MyFirebaseMessagingService extends SendBirdPushHandler {

    private static final String TAG = "MyFirebaseMsgService";
    private static final AtomicReference<String> pushToken = new AtomicReference<>();

    @Override
    protected boolean isUniquePushToken() {
        return false;
    }

    @Override
    public void onNewToken(String token) {
        Log.i(TAG, "onNewToken(" + token + ")");
        pushToken.set(token);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(Context context, RemoteMessage remoteMessage) {
        Logger.d("From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Logger.d( "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Logger.d( "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        try {
            if (remoteMessage.getData().containsKey(StringSet.sendbird)) {
                String jsonStr = remoteMessage.getData().get(StringSet.sendbird);
                JSONObject sendBird = new JSONObject(jsonStr);
                JSONObject channel = sendBird.getJSONObject(StringSet.channel);
                String channelUrl = channel.getString(StringSet.channel_url);

                SendBird.markAsDelivered(channelUrl);
                sendNotification(context, sendBird);

            }
        } catch (JSONException e) {
            Logger.e(e);
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param sendBird JSONObject payload from FCM
     */
    public static void sendNotification(@NonNull Context context, @NonNull JSONObject sendBird) throws JSONException {
        String message = sendBird.getString(StringSet.message);
        JSONObject channel = sendBird.getJSONObject(StringSet.channel);
        String channelUrl = channel.getString(StringSet.channel_url);

        JSONObject sender = sendBird.getJSONObject(StringSet.sender);
        String senderName = sender.getString(StringSet.name);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final String CHANNEL_ID = StringSet.CHANNEL_ID;
        if (Build.VERSION.SDK_INT >= 26) {  // Build.VERSION_CODES.O
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, StringSet.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        Intent intent = MainActivity.newRedirectToChannelIntent(context, channelUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_push_lollipop)
                .setColor(ContextCompat.getColor(context, R.color.primary_300))  // small icon background color
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_push_oreo))
                .setContentTitle(senderName)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);
        notificationBuilder.setContentText(message);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}