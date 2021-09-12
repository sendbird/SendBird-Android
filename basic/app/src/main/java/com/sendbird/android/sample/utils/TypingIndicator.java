package com.sendbird.android.sample.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import java.util.List;


public class TypingIndicator {

    List<ImageView> imageViewList;
    private final int animDuration;

    public TypingIndicator(List<ImageView> imageViews, int duration) {
        imageViewList = imageViews;
        animDuration = duration;
    }

    /**
     * Animates all dots in sequential order.
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void animate() {
        int startDelay = 0;

        AnimatorSet animSet = new AnimatorSet();

        for (int i = 0; i < imageViewList.size(); i++) {
            ImageView dot = imageViewList.get(i);
            ValueAnimator fadeIn = ObjectAnimator.ofFloat(dot, "alpha", 1f, 0.5f);
            ValueAnimator scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 1f, 0.7f);
            ValueAnimator scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 1f, 0.7f);

            fadeIn.setDuration(animDuration);
            fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
            fadeIn.setRepeatMode(ValueAnimator.REVERSE);
            fadeIn.setRepeatCount(ValueAnimator.INFINITE);

            scaleX.setDuration(animDuration);
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleX.setRepeatMode(ValueAnimator.REVERSE);
            scaleX.setRepeatCount(ValueAnimator.INFINITE);

            scaleY.setDuration(animDuration);
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setRepeatMode(ValueAnimator.REVERSE);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);

            animSet.play(fadeIn).after(startDelay);
            animSet.play(scaleX).with(fadeIn);
            animSet.play(scaleY).with(fadeIn);

            animSet.setStartDelay(500);

            startDelay += (animDuration / (imageViewList.size() - 1));
        }
        animSet.start();
    }

}
