package com.sendbird.uikit.customsample.groupchannel.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.sendbird.uikit.activities.MutedMemberListActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomMutedMemberListFragment;
import com.sendbird.uikit.fragments.MutedMemberListFragment;

public class CustomMutedMemberListActivity extends MutedMemberListActivity {
    @Override
    protected Fragment createMutedMemberListFragment(@NonNull String channelUrl) {
        return new MutedMemberListFragment.Builder(channelUrl, R.style.SendBird_Custom)
                .setCustomMutedMemberFragment(new CustomMutedMemberListFragment())
                .setUseHeader(true)
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(false)
                .setHeaderTitle(getString(R.string.sb_text_menu_muted_members))
                .setEmptyIcon(R.drawable.icon_mute, AppCompatResources.getColorStateList(this, R.color.primary_300))
                .setEmptyText(R.string.sb_text_empty_no_muted_member)
                .setHeaderLeftButtonIcon(R.drawable.icon_arrow_left, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setHeaderLeftButtonListener(null)
                .setHeaderRightButtonIcon(R.drawable.icon_plus, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setHeaderRightButtonListener(null)
                .setItemClickListener(null)
                .setItemLongClickListener(null)
                .setActionItemClickListener(null)
                .setMemberListAdpater(null)
                .build();
    }
}
