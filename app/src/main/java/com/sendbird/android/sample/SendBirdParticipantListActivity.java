package com.sendbird.android.sample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public class SendBirdParticipantListActivity extends FragmentActivity {
    private SendBirdParticipantListFragment mSendBirdParticipantListFragment;

    private View mTopBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.sendbird_slide_in_from_bottom, R.anim.sendbird_slide_out_to_top);
        setContentView(R.layout.activity_sendbird_participant_list);
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
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
        mSendBirdParticipantListFragment = new SendBirdParticipantListFragment();
        mSendBirdParticipantListFragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdParticipantListFragment)
                .commit();
    }

    private void initUIComponents() {
        mTopBarContainer = findViewById(R.id.top_bar_container);

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        resizeMenubar();
    }

    public static class SendBirdParticipantListFragment extends Fragment {
        private ListView mListView;
        private UserListQuery mUserListQuery;
        private SendBirdUserAdapter mAdapter;
        private String mChannelUrl;

        public SendBirdParticipantListFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sendbird_fragment_participant_list, container, false);
            initUIComponents(rootView);

            mChannelUrl = getArguments().getString("channel_url");

            OpenChannel.getChannel(mChannelUrl, new OpenChannel.OpenChannelGetHandler() {
                @Override
                public void onResult(final OpenChannel openChannel, SendBirdException e) {
                    if(e != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    openChannel.refresh(new OpenChannel.OpenChannelRefreshHandler() {
                        @Override
                        public void onResult(SendBirdException e) {
                            if (e != null) {
                                Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ((TextView)getActivity().findViewById(R.id.txt_channel_name)).setText("Participants (" + openChannel.getParticipantCount() + ")");
                            mUserListQuery = openChannel.createParticipantListQuery();
                            loadMoreUsers();
                        }
                    });
                }
            });

            return rootView;
        }

        private void initUIComponents(View rootView) {
            mListView = (ListView)rootView.findViewById(R.id.list);
            mAdapter = new SendBirdUserAdapter(getActivity());

            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("User Block")
                            .setMessage("Do you want to block the user?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SendBird.blockUser(mAdapter.getItem(position), new SendBird.UserBlockHandler() {
                                        @Override
                                        public void onBlocked(User user, SendBirdException e) {
                                            if (e != null) {
                                                Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            Toast.makeText(getActivity(), user.getNickname() + " is blocked.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create().show();

                    return true;
                }
            });
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem + visibleItemCount >= (int) (totalItemCount * 0.8f)) {
                        loadMoreUsers();
                    }
                }
            });
            mListView.setAdapter(mAdapter);
        }

        private void loadMoreUsers() {
            if(mUserListQuery != null && mUserListQuery.hasNext() && !mUserListQuery.isLoading()) {
                mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
                    @Override
                    public void onResult(List<User> list, SendBirdException e) {
                        if(e != null) {
                            Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mAdapter.addAll(list);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        public class SendBirdUserAdapter extends BaseAdapter {
            private final Context mContext;
            private final LayoutInflater mInflater;
            private final ArrayList<User> mItemList;

            public SendBirdUserAdapter(Context context) {
                mContext = context;
                mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mItemList = new ArrayList<>();
            }

            @Override
            public int getCount() {
                return mItemList.size();
            }

            @Override
            public User getItem(int position) {
                return mItemList.get(position);
            }

            public void clear() {
                mItemList.clear();
            }

            public User remove(int index) {
                return mItemList.remove(index);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            public void addAll(Collection<User> users) {
                mItemList.addAll(users);
                notifyDataSetChanged();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;

                if(convertView == null) {
                    viewHolder = new ViewHolder();

                    convertView = mInflater.inflate(R.layout.sendbird_view_user, parent, false);
                    viewHolder.setView("root_view", convertView);
                    viewHolder.setView("img_thumbnail", convertView.findViewById(R.id.img_thumbnail));
                    viewHolder.setView("txt_name", convertView.findViewById(R.id.txt_name));
                    viewHolder.setView("txt_status", convertView.findViewById(R.id.txt_status));
                    viewHolder.setView("chk_select", convertView.findViewById(R.id.chk_select));
                    viewHolder.getView("chk_select", CheckBox.class).setVisibility(View.GONE);

                    convertView.setTag(viewHolder);
                }

                final User item = getItem(position);
                viewHolder = (ViewHolder) convertView.getTag();
                Helper.displayUrlImage(viewHolder.getView("img_thumbnail", ImageView.class), item.getProfileUrl());
                viewHolder.getView("txt_name", TextView.class).setText(item.getNickname());
                viewHolder.getView("txt_status", TextView.class).setText(""); // Always online
                return convertView;
            }

            private class ViewHolder {
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
