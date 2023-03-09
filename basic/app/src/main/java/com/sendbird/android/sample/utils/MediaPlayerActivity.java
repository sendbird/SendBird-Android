/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sendbird.android.sample.utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import com.sendbird.android.sample.R;

public class MediaPlayerActivity extends Activity implements
        OnBufferingUpdateListener, OnCompletionListener,
        OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback {

    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private String url;

    private ProgressBar progressBar;
    private View container;

    private int videoWidth;
    private int videoHeight;

    private boolean isVideoReadyToBePlayed = false;
    private boolean isVideoSizeKnown = false;
    private boolean isContainerSizeKnown = false;

    private boolean isPaused = false;
    private int currentPosition = -1;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_media_player);

        surfaceView = (SurfaceView) findViewById(R.id.surface);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        Bundle extras = getIntent().getExtras();
        url = extras.getString("url");

        progressBar.setVisibility(View.VISIBLE);
        initToolbar();
    }

    private void initToolbar() {
        container = findViewById(R.id.layout_media_player);
        setContainerLayoutListener(false);
    }

    private void setContainerLayoutListener(final boolean screenRotated) {
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                isContainerSizeKnown = true;
                if (screenRotated) {
                    setVideoSize();
                } else {
                    tryToStartVideoPlayback();
                }
            }
        });
    }

    private void playVideo() {
        progressBar.setVisibility(View.VISIBLE);
        doCleanUp();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    public void onPrepared(MediaPlayer mediaplayer) {
        isVideoReadyToBePlayed = true;
        tryToStartVideoPlayback();
    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }

        videoWidth = width;
        videoHeight = height;
        isVideoSizeKnown = true;
        tryToStartVideoPlayback();
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        playVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            currentPosition = mediaPlayer.getCurrentPosition();
            isPaused = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        doCleanUp();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void doCleanUp() {
        videoWidth = 0;
        videoHeight = 0;

        isVideoReadyToBePlayed = false;
        isVideoSizeKnown = false;
    }

    private void tryToStartVideoPlayback() {
        if (isVideoReadyToBePlayed && isVideoSizeKnown && isContainerSizeKnown) {
            startVideoPlayback();
        }
    }

    private void startVideoPlayback() {
        progressBar.setVisibility(View.INVISIBLE);
        if (!mediaPlayer.isPlaying()) {
            surfaceHolder.setFixedSize(videoWidth, videoHeight);
            setVideoSize();

            if (isPaused) {
                mediaPlayer.seekTo(currentPosition);
                isPaused = false;
            }
            mediaPlayer.start();
        }
    }

    private void setVideoSize() {
        try {
            int videoWidth = mediaPlayer.getVideoWidth();
            int videoHeight = mediaPlayer.getVideoHeight();
            float videoProportion = (float) videoWidth / (float) videoHeight;

            int videoWidthInContainer = container.getWidth();
            int videoHeightInContainer = container.getHeight();
            float videoInContainerProportion = (float) videoWidthInContainer / (float) videoHeightInContainer;

            LayoutParams layoutParams = surfaceView.getLayoutParams();
            if (videoProportion > videoInContainerProportion) {
                layoutParams.width = videoWidthInContainer;
                layoutParams.height = (int) ((float) videoWidthInContainer / videoProportion);
            } else {
                layoutParams.width = (int) (videoProportion * (float) videoHeightInContainer);
                layoutParams.height = videoHeightInContainer;
            }
            surfaceView.setLayoutParams(layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContainerLayoutListener(true);
    }
}