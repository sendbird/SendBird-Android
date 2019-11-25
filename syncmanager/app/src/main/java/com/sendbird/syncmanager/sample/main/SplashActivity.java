package com.sendbird.syncmanager.sample.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.android.SendBirdException;
import com.sendbird.syncmanager.SendBirdSyncManager;
import com.sendbird.syncmanager.handler.CompletionHandler;
import com.sendbird.syncmanager.sample.R;
import com.sendbird.syncmanager.sample.utils.PreferenceUtils;
import com.sendbird.syncmanager.sample.utils.SyncManagerUtils;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getIntent() != null && (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            getIntent().removeExtra(MainActivity.EXTRA_GROUP_CHANNEL_URL);
        }

        if (ConnectionManager.isLogin() && PreferenceUtils.getUserId() != null) {
            setUpSyncManager();
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void setUpSyncManager() {
        if (PreferenceUtils.getUserId() != null) {
            SyncManagerUtils.setup(SplashActivity.this, PreferenceUtils.getUserId(), new CompletionHandler() {
                @Override
                public void onCompleted(SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(SplashActivity.this, "Cannot Setup SyncManager", Toast.LENGTH_SHORT).show();
                        SendBirdSyncManager.getInstance().clearCache();
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }

                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    if (getIntent().hasExtra(MainActivity.EXTRA_GROUP_CHANNEL_URL)) {
                        String pushedChannelUrl = getIntent().getStringExtra(MainActivity.EXTRA_GROUP_CHANNEL_URL);
                        intent.putExtra(MainActivity.EXTRA_GROUP_CHANNEL_URL, pushedChannelUrl);
                    }
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

}
