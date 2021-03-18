package com.sendbird.uikit.customsample.openchannel;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.SettingsFragment;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.databinding.ActivityOpenChannelMainBinding;
import com.sendbird.uikit.customsample.openchannel.community.CommunityListFragment;
import com.sendbird.uikit.customsample.openchannel.livestream.LiveStreamListFragment;
import com.sendbird.uikit.customsample.utils.PreferenceUtils;
import com.sendbird.uikit.customsample.widgets.CustomTabView;

import java.util.Objects;

public class OpenChannelMainActivity extends AppCompatActivity {
    private ActivityOpenChannelMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeResId = SendBirdUIKit.getDefaultThemeMode().getResId();
        setTheme(themeResId);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_open_channel_main);
        initPage();
    }

    private void initPage() {
        setSupportActionBar(binding.tbMain);
        binding.vpMain.setAdapter(new MainAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));

        int backgroundRedId = R.color.background_50;
        binding.tlMain.setBackgroundResource(backgroundRedId);
        binding.tlMain.setupWithViewPager(binding.vpMain);

        CustomTabView liveStreamTab = new CustomTabView(this);
        liveStreamTab.setBadgeVisibility(View.GONE);
        liveStreamTab.setTitle(getString(R.string.text_live_streams));
        liveStreamTab.setIcon(R.drawable.icon_streaming);

        CustomTabView communityTab = new CustomTabView(this);
        communityTab.setBadgeVisibility(View.GONE);
        communityTab.setTitle(getString(R.string.text_community));
        communityTab.setIcon(R.drawable.icon_channels);

        CustomTabView settingsTab = new CustomTabView(this);
        settingsTab.setBadgeVisibility(View.GONE);
        settingsTab.setTitle(getString(R.string.text_tab_settings));
        settingsTab.setIcon(R.drawable.icon_settings_filled);

        Objects.requireNonNull(binding.tlMain.getTabAt(0)).setCustomView(liveStreamTab);
        Objects.requireNonNull(binding.tlMain.getTabAt(1)).setCustomView(communityTab);
        Objects.requireNonNull(binding.tlMain.getTabAt(2)).setCustomView(settingsTab);

        binding.tvDescription.setVisibility(View.VISIBLE);
        binding.tvDescription.setText(R.string.text_live_streaming_description);
        setActionBarTitle(getString(R.string.text_live_streams));

        binding.vpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    binding.tvDescription.setVisibility(View.VISIBLE);
                    binding.tvDescription.setText(R.string.text_live_streaming_description);
                    setActionBarTitle(getString(R.string.text_live_streams));
                } else if (position == 1) {
                    binding.tvDescription.setVisibility(View.GONE);
                    setActionBarTitle(getString(R.string.text_community));
                } else {
                    binding.tvDescription.setVisibility(View.GONE);
                    setActionBarTitle(getString(R.string.text_tab_settings));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void setActionBarTitle(String title) {
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setTitle(title);
    }

    private static class MainAdapter extends FragmentPagerAdapter {
        private static final int PAGE_SIZE = 3;

        public MainAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new LiveStreamListFragment();
            } else if (position == 1) {
                return new CommunityListFragment();
            } else {
                SettingsFragment fragment = new SettingsFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean(StringSet.SETTINGS_USE_DO_NOT_DISTURB, false);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getCount() {
            return PAGE_SIZE;
        }
    }
}
