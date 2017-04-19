package com.sendbird.android.sample.openchannel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.groupchannel.UserListAdapter;

import java.util.List;

/**
 * Displays a list of the participants of a specified Open Channel.
 */

public class ParticipantListActivity extends AppCompatActivity {

    private UserListAdapter mListAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private String mChannelUrl;
    private OpenChannel mChannel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_participant_list);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_participant_list);

        mChannelUrl = getIntent().getStringExtra(OpenChatFragment.EXTRA_CHANNEL_URL);
        mListAdapter = new UserListAdapter(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_participant_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setUpRecyclerView();

        getChannelFromUrl();
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

    /**
     * Gets the channel instance with the channel URL.
     */
    private void getChannelFromUrl() {
        OpenChannel.getChannel(mChannelUrl, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(OpenChannel openChannel, SendBirdException e) {
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

                mListAdapter.setUserList(list);
            }
        });
    }


}
