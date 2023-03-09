package com.sendbird.android.sample.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class UrlPreviewInfo {

    private final String url;
    private final String siteName;
    private final String title;
    private final String description;
    private final String imageUrl;

    public UrlPreviewInfo(String url, String siteName, String title, String description, String imageUrl) {
        this.url = url;
        this.siteName = siteName;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public UrlPreviewInfo(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        url = jsonObject.getString("url");
        siteName = jsonObject.getString("site_name");
        title = jsonObject.getString("title");
        description = jsonObject.getString("description");
        imageUrl = jsonObject.getString("image");
    }

    public String toJsonString() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", url);
        jsonObject.put("site_name", siteName);
        jsonObject.put("title", title);
        jsonObject.put("description", description);
        jsonObject.put("image", imageUrl);

        return jsonObject.toString();
    }


    public String getSiteName() {
        return siteName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

