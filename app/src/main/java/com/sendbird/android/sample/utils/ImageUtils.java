package com.sendbird.android.sample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;


public class ImageUtils {

    // Prevent instantiation
    private ImageUtils() {

    }

    /**
     * Crops image into a circle that fits within the ImageView.
     */
    public static void displayRoundImageFromUrl(final Context context, final String url, final ImageView imageView) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .centerCrop()
                .dontAnimate()
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static void displayImageFromUrl(final Context context, final String url,
                                           final ImageView imageView) {
        displayImageFromUrl(context, url, imageView, null);
    }

    /**
     * Displays an image from a URL in an ImageView.
     */
    public static void displayImageFromUrl(final Context context, final String url,
                                           final ImageView imageView, RequestListener listener) {
        if (listener != null) {
            Glide.with(context)
                    .load(url)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(listener)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(url)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);
        }
    }

    /**
     * Displays an image from a URL in an ImageView.
     * If the image is loading or nonexistent, displays the specified placeholder image instead.
     */
    public static void displayImageFromUrlWithPlaceHolder(final Context context, final String url,
                                                          final ImageView imageView,
                                                          int placeholderResId) {
        Glide.with(context)
                .load(url)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placeholderResId)
                .into(imageView);
    }

    /**
     * Displays an image from a URL in an ImageView.
     */
    public static void displayGifImageFromUrl(Context context, String url, ImageView imageView, RequestListener listener) {
        if (listener != null) {
            Glide.with(context)
                    .load(url)
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .dontAnimate()
                    .listener(listener)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(url)
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .dontAnimate()
                    .into(imageView);
        }
    }

    /**
     * Displays an GIF image from a URL in an ImageView.
     */
    public static void displayGifImageFromUrl(Context context, String url, ImageView imageView, String thumbnailUrl) {

        if (thumbnailUrl != null) {
            Glide.with(context)
                    .load(url)
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .dontAnimate()
                    .thumbnail(Glide.with(context).load(thumbnailUrl).asGif())
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(url)
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .dontAnimate()
                    .into(imageView);
        }
    }
}
