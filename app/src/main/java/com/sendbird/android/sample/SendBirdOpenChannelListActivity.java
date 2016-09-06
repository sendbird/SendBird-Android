package com.sendbird.android.sample;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.OpenChannel;
import com.sendbird.android.OpenChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public class SendBirdOpenChannelListActivity extends FragmentActivity {
    private SendBirdChannelListFragment mSendBirdChannelListFragment;

    private View mTopBarContainer;
    private View mSettingsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.sendbird_slide_in_from_bottom, R.anim.sendbird_slide_out_to_top);
        setContentView(R.layout.activity_sendbird_open_channel_list);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initFragment();
        initUIComponents();
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
        mSendBirdChannelListFragment = new SendBirdChannelListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdChannelListFragment)
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
                View view = SendBirdOpenChannelListActivity.this.getLayoutInflater().inflate(R.layout.sendbird_view_open_create_channel, null);
                final EditText chName = (EditText) view.findViewById(R.id.etxt_chname);

                new AlertDialog.Builder(SendBirdOpenChannelListActivity.this)
                        .setView(view)
                        .setTitle("Create Open Channel")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<User> operators = new ArrayList<>();
                                operators.add(SendBird.getCurrentUser());

                                OpenChannel.createChannel(chName.getText().toString(), null, null, operators, new OpenChannel.OpenChannelCreateHandler() {
                                    @Override
                                    public void onResult(OpenChannel openChannel, SendBirdException e) {
                                        if (e != null) {
                                            Toast.makeText(SendBirdOpenChannelListActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        if (!mSendBirdChannelListFragment.mChannelListQuery.hasNext()) {
                                            mSendBirdChannelListFragment.mAdapter.add(openChannel);
                                        }

                                        Intent intent = new Intent(SendBirdOpenChannelListActivity.this, SendBirdOpenChatActivity.class);
                                        intent.putExtra("channel_url", openChannel.getUrl());
                                        startActivity(intent);
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null).create().show();

                mSettingsContainer.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.btn_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SendBirdOpenChannelListActivity.this)
                        .setTitle("SendBird")
                        .setMessage("SendBird SDK " + SendBird.getSDKVersion())
                        .setPositiveButton("OK", null).create().show();

                mSettingsContainer.setVisibility(View.GONE);
            }
        });

        resizeMenubar();
    }

    public static class SendBirdChannelListFragment extends Fragment {
        private ListView mListView;
        private OpenChannelListQuery mChannelListQuery;
        private SendBirdChannelAdapter mAdapter;

        public SendBirdChannelListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sendbird_fragment_open_channel_list, container, false);
            initUIComponents(rootView);

            mListView.setAdapter(mAdapter);

            return rootView;
        }

        private void initUIComponents(View rootView) {
            mListView = (ListView) rootView.findViewById(R.id.list);
            mAdapter = new SendBirdChannelAdapter(getActivity());

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    OpenChannel channel = mAdapter.getItem(position);
                    Intent intent = new Intent(getActivity(), SendBirdOpenChatActivity.class);
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
                        loadMoreChannels();
                    }
                }
            });
        }

        private void loadMoreChannels() {
            if (mChannelListQuery != null && mChannelListQuery.hasNext() && !mChannelListQuery.isLoading()) {
                mChannelListQuery.next(new OpenChannelListQuery.OpenChannelListQueryResultHandler() {
                    @Override
                    public void onResult(List<OpenChannel> channels, SendBirdException e) {
                        if (e != null) {
                            return;
                        }

                        mAdapter.addAll(channels);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            mAdapter.clear();
            mAdapter.notifyDataSetChanged();

            mChannelListQuery = OpenChannel.createOpenChannelListQuery();
            mChannelListQuery.next(new OpenChannelListQuery.OpenChannelListQueryResultHandler() {
                @Override
                public void onResult(List<OpenChannel> channels, SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (channels.size() <= 0) {
                        Toast.makeText(getActivity(), "No channels were found.", Toast.LENGTH_SHORT).show();
                    } else {
                        mAdapter.addAll(channels);
                    }
                }
            });
        }

        public static class SendBirdChannelAdapter extends BaseAdapter {
            private final LayoutInflater mInflater;
            private final ArrayList<OpenChannel> mItemList;

            public SendBirdChannelAdapter(Context context) {
                mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mItemList = new ArrayList<>();
            }

            @Override
            public int getCount() {
                return mItemList.size();
            }

            @Override
            public OpenChannel getItem(int position) {
                return mItemList.get(position);
            }

            public void clear() {
                mItemList.clear();
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            public void add(OpenChannel channel) {
                mItemList.add(channel);
                notifyDataSetChanged();
            }

            public void addAll(Collection<OpenChannel> channels) {
                mItemList.addAll(channels);
                notifyDataSetChanged();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;

                if (convertView == null) {
                    viewHolder = new ViewHolder();

                    convertView = mInflater.inflate(R.layout.sendbird_view_open_channel, parent, false);
                    viewHolder.setView("img_thumbnail", convertView.findViewById(R.id.img_thumbnail));
                    viewHolder.setView("txt_topic", convertView.findViewById(R.id.txt_topic));
                    viewHolder.setView("txt_desc", convertView.findViewById(R.id.txt_desc));

                    convertView.setTag(viewHolder);
                }

                OpenChannel item = getItem(position);
                viewHolder = (ViewHolder) convertView.getTag();
                Helper.displayUrlImage(viewHolder.getView("img_thumbnail", ImageView.class), item.getCoverUrl());
                viewHolder.getView("txt_topic", TextView.class).setText("#" + item.getName());
                viewHolder.getView("txt_desc", TextView.class).setText("" + item.getParticipantCount() + ((item.getParticipantCount() <= 1) ? " Member" : " Members"));

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
}
