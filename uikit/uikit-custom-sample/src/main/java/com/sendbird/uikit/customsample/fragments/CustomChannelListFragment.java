package com.sendbird.uikit.customsample.fragments;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.fragments.ChannelListFragment;

public class CustomChannelListFragment extends ChannelListFragment {
    @Override
    protected void leaveChannel(@NonNull GroupChannel channel) {
        super.leaveChannel(channel);
    }
}
