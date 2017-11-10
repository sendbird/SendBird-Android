package com.sendbird.android.sample.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {

    public static final String PREFERENCE_KEY_USER_ID = "userId";
    public static final String PREFERENCE_KEY_NICKNAME = "nickname";
    public static final String PREFERENCE_KEY_PROFILE_URL = "profileUrl";
    public static final String PREFERENCE_KEY_CONNECTED = "connected";

    public static final String PREFERENCE_KEY_NOTIFICATIONS = "notifications";
    public static final String PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS = "notificationsShowPreviews";
    public static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB = "notificationsDoNotDisturb";
    public static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM = "notificationsDoNotDisturbFrom";
    public static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO = "notificationsDoNotDisturbTo";
    public static final String PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT = "channelDistinct";

    // Prevent instantiation
    private PreferenceUtils() {

    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("sendbird", Context.MODE_PRIVATE);
    }

    public static void setUserId(Context context, String userId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREFERENCE_KEY_USER_ID, userId).apply();
    }

    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(PREFERENCE_KEY_USER_ID, "");
    }

    public static void setNickname(Context context, String nickname) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREFERENCE_KEY_NICKNAME, nickname).apply();
    }

    public static String getNickname(Context context) {
        return getSharedPreferences(context).getString(PREFERENCE_KEY_NICKNAME, "");
    }

    public static void setProfileUrl(Context context, String profileUrl) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREFERENCE_KEY_PROFILE_URL, profileUrl).apply();
    }

    public static String getProfileUrl(Context context) {
        return getSharedPreferences(context).getString(PREFERENCE_KEY_PROFILE_URL, "");
    }

    public static void setConnected(Context context, boolean tf) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(PREFERENCE_KEY_CONNECTED, tf).apply();
    }

    public static boolean getConnected(Context context) {
        return getSharedPreferences(context).getBoolean(PREFERENCE_KEY_CONNECTED, false);
    }

    public static void clearAll(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear().apply();
    }

    public static void setNotifications(Context context, boolean notifications) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS, notifications).apply();
    }

    public static boolean getNotifications(Context context) {
        return getSharedPreferences(context).getBoolean(PREFERENCE_KEY_NOTIFICATIONS, true);
    }

    public static void setNotificationsShowPreviews(Context context, boolean notificationsShowPreviews) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, notificationsShowPreviews).apply();
    }

    public static boolean getNotificationsShowPreviews(Context context) {
        return getSharedPreferences(context).getBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, true);
    }

    public static void setNotificationsDoNotDisturb(Context context, boolean notificationsDoNotDisturb) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB, notificationsDoNotDisturb).apply();
    }

    public static boolean getNotificationsDoNotDisturb(Context context) {
        return getSharedPreferences(context).getBoolean(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB, false);
    }

    public static void setNotificationsDoNotDisturbFrom(Context context, String notificationsDoNotDisturbFrom) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM, notificationsDoNotDisturbFrom).apply();
    }

    public static String getNotificationsDoNotDisturbFrom(Context context) {
        return getSharedPreferences(context).getString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM, "");
    }

    public static void setNotificationsDoNotDisturbTo(Context context, String notificationsDoNotDisturbTo) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO, notificationsDoNotDisturbTo).apply();
    }

    public static String getNotificationsDoNotDisturbTo(Context context) {
        return getSharedPreferences(context).getString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO, "");
    }
    public static void setGroupChannelDistinct(Context context, boolean channelDistinct) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT, channelDistinct).apply();
    }

    public static boolean getGroupChannelDistinct(Context context) {
        return getSharedPreferences(context).getBoolean(PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT, true);
    }
}
