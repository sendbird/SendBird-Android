package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.content.Intent;
import android.view.View;

import com.sendbird.android.BaseChannel;
import com.sendbird.uikit.activities.BannedListActivity;
import com.sendbird.uikit.activities.MutedMemberListActivity;
import com.sendbird.uikit.activities.OperatorListActivity;
import com.sendbird.uikit.customsample.groupchannel.activities.CustomBannedListActivity;
import com.sendbird.uikit.customsample.groupchannel.activities.CustomMutedMemberListActivity;
import com.sendbird.uikit.customsample.groupchannel.activities.CustomOperatorListActivity;
import com.sendbird.uikit.fragments.ModerationFragment;
import com.sendbird.uikit.interfaces.OnMenuItemClickListener;

public class CustomModerationFragment extends ModerationFragment implements OnMenuItemClickListener<ModerationFragment.ModerationMenu, BaseChannel> {
    @Override
    protected void setOnMenuItemClickListener(OnMenuItemClickListener<ModerationMenu, BaseChannel> listener) {
        super.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClicked(View view, ModerationMenu menu, BaseChannel data) {
        if (getContext() == null) {
            return false;
        }

        Intent intent;

        switch (menu) {
            case OPERATORS:
                intent = OperatorListActivity.newIntentFromCustomActivity(getContext(), CustomOperatorListActivity.class, data.getUrl());
                startActivity(intent);
                return true;
            case MUTED_MEMBERS:
                intent = MutedMemberListActivity.newIntentFromCustomActivity(getContext(), CustomMutedMemberListActivity.class, data.getUrl());
                startActivity(intent);
                return true;
            case BANNED_MEMBERS:
                intent = BannedListActivity.newIntentFromCustomActivity(getContext(), CustomBannedListActivity.class, data.getUrl());
                startActivity(intent);
                return true;
        }
        return false;
    }
}
