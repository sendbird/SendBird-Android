package com.sendbird.android.sample.openchannel;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.groupchannel.UserListAdapter;
import com.sendbird.android.sample.main.ConnectionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of the participants of a specified Open Channel.
 */

public class ParticipantListActivity extends AppCompatActivity {

    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_PARTICIPANT_LIST";

    private UserListAdapter mListAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private String mChannelUrl;
    private OpenChannel mChannel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_participant_list);
        mRecyclerView = findViewById(R.id.recycler_participant_list);

        mChannelUrl = getIntent().getStringExtra(OpenChatFragment.EXTRA_CHANNEL_URL);
        mListAdapter = new UserListAdapter(this, mChannelUrl, false);

        Toolbar toolbar = findViewById(R.id.toolbar_participant_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp);
        }

        setUpRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, new ConnectionManager.ConnectionManagementHandler() {
            @Override
            public void onConnected(boolean reconnect) {
                getChannelFromUrl(mChannelUrl);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
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
    }

    private void getChannelFromUrl(String url) {
        OpenChannel.getChannel(url, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(OpenChannel openChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                mChannel = openChannel;

                getUserList();
            }
        });
    }

    private void getUserList() {
        UserListQuery userListQuery = mChannel.createParticipantListQuery();
        userListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                setUserList(list);
            }
        });
    }

    private void setUserList(List<User> userList) {
        List<User> sortedUserList = new ArrayList<>();
        for (User me : userList) {
            if (me.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                sortedUserList.add(me);
                break;
            }
        }
        for (User other : userList) {
            if (other.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                continue;
            }
            sortedUserList.add(other);
        }

        mListAdapter.setUserList(sortedUserList);
    }
}
