package com.sendbird.uikit.customsample.groupchannel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sendbird.android.GroupChannelTotalUnreadMessageCountParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.SettingsFragment;
import com.sendbird.uikit.customsample.groupchannel.activities.CustomChannelActivity;
import com.sendbird.uikit.customsample.groupchannel.activities.adapters.CustomChannelListAdapter;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomChannelListFragment;
import com.sendbird.uikit.customsample.widgets.CustomTabView;
import com.sendbird.uikit.fragments.ChannelListFragment;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.sendbird.uikit.customsample.consts.StringSet.PUSH_REDIRECT_CHANNEL;

public class GroupChannelMainActivity extends AppCompatActivity {
    private static final String USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY";
    private CustomTabView unreadCountTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPage();
    }

    private void initPage() {
        Toolbar toolbar = findViewById(R.id.tbMain);
        setSupportActionBar(toolbar);

        ViewPager mainPage = findViewById(R.id.vpMain);
        mainPage.setAdapter(new MainAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));

        TabLayout tabLayout = findViewById(R.id.tlMain);
        tabLayout.setupWithViewPager(mainPage);

        unreadCountTab = new CustomTabView(this);
        unreadCountTab.setBadgeVisibility(View.GONE);
        unreadCountTab.setTitle(getString(R.string.text_tab_channels));
        unreadCountTab.setIcon(R.drawable.icon_chat_filled);

        CustomTabView settingsTab = new CustomTabView(this);
        settingsTab.setBadgeVisibility(View.GONE);
        settingsTab.setTitle(getString(R.string.text_tab_settings));
        settingsTab.setIcon(R.drawable.icon_settings_filled);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setCustomView(unreadCountTab);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setCustomView(settingsTab);

        redirectChannelIfNeeded(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SendBird.getTotalUnreadMessageCount(new GroupChannelTotalUnreadMessageCountParams(), (totalCount, e) -> {
            if (e != null) {
                return;
            }

            if (totalCount > 0) {
                unreadCountTab.setBadgeVisibility(View.VISIBLE);
                unreadCountTab.setBadgeCount(totalCount > 99 ?
                        getString(R.string.text_tab_badge_max_count) :
                        String.valueOf(totalCount));
            } else {
                unreadCountTab.setBadgeVisibility(View.GONE);
            }
        });

        SendBird.addUserEventHandler(USER_EVENT_HANDLER_KEY, new SendBird.UserEventHandler() {
            @Override
            public void onFriendsDiscovered(List<User> list) {}

            @Override
            public void onTotalUnreadMessageCountChanged(int totalCount, Map<String, Integer> totalCountByCustomType) {
                if (totalCount > 0) {
                    unreadCountTab.setBadgeVisibility(View.VISIBLE);
                    unreadCountTab.setBadgeCount(totalCount > 99 ?
                            getString(R.string.text_tab_badge_max_count) :
                            String.valueOf(totalCount));
                } else {
                    unreadCountTab.setBadgeVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SendBird.removeUserEventHandler(USER_EVENT_HANDLER_KEY);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        redirectChannelIfNeeded(intent);
    }

    public static Intent newRedirectToChannelIntent(@NonNull Context context, @NonNull String channelUrl) {
        Intent intent = new Intent(context, GroupChannelMainActivity.class);
        intent.putExtra(PUSH_REDIRECT_CHANNEL, channelUrl);
        return intent;
    }

    private void redirectChannelIfNeeded(Intent intent) {
        if (intent == null) return;

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            getIntent().removeExtra(PUSH_REDIRECT_CHANNEL);
        }
        if (intent.hasExtra(PUSH_REDIRECT_CHANNEL)) {
            String channelUrl = intent.getStringExtra(PUSH_REDIRECT_CHANNEL);
            showCustomChannelActivity(channelUrl);
            intent.removeExtra(PUSH_REDIRECT_CHANNEL);
        }
    }

    private void showCustomChannelActivity(String channelUrl) {
        Intent intent = CustomChannelActivity.newIntentFromCustomActivity(GroupChannelMainActivity.this, CustomChannelActivity.class, channelUrl);
        startActivity(intent);
    }

    private class MainAdapter extends FragmentPagerAdapter {
        private static final int PAGE_SIZE = 2;

        public MainAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ChannelListFragment.Builder(R.style.CustomChannelListStyle)
                        .setCustomChannelListFragment(new CustomChannelListFragment())
                        .setUseHeader(false)
                        .setHeaderTitle(null)
                        .setUseHeaderLeftButton(true)
                        .setUseHeaderRightButton(true)
                        .setHeaderLeftButtonIconResId(R.drawable.icon_arrow_left)
                        .setHeaderRightButtonIconResId(R.drawable.icon_create)
                        .setHeaderLeftButtonListener(null)
                        .setHeaderRightButtonListener(null)
                        .setChannelListAdapter(new CustomChannelListAdapter())
                        .setItemClickListener((view, i, channel) -> showCustomChannelActivity(channel.getUrl()))
                        .setItemLongClickListener(null)
                        .setGroupChannelListQuery(null)
                        .build();
            } else {
                return new SettingsFragment();
            }
        }

        @Override
        public int getCount() {
            return PAGE_SIZE;
        }
    }
}
