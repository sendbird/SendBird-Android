package com.sendbird.uikit.customsample.groupchannel.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.uikit.activities.ChannelSettingsActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomChannelSettingsFragment;
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
                .setHeaderLeftButtonIcon(R.drawable.icon_arrow_left, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setHeaderLeftButtonListener(null)
                .setOnSettingMenuClickListener(null)
                .build();
    }
}