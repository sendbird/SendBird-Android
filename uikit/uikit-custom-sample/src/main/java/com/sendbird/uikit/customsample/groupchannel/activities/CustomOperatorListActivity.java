package com.sendbird.uikit.customsample.groupchannel.activities;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.sendbird.uikit.activities.PromoteOperatorsActivity;
import com.sendbird.uikit.activities.OperatorListActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomOperatorListFragment;
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
                .setEmptyIcon(R.drawable.icon_chat, AppCompatResources.getColorStateList(this, R.color.primary_300))
                .setEmptyText(R.string.sb_text_user_list_empty)
                .setHeaderLeftButtonIcon(R.drawable.icon_arrow_left, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setHeaderLeftButtonListener(null)
                .setHeaderRightButtonIcon(R.drawable.icon_plus, AppCompatResources.getColorStateList(this, R.color.ondark_01))
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
