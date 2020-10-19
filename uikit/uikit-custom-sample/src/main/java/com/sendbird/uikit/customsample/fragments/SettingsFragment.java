package com.sendbird.uikit.customsample.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.SendBirdPushHelper;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.activities.LoginActivity;
import com.sendbird.uikit.customsample.utils.PreferenceUtils;
import com.sendbird.uikit.customsample.utils.PushUtils;
import com.sendbird.uikit.dialogs.DialogHelper;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.PermissionUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.widgets.ChannelCoverView;
import com.sendbird.uikit.widgets.WaitingDialog;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment {
    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 1001;
    private static final int PERMISSION_SETTINGS_REQUEST_ID = 2000;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2001;
    private static final int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 2002;

    private ChannelCoverView profileImageView;
    private TextView nicknameTextView;
    private SwitchCompat disturbSwitch;
    private Uri mediaUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.text_tab_settings));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPage(view);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        final MenuItem editMenuItem = menu.findItem(R.id.action_edit_profile);
        View rootView = editMenuItem.getActionView();
        rootView.setOnClickListener(v -> onOptionsItemSelected(editMenuItem));
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_edit_profile) {
            Logger.d("++ edit button clicked");
            showEditProfileDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendBird.setAutoBackgroundDetection(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_SETTINGS_REQUEST_ID) {
            final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), REQUIRED_PERMISSIONS);
            if (hasPermission) {
                showMediaSelectDialog();
            }
            return;
        }

        SendBird.setAutoBackgroundDetection(true);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                    break;
                case PICK_IMAGE_ACTIVITY_REQUEST_CODE:
                    if (data != null) {
                        this.mediaUri = data.getData();
                    }
                    break;
            }

            if (this.mediaUri != null && getContext() != null) {
                final File file = FileUtils.uriToFile(getContext().getApplicationContext(), mediaUri);
                updateUserProfileImage(file);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
            boolean isAllGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                showMediaSelectDialog();
            } else {
                String[] notGranted = PermissionUtils.getNotGrantedPermissions(getContext(), permissions);
                List<String> deniedList = PermissionUtils.getShowRequestPermissionRationale(getActivity(), permissions);
                if (deniedList.size() == 0 && getActivity() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getActivity().getString(com.sendbird.uikit.R.string.sb_text_dialog_permission_title));
                    builder.setMessage(getPermissionGuildeMessage(getActivity(), notGranted[0]));
                    builder.setPositiveButton(com.sendbird.uikit.R.string.sb_text_go_to_settings, (dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivityForResult(intent, PERMISSION_SETTINGS_REQUEST_ID);
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), com.sendbird.uikit.R.color.secondary_300));
                }
            }
        }
    }

    private void initPage(@NonNull View view) {
        if (getContext() == null) {
            return;
        }

        profileImageView = view.findViewById(R.id.ivProfileView);
        profileImageView.loadImages(Collections.singletonList(PreferenceUtils.getProfileUrl()));
        TextView userIdTextView = view.findViewById(R.id.tvUserId);
        userIdTextView.setText(PreferenceUtils.getUserId());
        nicknameTextView = view.findViewById(R.id.tvNickname);
        nicknameTextView.setText(PreferenceUtils.getNickname());

        View disturbItem = view.findViewById(R.id.itemDisturb);
        disturbItem.setOnClickListener(v -> {
            Logger.d("++ disturb clicked");
            updateDoNotDisturb();
        });

        disturbSwitch = view.findViewById(R.id.scDisturbSwitch);
        SendBird.getDoNotDisturb((b, i, i1, i2, i3, s, e) -> {
            PreferenceUtils.setDoNotDisturb(b);
            disturbSwitch.setChecked(PreferenceUtils.getDoNotDisturb());
        });
        disturbSwitch.setOnClickListener(v -> {
            Logger.d("++ disturb clicked");
            updateDoNotDisturb();
        });

        View signOutItem = view.findViewById(R.id.itemSignOut);
        signOutItem.setOnClickListener(v -> {
            Logger.d("++ sign out clicked");
            signOut();
        });
    }

    private void showEditProfileDialog() {
        DialogHelper.DialogListItem[] items = {
                new DialogHelper.DialogListItem(R.string.text_settings_change_user_nickname),
                new DialogHelper.DialogListItem(R.string.text_settings_change_user_profile_image)
        };

        DialogHelper.showBottomList(getContext(), items, (key) -> {
            if (key == R.string.text_settings_change_user_nickname) {
                Logger.dev("change user nickname");
                DialogHelper.OnEditTextResultListener listener = result -> {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    updateUserNickname(result);
                };

                DialogHelper.showEditText(getContext(),
                        R.string.text_settings_change_user_nickname,
                        R.string.text_settings_change_user_nickname_hint,
                        com.sendbird.uikit.R.string.sb_text_button_cancel, v1 -> {
                        },
                        com.sendbird.uikit.R.string.sb_text_button_save, listener);

            } else if (key == R.string.text_settings_change_user_profile_image) {
                Logger.dev("change user profile");

                final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), REQUIRED_PERMISSIONS);
                if (hasPermission) {
                    showMediaSelectDialog();
                    return;
                }

                requestPermissions(REQUIRED_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
            }
        });
    }

    private void updateUserNickname(@NonNull String nickname) {
        WaitingDialog.show(getContext());
        SendBirdUIKit.updateUserInfo(nickname, SendBirdUIKit.getAdapter().getUserInfo().getProfileUrl(), e -> {
            WaitingDialog.dismiss();
            if (e != null) {
                Logger.e(e);
                return;
            }

            PreferenceUtils.setNickname(nickname);
            nicknameTextView.setText(nickname);
        });
    }

    private void updateUserProfileImage(@NonNull File profileImage) {
        WaitingDialog.show(getContext());
        SendBird.updateCurrentUserInfoWithProfileImage(SendBirdUIKit.getAdapter().getUserInfo().getNickname(), profileImage, e -> {
            WaitingDialog.dismiss();
            if (e != null) {
                Logger.e(e);
                return;
            }

            String profileUrl = SendBird.getCurrentUser().getProfileUrl();
            PreferenceUtils.setProfileUrl(profileUrl);
            profileImageView.loadImages(Collections.singletonList(profileUrl));
        });
    }

    private void updateDoNotDisturb() {
        disturbSwitch.setChecked(!PreferenceUtils.getDoNotDisturb());
        Logger.d("update do not disturb : " + !PreferenceUtils.getDoNotDisturb());
        SendBird.setDoNotDisturb(!PreferenceUtils.getDoNotDisturb(), 0, 0, 23, 59, TimeZone.getDefault().getID(), e -> {
            if (e != null) {
                disturbSwitch.setChecked(PreferenceUtils.getDoNotDisturb());
                return;
            }
            Logger.d("update do not disturb on callback : " + !PreferenceUtils.getDoNotDisturb());
            PreferenceUtils.setDoNotDisturb(!PreferenceUtils.getDoNotDisturb());
        });
    }

    private void signOut() {
        if (getContext() == null) {
            return;
        }
        WaitingDialog.show(getContext());
        PushUtils.unregisterPushHandler(new SendBirdPushHelper.OnPushRequestCompleteListener() {
            @Override
            public void onComplete(boolean isActive, String token) {
                SendBirdUIKit.disconnect(() -> {
                    if (getActivity() == null) {
                        return;
                    }
                    WaitingDialog.dismiss();
                    PreferenceUtils.clearAll();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                });
            }

            @Override
            public void onError(SendBirdException e) {
                WaitingDialog.dismiss();
            }
        });
    }

    private void showMediaSelectDialog() {
        DialogHelper.DialogListItem[] items = {
                new DialogHelper.DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera),
                new DialogHelper.DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery)};

        DialogHelper.showList(getContext(),
                com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image,
                items, (key) -> {
                    try {
                        SendBird.setAutoBackgroundDetection(false);
                        if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera) {
                            takeCamera();
                        } else if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery) {
                            pickImage();
                        }
                    } catch (Exception e) {
                        Logger.e(e);
                    }
                });
    }

    private void takeCamera() {
        if (getContext() == null) {
            return;
        }

        this.mediaUri = FileUtils.createPictureImageUri(getContext());
        Intent intent = IntentUtils.getCameraIntent(getContext(), mediaUri);
        if (IntentUtils.hasIntent(getContext(), intent)) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void pickImage() {
        Intent intent = IntentUtils.getGalleryIntent();
        startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private static String getPermissionGuildeMessage(@NonNull Context context, @NonNull String permission) {
        int textResId;
        if (Manifest.permission.CAMERA.equals(permission)) {
            textResId = com.sendbird.uikit.R.string.sb_text_need_to_allow_permission_camera;
        } else {
            textResId = com.sendbird.uikit.R.string.sb_text_need_to_allow_permission_storage;
        }
        return String.format(Locale.US, context.getString(textResId), ContextUtils.getApplicationName(context));
    }
}
