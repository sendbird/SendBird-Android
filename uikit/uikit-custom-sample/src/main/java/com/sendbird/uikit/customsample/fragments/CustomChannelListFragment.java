package com.sendbird.uikit.customsample.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.activities.CreateChannelActivity;
import com.sendbird.uikit.consts.CreateableChannelType;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.activities.CustomCreateChannelActivity;
import com.sendbird.uikit.dialogs.DialogHelper;
import com.sendbird.uikit.fragments.ChannelListFragment;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.Available;
import com.sendbird.uikit.widgets.SelectChannelTypeView;

public class CustomChannelListFragment extends ChannelListFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.text_tab_channels));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.channels_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        final MenuItem createMenuItem = menu.findItem(R.id.action_create_channel);
        View rootView = createMenuItem.getActionView();
        rootView.setOnClickListener(v -> onOptionsItemSelected(createMenuItem));
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create_channel) {
            if (getContext() != null) {
                if (Available.isSupportSuper() || Available.isSupportBroadcast()) {
                    final SelectChannelTypeView layout = new SelectChannelTypeView(getContext());
                    layout.canCreateSuperGroupChannel(Available.isSupportSuper());
                    layout.canCreateBroadcastGroupChannel(Available.isSupportBroadcast());
                    final PopupWindow popupWindow = DialogHelper.showPopupWindow(layout);
                    layout.setOnItemClickListener((view, position, channelType) -> {
                        Logger.dev("++ channelType : " + channelType);
                        if (popupWindow.isShowing()) {
                            popupWindow.dismiss();
                        }
                        if (isActive()) {
                            onSelectedChannelType(channelType);
                        }
                    });
                } else {
                    if (isActive()) {
                        onSelectedChannelType(CreateableChannelType.Normal);
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSelectedChannelType(@NonNull CreateableChannelType channelType) {
        showCustomCreateChannelActivity(channelType);
    }

    @Override
    protected void leaveChannel(@NonNull GroupChannel channel) {
        super.leaveChannel(channel);
    }


    private void showCustomCreateChannelActivity(@NonNull CreateableChannelType channelType) {
        if (getContext() != null) {
            Intent intent = CreateChannelActivity.newIntentFromCustomActivity(getContext(), CustomCreateChannelActivity.class, channelType);
            startActivity(intent);
        }
    }
}
