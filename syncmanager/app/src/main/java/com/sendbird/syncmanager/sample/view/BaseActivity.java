package com.sendbird.syncmanager.sample.view;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.syncmanager.sample.model.ConnectionEvent;
import com.sendbird.syncmanager.sample.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConnectionEvent event) {
        if (!event.isConnected()) {
            DialogUtils.showConnectionRetryDialog(this);
        }
    }
}
