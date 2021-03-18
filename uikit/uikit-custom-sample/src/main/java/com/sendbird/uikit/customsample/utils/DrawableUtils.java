package com.sendbird.uikit.customsample.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.sendbird.uikit.R;

public class DrawableUtils {
    public static Drawable createDividerDrawable(int height, int color) {
        GradientDrawable divider = new GradientDrawable();
        divider.setShape(GradientDrawable.RECTANGLE);
        divider.setSize(0, height);
        divider.setColor(color);
        return divider;
    }

    public static Drawable createRoundedRectrangle(float radius, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radius);
        drawable.setColor(color);
        return drawable;
    }

    public static Drawable setTintList(Context context, int resId, int colorRes) {
        if (colorRes == 0) {
            return AppCompatResources.getDrawable(context, resId);
        }
        return setTintList(AppCompatResources.getDrawable(context, resId), AppCompatResources.getColorStateList(context, colorRes));
    }

    public static Drawable setTintList(Context context, Drawable drawable, int colorRes) {
        if (colorRes == 0) {
            return drawable;
        }
        return setTintList(drawable, AppCompatResources.getColorStateList(context, colorRes));
    }

    public static Drawable setTintList(Context context, int resId, ColorStateList colorStateList) {
        return setTintList(AppCompatResources.getDrawable(context, resId), colorStateList);
    }

    public static Drawable setTintList(Drawable drawable, ColorStateList colorStateList) {
        if (drawable == null || colorStateList == null) {
            return drawable;
        }
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(drawable, colorStateList);
        return drawable.mutate();
    }

    public static Drawable createOvalIcon(@NonNull Context context, @ColorRes int backgroundColor,
                                          @DrawableRes int iconRes, @ColorRes int iconTint) {
        return createOvalIcon(context, backgroundColor, 255, iconRes, iconTint);
    }

    public static Drawable createOvalIcon(@NonNull Context context, @ColorRes int backgroundColor, int backgroundAlpha,
                                          @DrawableRes int iconRes, @ColorRes int iconTint) {
        ShapeDrawable ovalBackground = new ShapeDrawable(new OvalShape());
        ovalBackground.getPaint().setColor(context.getResources().getColor(backgroundColor));
        ovalBackground.getPaint().setAlpha(backgroundAlpha);
        Drawable icon = setTintList(context, iconRes, iconTint);
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_24);
        return createLayerIcon(ovalBackground, icon, inset);
    }

    public static Drawable createLayerIcon(Drawable background, Drawable icon, int inset) {
        Drawable[] layer = {background, icon};
        LayerDrawable layerDrawable = new LayerDrawable(layer);
        layerDrawable.setLayerInset(1, inset, inset, inset, inset);
        return layerDrawable;
    }
}
