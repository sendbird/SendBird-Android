package com.sendbird.uikit_messaging_android.openchannel.livestream;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.databinding.ViewLiveStreamListItemBinding;
import com.sendbird.uikit_messaging_android.model.LiveStreamingChannelData;
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelListAdapter;
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelListViewHolder;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class LiveStreamListAdapter extends OpenChannelListAdapter<LiveStreamListAdapter.LiveStreamingListViewHolder> {
    @NonNull
    @Override
    public LiveStreamingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewLiveStreamListItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.view_live_stream_list_item, parent, false);
        return new LiveStreamingListViewHolder(binding);
    }

    static class LiveStreamingListViewHolder extends OpenChannelListViewHolder {
        private ViewLiveStreamListItemBinding binding;

        public LiveStreamingListViewHolder(@NonNull ViewLiveStreamListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        protected void bind(OpenChannel openChannel) {
            if (openChannel == null) return;
            int count = openChannel.getParticipantCount();
            String text = String.valueOf(count);
            if (count > 1000) {
                text = String.format(Locale.US, "%.1fK", count / 1000F);
            }
            binding.tvParticipantCount.setText(text);

            try {
                LiveStreamingChannelData channelData = new LiveStreamingChannelData(new JSONObject(openChannel.getData()));

                binding.tvLiveTitle.setVisibility(View.VISIBLE);
                binding.tvLiveTitle.setText(channelData.getName());

                UserInfo creatorInfo = channelData.getCreator();
                if (creatorInfo == null || TextUtils.isEmpty(creatorInfo.getNickname())) {
                    binding.tvCreator.setVisibility(View.GONE);
                } else {
                    binding.tvCreator.setVisibility(View.VISIBLE);
                    binding.tvCreator.setText(creatorInfo.getNickname());
                }

                if (channelData.getTags() == null || TextUtils.isEmpty(channelData.getTags().get(0))) {
                    binding.tvBadge.setVisibility(View.GONE);
                } else {
                    binding.tvBadge.setVisibility(View.VISIBLE);
                    binding.tvBadge.setText(channelData.getTags().get(0));
                }

                Glide.with(binding.getRoot().getContext())
                        .load(channelData.getLiveUrl())
                        .override(binding.ivLiveThumbnail.getWidth(), binding.ivLiveThumbnail.getHeight())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.color.background_600)
                        .into(binding.ivLiveThumbnail);

                binding.ivChannelThumbnail.setVisibility(View.VISIBLE);
                Glide.with(binding.getRoot().getContext())
                        .load(channelData.getThumbnailUrl())
                        .override(binding.ivChannelThumbnail.getWidth(), binding.ivChannelThumbnail.getHeight())
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(PreferenceUtils.isUsingDarkTheme() ? R.drawable.layer_channel_cover_default_dark : R.drawable.layer_channel_cover_default_light)
                        .into(binding.ivChannelThumbnail);
            } catch (JSONException e) {
                e.printStackTrace();
                binding.ivLiveThumbnail.setImageDrawable(null);
                binding.ivChannelThumbnail.setVisibility(View.GONE);
                binding.tvLiveTitle.setVisibility(View.GONE);
                binding.tvBadge.setVisibility(View.GONE);
                binding.tvCreator.setVisibility(View.GONE);
            }
        }
    }
}
