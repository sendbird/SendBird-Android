package com.sendbird.localcaching.sample.view;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.localcaching.sample.main.LoginActivity;
import com.sendbird.localcaching.sample.model.LoginEvent;
import com.sendbird.localcaching.sample.utils.PreferenceUtils;
import com.sendbird.localcaching.sample.widget.WaitingDialog;

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

    // Shows or hides the ProgressBar
    protected void showProgressBar(boolean show) {
        if (show) {
            WaitingDialog.show(this);
        } else {
            WaitingDialog.dismiss();
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
    public void onEvent(LoginEvent event) {
        if (!event.isLoggedIn()) {
            // cannot log in either in online (connected) mode or an offline mode.
            // must try to sign in again.
            PreferenceUtils.setConnected(false);
            Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
