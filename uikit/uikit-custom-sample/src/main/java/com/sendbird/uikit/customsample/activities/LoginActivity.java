package com.sendbird.uikit.customsample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.android.SendBird;
import com.sendbird.uikit.BuildConfig;
import com.sendbird.uikit.customsample.BaseApplication;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.utils.TextUtils;

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

            ((BaseApplication) getApplication()).setUserId(userId);
            ((BaseApplication) getApplication()).setUserNickname(userNickname);

            Intent intent = new Intent(LoginActivity.this, CustomChannelListActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
