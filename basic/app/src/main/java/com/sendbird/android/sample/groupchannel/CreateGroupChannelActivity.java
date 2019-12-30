package com.sendbird.android.sample.groupchannel;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An Activity to create a new Group Channel.
 * First displays a selectable list of users,
 * then shows an option to create a Distinct channel.
 */

public class CreateGroupChannelActivity extends AppCompatActivity
        implements SelectUserFragment.UsersSelectedListener, SelectDistinctFragment.DistinctSelectedListener {

    public static final String EXTRA_NEW_CHANNEL_URL = "EXTRA_NEW_CHANNEL_URL";

    static final int STATE_SELECT_USERS = 0;
    static final int STATE_SELECT_DISTINCT = 1;

    private Button mNextButton, mCreateButton;

    private List<String> mSelectedIds;
    private boolean mIsDistinct = true;

    private int mCurrentState;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_group_channel);

        mSelectedIds = new ArrayList<>();

        if (savedInstanceState == null) {
            Fragment fragment = SelectUserFragment.newInstance();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container_create_group_channel, fragment)
                    .commit();
        }

        mNextButton = (Button) findViewById(R.id.button_create_group_channel_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentState == STATE_SELECT_USERS) {
                    Fragment fragment = SelectDistinctFragment.newInstance();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container_create_group_channel, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        mNextButton.setEnabled(false);

        mCreateButton = (Button) findViewById(R.id.button_create_group_channel_create);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentState == STATE_SELECT_USERS) {
//                if (mCurrentState == STATE_SELECT_DISTINCT) {
                    mIsDistinct = PreferenceUtils.getGroupChannelDistinct();
                    createGroupChannel(mSelectedIds, mIsDistinct);
                }
            }
        });
        mCreateButton.setEnabled(false);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_create_group_channel);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp);
        }

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

    void setState(int state) {
        if (state == STATE_SELECT_USERS) {
            mCurrentState = STATE_SELECT_USERS;
            mCreateButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.GONE);
//            mCreateButton.setVisibility(View.GONE);
//            mNextButton.setVisibility(View.VISIBLE);
        } else if (state == STATE_SELECT_DISTINCT){
            mCurrentState = STATE_SELECT_DISTINCT;
            mCreateButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUserSelected(boolean selected, String userId) {
        if (selected) {
            mSelectedIds.add(userId);
        } else {
            mSelectedIds.remove(userId);
        }

        if (mSelectedIds.size() > 0) {
            mCreateButton.setEnabled(true);
//            mNextButton.setEnabled(true);
        } else {
            mCreateButton.setEnabled(false);
//            mNextButton.setEnabled(false);
        }
    }

    @Override
    public void onDistinctSelected(boolean distinct) {
        mIsDistinct = distinct;
    }

    /**
     * Creates a new Group Channel.
     *
     * Note that if you have not included empty channels in your GroupChannelListQuery,
     * the channel will not be shown in the user's channel list until at least one message
     * has been sent inside.
     *
     * @param userIds   The users to be members of the new channel.
     * @param distinct  Whether the channel is unique for the selected members.
     *                  If you attempt to create another Distinct channel with the same members,
     *                  the existing channel instance will be returned.
     */
    private void createGroupChannel(List<String> userIds, boolean distinct) {
        GroupChannel.createChannelWithUserIds(userIds, distinct, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra(EXTRA_NEW_CHANNEL_URL, groupChannel.getUrl());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


}
