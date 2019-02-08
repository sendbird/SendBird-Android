package com.sendbird.syncmanager.sample.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.UserMessage;

import java.util.List;

public class SyncManagerUtils {
    /**
     *  It returns the index that targetChannel should be inserted to the given channel list.
     */
    public static int findIndexOfChannel(@NonNull List<GroupChannel> channels, @NonNull GroupChannel targetChannel, @NonNull GroupChannelListQuery.Order order) {
        if (channels.size() == 0) {
            return 0;
        }

        if (compareTo(targetChannel, channels.get(0), order) == -1) {
            return 0;
        }

        int index = channels.size();
        for (int i = 0; i < channels.size() - 1; i++) {
            GroupChannel c1 = channels.get(i);
            GroupChannel c2 = channels.get(i + 1);
            int compare1 = compareTo(c1, targetChannel, order);
            int compare2 = compareTo(targetChannel, c2, order);

            if (compare1 == -1 && compare2 == -1) {
                return i + 1;
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
     *  If returned value is negative, it means that index of c1 is less than c2's
     *  If returned value is zero, it means that index of c1 and c2 is same.
     *  If returned value is positive, it means that index of c1 is larger than c2's
     */
    public static int compareTo(GroupChannel c1, GroupChannel c2, GroupChannelListQuery.Order order) {
        switch (order) {
            case CHRONOLOGICAL:
                if (c1.getCreatedAt() > c2.getCreatedAt()) {
                    return -1;
                } else if (c1.getCreatedAt() < c2.getCreatedAt()) {
                    return 1;
                } else {
                    return 0;
                }
            case LATEST_LAST_MESSAGE:
                BaseMessage m1 = c1.getLastMessage();
                BaseMessage m2 = c2.getLastMessage();

                long createdAt1 = m1 != null ? m1.getCreatedAt() : c1.getCreatedAt();
                long createdAt2 = m2 != null ? m2.getCreatedAt() : c2.getCreatedAt();

                if (createdAt1 > createdAt2) {
                    return -1;
                } else if (createdAt1 < createdAt2) {
                    return 1;
                } else {
                    return 0;
                }
            case CHANNEL_NAME_ALPHABETICAL:
                return c1.getName().compareTo(c2.getName());

            case METADATA_VALUE_ALPHABETICAL:
                // TODO
                break;
        }

        return 0;
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
}
