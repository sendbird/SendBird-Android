package com.sendbird.uikit.customsample.openchannel.community;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentManager;

import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.OpenChannelSettingsActivity;
import com.sendbird.uikit.activities.ParticipantsListActivity;
import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.models.CustomMessageType;
import com.sendbird.uikit.customsample.openchannel.CustomOpenChannelFragment;
import com.sendbird.uikit.customsample.openchannel.CustomOpenChannelMessageListAdapter;
import com.sendbird.uikit.customsample.openchannel.channelsettings.CustomOpenChannelSettingsActivity;
import com.sendbird.uikit.customsample.openchannel.participants.CustomParticipantsListActivity;
import com.sendbird.uikit.fragments.OpenChannelFragment;
import com.sendbird.uikit.utils.ContextUtils;


/**
 * Activity displays a list of messages from a channel.
 */
public class CommunityActivity extends AppCompatActivity {

    private final CustomOpenChannelFragment customOpenChannelFragment = new CustomOpenChannelFragment();

    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl) {
        Intent intent = new Intent(context, CommunityActivity.class);
        intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SendBirdUIKit.isDarkMode() ? R.style.SendBird_Dark : R.style.SendBird);
        setContentView(R.layout.activity_community);

        String url = getIntent().getStringExtra(StringSet.KEY_CHANNEL_URL);
        if (TextUtils.isEmpty(url)) {
            ContextUtils.toastError(this, R.string.sb_text_error_get_channel);
        } else {
            OpenChannel.getChannel(url, (openChannel, e) -> {
                if (e != null) {
                    ContextUtils.toastError(CommunityActivity.this, R.string.sb_text_error_get_channel);
                    return;
                }

                OpenChannelFragment fragment = createOpenChannelFragment(openChannel);
                FragmentManager manager = getSupportFragmentManager();
                manager.popBackStack();
                manager.beginTransaction()
                        .replace(R.id.sb_fragment_container, fragment)
                        .commit();
            });
        }
    }

    protected OpenChannelFragment createOpenChannelFragment(@NonNull OpenChannel openChannel) {
        final int themeResId = R.style.CustomMessageListStyle;
        final boolean useMessageGroupUI = false;
        final int headerRightButtonIconResId = openChannel.isOperator(SendBird.getCurrentUser()) ?
                com.sendbird.uikit.R.drawable.icon_info : com.sendbird.uikit.R.drawable.icon_members;

        return new OpenChannelFragment.Builder(openChannel.getUrl(), themeResId)
                .setUseHeader(true)
                .setUseHeaderLeftButton(true)
                .setHeaderTitle(null)
                .setHeaderDescription(null)
                .setKeyboardDisplayType(KeyboardDisplayType.Plane)
                .setCustomOpenChannelFragment(customOpenChannelFragment)
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setHeaderLeftButtonIcon(R.drawable.icon_arrow_left, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setHeaderRightButtonIcon(headerRightButtonIconResId, AppCompatResources.getColorStateList(this, R.color.ondark_01))
                .setInputLeftButtonIconResId(R.drawable.icon_add)
                .setInputRightButtonIconResId(R.drawable.icon_send)
                .setInputHint("Type here")
                .setHeaderLeftButtonListener(null)
                .setHeaderRightButtonListener(v -> clickHeaderRightButton(openChannel))
                .setOpenChannelMessageListAdapter(new CustomOpenChannelMessageListAdapter(useMessageGroupUI))
                .setItemClickListener(null)
                .setItemLongClickListener(null)
                .setInputLeftButtonListener(v -> showMessageTypeDialog())
                .setMessageListParams(null)
                .setUseMessageGroupUI(useMessageGroupUI)
                .setOnProfileClickListener(null)
                .setUseUserProfile(false)
                .setLoadingDialogHandler(null)
                .build();
    }

    private void showMessageTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick message type")
                .setMultiChoiceItems(new String[]{com.sendbird.uikit.customsample.consts.StringSet.highlight},
                        new boolean[]{customOpenChannelFragment.getCustomMessageType().equals(CustomMessageType.HIGHLIGHT)},
                        (dialog, which, isChecked) -> {
                            final CustomMessageType type = isChecked ? CustomMessageType.HIGHLIGHT : CustomMessageType.NONE;
                            customOpenChannelFragment.setCustomMessageType(type);
                        })
                .create()
                .show();
    }

    private void clickHeaderRightButton(@NonNull OpenChannel openChannel) {
        Intent intent;
        if (openChannel.isOperator(SendBird.getCurrentUser())) {
            intent = OpenChannelSettingsActivity.newIntentFromCustomActivity(CommunityActivity.this, CustomOpenChannelSettingsActivity.class, openChannel.getUrl());
        } else {
            intent = ParticipantsListActivity.newIntentFromCustomActivity(CommunityActivity.this, CustomParticipantsListActivity.class, openChannel.getUrl());
        }
        startActivity(intent);
    }
}
