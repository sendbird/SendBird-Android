package com.sendbird.syncmanager.sample.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.syncmanager.SendBirdSyncManager;
import com.sendbird.syncmanager.handler.CompletionHandler;
import com.sendbird.syncmanager.sample.R;
import com.sendbird.syncmanager.sample.utils.PreferenceUtils;
import com.sendbird.syncmanager.sample.utils.SyncManagerUtils;
import com.sendbird.syncmanager.sample.view.BaseActivity;

public class LoginActivity extends BaseActivity {

    private CoordinatorLayout mLoginLayout;
    private TextInputEditText mUserIdConnectEditText, mUserNicknameEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mLoginLayout = findViewById(R.id.layout_login);
        mUserIdConnectEditText = findViewById(R.id.edittext_login_user_id);
        mUserNicknameEditText = findViewById(R.id.edittext_login_user_nickname);

        mUserIdConnectEditText = (TextInputEditText) findViewById(R.id.edittext_login_user_id);
        mUserNicknameEditText = (TextInputEditText) findViewById(R.id.edittext_login_user_nickname);

        mUserIdConnectEditText.setText(PreferenceUtils.getUserId());
        mUserNicknameEditText.setText(PreferenceUtils.getNickname());

        findViewById(R.id.button_login_connect).setOnClickListener(v -> {
            String userId = mUserIdConnectEditText.getText().toString();
            // Remove all spaces from userID
            userId = userId.replaceAll("\\s", "");
            String userNickname = mUserNicknameEditText.getText().toString();

            PreferenceUtils.setUserId(userId);
            PreferenceUtils.setNickname(userNickname);

            connect(userId, userNickname);
        });

        mUserIdConnectEditText.setSelectAllOnFocus(true);
        mUserNicknameEditText.setSelectAllOnFocus(true);

        // Display current SendBird and app versions in a TextView
        String sdkVersion = String.format(getResources().getString(R.string.all_app_version),
                BaseApplication.VERSION, SendBird.getSDKVersion(), SendBirdSyncManager.getSDKVersion());
        ((TextView) findViewById(R.id.text_login_versions)).setText(sdkVersion);
    }

    // Displays a Snackbar from the bottom of the screen
    private void showSnackbar(String text) {
        Snackbar snackbar = Snackbar.make(mLoginLayout, text, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void connect(String userId, String userNickname) {
        ConnectionManager.connect(LoginActivity.this, userId, userNickname, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                showProgressBar(false);
                if (e == null) {
                    SyncManagerUtils.setup(LoginActivity.this, PreferenceUtils.getUserId(), new CompletionHandler() {
                        @Override
                        public void onCompleted(SendBirdException e) {
                            if (e != null) {
                                e.printStackTrace();
                                return;
                            }
                            PreferenceUtils.setConnected(true);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    showSnackbar(getString(R.string.login_failed));
                    PreferenceUtils.setConnected(false);
                }
            }
        });
    }
}
