package com.sendbird.localcaching.sample.utils;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.SendBird;

import java.util.List;

public class SyncManagerUtils {
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
                    if (messages.get(i).getRequestId().equals(targetMessage.getRequestId())) {
                        return i;
                    }
                } else {
                    return i;
                }
            } else {
                if (msgId1 == 0) {
                    if (messages.get(i).getRequestId().equals(targetMessage.getRequestId())) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    public static String getMyUserId() {
        if (SendBird.getCurrentUser() == null) {
            return PreferenceUtils.getUserId();
        }

        return SendBird.getCurrentUser().getUserId();
    }
}
