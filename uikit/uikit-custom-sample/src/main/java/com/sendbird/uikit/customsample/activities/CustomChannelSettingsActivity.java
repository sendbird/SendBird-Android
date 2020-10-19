package com.sendbird.uikit.customsample.activities;

import androidx.annotation.NonNull;

import com.sendbird.uikit.activities.ChannelSettingsActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.fragments.CustomChannelSettingsFragment;
import com.sendbird.uikit.fragments.ChannelSettingsFragment;

public class CustomChannelSettingsActivity extends ChannelSettingsActivity {
    @Override
    protected ChannelSettingsFragment createChannelSettingsFragment(@NonNull String channelUrl) {
        return new ChannelSettingsFragment.Builder(channelUrl, R.style.SendBird_Custom)
                .setCustomChannelSettingsFragment(new CustomChannelSettingsFragment())
                .setUseHeader(true)
                .setHeaderTitle(getString(R.string.sb_text_header_channel_settings))
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                .setHeaderLeftButtonListener(null)
                .setOnSettingMenuClickListener(null)
                .build();
    }
}