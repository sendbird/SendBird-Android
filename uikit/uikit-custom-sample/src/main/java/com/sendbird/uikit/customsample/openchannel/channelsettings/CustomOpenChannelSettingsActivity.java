package com.sendbird.uikit.customsample.openchannel.channelsettings;

import androidx.annotation.NonNull;

import com.sendbird.uikit.activities.ChannelSettingsActivity;
import com.sendbird.uikit.activities.OpenChannelSettingsActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.fragments.ChannelSettingsFragment;
import com.sendbird.uikit.fragments.OpenChannelSettingsFragment;

public class CustomOpenChannelSettingsActivity extends OpenChannelSettingsActivity {

    @Override
    protected OpenChannelSettingsFragment createOpenChannelSettingsFragment(@NonNull String channelUrl) {
        return new OpenChannelSettingsFragment.Builder(channelUrl, R.style.SendBird_Custom)
                .setCustomOpenChannelSettingsFragment(new CustomOpenChannelSettingsFragment())
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