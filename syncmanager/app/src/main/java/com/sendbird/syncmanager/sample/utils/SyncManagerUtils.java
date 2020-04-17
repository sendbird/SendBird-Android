package com.sendbird.syncmanager.sample.utils;

import android.content.Context;
import androidx.annotation.NonNull;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.UserMessage;
import com.sendbird.syncmanager.SendBirdSyncManager;
import com.sendbird.syncmanager.handler.CompletionHandler;

import java.util.List;

public class SyncManagerUtils {
    public static void setup(Context context, String userId, CompletionHandler handler) {
        SendBirdSyncManager.Options options = new SendBirdSyncManager.Options.Builder()
                .setMessageResendPolicy(SendBirdSyncManager.MessageResendPolicy.AUTOMATIC)
                .setAutomaticMessageResendRetryCount(5)
                .build();
        SendBirdSyncManager.setup(context, userId, options, handler);
    }

    /**
     *  It returns the index that targetChannel should be inserted to the given channel list.
     */
    public static int findIndexOfChannel(@NonNull List<GroupChannel> channels, @NonNull GroupChannel targetChannel, @NonNull GroupChannelListQuery.Order order) {
        if (channels.size() == 0) {
            return 0;
        }

        int index = channels.size();
        for (int i = 0; i < channels.size(); i++) {
            GroupChannel c = channels.get(i);
            if (c.getUrl().equals(targetChannel.getUrl())) {
                return i;
            }

            if (GroupChannel.compareTo(targetChannel, c, order) < 0) {
                return i;
            }
        }

        return index;
    }

    /**
     *  It returns the index of targetChannel in the given channel list.
     *  If not exists, it will return -1.
     */
    public static int getIndexOfChannel(@NonNull List<GroupChannel> channels,@NonNull GroupChannel targetChannel) {
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).getUrl().equals(targetChannel.getUrl())) {
                return i;
            }
        }

        return -1;
    }

    /**
     * It returns the index that targetMessage should be inserted to the given message list.
     * If isLatestFirst is set to true, latest message's index will be zero.
     * If isLatestFirst is set to true, oldest message's index will be zero.
     *
     * @param messages <code>BaseMessage</code> list associated with view.
     * @param newMessage New <code>BaseMessage</code> to be inserted to existing message list.
     * @return Index of new message have to be inserted.
     */
    public static int findIndexOfMessage(@NonNull List<BaseMessage> messages, @NonNull BaseMessage newMessage) {
        if (messages.size() == 0) {
            return 0;
        }

        if (messages.get(0).getCreatedAt() < newMessage.getCreatedAt()) {
            return 0;
        }

        for (int i = 0; i < messages.size() - 1; i++) {
            BaseMessage m1 = messages.get(i);
            BaseMessage m2 = messages.get(i + 1);

            if (m1.getCreatedAt() > newMessage.getCreatedAt() && newMessage.getCreatedAt() > m2.getCreatedAt()) {
                return i + 1;
            }
        }

        return messages.size();
    }

    /**
     *  It returns the index of targetMessage in the given message list.
     *  If not exists, it will return -1.
     *
     * @param messages <code>BaseMessage</code> list associated with view.
     * @param targetMessage Target <code>BaseMessage</code> to find out.
     * @return Index of target message in the given message list.
     */

    public static int getIndexOfMessage(@NonNull List<BaseMessage> messages, @NonNull BaseMessage targetMessage) {
        for (int i = 0; i < messages.size(); i++) {
            long msgId1 = messages.get(i).getMessageId();
            long msgId2 = targetMessage.getMessageId();

            if (msgId1 == msgId2) {
                if (msgId1 == 0) {
                    if (getRequestId(messages.get(i)).equals(getRequestId(targetMessage))) {
                        return i;
                    }
                } else {
                    return i;
                }
            }
        }

        return -1;
    }

    private static String getRequestId(BaseMessage message) {
        if (message instanceof AdminMessage) {
            return "";
        }

        if (message instanceof UserMessage) {
            return ((UserMessage)message).getRequestId();
        }

        if (message instanceof FileMessage) {
            return ((FileMessage)message).getRequestId();
        }

        return "";
    }

    public static String getMyUserId() {
        if (SendBird.getCurrentUser() == null) {
            return PreferenceUtils.getUserId();
        }

        return SendBird.getCurrentUser().getUserId();
    }
}
