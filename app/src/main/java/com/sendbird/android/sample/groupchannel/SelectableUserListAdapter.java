package com.sendbird.android.sample.groupchannel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sendbird.android.User;
import com.sendbird.android.sample.R;
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

    // For the adapter to track which users have been selected
    private OnItemCheckedChangeListener mCheckedChangeListener;

    public interface OnItemCheckedChangeListener {
        void OnItemChecked(User user, boolean checked);
    }

    public SelectableUserListAdapter(Context context) {
        mContext = context;
        mUsers = new ArrayList<>();
        mSelectedUserIds = new ArrayList<>();
    }

    public void setItemCheckedChangeListener(OnItemCheckedChangeListener listener) {
        mCheckedChangeListener = listener;
    }

    public void setUserList(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_selectable_user, parent, false);
        return new SelectableUserHolder(view);
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

    private static class SelectableUserHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private ImageView profileImage;
        private CheckBox checkbox;

        public SelectableUserHolder(View itemView) {
            super(itemView);

            this.setIsRecyclable(false);

            nameText = (TextView) itemView.findViewById(R.id.text_selectable_user_list_nickname);
            profileImage = (ImageView) itemView.findViewById(R.id.image_selectable_user_list_profile);
            checkbox = (CheckBox) itemView.findViewById(R.id.checkbox_selectable_user_list);
        }

        private void bind(final Context context, final User user, boolean isSelected, final OnItemCheckedChangeListener listener) {

            nameText.setText(user.getNickname());
            ImageUtils.displayRoundImageFromUrl(context, user.getProfileUrl(), profileImage);

            if (isSelected) {
                checkbox.setChecked(true);
            } else {
                checkbox.setChecked(false);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkbox.setChecked(!checkbox.isChecked());
                }
            });

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
