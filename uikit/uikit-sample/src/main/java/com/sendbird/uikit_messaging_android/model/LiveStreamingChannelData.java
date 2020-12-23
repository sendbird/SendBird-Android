package com.sendbird.uikit_messaging_android.model;

import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit_messaging_android.consts.StringSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LiveStreamingChannelData {
    private final String name;
    private final List<String> tags = new ArrayList<>();
    private final Creator creator;
    private final String thumbnailUrl;
    private final String liveUrl;

    public LiveStreamingChannelData(JSONObject jsonObject) throws JSONException {
        this.name = jsonObject.optString(StringSet.name);
        this.creator = jsonObject.has(StringSet.creator_info) ? new Creator(jsonObject.getJSONObject(StringSet.creator_info)) : null;
        JSONArray tagsJsonArray = jsonObject.optJSONArray(StringSet.tags);
        if (tagsJsonArray != null) {
            for (int i = 0; i < tagsJsonArray.length(); i++) {
                this.tags.add(tagsJsonArray.opt(i).toString());
            }
        }

        this.thumbnailUrl = jsonObject.optString(StringSet.thumbnail_url);
        this.liveUrl = jsonObject.optString(StringSet.live_channel_url);
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return tags;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getLiveUrl() {
        return liveUrl;
    }

    private static class Creator implements UserInfo {
        final String userId;
        final String nickname;
        final String profileUrl;

        public Creator(JSONObject jsonObject) {
            userId = jsonObject.optString(StringSet.id);
            nickname = jsonObject.optString(StringSet.name);
            profileUrl = jsonObject.optString(StringSet.profile_url);
        }

        @Override
        public String getUserId() {
            return userId;
        }

        @Override
        public String getNickname() {
            return nickname;
        }

        @Override
        public String getProfileUrl() {
            return profileUrl;
        }
    }
}
