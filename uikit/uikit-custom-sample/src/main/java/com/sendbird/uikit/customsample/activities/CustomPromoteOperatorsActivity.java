package com.sendbird.uikit.customsample.activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.activities.PromoteOperatorsActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.fragments.CustomPromoteOperatorsFragment;
import com.sendbird.uikit.fragments.PromoteOperatorsFragment;

public class CustomPromoteOperatorsActivity extends PromoteOperatorsActivity {
    @Override
    protected Fragment createPromoteOperatorFragment(@NonNull GroupChannel channel) {
        return new PromoteOperatorsFragment.Builder(channel.getUrl(), R.style.SendBird_Custom)
                .setCustomPromoteOperatorFragment(new CustomPromoteOperatorsFragment())
                .setUseHeader(true)
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setHeaderTitle(getString(R.string.sb_text_header_select_members))
                .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                .setHeaderLeftButtonListener(null)
                .setRightButtonText(getString(R.string.sb_text_promote_operator))
                .setUserListAdapter(null)
                .setCustomUserListQueryHandler(null)
                .build();
    }
}
