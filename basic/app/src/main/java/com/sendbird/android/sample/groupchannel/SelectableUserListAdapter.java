package com.sendbird.android.sample.groupchannel;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.main.BlockedMembersListActivity;
import com.sendbird.android.sample.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Populates a RecyclerView with a list of users, each with a checkbox.
 */

public class SelectableUserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<User> mUsers;
    private Context mContext;
    private static List<String> mSelectedUserIds;
    private boolean mIsBlockedList;
    private boolean mShowCheckBox;

    private SelectableUserHolder mSelectableUserHolder;

    // For the adapter to track which users have been selected
    private OnItemCheckedChangeListener mCheckedChangeListener;

    public interface OnItemCheckedChangeListener {
        void OnItemChecked(User user, boolean checked);
    }

    public SelectableUserListAdapter(Context context, boolean isBlockedList, boolean showCheckBox) {
        mContext = context;
        mUsers = new ArrayList<>();
        mSelectedUserIds = new ArrayList<>();
        mIsBlockedList = isBlockedList;
        mShowCheckBox = showCheckBox;
    }

    public void setItemCheckedChangeListener(OnItemCheckedChangeListener listener) {
        mCheckedChangeListener = listener;
    }

    public void setUserList(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    public void setShowCheckBox(boolean showCheckBox) {
        mShowCheckBox = showCheckBox;
        if (mSelectableUserHolder != null) {
            mSelectableUserHolder.setShowCheckBox(showCheckBox);
        }
        notifyDataSetChanged();
    }

    public void unblock() {
        for (final String userId : mSelectedUserIds) {
            SendBird.unblockUserWithUserId(userId, new SendBird.UserUnblockHandler() {
                @Override
                public void onUnblocked(SendBirdException e) {
                    if (e != null) {
                        return;
                    }

                    User user;
                    for (int index = 0; index < mUsers.size(); index++) {
                        user = mUsers.get(index);
                        if (userId.equals(user.getUserId())) {
                            mUsers.remove(index);
                            break;
                        }
                    }

                    ((BlockedMembersListActivity)mContext).blockedMemberCount(mUsers.size());

                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_selectable_user, parent, false);
        mSelectableUserHolder = new SelectableUserHolder(view, mIsBlockedList, mShowCheckBox);
        return mSelectableUserHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((SelectableUserHolder) holder).bind(
                mContext,
                mUsers.get(position),
                isSelected(mUsers.get(position)),
                mCheckedChangeListener);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public boolean isSelected(User user) {
        return mSelectedUserIds.contains(user.getUserId());
    }

    public void addLast(User user) {
        mUsers.add(user);
        notifyDataSetChanged();
    }

    private class SelectableUserHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private ImageView profileImage;
        private ImageView blockedImage;
        private CheckBox checkbox;

        private boolean mIsBlockedList;
        private boolean mShowCheckBox;

        public SelectableUserHolder(View itemView, boolean isBlockedList, boolean hideCheckBox) {
            super(itemView);

            this.setIsRecyclable(false);
            mIsBlockedList = isBlockedList;
            mShowCheckBox = hideCheckBox;

            nameText = (TextView) itemView.findViewById(R.id.text_selectable_user_list_nickname);
            profileImage = (ImageView) itemView.findViewById(R.id.image_selectable_user_list_profile);
            blockedImage = (ImageView) itemView.findViewById(R.id.image_user_list_blocked);
            checkbox = (CheckBox) itemView.findViewById(R.id.checkbox_selectable_user_list);
        }

        public void setShowCheckBox(boolean showCheckBox) {
            mShowCheckBox = showCheckBox;
        }

        private void bind(final Context context, final User user, boolean isSelected, final OnItemCheckedChangeListener listener) {
            nameText.setText(user.getNickname());
            ImageUtils.displayRoundImageFromUrl(context, user.getProfileUrl(), profileImage);

            if (mIsBlockedList) {
                blockedImage.setVisibility(View.VISIBLE);
            } else {
                blockedImage.setVisibility(View.GONE);
            }

            if (mShowCheckBox) {
                checkbox.setVisibility(View.VISIBLE);
            } else {
                checkbox.setVisibility(View.GONE);
            }

            if (isSelected) {
                checkbox.setChecked(true);
            } else {
                checkbox.setChecked(false);
            }

            if (mShowCheckBox) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mShowCheckBox) {
                            checkbox.setChecked(!checkbox.isChecked());
                        }
                    }
                });
            }

            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listener.OnItemChecked(user, isChecked);

                    if (isChecked) {
                        mSelectedUserIds.add(user.getUserId());
                    } else {
                        mSelectedUserIds.remove(user.getUserId());
                    }
                }
            });
        }
    }
}
