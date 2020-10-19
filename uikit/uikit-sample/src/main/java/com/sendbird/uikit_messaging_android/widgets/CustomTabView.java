package com.sendbird.uikit_messaging_android.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;

public class CustomTabView extends FrameLayout {
    private int tintColorRedId;
    private TextView badgeView;
    private ImageView iconView;
    private TextView titleView;

    public CustomTabView(@NonNull Context context) {
        this(context, null);
    }

    public CustomTabView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTabView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(@NonNull Context context) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_custom_tab, this, true);
        badgeView = view.findViewById(R.id.badge);
        iconView = view.findViewById(R.id.ivIcon);
        titleView = view.findViewById(R.id.tvTitle);

        boolean isDarkMode = PreferenceUtils.isUsingDarkTheme();
        tintColorRedId = isDarkMode ? R.color.selector_tab_tint_dark : R.color.selector_tab_tint;

        int badgeTextAppearance = isDarkMode ? R.style.SendbirdCaption4OnLight01 : R.style.SendbirdCaption4OnDark01;
        int badgeBackgroundRes = isDarkMode ? R.drawable.shape_badge_background_dark : R.drawable.shape_badge_background;
        int titleTextAppearance = isDarkMode ? R.style.SendbirdCaption2Primary200 : R.style.SendbirdCaption2Primary300;

        badgeView.setTextAppearance(context, badgeTextAppearance);
        badgeView.setBackgroundResource(badgeBackgroundRes);
        titleView.setTextAppearance(context, titleTextAppearance);
        titleView.setTextColor(context.getResources().getColorStateList(tintColorRedId));
    }

    public void setBadgeVisibility(int visibility) {
        badgeView.setVisibility(visibility);
    }

    public void setBadgeCount(String countString) {
        badgeView.setText(countString);
    }

    public void setIcon(@DrawableRes int iconResId) {
        iconView.setImageDrawable(DrawableUtils.setTintList(getContext(), iconResId, tintColorRedId));
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }
}
