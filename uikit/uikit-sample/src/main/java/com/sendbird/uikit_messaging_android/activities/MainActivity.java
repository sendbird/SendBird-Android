package com.sendbird.uikit_messaging_android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sendbird.android.GroupChannelTotalUnreadMessageCountParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.fragments.ChannelListFragment;
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.fragments.SettingsFragment;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.widgets.CustomTabView;

import java.util.List;
import java.util.Map;

import static com.sendbird.uikit_messaging_android.consts.StringSet.PUSH_REDIRECT_CHANNEL;

public class MainActivity extends AppCompatActivity {
    private static final String USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY";
    private CustomTabView unreadCountTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPage();
    }

    private void initPage() {
        ViewPager2 mainPage = findViewById(R.id.vpMain);
        mainPage.setAdapter(new MainAdapter(getSupportFragmentManager(), getLifecycle()));

        TabLayout tabLayout = findViewById(R.id.tlMain);
        boolean isDarkMode = PreferenceUtils.isUsingDarkTheme();
        int backgroundRedId = isDarkMode ? R.color.background_600 : R.color.background_100;
        tabLayout.setBackgroundResource(backgroundRedId);

        new TabLayoutMediator(tabLayout, mainPage, (tab, position) -> {
                if (position == 0) {
                    unreadCountTab = new CustomTabView(this);
                    unreadCountTab.setBadgeVisibility(View.GONE);
                    unreadCountTab.setTitle(getString(R.string.text_tab_channels));
                    unreadCountTab.setIcon(R.drawable.icon_chat_filled);
                    tab.setCustomView(unreadCountTab);
                } else {
                    CustomTabView settingsTab = new CustomTabView(this);
                    settingsTab.setBadgeVisibility(View.GONE);
                    settingsTab.setTitle(getString(R.string.text_tab_settings));
                    settingsTab.setIcon(R.drawable.icon_settings_filled);
                    tab.setCustomView(settingsTab);
                }
            }
        ).attach();

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
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(PUSH_REDIRECT_CHANNEL, channelUrl);
        return intent;
    }

    private void redirectChannelIfNeeded(Intent intent) {
        if (intent == null) return;
        //if (SendBird.getCurrentUser() == null) return;

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            getIntent().removeExtra(PUSH_REDIRECT_CHANNEL);
        }
        if (intent.hasExtra(PUSH_REDIRECT_CHANNEL)) {
            String channelUrl = intent.getStringExtra(PUSH_REDIRECT_CHANNEL);
            startActivity(ChannelActivity.newIntent(this, channelUrl));
            intent.removeExtra(PUSH_REDIRECT_CHANNEL);
        }
    }

    private static class MainAdapter extends FragmentStateAdapter {
        private static final int PAGE_SIZE = 2;

        public MainAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new ChannelListFragment.Builder()
                        .setUseHeader(true)
                        .setUseHeaderLeftButton(false)
                        .build();
            } else {
                return new SettingsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return PAGE_SIZE;
        }
    }
}
