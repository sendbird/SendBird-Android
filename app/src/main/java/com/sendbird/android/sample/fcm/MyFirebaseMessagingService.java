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

package com.sendbird.android.sample.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.groupchannel.GroupChannelActivity;
import com.sendbird.android.sample.utils.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        String channelUrl = null, channelName = "";
        int notificationId = 0;
        try {
            JSONObject sendBird = new JSONObject(remoteMessage.getData().get("sendbird"));
            JSONObject channel = (JSONObject) sendBird.get("channel");
            channelUrl = (String) channel.get("channel_url");
            channelName = (String) channel.get("name");
            notificationId = new JSONObject(remoteMessage.getData().get("sendbird")).optInt("unread_message_count");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        if (!TextUtils.isEmpty(remoteMessage.getData().get("message")) &&
                !TextUtils.isEmpty(channelUrl)) {
            try {
                sendNotification(this, remoteMessage.getData().get("message"), channelUrl, notificationId, channelName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    public static void sendNotification(Context context, String messageBody, String channelUrl, int notificationId, String channelName) {
        Intent intent = new Intent(context, GroupChannelActivity.class);
        intent.putExtra("groupChannelUrl", channelUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.sendbird_ic_launcher)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setGroup(channelUrl)
                .setContentIntent(pendingIntent);

        if (PreferenceUtils.getNotificationsShowPreviews()) {
            if (channelName.equalsIgnoreCase("group channel")) {
                notificationBuilder.setContentText(messageBody);
            } else {
                String message = "";
                int indexOfColon = messageBody.lastIndexOf(":");
                message = message.concat(messageBody.substring(0, indexOfColon));
                message = message.concat("@ " + channelName + messageBody.substring(indexOfColon));
                notificationBuilder.setContentText(message);
            }
        } else {
            notificationBuilder.setContentText("Somebody sent you a message.");
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId/* ID of notification */, notificationBuilder.build());
    }
}