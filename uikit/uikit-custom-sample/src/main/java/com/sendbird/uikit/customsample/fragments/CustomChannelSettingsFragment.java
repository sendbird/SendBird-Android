package com.sendbird.uikit.customsample.fragments;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannelParams;
import com.sendbird.uikit.fragments.ChannelSettingsFragment;

public class CustomChannelSettingsFragment extends ChannelSettingsFragment {
    @Override
    protected void onBeforeUpdateGroupChannel(@NonNull GroupChannelParams params) {
        super.onBeforeUpdateGroupChannel(params);
    }

    @Override
    protected void updateGroupChannel(@NonNull GroupChannelParams params) {
        super.updateGroupChannel(params);
    }

    @Override
    protected void leaveChannel() {
        super.leaveChannel();
    }
}
