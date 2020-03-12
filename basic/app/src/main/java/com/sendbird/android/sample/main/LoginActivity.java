package com.sendbird.android.sample.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.fcm.MyFirebaseMessagingService;
import com.sendbird.android.sample.utils.PreferenceUtils;
import com.sendbird.android.sample.utils.PushUtils;
import com.sendbird.android.sample.widget.WaitingDialog;

public class LoginActivity extends AppCompatActivity {

    private CoordinatorLayout mLoginLayout;
    private TextInputEditText mUserIdConnectEditText, mUserNicknameEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mLoginLayout = findViewById(R.id.layout_login);
        mUserIdConnectEditText = findViewById(R.id.edittext_login_user_id);
        mUserNicknameEditText = findViewById(R.id.edittext_login_user_nickname);

        mUserIdConnectEditText.setText(PreferenceUtils.getUserId());
        mUserNicknameEditText.setText(PreferenceUtils.getNickname());

        findViewById(R.id.button_login_connect).setOnClickListener(v -> {
            String userId = mUserIdConnectEditText.getText().toString();
            // Remove all spaces from userID
            userId = userId.replaceAll("\\s", "");
            String userNickname = mUserNicknameEditText.getText().toString();

            PreferenceUtils.setUserId(userId);
            PreferenceUtils.setNickname(userNickname);

            connectToSendBird(userId, userNickname);
        });

        mUserIdConnectEditText.setSelectAllOnFocus(true);
        mUserNicknameEditText.setSelectAllOnFocus(true);

        // Display current SendBird and app versions in a TextView
        String sdkVersion = String.format(getResources().getString(R.string.all_app_version),
                BaseApplication.VERSION, SendBird.getSDKVersion());

        ((TextView) findViewById(R.id.text_login_versions)).setText(sdkVersion);
    }

    /**
     * Attempts to connect a user to SendBird.
     * @param userId    The unique ID of the user.
     * @param userNickname  The user's nickname, which will be displayed in chats.
     */
    private void connectToSendBird(final String userId, final String userNickname) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userNickname)) {
            return;
        }
        // Show the loading indicator
        showProgressBar(true);
        ConnectionManager.login(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                // Callback received; hide the progress bar.
                showProgressBar(false);

                if (e != null) {
                    // Error!
                    Toast.makeText(
                            LoginActivity.this, "" + e.getCode() + ": " + e.getMessage(),
                            Toast.LENGTH_SHORT)
                            .show();

                    // Show login failure snackbar
                    showSnackbar("Login to SendBird failed");
                    return;
                }

                PreferenceUtils.setConnected(true);

                // Update the user's nickname
                updateCurrentUserInfo(userNickname);
                PushUtils.registerPushHandler(new MyFirebaseMessagingService());

                // Proceed to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Updates the user's nickname.
     * @param userNickname  The new nickname of the user.
     */
    private void updateCurrentUserInfo(final String userNickname) {
        SendBird.updateCurrentUserInfo(userNickname, null, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(
                            LoginActivity.this, "" + e.getCode() + ":" + e.getMessage(),
                            Toast.LENGTH_SHORT)
                            .show();

                    // Show update failed snackbar
                    showSnackbar("Update user nickname failed");

                    return;
                }

                PreferenceUtils.setNickname(userNickname);
            }
        });
    }

    // Displays a Snackbar from the bottom of the screen
    private void showSnackbar(String text) {
        Snackbar snackbar = Snackbar.make(mLoginLayout, text, Snackbar.LENGTH_SHORT);

        snackbar.show();
    }

    // Shows or hides the ProgressBar
    private void showProgressBar(boolean show) {
        if (show) {
            WaitingDialog.show(this);
        } else {
            WaitingDialog.dismiss();
        }
    }
}
