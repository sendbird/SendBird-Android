package com.sendbird.syncmanager.sample.groupchannel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.syncmanager.ChannelCollection;
import com.sendbird.syncmanager.ChannelEventAction;
import com.sendbird.syncmanager.handler.ChannelCollectionHandler;
import com.sendbird.syncmanager.handler.CompletionHandler;
import com.sendbird.syncmanager.sample.R;
import com.sendbird.syncmanager.sample.utils.PreferenceUtils;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class GroupChannelListFragment extends Fragment {

    public static final String EXTRA_GROUP_CHANNEL_URL = "GROUP_CHANNEL_URL";
    private static final int INTENT_REQUEST_NEW_GROUP_CHANNEL = 302;
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";

    private static final int CHANNEL_LIST_LIMIT = 15;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private GroupChannelListAdapter mChannelListAdapter;
    private FloatingActionButton mCreateChannelFab;
    private SwipeRefreshLayout mSwipeRefresh;
    private ChannelCollection mChannelCollection;

    public static GroupChannelListFragment newInstance() {
        GroupChannelListFragment fragment = new GroupChannelListFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("LIFECYCLE", "GroupChannelListFragment onCreateView()");

        View rootView = inflater.inflate(R.layout.fragment_group_channel_list, container, false);

        setRetainInstance(true);

        // Change action bar title
        ((GroupChannelActivity) getActivity()).setActionBarTitle(getResources().getString(R.string.all_group_channels));

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_group_channel_list);
        mCreateChannelFab = (FloatingActionButton) rootView.findViewById(R.id.fab_group_channel_list);
        mSwipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout_group_channel_list);

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                refresh();
            }
        });

        mCreateChannelFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateGroupChannelActivity.class);
                startActivityForResult(intent, INTENT_REQUEST_NEW_GROUP_CHANNEL);
            }
        });

        mChannelListAdapter = new GroupChannelListAdapter(getActivity());

        setUpRecyclerView();
        setUpChannelListAdapter();

        refresh();

        return rootView;
    }

    @Override
    public void onResume() {
        Log.d("LIFECYCLE", "GroupChannelListFragment onResume()");

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                mChannelListAdapter.notifyDataSetChanged();
            }
        });

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("LIFECYCLE", "GroupChannelListFragment onPause()");

        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mChannelCollection != null) {
            mChannelCollection.setCollectionHandler(null);
            mChannelCollection.remove();
        }

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_REQUEST_NEW_GROUP_CHANNEL) {
            if (resultCode == RESULT_OK) {
                // Channel successfully created
                // Enter the newly created channel.
                String newChannelUrl = data.getStringExtra(CreateGroupChannelActivity.EXTRA_NEW_CHANNEL_URL);
                if (newChannelUrl != null) {
                    enterGroupChannel(newChannelUrl);
                }
            } else {
                Log.d("GrChLIST", "resultCode not STATUS_OK");
            }
        }
    }

    // Sets up recycler view
    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChannelListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        // If user scrolls to bottom of the list, loads more channels.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mLayoutManager.findLastVisibleItemPosition() == mChannelListAdapter.getItemCount() - 1) {
                        if (mChannelCollection != null) {
                            mChannelCollection.fetch(new CompletionHandler() {
                                @Override
                                public void onCompleted(SendBirdException e) {
                                    if (mSwipeRefresh.isRefreshing()) {
                                        mSwipeRefresh.setRefreshing(false);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    // Sets up channel list adapter
    private void setUpChannelListAdapter() {
        mChannelListAdapter.setOnItemClickListener(new GroupChannelListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupChannel channel) {
                enterGroupChannel(channel);
            }
        });

        mChannelListAdapter.setOnItemLongClickListener(new GroupChannelListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final GroupChannel channel) {
                showChannelOptionsDialog(channel);
            }
        });
    }

    /**
     * Displays a dialog listing channel-specific options.
     */
    private void showChannelOptionsDialog(final GroupChannel channel) {
        String[] options;
        final boolean pushCurrentlyEnabled = channel.isPushEnabled();

        options = pushCurrentlyEnabled
                ? new String[]{getString(R.string.leave_channel), getString(R.string.set_push_off)}
                : new String[]{getString(R.string.leave_channel), getString(R.string.set_push_on)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.channel_option))
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Show a dialog to confirm that the user wants to leave the channel.
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.request_leave_channel, channel.getName()))
                                    .setPositiveButton(R.string.action_leave_channel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            leaveChannel(channel);
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.cancel), null)
                                    .create().show();
                        } else if (which == 1) {
                            setChannelPushPreferences(channel, !pushCurrentlyEnabled);
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * Turns push notifications on or off for a selected channel.
     *
     * @param channel The channel for which push preferences should be changed.
     * @param on      Whether to set push notifications on or off.
     */
    private void setChannelPushPreferences(final GroupChannel channel, final boolean on) {
        // Change push preferences.
        channel.setPushPreference(on, new GroupChannel.GroupChannelSetPushPreferenceHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), getString(R.string.sendbird_error, e.getMessage()), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                String toast = on
                        ? getString(R.string.text_push_on)
                        : getString(R.string.text_push_off);

                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * Enters a Group Channel. Upon entering, a GroupChatFragment will be inflated
     * to display messages within the channel.
     *
     * @param channel The Group Channel to enter.
     */
    void enterGroupChannel(GroupChannel channel) {
        final String channelUrl = channel.getUrl();

        enterGroupChannel(channelUrl);
    }

    /**
     * Enters a Group Channel with a URL.
     *
     * @param channelUrl The URL of the channel to enter.
     */
    void enterGroupChannel(String channelUrl) {
        GroupChatFragment fragment = GroupChatFragment.newInstance(channelUrl);
        getFragmentManager().beginTransaction()
                .replace(R.id.container_group_channel, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Creates a new query to get the list of the user's Group Channels,
     * then replaces the existing dataset.
     *
     */
    private void refresh() {
        if (mChannelCollection != null) {
            mChannelCollection.remove();
        }

        mChannelListAdapter.clearMap();
        mChannelListAdapter.clearChannelList();
        GroupChannelListQuery query = GroupChannel.createMyGroupChannelListQuery();
        query.setLimit(CHANNEL_LIST_LIMIT);
        mChannelCollection = new ChannelCollection(query);
        mChannelCollection.setCollectionHandler(mChannelCollectionHandler);
        mChannelCollection.fetch(new CompletionHandler() {
            @Override
            public void onCompleted(SendBirdException e) {
                if (mSwipeRefresh.isRefreshing()) {
                    mSwipeRefresh.setRefreshing(false);
                }
            }
        });
    }

    /**
     * Leaves a Group Channel.
     *
     * @param channel The channel to leave.
     */
    private void leaveChannel(final GroupChannel channel) {
        channel.leave(new GroupChannel.GroupChannelLeaveHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                // Re-query message list
                refresh();
            }
        });
    }

    ChannelCollectionHandler mChannelCollectionHandler = new ChannelCollectionHandler() {
        @Override
        public void onChannelEvent(final ChannelCollection channelCollection, final List<GroupChannel> list, final ChannelEventAction channelEventAction) {
            Log.d("SyncManager", "onChannelEvent: size = " + list.size() + ", action = " + channelEventAction);
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSwipeRefresh.isRefreshing()) {
                        mSwipeRefresh.setRefreshing(false);
                    }

                    switch (channelEventAction) {
                        case INSERT:
                            mChannelListAdapter.clearMap();
                            mChannelListAdapter.insertChannels(list, channelCollection.getQuery().getOrder());
                            break;

                        case UPDATE:
                            mChannelListAdapter.clearMap();
                            mChannelListAdapter.updateChannels(list);
                            break;

                        case MOVE:
                            mChannelListAdapter.clearMap();
                            mChannelListAdapter.moveChannels(list, channelCollection.getQuery().getOrder());
                            break;

                        case REMOVE:
                            mChannelListAdapter.clearMap();
                            mChannelListAdapter.removeChannels(list);
                            break;

                        case CLEAR:
                            mChannelListAdapter.clearMap();
                            mChannelListAdapter.clearChannelList();
                            break;
                    }
                }
            });
        }
    };
}