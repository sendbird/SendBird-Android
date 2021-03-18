package com.sendbird.uikit.customsample.openchannel.participants;

import androidx.annotation.NonNull;

import com.sendbird.uikit.activities.ParticipantsListActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.fragments.ParticipantsListFragment;

public class CustomParticipantsListActivity extends ParticipantsListActivity {

    @Override
    protected ParticipantsListFragment createParticipantsListFragment(@NonNull String channelUrl) {
        return new ParticipantsListFragment.Builder(channelUrl, R.style.CustomUserListStyle)
                .setCustomParticipantsListFragment(null)
                .setUseHeader(true)
                .setHeaderTitle(getString(R.string.sb_text_header_member_list))
                .setUseHeaderLeftButton(true)
                .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                .setHeaderLeftButtonListener(null)
                .setItemClickListener(null)
                .setItemLongClickListener(null)
                .build();
    }
}