package com.sendbird.android.sample;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class SendBirdUserListActivity extends FragmentActivity {
    private SendBirdUserListFragment mSendBirdUserListFragment;

    private View mTopBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.sendbird_slide_in_from_bottom, R.anim.sendbird_slide_out_to_top);
        setContentView(R.layout.activity_sendbird_user_list);
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
        mSendBirdUserListFragment = new SendBirdUserListFragment();
        mSendBirdUserListFragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdUserListFragment)
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

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] userIds = mSendBirdUserListFragment.mSelectedUserIds.toArray(new String[0]);
                if (userIds.length > 0) {
                    Intent data = new Intent();
                    data.putExtra("user_ids", userIds);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });

        resizeMenubar();
    }

    public static class SendBirdUserListFragment extends Fragment {
        private ListView mListView;
        private UserListQuery mUserListQuery;
        private SendBirdUserAdapter mAdapter;
        private HashSet<String> mSelectedUserIds;

        public SendBirdUserListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sendbird_fragment_user_list, container, false);
            initUIComponents(rootView);

            mUserListQuery = SendBird.createUserListQuery();
            loadMoreUsers();

            return rootView;

        }

        private void initUIComponents(View rootView) {
            mSelectedUserIds = new HashSet<>();
            mListView = (ListView) rootView.findViewById(R.id.list);
            mAdapter = new SendBirdUserAdapter(getActivity());

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
            if (mUserListQuery != null && mUserListQuery.hasNext() && !mUserListQuery.isLoading()) {
                mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
                    @Override
                    public void onResult(List<User> list, SendBirdException e) {
                        if (e != null) {
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
                mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

                if (convertView == null) {
                    viewHolder = new ViewHolder();

                    convertView = mInflater.inflate(R.layout.sendbird_view_user, parent, false);
                    viewHolder.setView("root_view", convertView);
                    viewHolder.setView("img_thumbnail", convertView.findViewById(R.id.img_thumbnail));
                    viewHolder.setView("txt_name", convertView.findViewById(R.id.txt_name));
                    viewHolder.setView("chk_select", convertView.findViewById(R.id.chk_select));
                    viewHolder.setView("txt_status", convertView.findViewById(R.id.txt_status));

                    convertView.setTag(viewHolder);
                }

                final User item = getItem(position);
                viewHolder = (ViewHolder) convertView.getTag();
                Helper.displayUrlImage(viewHolder.getView("img_thumbnail", ImageView.class), item.getProfileUrl());
                viewHolder.getView("txt_name", TextView.class).setText(item.getNickname());
                viewHolder.getView("chk_select", CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mSelectedUserIds.add(item.getUserId());
                        } else {
                            mSelectedUserIds.remove(item.getUserId());
                        }
                        if (mSelectedUserIds.size() <= 0) {
                            ((Button) getActivity().findViewById(R.id.btn_ok)).setTextColor(Color.parseColor("#6f5ca7"));
                        } else {
                            ((Button) getActivity().findViewById(R.id.btn_ok)).setTextColor(Color.parseColor("#35f8ca"));
                        }
                    }
                });
                viewHolder.getView("chk_select", CheckBox.class).setChecked(mSelectedUserIds.contains(item.getUserId()));
                if (item.getConnectionStatus() == User.ConnectionStatus.ONLINE) {
                    viewHolder.getView("txt_status", TextView.class).setText("Online");
                } else {
                    viewHolder.getView("txt_status", TextView.class).setText("");
                }
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
