package com.sendbird.uikit.customsample.activities;

import android.content.Intent;

import com.sendbird.uikit.activities.ChannelListActivity;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.activities.adapters.CustomChannelListAdapter;
import com.sendbird.uikit.customsample.fragments.CustomChannelListFragment;
import com.sendbird.uikit.fragments.ChannelListFragment;

public class CustomChannelListActivity extends ChannelListActivity {
    @Override
    protected ChannelListFragment createChannelListFragment() {
        return new ChannelListFragment.Builder(R.style.CustomChannelListStyle)
                .setCustomChannelListFragment(new CustomChannelListFragment())
                .setUseHeader(true)
                .setHeaderTitle(getString(R.string.sb_text_header_channel_list))
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                .setHeaderRightButtonIconResId(R.drawable.icon_create)
                .setHeaderLeftButtonListener(v -> onBackPressed())
                .setHeaderRightButtonListener(v -> showCustomCreateChannelActivity())
                .setChannelListAdapter(new CustomChannelListAdapter())
                .setItemClickListener((view, i, channel) -> showCustomChannelActivity(channel.getUrl()))
                .setItemLongClickListener(null)
                .setGroupChannelListQuery(null)
                .build();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CustomChannelListActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showCustomCreateChannelActivity() {
        Intent intent = new Intent(CustomChannelListActivity.this, CustomCreateChannelActivity.class);
        startActivity(intent);
    }

    private void showCustomChannelActivity(String channelUrl) {
        Intent intent = CustomChannelActivity.newIntentFromCustomActivity(CustomChannelListActivity.this, CustomChannelActivity.class, channelUrl);
        startActivity(intent);
    }
}