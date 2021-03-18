package com.sendbird.uikit_messaging_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.GroupChannelTotalUnreadMessageCountParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.SendBirdPushHelper;
import com.sendbird.android.User;
import com.sendbird.uikit.BuildConfig;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.widgets.WaitingDialog;
import com.sendbird.uikit_messaging_android.databinding.ActivityHomeBinding;
import com.sendbird.uikit_messaging_android.groupchannel.GroupChannelMainActivity;
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelMainActivity;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.utils.PushUtils;

import java.util.List;
import java.util.Map;


public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private static final String USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        String sdkVersion = String.format(getResources().getString(R.string.text_version_info), BuildConfig.VERSION_NAME, SendBird.getSDKVersion());
        binding.tvVersionInfo.setText(sdkVersion);

        binding.groupChannelButton.setOnClickListener(v -> clickGroupChannel());
        binding.openChannelButton.setOnClickListener(v -> clickOpenChannel());
        binding.btSignOut.setOnClickListener(v -> signOut());

        binding.tvUnreadCount.setTextAppearance(this, R.style.SendbirdCaption3OnDark01);
        binding.tvUnreadCount.setBackgroundResource(R.drawable.shape_badge_background);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SendBird.getTotalUnreadMessageCount(new GroupChannelTotalUnreadMessageCountParams(), (totalCount, e) -> {
            if (e != null) {
                return;
            }

            if (totalCount > 0) {
                binding.tvUnreadCount.setVisibility(View.VISIBLE);
                binding.tvUnreadCount.setText(totalCount > 99 ?
                        getString(R.string.text_tab_badge_max_count) :
                        String.valueOf(totalCount));
            } else {
                binding.tvUnreadCount.setVisibility(View.GONE);
            }
        });

        SendBird.addUserEventHandler(USER_EVENT_HANDLER_KEY, new SendBird.UserEventHandler() {
            @Override
            public void onFriendsDiscovered(List<User> list) {}

            @Override
            public void onTotalUnreadMessageCountChanged(int totalCount, Map<String, Integer> totalCountByCustomType) {
                if (totalCount > 0) {
                    binding.tvUnreadCount.setVisibility(View.VISIBLE);
                    binding.tvUnreadCount.setText(totalCount > 99 ?
                            getString(R.string.text_tab_badge_max_count) :
                            String.valueOf(totalCount));
                } else {
                    binding.tvUnreadCount.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SendBird.removeUserEventHandler(USER_EVENT_HANDLER_KEY);
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