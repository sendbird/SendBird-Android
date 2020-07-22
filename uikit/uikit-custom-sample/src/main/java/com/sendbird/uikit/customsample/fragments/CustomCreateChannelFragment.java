package com.sendbird.uikit.customsample.fragments;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.uikit.customsample.activities.CustomChannelActivity;
import com.sendbird.uikit.fragments.CreateChannelFragment;

import java.util.List;

public class CustomCreateChannelFragment extends CreateChannelFragment {
    private List<String> userIds;

    @Override
    protected void onBeforeCreateGroupChannel(@NonNull GroupChannelParams params) {
        super.onBeforeCreateGroupChannel(params);
        params.addUserIds(userIds);
    }

    @Override
    protected void createGroupChannel(@NonNull GroupChannelParams params) {
        super.createGroupChannel(params);
    }

    @Override
    public void setCreateButtonText(CharSequence text) {
        super.setCreateButtonText(text);
    }

    @Override
    public void setCreateButtonEnabled(boolean enabled) {
        super.setCreateButtonEnabled(enabled);
    }

    @Override
    protected void onNewChannelCreated(@NonNull GroupChannel channel) {
        showCustomChannelActivity(channel.getUrl());
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    private void showCustomChannelActivity(String channelUrl) {
        if (getActivity() != null && getContext() != null) {
            Intent intent = CustomChannelActivity.newIntentFromCustomActivity(getActivity(), CustomChannelActivity.class, channelUrl);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
