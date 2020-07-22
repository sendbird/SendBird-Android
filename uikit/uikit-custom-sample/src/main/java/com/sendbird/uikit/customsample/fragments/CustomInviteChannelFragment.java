package com.sendbird.uikit.customsample.fragments;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.User;
import com.sendbird.uikit.customsample.activities.CustomChannelActivity;
import com.sendbird.uikit.fragments.InviteChannelFragment;
import com.sendbird.uikit.model.ReadyStatus;

import java.util.List;

public class CustomInviteChannelFragment extends InviteChannelFragment {

    private List<String> inviteUsers;

    @Override
    protected void onNewUserInvited(@NonNull GroupChannel channel) {
        if (isActive()) {
            Intent intent = new Intent(getContext(), CustomChannelActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onReady(User user, ReadyStatus status) {
        super.onReady(user, status);
    }

    @Override
    public void onBeforeInviteUsers(@NonNull List<String> userIds) {
        super.onBeforeInviteUsers(userIds);
        userIds.addAll(inviteUsers);
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

    public void setInviteUsers(List<String> inviteUsers) {
        this.inviteUsers = inviteUsers;
    }
}
