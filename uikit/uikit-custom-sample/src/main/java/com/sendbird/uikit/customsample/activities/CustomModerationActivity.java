package com.sendbird.uikit.customsample.activities;

import androidx.annotation.NonNull;

import com.sendbird.uikit.activities.ModerationActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.fragments.CustomModerationFragment;
import com.sendbird.uikit.fragments.ModerationFragment;

public class CustomModerationActivity extends ModerationActivity {
    @Override
    protected ModerationFragment createModerationFragment(@NonNull String channelUrl) {
        return new ModerationFragment.Builder(channelUrl, R.style.SendBird_Custom)
                .setCustomModerationFragment(new CustomModerationFragment())
                .setUseHeader(true)
                .setUseHeaderLeftButton(true)
                .setHeaderTitle(getString(R.string.sb_text_channel_settings_moderations))
                .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                .setHeaderLeftButtonListener(null)
                .setOnMenuItemClickListener(null)
                .build();
    }
}
