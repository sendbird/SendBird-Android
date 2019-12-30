package com.sendbird.android.sample.utils;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sendbird.android.sample.R;

public class PhotoViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        String url = getIntent().getStringExtra("url");
        String type = getIntent().getStringExtra("type");

        ImageView imageView = (ImageView) findViewById(R.id.main_image_view);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);


        progressBar.setVisibility(View.VISIBLE);

        if (type != null && type.toLowerCase().contains("gif")) {
            ImageUtils.displayGifImageFromUrl(this, url, imageView, new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            });
        } else {
            ImageUtils.displayImageFromUrl(this, url, imageView, new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            });
        }
    }
}
