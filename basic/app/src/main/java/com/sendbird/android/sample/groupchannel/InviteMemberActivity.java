package com.sendbird.android.sample.groupchannel;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.sendbird.android.ApplicationUserListQuery;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a selectable list of users within the app. Selected users will be invited into the
 * current channel.
 */

public class InviteMemberActivity extends AppCompatActivity{

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private SelectableUserListAdapter mListAdapter;
    private Toolbar mToolbar;

    private ApplicationUserListQuery mUserListQuery;
    private String mChannelUrl;
    private Button mInviteButton;

    private List<String> mSelectedUserIds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invite_member);

        mSelectedUserIds = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_invite_member);
        mListAdapter = new SelectableUserListAdapter(this, false, true);
        mListAdapter.setItemCheckedChangeListener(new SelectableUserListAdapter.OnItemCheckedChangeListener() {
            @Override
            public void OnItemChecked(User user, boolean checked) {
                if (checked) {
                    mSelectedUserIds.add((user.getUserId()));
                } else {
                    mSelectedUserIds.remove(user.getUserId());
                }

                // If no users are selected, disable the invite button.
                if (mSelectedUserIds.size() > 0) {
                    mInviteButton.setEnabled(true);
                } else {
                    mInviteButton.setEnabled(false);
                }
            }
        });

        mToolbar = (Toolbar) findViewById(R.id.toolbar_invite_member);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp);
        }

        mChannelUrl = getIntent().getStringExtra(GroupChatFragment.EXTRA_CHANNEL_URL);

        mInviteButton = (Button) findViewById(R.id.button_invite_member);
        mInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedUserIds.size() > 0) {
                    inviteSelectedMembersWithUserIds();
                }
            }
        });
        mInviteButton.setEnabled(false);

        setUpRecyclerView();

        loadInitialUserList(15);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mListAdapter.getItemCount() - 1) {
                    loadNextUserList(10);
                }
            }
        });
    }

    private void inviteSelectedMembersWithUserIds() {

        // Get channel instance from URL first.
        GroupChannel.getChannel(mChannelUrl, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                // Then invite the selected members to the channel.
                groupChannel.inviteWithUserIds(mSelectedUserIds, new GroupChannel.GroupChannelInviteHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null) {
                            // Error!
                            return;
                        }

                        finish();
                    }
                });
            }
        });
    }

    /**
     * Replaces current user list with new list.
     * Should be used only on initial load.
     */
    private void loadInitialUserList(int size) {
        mUserListQuery = SendBird.createApplicationUserListQuery();

        mUserListQuery.setLimit(size);
        mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                mListAdapter.setUserList(list);
            }
        });
    }

    /**
     * Loads users and adds them to current user list.
     *
     * A PreviousMessageListQuery must have been already initialized through {@link #loadInitialUserList(int)}
     */
    private void loadNextUserList(int size) {
        mUserListQuery.setLimit(size);

        mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                for (User user : list) {
                    mListAdapter.addLast(user);
                }
            }
        });
    }

}
