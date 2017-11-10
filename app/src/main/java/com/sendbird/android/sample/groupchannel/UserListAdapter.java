package com.sendbird.android.sample.groupchannel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple adapter that displays a list of Users.
 */
public class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean mIsGroupChannel;

    public UserListAdapter(Context context, boolean isGroupChannel) {
        mContext = context;
        mUsers = new ArrayList<>();
        mIsGroupChannel = isGroupChannel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UserHolder) holder).bind(mContext, mUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setUserList(List<? extends User> users) {
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    public void addLast(User user) {
        mUsers.add(user);
        notifyDataSetChanged();
    }

    private class UserHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private ImageView profileImage;
        private ImageView blockedImage;
        private SwitchCompat switchBlock;

        public UserHolder(View itemView) {
            super(itemView);

            nameText = (TextView) itemView.findViewById(R.id.text_user_list_nickname);
            profileImage = (ImageView) itemView.findViewById(R.id.image_user_list_profile);
            blockedImage = (ImageView) itemView.findViewById(R.id.image_user_list_blocked);
            switchBlock = (SwitchCompat) itemView.findViewById(R.id.switch_block);
        }

        private void bind(final Context context, final User user) {
            nameText.setText(user.getNickname());
            ImageUtils.displayRoundImageFromUrl(context, user.getProfileUrl(), profileImage);

            if (mIsGroupChannel) {
                if (SendBird.getCurrentUser().getUserId().equals(user.getUserId())) {
                    switchBlock.setVisibility(View.GONE);
                    blockedImage.setVisibility(View.GONE);
                } else {
                    switchBlock.setVisibility(View.VISIBLE);
                }

                boolean isBlockedByMe = ((Member) user).isBlockedByMe();
                switchBlock.setChecked(!isBlockedByMe);
                if (isBlockedByMe) {
                    blockedImage.setVisibility(View.VISIBLE);
                } else {
                    blockedImage.setVisibility(View.GONE);
                }

                switchBlock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            SendBird.unblockUser(user, new SendBird.UserUnblockHandler() {
                                @Override
                                public void onUnblocked(SendBirdException e) {
                                    if (e != null) {
                                        return;
                                    }
                                    blockedImage.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            SendBird.blockUser(user, new SendBird.UserBlockHandler() {
                                @Override
                                public void onBlocked(User user, SendBirdException e) {
                                    if (e != null) {
                                        return;
                                    }
                                    blockedImage.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                });
            } else {
                blockedImage.setVisibility(View.GONE);
                switchBlock.setVisibility(View.GONE);
            }
        }
    }
}

