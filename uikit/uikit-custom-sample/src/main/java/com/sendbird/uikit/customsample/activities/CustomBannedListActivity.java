package com.sendbird.uikit.customsample.activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sendbird.uikit.activities.BannedListActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.fragments.CustomBannedListFragment;
import com.sendbird.uikit.fragments.BannedListFragment;

public class CustomBannedListActivity extends BannedListActivity {
    @Override
    protected Fragment createBannedListFragment(@NonNull String channelUrl) {
        return new BannedListFragment.Builder(channelUrl, R.style.SendBird_Custom)
                .setCustomBannedMemberFragment(new CustomBannedListFragment())
                .setUseHeader(true)
                .setHeaderTitle(getString(R.string.sb_text_menu_banned_members))
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(false)
                .setEmptyText(R.string.sb_text_empty_no_banned_member)
                .setEmptyIcon(R.drawable.icon_banned)
                .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                .setHeaderLeftButtonListener(null)
                .setHeaderRightButtonIconResId(R.drawable.icon_plus)
                .setHeaderRightButtonListener(null)
                .setActionItemClickListener(null)
                .setItemClickListener(null)
                .setItemLongClickListener(null)
                .setUserListAdapter(null)
                .build();
    }
}
