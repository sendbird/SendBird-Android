package com.sendbird.uikit.customsample.groupchannel.activities;

import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.uikit.activities.CreateChannelActivity;
import com.sendbird.uikit.consts.CreateableChannelType;
import com.sendbird.uikit.customsample.BaseApplication;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomCreateChannelFragment;
import com.sendbird.uikit.fragments.CreateChannelFragment;

public class CustomCreateChannelActivity extends CreateChannelActivity {
    @Override
    protected CreateChannelFragment createCreateChannelFragment() {
        return super.createCreateChannelFragment();
    }

    @Override
    protected CreateChannelFragment createCreateChannelFragment(CreateableChannelType type) {
        return new CreateChannelFragment.Builder(R.style.CustomUserListStyle, type)
                .setCustomCreateChannelFragment(new CustomCreateChannelFragment())
                .setUseHeader(true)
                .setHeaderTitle(getString(R.string.sb_text_header_create_channel))
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setHeaderLeftButtonIcon(R.drawable.icon_arrow_left, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setIsDistinct(false)
                .setHeaderLeftButtonListener(null)
                .setUserListAdapter(null)
                .setCustomUserListQueryHandler(BaseApplication.getCustomUserListQuery())
                .setCreateButtonText(null)
                .build();
    }
}