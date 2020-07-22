package com.sendbird.uikit.customsample.activities;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.activities.adapters.CustomMessageListAdapter;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.fragments.CustomChannelFragment;
import com.sendbird.uikit.fragments.ChannelFragment;

public class CustomChannelActivity extends ChannelActivity {
    private CustomChannelFragment customChannelFragment;

    @Override
    protected ChannelFragment createChannelFragment(@NonNull String channelUrl) {
        customChannelFragment = new CustomChannelFragment();
        return new ChannelFragment.Builder(channelUrl, R.style.CustomMessageListStyle)
                .setCustomChannelFragment(customChannelFragment)
                .setUseHeader(true)
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setUseLastSeenAt(true)
                .setUseTypingIndicator(true)
                .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                .setHeaderRightButtonIconResId(R.drawable.icon_info)
                .setInputLeftButtonIconResId(R.drawable.icon_add)
                .setInputRightButtonIconResId(R.drawable.icon_send)
                .setInputHint(getString(R.string.sb_text_channel_input_text_hint))
                .setHeaderLeftButtonListener(null)
                .setHeaderRightButtonListener(v -> showCustomChannelSettingsActivity(channelUrl))
                .setMessageListAdapter(new CustomMessageListAdapter())
                .setItemClickListener(null)
                .setItemLongClickListener(null)
                .setInputLeftButtonListener(v -> showMessageTypeDialog())
                .setMessageListParams(null)
                .build();
    }

    private void showCustomChannelSettingsActivity(String channelUrl) {
        Intent intent = CustomChannelSettingsActivity.newIntentFromCustomActivity(CustomChannelActivity.this, CustomChannelSettingsActivity.class, channelUrl);
        startActivity(intent);
    }

    private void showMessageTypeDialog() {
        if (customChannelFragment == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick message type")
                .setMultiChoiceItems(new String[]{StringSet.highlight},
                        new boolean[]{customChannelFragment.isHighlightMode()},
                        (dialog, which, isChecked) -> {
                            customChannelFragment.setHighlightMode(isChecked);
                        })
                .create()
                .show();
    }
}
