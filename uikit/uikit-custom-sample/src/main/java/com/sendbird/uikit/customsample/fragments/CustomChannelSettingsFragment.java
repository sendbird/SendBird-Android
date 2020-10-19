package com.sendbird.uikit.customsample.fragments;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.uikit.activities.MemberListActivity;
import com.sendbird.uikit.activities.ModerationActivity;
import com.sendbird.uikit.customsample.activities.CustomMemberListActivity;
import com.sendbird.uikit.customsample.activities.CustomModerationActivity;
import com.sendbird.uikit.fragments.ChannelSettingsFragment;
import com.sendbird.uikit.interfaces.OnMenuItemClickListener;
import com.sendbird.uikit.widgets.ChannelSettingsView;

public class CustomChannelSettingsFragment extends ChannelSettingsFragment implements OnMenuItemClickListener<ChannelSettingsView.ChannelSettingMenu, GroupChannel>{
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

    @Override
    protected void setOnMenuItemClickListener(OnMenuItemClickListener<ChannelSettingsView.ChannelSettingMenu, GroupChannel> listener) {
        super.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClicked(View view, ChannelSettingsView.ChannelSettingMenu menu, GroupChannel data) {
        if (getActivity() == null) {
            return true;
        }

        switch (menu) {
            case MODERATIONS:
                Intent moderationIntent = ModerationActivity.newIntentFromCustomActivity(getActivity(), CustomModerationActivity.class, data.getUrl());
                startActivity(moderationIntent);
                return true;
            case MEMBERS:
                Intent memberListIntent = MemberListActivity.newIntentFromCustomActivity(getActivity(), CustomMemberListActivity.class, data.getUrl());
                startActivity(memberListIntent);
                return true;
            case NOTIFICATIONS:
            case LEAVE_CHANNEL:
            default:
        }
        return false;
    }
}
