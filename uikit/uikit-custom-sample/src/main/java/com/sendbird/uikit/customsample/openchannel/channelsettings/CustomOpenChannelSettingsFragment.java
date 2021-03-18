package com.sendbird.uikit.customsample.openchannel.channelsettings;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.OpenChannel;
import com.sendbird.android.OpenChannelParams;
import com.sendbird.uikit.activities.ParticipantsListActivity;
import com.sendbird.uikit.customsample.openchannel.participants.CustomParticipantsListActivity;
import com.sendbird.uikit.fragments.OpenChannelSettingsFragment;
import com.sendbird.uikit.interfaces.OnMenuItemClickListener;
import com.sendbird.uikit.widgets.OpenChannelSettingsView;

public class CustomOpenChannelSettingsFragment extends OpenChannelSettingsFragment implements OnMenuItemClickListener<OpenChannelSettingsView.OpenChannelSettingMenu, OpenChannel>{

    @Override
    protected void onBeforeUpdateOpenChannel(@NonNull OpenChannelParams params) {
        super.onBeforeUpdateOpenChannel(params);
    }

    @Override
    protected void updateOpenChannel(@NonNull OpenChannelParams params) {
        super.updateOpenChannel(params);
    }

    @Override
    protected void deleteChannel() {
        super.deleteChannel();
    }

    @Override
    protected void setHeaderLeftButtonListener(View.OnClickListener listener) {
        super.setHeaderLeftButtonListener(listener);
    }

    @Override
    protected void setOnMenuItemClickListener(OnMenuItemClickListener<OpenChannelSettingsView.OpenChannelSettingMenu, OpenChannel> listener) {
        super.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean shouldShowLoadingDialog() {
        return super.shouldShowLoadingDialog();
    }

    @Override
    public void shouldDismissLoadingDialog() {
        super.shouldDismissLoadingDialog();
    }

    @Override
    public boolean onMenuItemClicked(View view, OpenChannelSettingsView.OpenChannelSettingMenu openChannelSettingMenu, OpenChannel openChannel) {
        if (getActivity() == null) {
            return true;
        }

        switch (openChannelSettingMenu) {
            case PARTICIPANTS:
                Intent participantsListIntent = ParticipantsListActivity.newIntentFromCustomActivity(getActivity(), CustomParticipantsListActivity.class, openChannel.getUrl());
                startActivity(participantsListIntent);
                return true;
            case DELETE_CHANNEL:
            default:
        }
        return false;
    }
}
