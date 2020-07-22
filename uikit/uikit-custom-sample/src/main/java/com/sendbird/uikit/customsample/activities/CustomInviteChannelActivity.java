package com.sendbird.uikit.customsample.activities;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.uikit.activities.InviteChannelActivity;
import com.sendbird.uikit.customsample.BaseApplication;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.activities.adapters.CustomUserListAdapter;
import com.sendbird.uikit.customsample.fragments.CustomInviteChannelFragment;
import com.sendbird.uikit.fragments.InviteChannelFragment;

import java.util.ArrayList;
import java.util.List;

public class CustomInviteChannelActivity extends InviteChannelActivity {
    private List<String> inviteUserIds = new ArrayList<>();

    @Override
    protected InviteChannelFragment createInviteChannelFragment(@NonNull String channelUrl) {
        CustomInviteChannelFragment customInviteChannelFragment = new CustomInviteChannelFragment();
        customInviteChannelFragment.setInviteUsers(inviteUserIds);

        CustomUserListAdapter adapter = new CustomUserListAdapter();
        GroupChannel.getChannel(channelUrl, (groupChannel, e) -> {
            if (e != null) {
                return;
            }

            List<String> inviteUsers = new ArrayList<>();
            for (Member member : groupChannel.getMembers()) {
                inviteUsers.add(member.getUserId());
            }
            adapter.setInvitedUsers(inviteUsers);
        });
        adapter.setOnUserCheckedListener((selectedUsers, isChecked) -> {
            inviteUserIds.clear();
            inviteUserIds.addAll(selectedUsers);

            if (selectedUsers.size() > 0) {
                customInviteChannelFragment.setInviteButtonEnabled(true);
                customInviteChannelFragment.setInviteButtonText(String.format(getString(R.string.sb_text_button_invite_with_count), selectedUsers.size()));
            } else {
                customInviteChannelFragment.setInviteButtonEnabled(false);
                customInviteChannelFragment.setInviteButtonText(getString(R.string.sb_text_button_invite));
            }
        });

        return new InviteChannelFragment.Builder(channelUrl, R.style.CustomUserListStyle)
                .setCustomInviteChannelFragment(customInviteChannelFragment)
                .setUseHeader(true)
                .setHeaderTitle(getString(R.string.sb_text_header_invite_member))
                .setUseHeaderLeftButton(true)
                .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                .setHeaderLeftButtonListener(null)
                .setUserListAdapter(adapter)
                .setCustomUserListQueryHandler(BaseApplication.getCustomUserListQuery())
                .build();
    }
}