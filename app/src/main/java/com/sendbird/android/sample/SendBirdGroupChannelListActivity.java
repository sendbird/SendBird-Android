package com.sendbird.android.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class SendBirdGroupChannelListActivity extends FragmentActivity {
    private SendBirdGroupChannelListFragment mSendBirdGroupChannelListFragment;

    private View mTopBarContainer;
    private View mSettingsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.sendbird_slide_in_from_bottom, R.anim.sendbird_slide_out_to_top);
        setContentView(R.layout.activity_sendbird_group_channel_list);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initFragment();
        initUIComponents();

        Toast.makeText(this, "Long press the channel to hide or leave it.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * If the minimum SDK version you support is under Android 4.0,
         * you MUST uncomment the below code to receive push notifications.
         */
//        SendBird.notifyActivityResumedForOldAndroids();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * If the minimum SDK version you support is under Android 4.0,
         * you MUST uncomment the below code to receive push notifications.
         */
//        SendBird.notifyActivityPausedForOldAndroids();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resizeMenubar();
    }

    private void resizeMenubar() {
        ViewGroup.LayoutParams lp = mTopBarContainer.getLayoutParams();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp.height = (int) (28 * getResources().getDisplayMetrics().density);
        } else {
            lp.height = (int) (48 * getResources().getDisplayMetrics().density);
        }
        mTopBarContainer.setLayoutParams(lp);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.sendbird_slide_in_from_top, R.anim.sendbird_slide_out_to_bottom);
    }

    private void initFragment() {
        mSendBirdGroupChannelListFragment = new SendBirdGroupChannelListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdGroupChannelListFragment)
                .commit();
    }

    private void initUIComponents() {
        mTopBarContainer = findViewById(R.id.top_bar_container);

        mSettingsContainer = findViewById(R.id.settings_container);
        mSettingsContainer.setVisibility(View.GONE);

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSettingsContainer.getVisibility() != View.VISIBLE) {
                    mSettingsContainer.setVisibility(View.VISIBLE);
                } else {
                    mSettingsContainer.setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.btn_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendBirdGroupChannelListActivity.this, SendBirdUserListActivity.class);
                mSendBirdGroupChannelListFragment.startActivityForResult(intent, SendBirdGroupChannelListFragment.REQUEST_INVITE_USERS);
                mSettingsContainer.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.btn_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SendBirdGroupChannelListActivity.this)
                        .setTitle("SendBird")
                        .setMessage("SendBird SDK " + SendBird.getSDKVersion())
                        .setPositiveButton("OK", null).create().show();

                mSettingsContainer.setVisibility(View.GONE);
            }
        });

        resizeMenubar();
    }

    public static class SendBirdGroupChannelListFragment extends Fragment {
        private static final String identifier = "SendBirdGroupChannelList";
        private static final int REQUEST_INVITE_USERS = 100;
        private ListView mListView;
        private SendBirdGroupChannelAdapter mAdapter;
        private GroupChannelListQuery mQuery;

        public SendBirdGroupChannelListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sendbird_fragment_group_channel_list, container, false);
            initUIComponents(rootView);
            return rootView;
        }

        private void initUIComponents(View rootView) {
            mListView = (ListView) rootView.findViewById(R.id.list);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GroupChannel channel = mAdapter.getItem(position);
                    Intent intent = new Intent(getActivity(), SendBirdGroupChatActivity.class);
                    intent.putExtra("channel_url", channel.getUrl());
                    startActivity(intent);
                }
            });
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem + visibleItemCount >= (int) (totalItemCount * 0.8f)) {
                        loadNextChannels();
                    }
                }
            });
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    final GroupChannel channel = mAdapter.getItem(position);
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Leave")
                            .setMessage("Do you want to leave or hide this channel?")
                            .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    channel.leave(new GroupChannel.GroupChannelLeaveHandler() {
                                        @Override
                                        public void onResult(SendBirdException e) {
                                            if (e != null) {
                                                Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            Toast.makeText(getActivity(), "Channel left.", Toast.LENGTH_SHORT).show();
                                            mAdapter.remove(position);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            })
                            .setNeutralButton("Hide", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    channel.hide(new GroupChannel.GroupChannelHideHandler() {
                                        @Override
                                        public void onResult(SendBirdException e) {
                                            if (e != null) {
                                                Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            Toast.makeText(getActivity(), "Channel hidden.", Toast.LENGTH_SHORT).show();
                                            mAdapter.remove(position);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Cancel", null).create().show();
                    return true;
                }
            });

            mAdapter = new SendBirdGroupChannelAdapter(getActivity());
            mListView.setAdapter(mAdapter);
        }

        private void loadNextChannels() {
            if (mQuery == null || mQuery.isLoading()) {
                return;
            }

            if (!mQuery.hasNext()) {
                return;
            }

            mQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
                @Override
                public void onResult(List<GroupChannel> list, SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mAdapter.addAll(list);
                    mAdapter.notifyDataSetChanged();

                    if (mAdapter.getCount() == 0) {
                        Toast.makeText(getActivity(), "No channels found.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        private void create(final String[] userIds) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.sendbird_view_group_create_channel, null);
            final EditText chName = (EditText) view.findViewById(R.id.etxt_chname);
            final CheckBox distinct = (CheckBox) view.findViewById(R.id.chk_distinct);

            new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle("Create Group Channel")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GroupChannel.createChannelWithUserIds(Arrays.asList(userIds), distinct.isChecked(), chName.getText().toString(), null, null, new GroupChannel.GroupChannelCreateHandler() {
                                @Override
                                public void onResult(GroupChannel groupChannel, SendBirdException e) {
                                    if (e != null) {
                                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    mAdapter.replace(groupChannel);
                                }

                            });
                        }
                    })
                    .setNegativeButton("Cancel", null).create().show();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_INVITE_USERS) {
                    String[] userIds = data.getStringArrayExtra("user_ids");
                    create(userIds);
                }
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            SendBird.removeChannelHandler(identifier);
        }

        @Override
        public void onResume() {
            super.onResume();

            SendBird.addChannelHandler(identifier, new SendBird.ChannelHandler() {
                @Override
                public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                    if (baseChannel instanceof GroupChannel) {
                        GroupChannel groupChannel = (GroupChannel) baseChannel;
                        mAdapter.replace(groupChannel);
                    }
                }

                @Override
                public void onUserJoined(GroupChannel groupChannel, User user) {
                    // Member changed. Refresh group channel item.
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onUserLeft(GroupChannel groupChannel, User user) {
                    // Member changed. Refresh group channel item.
                    mAdapter.notifyDataSetChanged();
                }
            });

            mAdapter.clear();
            mAdapter.notifyDataSetChanged();

            mQuery = GroupChannel.createMyGroupChannelListQuery();
            mQuery.setIncludeEmpty(true);
            loadNextChannels();
        }

    }

    public static class SendBirdGroupChannelAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ArrayList<GroupChannel> mItemList;

        public SendBirdGroupChannelAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItemList = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public GroupChannel getItem(int position) {
            return mItemList.get(position);
        }

        public void clear() {
            mItemList.clear();
        }

        public GroupChannel remove(int index) {
            return mItemList.remove(index);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addAll(List<GroupChannel> channels) {
            mItemList.addAll(channels);
        }

        public void replace(GroupChannel newChannel) {
            for (GroupChannel oldChannel : mItemList) {
                if (oldChannel.getUrl().equals(newChannel.getUrl())) {
                    mItemList.remove(oldChannel);
                    break;
                }
            }

            mItemList.add(0, newChannel);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.sendbird_view_group_channel, parent, false);
                viewHolder.setView("img_thumbnail", convertView.findViewById(R.id.img_thumbnail));
                viewHolder.setView("txt_topic", convertView.findViewById(R.id.txt_topic));
                viewHolder.setView("txt_member_count", convertView.findViewById(R.id.txt_member_count));
                viewHolder.setView("txt_unread_count", convertView.findViewById(R.id.txt_unread_count));
                viewHolder.setView("txt_date", convertView.findViewById(R.id.txt_date));
                viewHolder.setView("txt_desc", convertView.findViewById(R.id.txt_desc));

                convertView.setTag(viewHolder);
            }

            GroupChannel item = getItem(position);
            viewHolder = (ViewHolder) convertView.getTag();
            Helper.displayUrlImage(viewHolder.getView("img_thumbnail", ImageView.class), Helper.getDisplayCoverImageUrl(item.getMembers()));
            viewHolder.getView("txt_topic", TextView.class).setText(Helper.getDisplayMemberNames(item.getMembers(), false));

            if (item.getUnreadMessageCount() > 0) {
                viewHolder.getView("txt_unread_count", TextView.class).setVisibility(View.VISIBLE);
                viewHolder.getView("txt_unread_count", TextView.class).setText("" + item.getUnreadMessageCount());
            } else {
                viewHolder.getView("txt_unread_count", TextView.class).setVisibility(View.INVISIBLE);
            }

            viewHolder.getView("txt_member_count", TextView.class).setVisibility(View.VISIBLE);
            viewHolder.getView("txt_member_count", TextView.class).setText("" + item.getMemberCount());

            BaseMessage message = item.getLastMessage();
            if (message == null) {
                viewHolder.getView("txt_date", TextView.class).setText("");
                viewHolder.getView("txt_desc", TextView.class).setText("");
            } else if (message instanceof UserMessage) {
                viewHolder.getView("txt_date", TextView.class).setText(Helper.getDisplayTimeOrDate(mContext, message.getCreatedAt()));
                viewHolder.getView("txt_desc", TextView.class).setText(((UserMessage) message).getMessage());
            } else if (message instanceof AdminMessage) {
                viewHolder.getView("txt_date", TextView.class).setText(Helper.getDisplayTimeOrDate(mContext, message.getCreatedAt()));
                viewHolder.getView("txt_desc", TextView.class).setText(((AdminMessage) message).getMessage());
            } else if (message instanceof FileMessage) {
                viewHolder.getView("txt_date", TextView.class).setText(Helper.getDisplayTimeOrDate(mContext, message.getCreatedAt()));
                viewHolder.getView("txt_desc", TextView.class).setText("(FILE)");
            }

            return convertView;
        }

        private static class ViewHolder {
            private Hashtable<String, View> holder = new Hashtable<>();

            public void setView(String k, View v) {
                holder.put(k, v);
            }

            public View getView(String k) {
                return holder.get(k);
            }

            public <T> T getView(String k, Class<T> type) {
                return type.cast(getView(k));
            }
        }
    }
}
