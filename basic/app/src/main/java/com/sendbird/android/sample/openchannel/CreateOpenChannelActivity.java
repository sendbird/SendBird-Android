package com.sendbird.android.sample.openchannel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.sample.R;

/**
 * Allows a user to create an Open Channel.
 * Dialog instead of activity?
 */

public class CreateOpenChannelActivity extends AppCompatActivity {

    private InputMethodManager mIMM;

    private TextInputEditText mNameEditText;
    private boolean enableCreate = false;
    private Button mCreateButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_open_channel);

        mIMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_create_open_channel);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp);
        }

        mNameEditText = (TextInputEditText) findViewById(R.id.edittext_create_open_channel_name);

        mCreateButton = (Button) findViewById(R.id.button_create_open_channel);

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOpenChannel(mNameEditText.getText().toString());
            }
        });

        mCreateButton.setEnabled(enableCreate);

        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    if (enableCreate) {
                        mCreateButton.setEnabled(false);
                        enableCreate = false;
                    }
                } else {
                    if (!enableCreate) {
                        mCreateButton.setEnabled(true);
                        enableCreate = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * #################### SECURITY TIPS ####################
     * Before launching, you should review "Allow creating open channels from SDK" under ⚙️ Sendbird Dashboard -> Settings -> Security.
     * It's turned on at first to simplify running samples and implementing your first code.
     * Most apps will want to disable "Allow creating open channels from SDK" as that could cause unwanted operations
     * #################### SECURITY TIPS ####################
     */
    private void createOpenChannel(String name) {
        OpenChannel.createChannelWithOperatorUserIds(name, null, null, null, new OpenChannel.OpenChannelCreateHandler() {
            @Override
            public void onResult(OpenChannel openChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIMM.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
    }
}
