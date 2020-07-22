package com.sendbird.uikit.customsample.activities.viewholders;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.Reaction;
import com.sendbird.uikit.activities.viewholder.MessageViewHolder;
import com.sendbird.uikit.customsample.databinding.ViewHighlightMessageOtherHolderBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;

import java.util.List;

public class HighlightMessageOtherViewHolder extends MessageViewHolder {
    private ViewHighlightMessageOtherHolderBinding binding;

    public HighlightMessageOtherViewHolder(ViewHighlightMessageOtherHolderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void bind(BaseChannel channel, @NonNull BaseMessage message) {
        binding.setMessage(message);
    }

    @Override
    public View getClickableView() {
        return binding.tvMessage;
    }

    @Override
    public void setEmojiReaction(List<Reaction> reactionList, OnItemClickListener<String> emojiReactionClickListener, OnItemLongClickListener<String> emojiReactionLongClickListener, View.OnClickListener moreButtonClickListener) {}
}
