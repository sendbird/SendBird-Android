package com.sendbird.uikit.customsample.openchannel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.Sender;
import com.sendbird.uikit.activities.viewholder.MessageViewHolder;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.databinding.ViewOpenChannelHighlightMessageHolderBinding;
import com.sendbird.uikit.customsample.utils.DrawableUtils;


public class HighlightOpenChannelMessageViewHolder extends MessageViewHolder {
    private final ViewOpenChannelHighlightMessageHolderBinding binding;
    private final int operatorAppearance;
    private final int nicknameAppearance;

    public HighlightOpenChannelMessageViewHolder(ViewOpenChannelHighlightMessageHolderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        TypedArray a = binding.getRoot().getContext().getTheme().obtainStyledAttributes(null, com.sendbird.uikit.R.styleable.MessageView, com.sendbird.uikit.R.attr.sb_open_channel_message_user_style, 0);
        try {
            nicknameAppearance = a.getResourceId(com.sendbird.uikit.R.styleable.MessageView_sb_message_sender_name_text_appearance, com.sendbird.uikit.R.style.SendbirdCaption1OnLight02);
            operatorAppearance = a.getResourceId(com.sendbird.uikit.R.styleable.MessageView_sb_message_operator_name_text_appearance, com.sendbird.uikit.R.style.SendbirdCaption1Secondary300);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void bind(BaseChannel channel, @NonNull BaseMessage message, MessageGroupType messageGroupType) {
        OpenChannel openChannel;
        if (channel instanceof OpenChannel) {
            openChannel = (OpenChannel) channel;
        } else {
            return;
        }

        Context context = binding.getRoot().getContext();
        binding.ivStatus.drawStatus(message, channel);

        binding.ivProfileView.setVisibility(View.VISIBLE);
        binding.tvNickname.setVisibility(View.VISIBLE);
        binding.tvSentAt.setVisibility(View.VISIBLE);
        String sentAt = DateUtils.formatDateTime(context, message.getCreatedAt(), DateUtils.FORMAT_SHOW_TIME);
        binding.tvSentAt.setText(sentAt);

        if (openChannel.isOperator(message.getSender())) {
            binding.tvNickname.setTextAppearance(context, operatorAppearance);
        } else {
            binding.tvNickname.setTextAppearance(context, nicknameAppearance);
        }

        Sender sender = message.getSender();
        String nickname = sender == null || TextUtils.isEmpty(sender.getNickname()) ?
                context.getString(R.string.sb_text_channel_list_title_unknown) :
                sender.getNickname();
        binding.tvNickname.setText(nickname);

        String url = "";
        if (sender != null && !TextUtils.isEmpty(sender.getProfileUrl())) {
            url = sender.getProfileUrl();
        }

        Drawable errorIcon = DrawableUtils.createOvalIcon(binding.getRoot().getContext(),
                R.color.background_300, R.drawable.icon_user, R.color.ondark_01);
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new ObjectKey(url))
                .error(errorIcon)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivProfileView);

        binding.tvMessage.setText(message.getMessage());
    }

    @Override
    public View getClickableView() {
        return binding.contentPanel;
    }
}
