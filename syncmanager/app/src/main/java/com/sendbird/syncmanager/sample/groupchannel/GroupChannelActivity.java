package com.sendbird.syncmanager.sample.groupchannel;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.sendbird.syncmanager.sample.R;
import com.sendbird.syncmanager.sample.main.MainActivity;
import com.sendbird.syncmanager.sample.view.BaseActivity;

public class GroupChannelActivity extends BaseActivity {

    @Override
    protected void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_channel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_group_channel);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp);
        }

        if (savedInstanceState == null) {
            // Load list of Group Channels
            Fragment fragment = GroupChannelListFragment.newInstance();

            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack();

            manager.beginTransaction()
                    .replace(R.id.container_group_channel, fragment)
                    .commit();
        }

        final String channelUrl = getIntent().getStringExtra(MainActivity.EXTRA_GROUP_CHANNEL_URL);
        if(channelUrl != null) {
            // If started from notification
            Fragment fragment = GroupChatFragment.newInstance(channelUrl);
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container_group_channel, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    protected String getConnectionHandlerId() {
        return "CONNECTION_HANDLER_GROUP_CHANNEL_ACTIVITY";
    }

    interface onBackPressedListener {
        boolean onBack();
    }
    private onBackPressedListener mOnBackPressedListener;

    public void setOnBackPressedListener(onBackPressedListener listener) {
        mOnBackPressedListener = listener;
    }

    @Override
    public void onBackPressed() {
        if (mOnBackPressedListener != null && mOnBackPressedListener.onBack()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}
