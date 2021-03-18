package com.sendbird.uikit.customsample.groupchannel.activities;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.activities.adapters.CustomMessageListAdapter;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomChannelFragment;
import com.sendbird.uikit.customsample.models.CustomMessageType;
import com.sendbird.uikit.fragments.ChannelFragment;

public class CustomChannelActivity extends ChannelActivity {
    private final CustomChannelFragment customChannelFragment = new CustomChannelFragment();

    @Override
    protected ChannelFragment createChannelFragment(@NonNull String channelUrl) {
        final boolean useMessageGroupUI = false;
        return new ChannelFragment.Builder(channelUrl, R.style.CustomMessageListStyle)
                .setCustomChannelFragment(customChannelFragment)
                .setUseHeader(true)
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setUseLastSeenAt(true)
                .setUseTypingIndicator(true)
                .setHeaderLeftButtonIcon(R.drawable.icon_arrow_left, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setHeaderRightButtonIcon(R.drawable.icon_info, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setInputLeftButtonIcon(R.drawable.icon_add, AppCompatResources.getColorStateList(this, R.color.primary_300))
                .setInputRightButtonIcon(R.drawable.icon_send, AppCompatResources.getColorStateList(this, R.color.secondary_300))
                .setInputHint(getString(R.string.sb_text_channel_input_text_hint))
                .setHeaderLeftButtonListener(null)
                .setHeaderRightButtonListener(v -> showCustomChannelSettingsActivity(channelUrl))
                .setMessageListAdapter(new CustomMessageListAdapter(useMessageGroupUI))
                .setItemClickListener(null)
                .setItemLongClickListener(null)
                .setInputLeftButtonListener(v -> showMessageTypeDialog())
                .setMessageListParams(null)
                .setUseMessageGroupUI(useMessageGroupUI)
                .build();
    }

    private void showCustomChannelSettingsActivity(String channelUrl) {
        Intent intent = CustomChannelSettingsActivity.newIntentFromCustomActivity(CustomChannelActivity.this, CustomChannelSettingsActivity.class, channelUrl);
        startActivity(intent);
    }

    private void showMessageTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick message type")
                .setMultiChoiceItems(new String[]{com.sendbird.uikit.customsample.consts.StringSet.highlight},
                        new boolean[]{customChannelFragment.getCustomMessageType().equals(CustomMessageType.HIGHLIGHT)},
                        (dialog, which, isChecked) -> {
                            final CustomMessageType type = isChecked ? CustomMessageType.HIGHLIGHT : CustomMessageType.NONE;
                            customChannelFragment.setCustomMessageType(type);
                        })
                .create()
                .show();
    }
}
