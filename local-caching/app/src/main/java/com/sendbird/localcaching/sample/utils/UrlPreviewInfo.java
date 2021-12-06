package com.sendbird.localcaching.sample.utils;

import com.sendbird.android.OGMetaData;

public class UrlPreviewInfo {
    private final String mUrl;
    private final String mSiteName;
    private final String mTitle;
    private final String mDescription;
    private final String mImageUrl;

    public UrlPreviewInfo(OGMetaData ogMetaData) {
        mUrl = ogMetaData.getUrl();
        mSiteName = ogMetaData.getUrl();
        mTitle = ogMetaData.getTitle();
        mDescription = ogMetaData.getDescription();
        mImageUrl = ogMetaData.getOGImage().getUrl();
    }

    public String getSiteName() {
        return mSiteName;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getImageUrl() {
        return mImageUrl;
    }
}

