package com.sendbird.uikit.customsample.openchannel;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.interfaces.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

abstract public class OpenChannelListAdapter<VH extends OpenChannelListViewHolder> extends RecyclerView.Adapter<VH> {
    protected List<OpenChannel> openChannelList = new ArrayList<>();
    private List<ChannelInfo> cachedOpenChannelList = new ArrayList<>();
    protected OnItemClickListener<OpenChannel> itemClickListener;

    public OpenChannelListAdapter() {
        setHasStableIds(true);
    }

    @Override
    public void onBindViewHolder(@NonNull OpenChannelListViewHolder holder, int position) {
        OpenChannel openChannel = getItem(position);
        holder.bind(openChannel);
        if (itemClickListener != null) {
            holder.itemView.setOnClickListener(v -> itemClickListener.onItemClick(v, position, openChannel));
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return openChannelList.size();
    }

    private OpenChannel getItem(int position) {
        return openChannelList.get(position);
    }

    public void setItems(List<OpenChannel> items) {
        final OpenChannelDiffCallback diffCallback = new OpenChannelDiffCallback(this.cachedOpenChannelList, items);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.openChannelList.clear();
        this.openChannelList.addAll(items);
        this.cachedOpenChannelList = ChannelInfo.toChannelInfoList(items);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setOnItemClickListener(OnItemClickListener<OpenChannel> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    private static class OpenChannelDiffCallback extends DiffUtil.Callback {
        private final List<ChannelInfo> oldChannelList;
        private final List<OpenChannel> newChannelList;

        OpenChannelDiffCallback(@NonNull List<ChannelInfo> oldChannelList, @NonNull List<OpenChannel> newChannelList) {
            this.oldChannelList = oldChannelList;
            this.newChannelList = newChannelList;
        }

        @Override
        public int getOldListSize() {
            return oldChannelList.size();
        }

        @Override
        public int getNewListSize() {
            return newChannelList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            ChannelInfo oldChannel = oldChannelList.get(oldItemPosition);
            OpenChannel newChannel = newChannelList.get(newItemPosition);
            if (!newChannel.getUrl().equals(oldChannel.getChannelUrl())) {
                return false;
            }

            return newChannel.getCreatedAt() == oldChannel.getCreatedAt();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ChannelInfo oldChannel = oldChannelList.get(oldItemPosition);
            OpenChannel newChannel = newChannelList.get(newItemPosition);

            if (!areItemsTheSame(oldItemPosition, newItemPosition)) {
                return false;
            }

            if (oldChannel.getParticipantCount() != newChannel.getParticipantCount()) {
                return false;
            }

            if (oldChannel.isFrozen() != newChannel.isFrozen()) {
                return false;
            }

            String channelName = oldChannel.getChannelName() != null ? oldChannel.getChannelName() : "";
            if (!channelName.equals(newChannel.getName())) {
                return false;
            }

            String coverUrl = oldChannel.getCoverImageUrl() != null ? oldChannel.getCoverImageUrl() : "";
            return coverUrl.equals(newChannel.getCoverUrl());
        }
    }

    private static class ChannelInfo {
        private final String channelUrl;
        private final long createdAt;
        private final int participantCount;
        private final String channelName;
        private final String coverImageUrl;
        private final boolean isFrozen;

        ChannelInfo(@NonNull OpenChannel channel) {
            this.channelUrl = channel.getUrl();
            this.createdAt = channel.getCreatedAt();
            this.participantCount = channel.getParticipantCount();
            this.channelName = channel.getName();
            this.coverImageUrl = channel.getCoverUrl();
            this.isFrozen = channel.isFrozen();
        }

        public String getChannelUrl() {
            return channelUrl;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public int getParticipantCount() {
            return participantCount;
        }

        String getChannelName() {
            return channelName;
        }

        public String getCoverImageUrl() {
            return coverImageUrl;
        }

        public boolean isFrozen() {
            return isFrozen;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChannelInfo that = (ChannelInfo) o;

            if (createdAt != that.createdAt) return false;
            if (channelUrl != null && channelUrl.equals(that.channelUrl)) return false;
            if (participantCount != that.participantCount) return false;
            if (channelName != null && channelName.equals(that.channelName)) return false;
            if (coverImageUrl != null && coverImageUrl.equals(that.coverImageUrl)) return false;
            return isFrozen == that.isFrozen;
        }

        @Override
        public int hashCode() {
            int result = channelUrl != null ? channelUrl.hashCode() : 0;
            result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
            result = 31 * result + participantCount;
            result = 31 * result + (channelName != null ? channelName.hashCode() : 0);
            result = 31 * result + (coverImageUrl != null ? coverImageUrl.hashCode() : 0);
            result = 31 * result + (isFrozen ? 1 : 0);
            return result;
        }

        @NonNull
        @Override
        public String toString() {
            return "ChannelInfo{" +
                    "channelUrl='" + channelUrl + '\'' +
                    ", createdAt=" + createdAt +
                    ", participantCount=" + participantCount +
                    ", channelName='" + channelName + '\'' +
                    ", coverImageUrl='" + coverImageUrl + '\'' +
                    ", isFrozen=" + isFrozen +
                    '}';
        }

        static List<ChannelInfo> toChannelInfoList(@NonNull List<OpenChannel> channelList) {
            List<ChannelInfo> results = new ArrayList<>();
            for (OpenChannel channel : channelList) {
                results.add(new ChannelInfo(channel));
            }
            return results;
        }
    }
}
