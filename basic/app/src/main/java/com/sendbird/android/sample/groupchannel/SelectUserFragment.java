package com.sendbird.android.sample.groupchannel;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sendbird.android.ApplicationUserListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.sample.R;

import java.util.List;


/**
 * A fragment displaying a list of selectable users.
 */
public class SelectUserFragment extends Fragment{

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private SelectableUserListAdapter mListAdapter;

    private ApplicationUserListQuery mUserListQuery;
    private UsersSelectedListener mListener;

    // To pass selected user IDs to the parent Activity.
    interface UsersSelectedListener {
        void onUserSelected(boolean selected, String userId);
    }

    static SelectUserFragment newInstance() {
        SelectUserFragment fragment = new SelectUserFragment();

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_select_user, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_select_user);
        mListAdapter = new SelectableUserListAdapter(getActivity(), false, true);

        mListAdapter.setItemCheckedChangeListener(new SelectableUserListAdapter.OnItemCheckedChangeListener() {
            @Override
            public void OnItemChecked(User user, boolean checked) {
                if (checked) {
                    mListener.onUserSelected(true, user.getUserId());
                } else {
                    mListener.onUserSelected(false, user.getUserId());
                }
            }
        });

        mListener = (UsersSelectedListener) getActivity();

        setUpRecyclerView();

        loadInitialUserList(15);

        ((CreateGroupChannelActivity) getActivity()).setState(CreateGroupChannelActivity.STATE_SELECT_USERS);

        return rootView;
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mListAdapter.getItemCount() - 1) {
                    loadNextUserList(10);
                }
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
