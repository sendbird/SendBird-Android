package com.sendbird.syncmanager.sample.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.syncmanager.SendBirdSyncManager;
import com.sendbird.syncmanager.sample.R;
import com.sendbird.syncmanager.sample.groupchannel.GroupChannelActivity;
import com.sendbird.syncmanager.sample.utils.PreferenceUtils;
import com.sendbird.syncmanager.sample.view.BaseActivity;

public class MainActivity extends BaseActivity {

    public static final String EXTRA_GROUP_CHANNEL_URL = "GROUP_CHANNEL_URL";
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MAIN_ACTIVITY";

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

        registerConnectionHandler();
        if (SendBird.getConnectionState() != SendBird.ConnectionState.OPEN) {
            if (getIntent().hasExtra(EXTRA_GROUP_CHANNEL_URL)) {
                String extraChannelUrl = getIntent().getStringExtra(EXTRA_GROUP_CHANNEL_URL);
                Intent intent = new Intent(MainActivity.this, GroupChannelActivity.class);
                intent.putExtra(EXTRA_GROUP_CHANNEL_URL, extraChannelUrl);
                startActivity(intent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ConnectionManager.connect(MainActivity.this, PreferenceUtils.getUserId(), PreferenceUtils.getNickname(), null);
                    }
                },500);
            } else {
                ConnectionManager.connect(this, PreferenceUtils.getUserId(), PreferenceUtils.getNickname(), null);
            }
        }
    }

    @Override
    protected void onDestroy() {
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        super.onDestroy();
    }

    /**
     * Unregisters all push tokens for the current user so that they do not receive any notifications,
     * then disconnects from SendBird.
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

    private void registerConnectionHandler() {
        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
                SendBirdSyncManager.getInstance().pauseSync();
            }

            @Override
            public void onReconnectSucceeded() {
                SendBirdSyncManager.getInstance().resumeSync();
            }

            @Override
            public void onReconnectFailed() {
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
