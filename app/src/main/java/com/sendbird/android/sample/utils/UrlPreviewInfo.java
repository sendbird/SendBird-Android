package com.sendbird.android.sample.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class UrlPreviewInfo {
    private final String mUrl;
    private final String mSiteName;
    private final String mTitle;
    private final String mDescription;
    private final String mImageUrl;

    public UrlPreviewInfo(String url, String siteName, String title, String description, String imageUrl) {
        mUrl = url;
        mSiteName = siteName;
        mTitle = title;
        mDescription = description;
        mImageUrl = imageUrl;
    }

    public UrlPreviewInfo(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        mUrl = jsonObject.getString("url");
        mSiteName = jsonObject.getString("site_name");
        mTitle = jsonObject.getString("title");
        mDescription = jsonObject.getString("description");
        mImageUrl = jsonObject.getString("image");
    }

    public String toJsonString() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", mUrl);
        jsonObject.put("site_name", mSiteName);
        jsonObject.put("title", mTitle);
        jsonObject.put("description", mDescription);
        jsonObject.put("image", mImageUrl);

        return jsonObject.toString();
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

