package com.sendbird.uikit_messaging_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.SendBirdPushHelper;
import com.sendbird.uikit.BuildConfig;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.ChannelListActivity;
import com.sendbird.uikit.widgets.WaitingDialog;
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.utils.PushUtils;

public class MainActivity extends AppCompatActivity {

    private SwitchCompat scTheme;
    private TextView tvLightTheme, tvDarkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPage();
    }

    private void initPage() {
        scTheme = findViewById(R.id.scTheme);
        tvLightTheme = findViewById(R.id.tvLightTheme);
        tvDarkTheme = findViewById(R.id.tvDarkTheme);

        boolean prefUseDarkTheme = PreferenceUtils.isUsingDarkTheme();
        SendBirdUIKit.setDefaultThemeMode(prefUseDarkTheme ? SendBirdUIKit.ThemeMode.Dark : SendBirdUIKit.ThemeMode.Light);
        drawTheme(prefUseDarkTheme);
        scTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenceUtils.setUseDarkTheme(isChecked);
            SendBirdUIKit.setDefaultThemeMode(isChecked ? SendBirdUIKit.ThemeMode.Dark : SendBirdUIKit.ThemeMode.Light);
            drawTheme(isChecked);
        });

        TextView tvVersion = findViewById(R.id.tvVersionInfo);
        String sdkVersion = String.format(getResources().getString(R.string.text_version_info), BuildConfig.VERSION_NAME, SendBird.getSDKVersion());
        tvVersion.setText(sdkVersion);

        findViewById(R.id.btStartChat).setOnClickListener(view -> startActivity(ChannelListActivity.newIntent(this)));
        findViewById(R.id.btSignOut).setOnClickListener(v -> {
            WaitingDialog.show(this);
            PushUtils.unregisterPushHandler(new SendBirdPushHelper.OnPushRequestCompleteListener() {
                @Override
                public void onComplete(boolean isActive, String token) {
                    SendBird.disconnect(() -> {
                        WaitingDialog.dismiss();
                        PreferenceUtils.clearAll();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    });
                }

                @Override
                public void onError(SendBirdException e) {
                    WaitingDialog.dismiss();
                }
            });
        });
    }

    private void drawTheme(boolean useDarkTheme) {
        scTheme.setChecked(useDarkTheme);
        tvLightTheme.setEnabled(!useDarkTheme);
        tvDarkTheme.setEnabled(useDarkTheme);
    }
}
