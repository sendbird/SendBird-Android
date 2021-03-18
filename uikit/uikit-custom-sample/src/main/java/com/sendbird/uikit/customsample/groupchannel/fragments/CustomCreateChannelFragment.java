package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.uikit.activities.adapter.UserListAdapter;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.activities.CustomChannelActivity;
import com.sendbird.uikit.customsample.groupchannel.activities.adapters.CustomUserListAdapter;
import com.sendbird.uikit.fragments.CreateChannelFragment;

import java.util.ArrayList;
import java.util.List;

public class CustomCreateChannelFragment extends CreateChannelFragment {
    private List<String> userIds = new ArrayList<>();

    @Override
    protected void onUserSelectComplete(List<String> selectedUsers) {
        super.onUserSelectComplete(selectedUsers);
    }

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
    protected void onNewChannelCreated(@NonNull GroupChannel channel) {
        showCustomChannelActivity(channel.getUrl());
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
    protected <T extends UserListAdapter> void setUserListAdapter(T adapter) {
        super.setUserListAdapter(createCustomUserListAdapter());
    }

    private CustomUserListAdapter createCustomUserListAdapter() {
        CustomUserListAdapter customAdapter = new CustomUserListAdapter();
        customAdapter.setOnUserCheckedListener((selectedUsers, isChecked) -> {
            userIds.clear();
            userIds.addAll(selectedUsers);

            int count = selectedUsers.size();
            setCreateButtonEnabled(count > 0);
            setCreateButtonText(count > 0 ? count + " " + getString(R.string.sb_text_button_create) : getString(R.string.sb_text_button_create));
        });
        return customAdapter;
    }

    private void showCustomChannelActivity(String channelUrl) {
        if (getActivity() != null && getContext() != null) {
            Intent intent = CustomChannelActivity.newIntentFromCustomActivity(getActivity(), CustomChannelActivity.class, channelUrl);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
