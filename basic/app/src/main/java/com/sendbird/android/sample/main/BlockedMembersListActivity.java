package com.sendbird.android.sample.main;

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

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.groupchannel.SelectableUserListAdapter;

import java.util.ArrayList;
import java.util.List;

public class BlockedMembersListActivity extends AppCompatActivity {

    private static final int STATE_NORMAL = 0;
    private static final int STATE_EDIT = 1;

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private SelectableUserListAdapter mListAdapter;
    private UserListQuery mUserListQuery;

    private Button mButtonEdit, mButtonUnblock;

    private List<String> mSelectedIds;
    private int mCurrentState;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blocked_members_list);

        mSelectedIds = new ArrayList<>();

        mButtonEdit = (Button) findViewById(R.id.button_edit);
        mButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setState(STATE_EDIT);
            }
        });
        mButtonEdit.setEnabled(false);

        mButtonUnblock = (Button) findViewById(R.id.button_unblock);
        mButtonUnblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListAdapter.unblock();
                setState(STATE_NORMAL);
            }
        });
        mButtonUnblock.setEnabled(false);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_blocked_members_list);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24_dp);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_select_user);
        mListAdapter = new SelectableUserListAdapter(this, true, false);
        mListAdapter.setItemCheckedChangeListener(new SelectableUserListAdapter.OnItemCheckedChangeListener() {
            @Override
            public void OnItemChecked(User user, boolean checked) {
                if (checked) {
                    userSelected(true, user.getUserId());
                } else {
                    userSelected(false, user.getUserId());
                }
            }
        });

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

    void setState(int state) {
        if (state == STATE_EDIT) {
            mCurrentState = STATE_EDIT;
            mButtonUnblock.setVisibility(View.VISIBLE);
            mButtonEdit.setVisibility(View.GONE);
            mListAdapter.setShowCheckBox(true);
        } else if (state == STATE_NORMAL){
            mCurrentState = STATE_NORMAL;
            mButtonUnblock.setVisibility(View.GONE);
            mButtonEdit.setVisibility(View.VISIBLE);
            mListAdapter.setShowCheckBox(false);
        }
    }

    public void userSelected(boolean selected, String userId) {
        if (selected) {
            mSelectedIds.add(userId);
        } else {
            mSelectedIds.remove(userId);
        }

        if (mSelectedIds.size() > 0) {
            mButtonUnblock.setEnabled(true);
        } else {
            mButtonUnblock.setEnabled(false);
        }
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

    private void loadInitialUserList(int size) {
        mUserListQuery = SendBird.createBlockedUserListQuery();

        mUserListQuery.setLimit(size);
        mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                mListAdapter.setUserList(list);
                mButtonEdit.setEnabled(list.size() > 0);
            }
        });
    }

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

    @Override
    public void onBackPressed() {
        if (mCurrentState == STATE_EDIT) {
            setState(STATE_NORMAL);
        } else {
            super.onBackPressed();
        }
    }

    public void blockedMemberCount(int size) {
        mButtonEdit.setEnabled(size > 0);
    }
}
