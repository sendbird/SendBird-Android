package com.sendbird.uikit.customsample.groupchannel.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.uikit.activities.InviteChannelActivity;
import com.sendbird.uikit.customsample.BaseApplication;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomInviteChannelFragment;
import com.sendbird.uikit.fragments.InviteChannelFragment;

public class CustomInviteChannelActivity extends InviteChannelActivity {
    @Override
    protected InviteChannelFragment createInviteChannelFragment(@NonNull String channelUrl) {
        return new InviteChannelFragment.Builder(channelUrl, R.style.CustomUserListStyle)
                .setCustomInviteChannelFragment(new CustomInviteChannelFragment())
                .setUseHeader(true)
                .setHeaderTitle(getString(R.string.sb_text_header_invite_member))
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setHeaderLeftButtonIcon(R.drawable.icon_arrow_left, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setHeaderLeftButtonListener(null)
                .setUserListAdapter(null)
                .setCustomUserListQueryHandler(BaseApplication.getCustomUserListQuery())
                .setInviteButtonText(null)
                .build();
    }
}