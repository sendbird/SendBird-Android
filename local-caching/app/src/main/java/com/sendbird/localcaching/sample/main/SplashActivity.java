package com.sendbird.localcaching.sample.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.localcaching.sample.R;
import com.sendbird.localcaching.sample.model.InitEvent;
import com.sendbird.localcaching.sample.utils.PreferenceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        EventBus.getDefault().register(this);
        if (getIntent() != null && (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            getIntent().removeExtra(MainActivity.EXTRA_GROUP_CHANNEL_URL);
        }

        if (BaseApplication.isInitialized) {
            proceed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void proceed() {
        if (ConnectionManager.isLoggedIn() && !PreferenceUtils.getUserId().isEmpty()) {
            setUpLocalCaching();
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void setUpLocalCaching() {
        ConnectionManager.connect(getApplicationContext(), PreferenceUtils.getUserId(), PreferenceUtils.getNickname(), new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (user != null) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    if (getIntent().hasExtra(MainActivity.EXTRA_GROUP_CHANNEL_URL)) {
                        String pushedChannelUrl = getIntent().getStringExtra(MainActivity.EXTRA_GROUP_CHANNEL_URL);
                        intent.putExtra(MainActivity.EXTRA_GROUP_CHANNEL_URL, pushedChannelUrl);
                    }
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InitEvent event) {
        Log.i("SplashActivity", "onEvent initialized: " + event.isInitialized());
        if (event.isInitialized()) {
            proceed();
        }
    }
}
