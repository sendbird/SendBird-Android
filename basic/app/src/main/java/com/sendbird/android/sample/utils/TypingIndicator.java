package com.sendbird.android.sample.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import java.util.List;


public class TypingIndicator {

    List<ImageView> mImageViewList;
    private int mAnimDuration;

    private AnimatorSet mAnimSet;

    public TypingIndicator(List<ImageView> imageViews, int duration) {
        mImageViewList = imageViews;
        mAnimDuration = duration;
    }

    /**
     * Animates all dots in sequential order.
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void animate() {
        int startDelay = 0;

        mAnimSet = new AnimatorSet();

        for (int i = 0; i < mImageViewList.size(); i++) {
            ImageView dot = mImageViewList.get(i);
//            ValueAnimator bounce = ObjectAnimator.ofFloat(dot, "y", mAnimMagnitude);
            ValueAnimator fadeIn = ObjectAnimator.ofFloat(dot, "alpha", 1f, 0.5f);
            ValueAnimator scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 1f, 0.7f);
            ValueAnimator scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 1f, 0.7f);

            fadeIn.setDuration(mAnimDuration);
            fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
            fadeIn.setRepeatMode(ValueAnimator.REVERSE);
            fadeIn.setRepeatCount(ValueAnimator.INFINITE);

            scaleX.setDuration(mAnimDuration);
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleX.setRepeatMode(ValueAnimator.REVERSE);
            scaleX.setRepeatCount(ValueAnimator.INFINITE);

            scaleY.setDuration(mAnimDuration);
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setRepeatMode(ValueAnimator.REVERSE);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);

//            bounce.setDuration(mAnimDuration);
//            bounce.setInterpolator(new AccelerateDecelerateInterpolator());
//            bounce.setRepeatMode(ValueAnimator.REVERSE);
//            bounce.setRepeatCount(ValueAnimator.INFINITE);

//            mAnimSet.play(bounce).after(startDelay);
            mAnimSet.play(fadeIn).after(startDelay);
            mAnimSet.play(scaleX).with(fadeIn);
            mAnimSet.play(scaleY).with(fadeIn);

            mAnimSet.setStartDelay(500);

            startDelay += (mAnimDuration / (mImageViewList.size() - 1));
        }

        mAnimSet.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void stop() {

        if (mAnimSet == null) {
            return;
        }

        mAnimSet.end();
    }

}
