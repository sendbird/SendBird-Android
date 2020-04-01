package com.sendbird.uikit_messaging_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.sendbird.android.SendBird;
import com.sendbird.uikit.BuildConfig;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.widgets.WaitingDialog;
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.fcm.MyFirebaseMessagingService;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.utils.PushUtils;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        EditText etUserId = findViewById(R.id.etUserId);
        EditText etNickname = findViewById(R.id.etNickname);
        TextView tvVersion = findViewById(R.id.tvVersionInfo);

        etUserId.setSelectAllOnFocus(true);
        etNickname.setSelectAllOnFocus(true);

        String sdkVersion = String.format(getResources().getString(R.string.text_version_info), BuildConfig.VERSION_NAME, SendBird.getSDKVersion());
        tvVersion.setText(sdkVersion);

        findViewById(R.id.btSignIn).setOnClickListener(v -> {
            String userId = etUserId.getText().toString();
            // Remove all spaces from userID
            userId = userId.replaceAll("\\s", "");

            String userNickname = etNickname.getText().toString();
            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userNickname)) {
                return;
            }

            PreferenceUtils.setUserId(userId);
            PreferenceUtils.setNickname(userNickname);

            WaitingDialog.show(this);
            SendBirdUIKit.connect((user, e) -> {
                if (e != null) {
                    Logger.e(e);
                    WaitingDialog.dismiss();
                    return;
                }
                WaitingDialog.dismiss();
                PushUtils.registerPushHandler(new MyFirebaseMessagingService());
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        });
    }
}
