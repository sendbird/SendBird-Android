package com.sendbird.uikit.customsample.models;

import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.interfaces.UserInfo;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Creator)) return false;
            Creator creator = (Creator) o;

            if (!userId.equals(creator.userId)) {
                return false;
            }

            if (!nickname.equals(creator.nickname)) {
                return false;
            }

            if (!profileUrl.equals(creator.profileUrl)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = result * 31 + (userId != null ? userId.hashCode() : 0);
            result = result * 31 + (nickname != null ? nickname.hashCode() : 0);
            result = result * 31 + (profileUrl != null ? profileUrl.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Creator{" +
                    "userId='" + userId + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", profileUrl='" + profileUrl + '\'' +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiveStreamingChannelData)) return false;
        LiveStreamingChannelData that = (LiveStreamingChannelData) o;

        if (!name.equals(that.name)) {
            return false;
        }

        if (!tags.equals(that.tags)) {
            return false;
        }

        if (!creator.equals(that.creator)) {
            return false;
        }

        if (!thumbnailUrl.equals(that.thumbnailUrl)) {
            return false;
        }

        if (!liveUrl.equals(that.liveUrl)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + (name != null ? name.hashCode() : 0);
        result = result * 31 + (tags != null ? tags.hashCode() : 0);
        result = result * 31 + (creator != null ? creator.hashCode() : 0);
        result = result * 31 + (thumbnailUrl != null ? thumbnailUrl.hashCode() : 0);
        result = result * 31 + (liveUrl != null ? liveUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LiveStreamingChannelData{" +
                "name='" + name + '\'' +
                ", tags=" + tags +
                ", creator=" + creator +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", liveUrl='" + liveUrl + '\'' +
                '}';
    }
}
