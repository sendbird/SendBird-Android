package com.sendbird.syncmanager.sample.groupchannel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.syncmanager.sample.R;
import com.sendbird.syncmanager.sample.main.ConnectionManager;

import java.util.ArrayList;
import java.util.List;


public class MemberListActivity extends AppCompatActivity {

    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MEMBER_LIST";
    static final String EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL";
    static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    static final String EXTRA_USER_PROFILE_URL = "EXTRA_USER_PROFILE_URL";
    static final String EXTRA_USER_NICKNAME = "EXTRA_USER_NICKNAME";
    static final String EXTRA_USER_BLOCKED_BY_ME = "EXTRA_USER_BLOCKED_BY_ME";

    private UserListAdapter mListAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private GroupChannel mChannel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);

        if (getIntent().hasExtra(GroupChatFragment.EXTRA_CHANNEL)) {
           byte[] serializedChannelData = getIntent().getByteArrayExtra(GroupChatFragment.EXTRA_CHANNEL);
           mChannel = (GroupChannel) BaseChannel.buildFromSerializedData(serializedChannelData);
        }

        if (mChannel == null) {
            Toast.makeText(this, R.string.channel_not_exists, Toast.LENGTH_SHORT).show();
            onBackPressed();
            finish();
        }

        mRecyclerView = findViewById(R.id.recycler_member_list);
        mListAdapter = new UserListAdapter(this, mChannel.getUrl(), true);

        Toolbar toolbar = findViewById(R.id.toolbar_member_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp);
        }

        setMemberList(mChannel.getMembers());
        setUpRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, new ConnectionManager.ConnectionManagementHandler() {
            @Override
            public void onConnected(boolean reconnect) {
                getChannelFromUrl(mChannel.getUrl());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
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

    private void getChannelFromUrl(String url) {
        GroupChannel.getChannel(url, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                mChannel = groupChannel;

                refreshChannel();
            }
        });
    }

    private void refreshChannel() {
        mChannel.refresh(new GroupChannel.GroupChannelRefreshHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                setMemberList(mChannel.getMembers());
            }
        });
    }

    private void setMemberList(List<Member> memberList) {
        List<Member> sortedUserList = new ArrayList<>();
        User currentUser = SendBird.getCurrentUser();
        if (currentUser != null) {
            for (Member me : memberList) {
                if (me.getUserId().equals(currentUser.getUserId())) {
                    sortedUserList.add(me);
                    break;
                }
            }
        }

        for (Member other : memberList) {
            if (currentUser != null && other.getUserId().equals(currentUser.getUserId())) {
                continue;
            }

            sortedUserList.add(other);
        }

        mListAdapter.setUserList(sortedUserList);
    }
}
