package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.UserMessageParams;
import com.sendbird.uikit.customsample.models.CustomMessageType;
import com.sendbird.uikit.fragments.ChannelFragment;

public class CustomChannelFragment extends ChannelFragment {
    private CustomMessageType customMessageType = CustomMessageType.NONE;

    @Override
    protected void onBeforeSendUserMessage(@NonNull UserMessageParams params) {
        super.onBeforeSendUserMessage(params);
        params.setCustomType(customMessageType.getValue())
                .setData(null)
                .setMentionedUserIds(null)
                .setMentionedUsers(null)
                .setMentionType(null)
                .setMetaArrays(null)
                .setParentMessageId(0)
                .setPushNotificationDeliveryOption(null)
                .setTranslationTargetLanguages(null);
    }

    @Override
    protected void onBeforeSendFileMessage(@NonNull FileMessageParams params) {
        super.onBeforeSendFileMessage(params);
        params.setCustomType(customMessageType.getValue())
                .setData(null)
                .setMentionedUserIds(null)
                .setMentionedUsers(null)
                .setMentionType(null)
                .setMetaArrays(null)
                .setParentMessageId(0)
                .setPushNotificationDeliveryOption(null);
    }

    @Override
    protected void onBeforeUpdateUserMessage(@NonNull UserMessageParams params) {
        super.onBeforeUpdateUserMessage(params);
        params.setCustomType(customMessageType.getValue())
                .setData(null)
                .setMentionedUserIds(null)
                .setMentionedUsers(null)
                .setMentionType(null)
                .setMetaArrays(null)
                .setParentMessageId(0)
                .setPushNotificationDeliveryOption(null)
                .setTranslationTargetLanguages(null);
    }

    @Override
    protected void sendUserMessage(@NonNull UserMessageParams params) {
        super.sendUserMessage(params);
    }

    @Override
    protected void sendFileMessage(@NonNull Uri uri) {
        super.sendFileMessage(uri);
    }

    @Override
    protected void updateUserMessage(long messageId, @NonNull UserMessageParams params) {
        super.updateUserMessage(messageId, params);
    }

    @Override
    protected void deleteMessage(@NonNull BaseMessage message) {
        super.deleteMessage(message);
    }

    @Override
    protected void resendMessage(@NonNull BaseMessage message) {
        super.resendMessage(message);
    }

    public void setCustomMessageType(CustomMessageType customMessageType) {
        this.customMessageType = customMessageType;
    }

    public CustomMessageType getCustomMessageType() {
        return customMessageType;
    }
}
