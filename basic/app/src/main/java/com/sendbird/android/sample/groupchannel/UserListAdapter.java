package com.sendbird.android.sample.groupchannel;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
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
    private String mChannelUrl;
    private boolean mIsGroupChannel;

    public UserListAdapter(Context context, String channelUrl, boolean isGroupChannel) {
        mContext = context;
        mUsers = new ArrayList<>();
        mChannelUrl = channelUrl;
        mIsGroupChannel = isGroupChannel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UserHolder) holder).bind(mContext, (UserHolder)holder, mUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setUserList(List<? extends User> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    public void addLast(User user) {
        mUsers.add(user);
        notifyDataSetChanged();
    }

    private class UserHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView nameText;
        private ImageView profileImage;
        private ImageView blockedImage;
        private RelativeLayout relativeLayoutBlock;
        private TextView textViewBlocked;

        UserHolder(View itemView) {
            super(itemView);

            view = itemView;
            nameText = itemView.findViewById(R.id.text_user_list_nickname);
            profileImage = itemView.findViewById(R.id.image_user_list_profile);
            blockedImage = itemView.findViewById(R.id.image_user_list_blocked);
            relativeLayoutBlock = itemView.findViewById(R.id.relative_layout_blocked_by_me);
            textViewBlocked = itemView.findViewById(R.id.text_view_blocked);
        }

        private void bind(final Context context, final UserHolder holder, final User user) {
            nameText.setText(user.getNickname());
            ImageUtils.displayRoundImageFromUrl(context, user.getProfileUrl(), profileImage);

            if (mIsGroupChannel) {
                if (SendBird.getCurrentUser().getUserId().equals(user.getUserId())) {
                    relativeLayoutBlock.setVisibility(View.GONE);
                    textViewBlocked.setVisibility(View.GONE);
                } else {
                    relativeLayoutBlock.setVisibility(View.VISIBLE);

                    holder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, MemberInfoActivity.class);
                            intent.putExtra(MemberListActivity.EXTRA_CHANNEL_URL, mChannelUrl);
                            intent.putExtra(MemberListActivity.EXTRA_USER_ID, user.getUserId());
                            intent.putExtra(MemberListActivity.EXTRA_USER_PROFILE_URL, user.getProfileUrl());
                            intent.putExtra(MemberListActivity.EXTRA_USER_NICKNAME, user.getNickname());
                            intent.putExtra(MemberListActivity.EXTRA_USER_BLOCKED_BY_ME, ((Member)user).isBlockedByMe());
                            context.startActivity(intent);
                        }
                    });
                }

                final boolean isBlockedByMe = ((Member) user).isBlockedByMe();
                if (isBlockedByMe) {
                    blockedImage.setVisibility(View.VISIBLE);
                    textViewBlocked.setVisibility(View.VISIBLE);
                } else {
                    blockedImage.setVisibility(View.GONE);
                    textViewBlocked.setVisibility(View.GONE);
                }
            } else {
                blockedImage.setVisibility(View.GONE);
                relativeLayoutBlock.setVisibility(View.GONE);
            }
        }
    }
}

