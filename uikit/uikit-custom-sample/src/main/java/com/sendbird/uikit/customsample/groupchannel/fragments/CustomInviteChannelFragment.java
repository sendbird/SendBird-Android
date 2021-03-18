package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.activities.CustomChannelActivity;
import com.sendbird.uikit.customsample.groupchannel.activities.adapters.CustomUserListAdapter;
import com.sendbird.uikit.fragments.InviteChannelFragment;

import java.util.ArrayList;
import java.util.List;

public class CustomInviteChannelFragment extends InviteChannelFragment {
    private List<String> inviteUserIds = new ArrayList<>();

    @Override
    protected void onNewUserInvited(@NonNull GroupChannel channel) {
        showCustomChannelActivity();
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setUserListAdapter(createCustomAdapter());
    }

    @Override
    protected void onUserSelectComplete(List<String> selectedUsers) {
        selectedUsers.addAll(inviteUserIds);
        super.onUserSelectComplete(selectedUsers);
    }

    @Override
    public void onBeforeInviteUsers(@NonNull List<String> userIds) {
        super.onBeforeInviteUsers(userIds);
    }

    @Override
    protected void inviteUser(@NonNull List<String> userIds) {
        super.inviteUser(userIds);
    }

    @Override
    public void setInviteButtonText(CharSequence text) {
        super.setInviteButtonText(text);
    }

    @Override
    public void setInviteButtonEnabled(boolean enabled) {
        super.setInviteButtonEnabled(enabled);
    }

    private CustomUserListAdapter createCustomAdapter() {
        CustomUserListAdapter customAdapter = new CustomUserListAdapter();
        customAdapter.setInvitedUsers(getDisabledUserIds());
        customAdapter.setOnUserCheckedListener((selectedUsers, isChecked) -> {
            inviteUserIds.clear();
            inviteUserIds.addAll(selectedUsers);

            int count = selectedUsers.size();
            setInviteButtonEnabled(count > 0);
            setInviteButtonText(count > 0 ? count + " " + getString(R.string.sb_text_button_invite) : getString(R.string.sb_text_button_invite));
        });
        return customAdapter;
    }

    private void showCustomChannelActivity() {
        if (getContext() != null) {
            Intent intent = ChannelActivity.newIntentFromCustomActivity(getContext(), CustomChannelActivity.class, channel.getUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
