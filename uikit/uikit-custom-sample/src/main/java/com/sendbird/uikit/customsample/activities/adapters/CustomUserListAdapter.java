package com.sendbird.uikit.customsample.activities.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.sendbird.uikit.activities.adapter.UserListAdapter;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.customsample.activities.viewholders.CustomUserViewHolder;
import com.sendbird.uikit.customsample.databinding.ViewCustomUserHolderBinding;
import com.sendbird.uikit.customsample.interfaces.OnUserSelectedChangeListener;
import com.sendbird.uikit.interfaces.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class CustomUserListAdapter extends UserListAdapter {
    private List<String> invitedUsers = new ArrayList<>();
    private List<String> selectedUsers = new ArrayList<>();
    private OnUserSelectedChangeListener userSelectedChangeListener;

    @NonNull
    @Override
    public BaseViewHolder<UserInfo> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomUserViewHolder(ViewCustomUserHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<UserInfo> holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof CustomUserViewHolder) {
            ((CustomUserViewHolder) holder).bindSelected(getItem(position),
                    invitedUsers, selectedUsers, userSelectedChangeListener);
        }
    }

    public void setOnUserCheckedListener(OnUserSelectedChangeListener userSelectedChangeListener) {
        this.userSelectedChangeListener = userSelectedChangeListener;
    }

    public void setInvitedUsers(List<String> invitedUsers) {
        this.invitedUsers = invitedUsers;
    }
}

