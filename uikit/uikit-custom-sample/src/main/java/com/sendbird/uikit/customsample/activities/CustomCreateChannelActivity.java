package com.sendbird.uikit.customsample.activities;

import com.sendbird.uikit.activities.CreateChannelActivity;
import com.sendbird.uikit.customsample.BaseApplication;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.activities.adapters.CustomUserListAdapter;
import com.sendbird.uikit.customsample.fragments.CustomCreateChannelFragment;
import com.sendbird.uikit.fragments.CreateChannelFragment;

import java.util.ArrayList;
import java.util.List;

public class CustomCreateChannelActivity extends CreateChannelActivity {
    private List<String> userIds = new ArrayList<>();

    @Override
    protected CreateChannelFragment createCreateChannelFragment() {
        CustomCreateChannelFragment customCreateChannelFragment = new CustomCreateChannelFragment();
        customCreateChannelFragment.setUserIds(userIds);
        CustomUserListAdapter adapter = new CustomUserListAdapter();
        adapter.setOnUserCheckedListener((selectedUsers, isChecked) -> {
            userIds.clear();
            userIds.addAll(selectedUsers);

            if (selectedUsers.size() > 0) {
                customCreateChannelFragment.setCreateButtonEnabled(true);
                customCreateChannelFragment.setCreateButtonText(String.format(getString(R.string.sb_text_button_create_with_count), selectedUsers.size()));
            } else {
                customCreateChannelFragment.setCreateButtonEnabled(false);
                customCreateChannelFragment.setCreateButtonText(getString(R.string.sb_text_button_create));
            }
        });

        return new CreateChannelFragment.Builder(R.style.CustomUserListStyle)
                .setCustomCreateChannelFragment(customCreateChannelFragment)
                .setUseHeader(true)
                .setHeaderTitle(getString(R.string.sb_text_header_create_channel))
                .setUseHeaderLeftButton(true)
                .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                .setIsDistinct(false)
                .setHeaderLeftButtonListener(null)
                .setUserListAdapter(adapter)
                .setCustomUserListQueryHandler(BaseApplication.getCustomUserListQuery())
                .build();
    }
}