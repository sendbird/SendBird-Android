package com.sendbird.uikit_messaging_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.SendBirdPushHelper;
import com.sendbird.uikit.BuildConfig;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.widgets.WaitingDialog;
import com.sendbird.uikit_messaging_android.groupchannel.GroupChannelMainActivity;
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelMainActivity;
import com.sendbird.uikit_messaging_android.databinding.ActivityHomeBinding;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.utils.PushUtils;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHomeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        String sdkVersion = String.format(getResources().getString(R.string.text_version_info), BuildConfig.VERSION_NAME, SendBird.getSDKVersion());
        binding.tvVersionInfo.setText(sdkVersion);

        binding.groupChannelButton.setOnClickListener(v -> clickGroupChannel());
        binding.openChannelButton.setOnClickListener(v -> clickOpenChannel());
        binding.btSignOut.setOnClickListener(v -> signOut());
    }

    private void clickGroupChannel() {
        Intent intent = new Intent(this, GroupChannelMainActivity.class);
        startActivity(intent);
    }

    private void clickOpenChannel() {
        Intent intent = new Intent(this, OpenChannelMainActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        WaitingDialog.show(this);
        PushUtils.unregisterPushHandler(new SendBirdPushHelper.OnPushRequestCompleteListener() {
            @Override
            public void onComplete(boolean isActive, String token) {
                SendBirdUIKit.disconnect(() -> {
                    WaitingDialog.dismiss();
                    PreferenceUtils.clearAll();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(SendBirdException e) {
                WaitingDialog.dismiss();
            }
        });
    }
}