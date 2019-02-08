package com.sendbird.android.sample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;


public class ImageUtils {

    // Prevent instantiation
    private ImageUtils() {

    }

    /**
     * Crops image into a circle that fits within the ImageView.
     */
    public static void displayRoundImageFromUrl(final Context context, final String url, final ImageView imageView) {
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .dontAnimate();

        Glide.with(context)
                .asBitmap()
                .apply(myOptions)
                .load(url)
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
                                           final ImageView imageView, Drawable placeholderDrawable) {
        displayImageFromUrl(context, url, imageView, placeholderDrawable, null);
    }

    /**
     * Displays an image from a URL in an ImageView.
     */
    public static void displayImageFromUrl(final Context context, final String url,
                                           final ImageView imageView, Drawable placeholderDrawable, RequestListener listener) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholderDrawable);

        if (listener != null) {
            Glide.with(context)
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(imageView);
        }
    }

    public static void displayRoundImageFromUrlWithoutCache(final Context context, final String url,
                                                            final ImageView imageView) {
        displayRoundImageFromUrlWithoutCache(context, url, imageView, null);
    }

    public static void displayRoundImageFromUrlWithoutCache(final Context context, final String url,
                                           final ImageView imageView, RequestListener listener) {
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true);

        if (listener != null) {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            imageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(myOptions)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            imageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }
    }

    /**
     * Displays an image from a URL in an ImageView.
     * If the image is loading or nonexistent, displays the specified placeholder image instead.
     */
    public static void displayImageFromUrlWithPlaceHolder(final Context context, final String url,
                                                          final ImageView imageView,
                                                          int placeholderResId) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholderResId);

        Glide.with(context)
                .load(url)
                .apply(myOptions)
                .into(imageView);
    }

    /**
     * Displays an image from a URL in an ImageView.
     */
    public static void displayGifImageFromUrl(Context context, String url, ImageView imageView, Drawable placeholderDrawable, RequestListener listener) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholderDrawable);

        if (listener != null) {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .into(imageView);
        }
    }

    /**
     * Displays an GIF image from a URL in an ImageView.
     */
    public static void displayGifImageFromUrl(Context context, String url, ImageView imageView, String thumbnailUrl, Drawable placeholderDrawable) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholderDrawable);

        if (thumbnailUrl != null) {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .thumbnail(Glide.with(context).asGif().load(thumbnailUrl))
                    .into(imageView);
        } else {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .into(imageView);
        }
    }
}
