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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.utils.DateUtils;
import com.sendbird.android.sample.utils.FileUtils;
import com.sendbird.android.sample.utils.ImageUtils;
import com.sendbird.android.sample.utils.PreferenceUtils;
import com.sendbird.android.sample.utils.PushUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class SettingsActivity extends AppCompatActivity {

    private static final int INTENT_REQUEST_CHOOSE_MEDIA                = 0xf0;
    private static final int INTENT_REQUEST_CAMERA                      = 0xf1;
    private static final int INTENT_REQUEST_CAMERA_WITH_FILE_PROVIDER   = 0xf2;


    private static final int MEDIA_REQUEST_PERMISSIONS_REQUEST_CODE     = 1;
    private static final int CAMERA_REQUEST_PERMISSIONS_REQUEST_CODE    = 2;

    private static final String[] MEDIA_MANDATORY_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String[] CAMERA_MANDATORY_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };


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
        String profileUrl = SendBird.getCurrentUser() != null ? SendBird.getCurrentUser().getProfileUrl() : "";
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
        final String nickname = SendBird.getCurrentUser() != null ? SendBird.getCurrentUser().getNickname() : "";
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
                PushUtils.setPushNotification(isChecked, new SendBird.SetPushTriggerOptionHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        mSwitchNotifications.setChecked((e == null) == isChecked);
                        PreferenceUtils.setNotifications(e == null);
                        checkNotifications(isChecked);
                    }
                });
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
                            checkPermissions(MEDIA_MANDATORY_PERMISSIONS, MEDIA_REQUEST_PERMISSIONS_REQUEST_CODE);
                        } else if (which == 1) {
                            checkPermissions(CAMERA_MANDATORY_PERMISSIONS, CAMERA_REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
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
                        if (profileImage.exists()) {
                            updateCurrentUserProfileImage(profileImage, mImageViewProfile);
                        }
                    }
                }
            } else if (requestCode == INTENT_REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
                if (!mRequestingCamera) {
                    return;
                }

                File profileImage = new File(mTempPhotoUri.getPath());
                if (profileImage.exists()) {
                    updateCurrentUserProfileImage(profileImage, mImageViewProfile);
                    mRequestingCamera = false;
                }
            } else if (requestCode == INTENT_REQUEST_CAMERA_WITH_FILE_PROVIDER && resultCode == Activity.RESULT_OK) {
                if (!mRequestingCamera) {
                    return;
                }

                File imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String fileName = mTempPhotoUri.getPathSegments().get(1);
                File profileImage = new File(imagePath, fileName);
                if (profileImage.exists()) {
                    updateCurrentUserProfileImage(profileImage, mImageViewProfile);
                    mRequestingCamera = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Set this as true to restore background connection management.
            SendBird.setAutoBackgroundDetection(true);
        }

    }

    private void checkPermissions(String[] permissions, int requestCode) {
        ArrayList<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }

        if (deniedPermissions.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                SendBird.setAutoBackgroundDetection(false);
                requestPermissions(deniedPermissions.toArray(new String[0]), requestCode);
            } else {
                permissionDenied();
            }
        } else {
            if (requestCode == MEDIA_REQUEST_PERMISSIONS_REQUEST_CODE) {
                requestMedia();
            } else if (requestCode == CAMERA_REQUEST_PERMISSIONS_REQUEST_CODE) {
                requestCamera();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        for (int result : grantResults) {
            allowed = allowed && (result == PackageManager.PERMISSION_GRANTED);
        }

        if (requestCode == MEDIA_REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (allowed) {
                requestMedia();
            } else {
                permissionDenied();
            }
        } else if (requestCode == CAMERA_REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (allowed) {
                requestCamera();
            } else {
                permissionDenied();
            }
        }
    }

    private void permissionDenied() {
        Snackbar.make(mSettingsLayout, "Permission denied.", Snackbar.LENGTH_LONG).show();
    }

    private void requestMedia() {
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

    private void requestCamera() {
        mRequestingCamera = true;

        try {
            File imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File tempFile = File.createTempFile("SendBird_" + System.currentTimeMillis(), ".jpg", imagePath);

            if (Build.VERSION.SDK_INT >= 24) {
                mTempPhotoUri = FileProvider.getUriForFile(this, "com.sendbird.android.sample.fileprovider", tempFile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, mTempPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, INTENT_REQUEST_CAMERA_WITH_FILE_PROVIDER);

                SendBird.setAutoBackgroundDetection(false);
            } else {
                mTempPhotoUri = Uri.fromFile(tempFile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
                startActivityForResult(intent, INTENT_REQUEST_CAMERA);

                SendBird.setAutoBackgroundDetection(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCurrentUserProfileImage(final File profileImage, final ImageView imageView) {
        final String nickname = SendBird.getCurrentUser() != null ? SendBird.getCurrentUser().getNickname() : "";
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
                    ImageUtils.displayRoundImageFromUrl(SettingsActivity.this, Uri.fromFile(profileImage).toString(), imageView);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void updateCurrentUserInfo(final String userNickname) {
        final String profileUrl = SendBird.getCurrentUser() != null ? SendBird.getCurrentUser().getProfileUrl() : "";
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
