package com.sendbird.uikit.customsample.activities;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sendbird.uikit.activities.PromoteOperatorsActivity;
import com.sendbird.uikit.activities.OperatorListActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.fragments.CustomOperatorListFragment;
import com.sendbird.uikit.fragments.OperatorListFragment;

public class CustomOperatorListActivity extends OperatorListActivity {
    @Override
    protected Fragment createOperatorListFragment(@NonNull String channelUrl) {
        return new OperatorListFragment.Builder(channelUrl, R.style.SendBird_Custom)
                .setCustomOperatorListFragment(new CustomOperatorListFragment())
                .setUseHeader(true)
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setHeaderTitle(getString(R.string.sb_text_menu_operators))
                .setEmptyIcon(R.drawable.icon_chat)
                .setEmptyText(R.string.sb_text_user_list_empty)
                .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                .setHeaderLeftButtonListener(null)
                .setHeaderRightButtonIconResId(R.drawable.icon_plus)
                .setHeaderRightButtonListener(v -> {
                    Intent intent = PromoteOperatorsActivity.newIntentFromCustomActivity(this, CustomPromoteOperatorsActivity.class, channelUrl);
                    startActivity(intent);
                })
                .setItemClickListener(null)
                .setItemLongClickListener(null)
                .setActionItemClickListener(null)
                .setUserListAdapter(null)
                .build();
    }
}
