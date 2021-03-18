package com.sendbird.uikit.customsample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.customsample.utils.PreferenceUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String userId = PreferenceUtils.getUserId();
        if (!TextUtils.isEmpty(userId)) {
            SendBirdUIKit.connect((user, e) -> {
                startActivity(getNextIntent());
                finish();
            });
        } else {
            startActivity(getNextIntent());
            finish();
        }
    }

    private Intent getNextIntent() {
        String userId = PreferenceUtils.getUserId();
        if (!TextUtils.isEmpty(userId)) {
            return new Intent(SplashActivity.this, HomeActivity.class);
        }

        return new Intent(SplashActivity.this, LoginActivity.class);
    }
}
