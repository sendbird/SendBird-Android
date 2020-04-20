package com.sendbird.syncmanager.sample.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.syncmanager.SendBirdSyncManager;
import com.sendbird.syncmanager.sample.R;
import com.sendbird.syncmanager.sample.groupchannel.GroupChannelActivity;
import com.sendbird.syncmanager.sample.utils.PreferenceUtils;
import com.sendbird.syncmanager.sample.view.BaseActivity;

public class MainActivity extends BaseActivity {

    public static final String EXTRA_GROUP_CHANNEL_URL = "GROUP_CHANNEL_URL";

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);

        findViewById(R.id.linear_layout_group_channels).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GroupChannelActivity.class);
                startActivity(intent);
            }
        });


        findViewById(R.id.button_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Unregister push tokens and disconnect
                disconnect();
            }
        });


        // Displays the SDK version in a TextView
        String sdkVersion = String.format(getResources().getString(R.string.all_app_version),
                BaseApplication.VERSION, SendBird.getSDKVersion(), SendBirdSyncManager.getSDKVersion());
        ((TextView) findViewById(R.id.text_main_versions)).setText(sdkVersion);

        connect();
    }

    @Override
    protected String getConnectionHandlerId() {
        return "CONNECTION_HANDLER_MAIN_ACTIVITY";
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void connect() {
        if (SendBird.getConnectionState() != SendBird.ConnectionState.OPEN) {
            showProgressBar(true);
            ConnectionManager.connect(this, PreferenceUtils.getUserId(), PreferenceUtils.getNickname(), new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {
                    showProgressBar(false);
                    if (e != null) {
                        e.printStackTrace();
                    } else {
                        checkExtra();
                    }
                }
            });
        } else {
            checkExtra();
        }
    }

    private void checkExtra() {
        if (getIntent().hasExtra(EXTRA_GROUP_CHANNEL_URL)) {
            String extraChannelUrl = getIntent().getStringExtra(EXTRA_GROUP_CHANNEL_URL);
            Intent mainIntent = new Intent(MainActivity.this, GroupChannelActivity.class);
            mainIntent.putExtra(EXTRA_GROUP_CHANNEL_URL, extraChannelUrl);
            startActivity(mainIntent);
        }
    }

    /**
     * Unregisters all push tokens for the current user so that they do not receive any notifications,
     * then disconnects from Sendbird.
     */
    private void disconnect() {
        SendBird.unregisterPushTokenAllForCurrentUser(new SendBird.UnregisterPushTokenHandler() {
            @Override
            public void onUnregistered(SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();

                    // Don't return because we still need to disconnect.
                } else {
//                    Toast.makeText(MainActivity.this, "All push tokens unregistered.", Toast.LENGTH_SHORT).show();

                }

                ConnectionManager.disconnect(new SendBird.DisconnectHandler() {
                    @Override
                    public void onDisconnected() {

                        String userId = PreferenceUtils.getUserId();
                        // if you want to clear cache of specific user when disconnect, you can do like this.

                        SendBirdSyncManager.getInstance().clearCache(userId);

                        PreferenceUtils.setConnected(false);
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }
}
