package com.sendbird.uikit.customsample.groupchannel.activities.viewholders;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.databinding.ViewCustomUserHolderBinding;
import com.sendbird.uikit.customsample.interfaces.OnUserSelectedChangeListener;
import com.sendbird.uikit.customsample.utils.DrawableUtils;
import com.sendbird.uikit.interfaces.UserInfo;

import java.util.List;

public class CustomUserViewHolder extends BaseViewHolder<UserInfo> {
    private ViewCustomUserHolderBinding binding;

    public CustomUserViewHolder(@NonNull ViewCustomUserHolderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void bind(UserInfo user) {
        if (user == null) {
            return;
        }

        Drawable errorIcon = DrawableUtils.createOvalIcon(binding.getRoot().getContext(),
                R.color.background_300, R.drawable.icon_user, R.color.ondark_01);
        Glide.with(binding.getRoot().getContext())
                .load(user.getProfileUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new ObjectKey(user.getProfileUrl()))
                .error(errorIcon)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivUserCover);
        binding.tvNickname.setText(user.getNickname());
    }

    public void bindSelected(UserInfo user, List<String> invitedUsers,
                             List<String> selectedUsers, OnUserSelectedChangeListener listener) {
        if (listener == null) {
            binding.cbUser.setVisibility(View.GONE);
            return;
        }

        binding.getRoot().setBackgroundResource(R.drawable.custom_user_view_holder_background);
        binding.cbUser.setOnCheckedChangeListener(null);
        binding.cbUser.setChecked(selectedUsers.contains(user.getUserId()) || invitedUsers.contains(user.getUserId()));
        binding.getRoot().setEnabled(!invitedUsers.contains(user.getUserId()));
        binding.cbUser.setEnabled(!invitedUsers.contains(user.getUserId()));

        binding.getRoot().setOnClickListener(v -> {
            binding.cbUser.setChecked(!selectedUsers.contains(user.getUserId()));
        });

        binding.cbUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUsers.add(user.getUserId());
            } else {
                selectedUsers.remove(user.getUserId());
            }

            if (listener != null) {
                listener.onUserSelectedChange(selectedUsers, isChecked);
            }
        });
    }
}
