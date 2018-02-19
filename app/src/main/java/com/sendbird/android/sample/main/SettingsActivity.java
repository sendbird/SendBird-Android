package com.sendbird.android.sample.main;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.utils.DateUtils;
import com.sendbird.android.sample.utils.FileUtils;
import com.sendbird.android.sample.utils.ImageUtils;
import com.sendbird.android.sample.utils.PreferenceUtils;
import com.sendbird.android.sample.utils.PushUtils;

import java.io.File;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class SettingsActivity extends AppCompatActivity {

    private static final int INTENT_REQUEST_CHOOSE_MEDIA = 0xf0;
    private static final int INTENT_REQUEST_CAMERA = 0xf1;

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_UPLOAD = 0xf0;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_CAMERA = 0xf1;

    private InputMethodManager mIMM;
    private Calendar mCalendar;

    private boolean mNickNameChanged = false;

    private boolean mRequestingCamera = false;
    private Uri mTempPhotoUri = null;

    private CoordinatorLayout mSettingsLayout;
    private ImageView mImageViewProfile;
    private EditText mEditTextNickname;
    private Button mButtonSaveNickName;

    private LinearLayout mLinearLayoutNotifications;
    private SwitchCompat mSwitchNotifications;
    private SwitchCompat mSwitchNotificationsShowPreviews;

    private SwitchCompat mSwitchNotificationsDoNotDisturb;
    private LinearLayout mLinearLayoutDoNotDisturb;
    private LinearLayout mLinearLayoutNotificationsDoNotDisturbFrom;
    private LinearLayout mLinearLayoutNotificationsDoNotDisturbTo;
    private TextView mTextViewNotificationsDoNotDisturbFrom;
    private TextView mTextViewNotificationsDoNotDisturbTo;

    private CheckBox mCheckBoxGroupChannelDistinct;

    private LinearLayout mLinearLayoutBlockedMembersList;

    private CompoundButton.OnCheckedChangeListener mCheckedChangeListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mIMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mCalendar = Calendar.getInstance(Locale.getDefault());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_window_close_white_24_dp);
        }

        mSettingsLayout = (CoordinatorLayout) findViewById(R.id.layout_settings);

        mImageViewProfile = (ImageView) findViewById(R.id.image_view_profile);
        mEditTextNickname = (EditText) findViewById(R.id.edit_text_nickname);
        mButtonSaveNickName = (Button) findViewById(R.id.button_save_nickname);

        mLinearLayoutNotifications = (LinearLayout) findViewById(R.id.linear_layout_notifications);
        mSwitchNotifications = (SwitchCompat) findViewById(R.id.switch_notifications);
        mSwitchNotificationsShowPreviews = (SwitchCompat) findViewById(R.id.switch_notifications_show_previews);

        mSwitchNotificationsDoNotDisturb = (SwitchCompat) findViewById(R.id.switch_notifications_do_not_disturb);
        mLinearLayoutDoNotDisturb = (LinearLayout) findViewById(R.id.linear_layout_do_not_disturb);
        mLinearLayoutNotificationsDoNotDisturbFrom = (LinearLayout) findViewById(R.id.linear_layout_notifications_do_not_disturb_from);
        mLinearLayoutNotificationsDoNotDisturbTo = (LinearLayout) findViewById(R.id.linear_layout_notifications_do_not_disturb_to);
        mTextViewNotificationsDoNotDisturbFrom = (TextView) findViewById(R.id.text_view_notifications_do_not_disturb_from);
        mTextViewNotificationsDoNotDisturbTo = (TextView) findViewById(R.id.text_view_notifications_do_not_disturb_to);

        mCheckBoxGroupChannelDistinct = (CheckBox) findViewById(R.id.checkbox_make_group_channel_distinct);

        mLinearLayoutBlockedMembersList = (LinearLayout) findViewById(R.id.linear_layout_blocked_members_list);
        mLinearLayoutBlockedMembersList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, BlockedMembersListActivity.class);
                startActivity(intent);
            }
        });

        //+ ProfileUrl
        String profileUrl = PreferenceUtils.getProfileUrl();
        if (profileUrl.length() > 0) {
            ImageUtils.displayRoundImageFromUrl(SettingsActivity.this, profileUrl, mImageViewProfile);
        }
        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetProfileOptionsDialog();
            }
        });
        //- ProfileUrl

        //+ Nickname
        mEditTextNickname.setEnabled(false);
        final String nickname = PreferenceUtils.getNickname();
        if (nickname.length() > 0) {
            mEditTextNickname.setText(nickname);
        }
        mEditTextNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0 && !s.toString().equals(nickname)) {
                    mNickNameChanged = true;
                }
            }
        });
        mButtonSaveNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditTextNickname.isEnabled()) {
                    if (mNickNameChanged) {
                        mNickNameChanged = false;

                        updateCurrentUserInfo(mEditTextNickname.getText().toString());
                    }

                    mButtonSaveNickName.setText("EDIT");
                    mEditTextNickname.setEnabled(false);
                    mEditTextNickname.setFocusable(false);
                    mEditTextNickname.setFocusableInTouchMode(false);
                } else {
                    mButtonSaveNickName.setText("SAVE");
                    mEditTextNickname.setEnabled(true);
                    mEditTextNickname.setFocusable(true);
                    mEditTextNickname.setFocusableInTouchMode(true);
                    if (mEditTextNickname.getText() != null && mEditTextNickname.getText().length() > 0) {
                        mEditTextNickname.setSelection(0, mEditTextNickname.getText().length());
                    }
                    mEditTextNickname.requestFocus();
                    mEditTextNickname.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIMM.showSoftInput(mEditTextNickname, 0);
                        }
                    }, 100);
                }
            }
        });
        //- Nickname

        //+ Notifications
        boolean notifications = PreferenceUtils.getNotifications();
        mSwitchNotifications.setChecked(notifications);
        mSwitchNotificationsShowPreviews.setChecked(PreferenceUtils.getNotificationsShowPreviews());
        checkNotifications(notifications);

        boolean doNotDisturb = PreferenceUtils.getNotificationsDoNotDisturb();
        mSwitchNotificationsDoNotDisturb.setChecked(doNotDisturb);
        checkDoNotDisturb(doNotDisturb);

        mSwitchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    PushUtils.registerPushTokenForCurrentUser(SettingsActivity.this, new SendBird.RegisterPushTokenWithStatusHandler() {
                        @Override
                        public void onRegistered(SendBird.PushTokenRegistrationStatus pushTokenRegistrationStatus, SendBirdException e) {
                            if (e != null) {
                                mSwitchNotifications.setChecked(!isChecked);
                                checkNotifications(!isChecked);
                                return;
                            }

                            PreferenceUtils.setNotifications(isChecked);
                            checkNotifications(isChecked);
                        }
                    });
                } else {
                    PushUtils.unregisterPushTokenForCurrentUser(SettingsActivity.this, new SendBird.UnregisterPushTokenHandler() {
                        @Override
                        public void onUnregistered(SendBirdException e) {
                            if (e != null) {
                                mSwitchNotifications.setChecked(!isChecked);
                                checkNotifications(!isChecked);
                                return;
                            }

                            PreferenceUtils.setNotifications(isChecked);
                            checkNotifications(isChecked);
                        }
                    });
                }
            }
        });

        mSwitchNotificationsShowPreviews.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceUtils.setNotificationsShowPreviews(isChecked);
            }
        });

        mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                saveDoNotDisturb(isChecked);
            }
        };

        mSwitchNotificationsDoNotDisturb.setOnCheckedChangeListener(mCheckedChangeListener);

        mLinearLayoutNotificationsDoNotDisturbFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDoNotDisturbTime(true, mTextViewNotificationsDoNotDisturbTo);
            }
        });

        mLinearLayoutNotificationsDoNotDisturbTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDoNotDisturbTime(false, mTextViewNotificationsDoNotDisturbTo);
            }
        });

        SendBird.getDoNotDisturb(new SendBird.GetDoNotDisturbHandler() {
            @Override
            public void onResult(boolean isDoNotDisturbOn, int startHour, int startMin, int endHour, int endMin, String timezone, SendBirdException e) {
                mCalendar.clear();
                mCalendar.set(Calendar.HOUR_OF_DAY, startHour);
                mCalendar.set(Calendar.MINUTE, startMin);
                long fromMillis = mCalendar.getTimeInMillis();

                PreferenceUtils.setNotificationsDoNotDisturbFrom(String.valueOf(fromMillis));
                mTextViewNotificationsDoNotDisturbTo.setText(DateUtils.formatTimeWithMarker(fromMillis));

                mCalendar.clear();
                mCalendar.set(Calendar.HOUR_OF_DAY, endHour);
                mCalendar.set(Calendar.MINUTE, endMin);
                long toMillis = mCalendar.getTimeInMillis();

                PreferenceUtils.setNotificationsDoNotDisturbTo(String.valueOf(toMillis));
                mTextViewNotificationsDoNotDisturbTo.setText(DateUtils.formatTimeWithMarker(toMillis));

                mSwitchNotificationsDoNotDisturb.setChecked(isDoNotDisturbOn);
            }
        });
        //- Notifications

        //+ Group Channel Distinct
        mCheckBoxGroupChannelDistinct.setChecked(PreferenceUtils.getGroupChannelDistinct());

        mCheckBoxGroupChannelDistinct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceUtils.setGroupChannelDistinct(isChecked);
            }
        });
        //- Group Channel Distinct
    }

    private void saveDoNotDisturb(final boolean doNotDisturb) {
        String doNotDisturbFrom = PreferenceUtils.getNotificationsDoNotDisturbFrom();
        String doNotDisturbTo = PreferenceUtils.getNotificationsDoNotDisturbTo();
        if (doNotDisturbFrom.length() > 0 && doNotDisturbTo.length() > 0) {
            int startHour = DateUtils.getHourOfDay(Long.valueOf(doNotDisturbFrom));
            int startMin = DateUtils.getMinute(Long.valueOf(doNotDisturbFrom));
            int endHour = DateUtils.getHourOfDay(Long.valueOf(doNotDisturbTo));
            int endMin = DateUtils.getMinute(Long.valueOf(doNotDisturbTo));

            SendBird.setDoNotDisturb(doNotDisturb, startHour, startMin, endHour, endMin, TimeZone.getDefault().getID(), new SendBird.SetDoNotDisturbHandler() {
                @Override
                public void onResult(SendBirdException e) {
                    if (e != null) {
                        mSwitchNotificationsDoNotDisturb.setOnCheckedChangeListener(null);
                        mSwitchNotificationsDoNotDisturb.setChecked(!doNotDisturb);
                        mSwitchNotificationsDoNotDisturb.setOnCheckedChangeListener(mCheckedChangeListener);

                        PreferenceUtils.setNotificationsDoNotDisturb(!doNotDisturb);
                        checkDoNotDisturb(!doNotDisturb);
                        return;
                    }

                    mSwitchNotificationsDoNotDisturb.setOnCheckedChangeListener(null);
                    mSwitchNotificationsDoNotDisturb.setChecked(doNotDisturb);
                    mSwitchNotificationsDoNotDisturb.setOnCheckedChangeListener(mCheckedChangeListener);

                    PreferenceUtils.setNotificationsDoNotDisturb(doNotDisturb);
                    checkDoNotDisturb(doNotDisturb);
                }
            });
        }
    }

    private void setDoNotDisturbTime(final boolean from, final TextView textView) {
        long timeMillis = System.currentTimeMillis();
        if (from) {
            String doNotDisturbFrom = PreferenceUtils.getNotificationsDoNotDisturbFrom();
            if (doNotDisturbFrom.length() > 0) {
                timeMillis = Long.valueOf(doNotDisturbFrom);
            }
        } else {
            String doNotDisturbTo = PreferenceUtils.getNotificationsDoNotDisturbTo();
            if (doNotDisturbTo.length() > 0) {
                timeMillis = Long.valueOf(doNotDisturbTo);
            }
        }

        new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int min) {
                mCalendar.clear();
                mCalendar.set(Calendar.HOUR_OF_DAY, hour);
                mCalendar.set(Calendar.MINUTE, min);
                long millis = mCalendar.getTimeInMillis();

                if (from) {
                    if (!String.valueOf(millis).equals(PreferenceUtils.getNotificationsDoNotDisturbFrom())) {
                        PreferenceUtils.setNotificationsDoNotDisturbFrom(String.valueOf(millis));
                        saveDoNotDisturb(true);
                    }
                } else {
                    if (!String.valueOf(millis).equals(PreferenceUtils.getNotificationsDoNotDisturbTo())) {
                        PreferenceUtils.setNotificationsDoNotDisturbTo(String.valueOf(millis));
                        saveDoNotDisturb(true);
                    }
                }
                textView.setText(DateUtils.formatTimeWithMarker(millis));
            }
        }, DateUtils.getHourOfDay(timeMillis), DateUtils.getMinute(timeMillis), true).show();
    }

    private void checkNotifications(boolean notifications) {
        if (notifications) {
            mLinearLayoutNotifications.setVisibility(View.VISIBLE);
            boolean doNotDisturb = PreferenceUtils.getNotificationsDoNotDisturb();
            checkDoNotDisturb(doNotDisturb);
        } else {
            mLinearLayoutNotifications.setVisibility(View.GONE);
        }
    }

    private void checkDoNotDisturb(boolean doNotDisturb) {
        if (doNotDisturb) {
            mLinearLayoutDoNotDisturb.setVisibility(View.VISIBLE);
        } else {
            mLinearLayoutDoNotDisturb.setVisibility(View.GONE);
        }

        String doNotDisturbFrom = PreferenceUtils.getNotificationsDoNotDisturbFrom();
        if (doNotDisturbFrom.length() > 0) {
            mTextViewNotificationsDoNotDisturbFrom.setText(DateUtils.formatTimeWithMarker(Long.valueOf(doNotDisturbFrom)));
        } else {
            mTextViewNotificationsDoNotDisturbFrom.setText("");
        }

        String doNotDisturbTo = PreferenceUtils.getNotificationsDoNotDisturbTo();
        if (doNotDisturbTo.length() > 0) {
            mTextViewNotificationsDoNotDisturbTo.setText(DateUtils.formatTimeWithMarker(Long.valueOf(doNotDisturbTo)));
        } else {
            mTextViewNotificationsDoNotDisturbTo.setText("");
        }
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

    private void showSetProfileOptionsDialog() {
        String[] options = new String[] { "Upload a photo", "Take a photo" };

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Set profile image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            requestMedia();
                        } else if (which == 1) {
                            requestCamera();
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_CHOOSE_MEDIA && resultCode == Activity.RESULT_OK) {
            // If user has successfully chosen the image, show a dialog to confirm upload.
            if (data == null) {
                return;
            }

            Uri uri = data.getData();
            Hashtable<String, Object> info = FileUtils.getFileInfo(SettingsActivity.this, uri);
            if (info != null) {
                String path = (String)info.get("path");
                if (path != null) {
                    File profileImage = new File(path);
                    updateCurrentUserProfileImage(profileImage, mImageViewProfile);
                }
            }
        } else if (requestCode == INTENT_REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            if (!mRequestingCamera) {
                return;
            }

            File profileImage = new File(mTempPhotoUri.getPath());
            updateCurrentUserProfileImage(profileImage, mImageViewProfile);
            mRequestingCamera = false;
        }

        // Set this as true to restore background connection management.
        SendBird.setAutoBackgroundDetection(true);
    }

    private void requestMedia() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions(PERMISSION_WRITE_EXTERNAL_STORAGE_UPLOAD);
        } else {
            Intent intent = new Intent();
            // Show only images, no videos or anything else
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, "Select Image"), INTENT_REQUEST_CHOOSE_MEDIA);

            // Set this as false to maintain connection
            // even when an external Activity is started.
            SendBird.setAutoBackgroundDetection(false);
        }
    }

    private void requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions(PERMISSION_WRITE_EXTERNAL_STORAGE_CAMERA);
        } else {
            mRequestingCamera = true;
            mTempPhotoUri = getTempFileUri(false);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);

            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, mTempPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivityForResult(intent, INTENT_REQUEST_CAMERA);

            SendBird.setAutoBackgroundDetection(false);
        }
    }

    private Uri getTempFileUri(boolean doNotUseFileProvider) {
        Uri uri = null;
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File tempFile = File.createTempFile("SendBird_" + System.currentTimeMillis(), ".jpg", path);

            if (Build.VERSION.SDK_INT >= 24 && !doNotUseFileProvider) {
                uri = FileProvider.getUriForFile(this, "com.sendbird.android.sample.provider", tempFile);
            } else {
                uri = Uri.fromFile(tempFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SendBird.setAutoBackgroundDetection(true);

        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE_UPLOAD:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestMedia();
                }
                break;

            case PERMISSION_WRITE_EXTERNAL_STORAGE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestCamera();
                }
                break;
        }
    }

    private void requestStoragePermissions(final int code) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(mSettingsLayout, "Storage access permissions are required to upload/download files.", Snackbar.LENGTH_LONG)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Maintains connection.
                            SendBird.setAutoBackgroundDetection(false);
                            ActivityCompat.requestPermissions(
                                    SettingsActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    code
                            );
                        }
                    })
                    .show();
        } else {
            // Maintains connection.
            SendBird.setAutoBackgroundDetection(false);
            // Permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(
                    SettingsActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    code
            );
        }
    }

    private void updateCurrentUserProfileImage(final File profileImage, final ImageView imageView) {
        final String nickname = PreferenceUtils.getNickname();
        SendBird.updateCurrentUserInfoWithProfileImage(nickname, profileImage, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(SettingsActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Show update failed snackbar
                    showSnackbar("Update user info failed");
                    return;
                }

                try {
                    PreferenceUtils.setProfileUrl(SendBird.getCurrentUser().getProfileUrl());
                    ImageUtils.displayRoundImageFromUrl(SettingsActivity.this, Uri.fromFile(profileImage).toString(), imageView);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void updateCurrentUserInfo(final String userNickname) {
        final String profileUrl = PreferenceUtils.getProfileUrl();
        SendBird.updateCurrentUserInfo(userNickname, profileUrl, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(SettingsActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Show update failed snackbar
                    showSnackbar("Update user info failed");
                    return;
                }

                PreferenceUtils.setNickname(userNickname);
            }
        });
    }

    private void showSnackbar(String text) {
        Snackbar snackbar = Snackbar.make(mSettingsLayout, text, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

}
