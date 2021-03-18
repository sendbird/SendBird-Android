package com.sendbird.uikit.customsample.groupchannel.activities.viewholders;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.uikit.activities.viewholder.MessageViewHolder;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.customsample.databinding.ViewHighlightMessageMeHolderBinding;

public class HighlightMessageMeViewHolder extends MessageViewHolder {
    private final ViewHighlightMessageMeHolderBinding binding;

    public HighlightMessageMeViewHolder(ViewHighlightMessageMeHolderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void bind(BaseChannel channel, @NonNull BaseMessage message, MessageGroupType messageGroupType) {
        Context context = binding.getRoot().getContext();
        boolean sendingState = message.getSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED;

        binding.tvSentAt.setVisibility(sendingState ? View.VISIBLE : View.GONE);
        String sentAt = DateUtils.formatDateTime(context, message.getCreatedAt(), DateUtils.FORMAT_SHOW_TIME);
        binding.tvSentAt.setText(sentAt);
        binding.ivStatus.drawStatus(message, channel);
        binding.tvMessage.setText(message.getMessage());

        int paddingTop = context.getResources().getDimensionPixelSize(com.sendbird.uikit.R.dimen.sb_size_8);
        int paddingBottom = context.getResources().getDimensionPixelSize(com.sendbird.uikit.R.dimen.sb_size_8);
        binding.root.setPadding(binding.root.getPaddingLeft(), paddingTop, binding.root.getPaddingRight(), paddingBottom);
    }

    @Override
    public View getClickableView() {
        return binding.tvMessage;
    }
}
