package com.sendbird.android.sample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.ChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.model.Channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class SendBirdChannelListActivity extends FragmentActivity {
    private SendBirdChannelListFragment mSendBirdChannelListFragment;

    private ImageButton mBtnClose;
    private ImageButton mBtnSettings;
    private TextView mTxtChannelUrl;
    private View mTopBarContainer;

    public static Bundle makeSendBirdArgs(String appKey, String uuid, String nickname) {
        Bundle args = new Bundle();
        args.putString("appKey", appKey);
        args.putString("uuid", uuid);
        args.putString("nickname", nickname);
        return args;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.sendbird_slide_in_from_bottom, R.anim.sendbird_slide_out_to_top);
        setContentView(R.layout.activity_sendbird_channel_list);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initSendBird(getIntent().getExtras());
        initFragment();
        initUIComponents();
    }
    private void initSendBird(Bundle extras) {
        String appKey = extras.getString("appKey");
        String uuid = extras.getString("uuid");
        String nickname = extras.getString("nickname");

        SendBird.init(this, appKey);
        SendBird.login(SendBird.LoginOption.build(uuid).setUserName(nickname));
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.sendbird_slide_in_from_top, R.anim.sendbird_slide_out_to_bottom);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void initFragment() {
        mSendBirdChannelListFragment = new SendBirdChannelListFragment();
        mSendBirdChannelListFragment.setSendBirdChannelListHandler(new SendBirdChannelListFragment.SendBirdChannelListHandler() {
            @Override
            public void onChannelSelected(Channel channel) {
                Intent data = new Intent();
                data.putExtra("channelUrl", channel.getUrl());
                setResult(RESULT_OK, data);
                finish();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdChannelListFragment)
                .commit();
    }

    private void initUIComponents() {
        mTopBarContainer = findViewById(R.id.top_bar_container);
        mTxtChannelUrl = (TextView)findViewById(R.id.txt_channel_url);

        mBtnClose = (ImageButton)findViewById(R.id.btn_close);
        mBtnSettings = (ImageButton)findViewById(R.id.btn_settings);

        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SendBirdChannelListActivity.this)
                        .setTitle("SendBird")
                        .setMessage("SendBird In App version " + SendBird.VERSION)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
            }
        });

        resizeMenubar();
    }

    public static class SendBirdChannelListFragment extends Fragment {
        private SendBirdChannelListHandler mHandler;
        private ListView mListView;
        private ChannelListQuery mChannelListQuery;
        private ChannelListQuery mChannelListSearchQuery;
        private SendBirdChannelAdapter mCurrentAdapter;
        private SendBirdChannelAdapter mAdapter;
        private SendBirdChannelAdapter mSearchAdapter;
        private EditText mEtxtSearch;

        public static interface SendBirdChannelListHandler {
            public void onChannelSelected(Channel channel);
        }

        public void setSendBirdChannelListHandler(SendBirdChannelListHandler handler) {
            mHandler = handler;
        }

        public SendBirdChannelListFragment() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sendbird_fragment_channel_list, container, false);
            initUIComponents(rootView);
            mChannelListQuery = SendBird.queryChannelList();
            mChannelListQuery.next(new ChannelListQuery.ChannelListQueryResult() {
                @Override
                public void onResult(Collection<Channel> channels) {
                    mAdapter.addAll(channels);
                    if(channels.size() <= 0) {
                        Toast.makeText(getActivity(), "No channels were found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                }
            });

            showChannelList();

            return rootView;

        }
        private void initUIComponents(View rootView) {
            mListView = (ListView)rootView.findViewById(R.id.list);
            mAdapter = new SendBirdChannelAdapter(getActivity());
            mSearchAdapter = new SendBirdChannelAdapter(getActivity());
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Channel channel = mCurrentAdapter.getItem(position);
                    if(mHandler != null) {
                        mHandler.onChannelSelected(channel);
                    }
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

            mEtxtSearch = (EditText)rootView.findViewById(R.id.etxt_search);
            mEtxtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.length() <= 0) {
                        showChannelList();
                    } else {
                        showSearchList();
                    }
                }
            });
            mEtxtSearch.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        search(mEtxtSearch.getText().toString());
                    }
                    return false;
                }
            });
        }

        private void loadMoreChannels() {
            if(mCurrentAdapter == mAdapter) {
                if(mChannelListQuery != null && mChannelListQuery.hasNext() && !mChannelListQuery.isLoading()) {
                    mChannelListQuery.next(new ChannelListQuery.ChannelListQueryResult() {
                        @Override
                        public void onResult(Collection<Channel> channels) {
                            mAdapter.addAll(channels);
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
                }
            } else if(mCurrentAdapter == mSearchAdapter) {
                if(mChannelListSearchQuery != null && mChannelListSearchQuery.hasNext() && !mChannelListSearchQuery.isLoading()) {
                    mChannelListSearchQuery.next(new ChannelListQuery.ChannelListQueryResult() {
                        @Override
                        public void onResult(Collection<Channel> channels) {
                            mSearchAdapter.addAll(channels);
                            mSearchAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            mAdapter.notifyDataSetChanged();
            mSearchAdapter.notifyDataSetChanged();
        }

        private void showChannelList() {
            mCurrentAdapter = mAdapter;
            mListView.setAdapter(mCurrentAdapter);
            mCurrentAdapter.notifyDataSetChanged();
        }

        private void showSearchList() {
            mCurrentAdapter = mSearchAdapter;
            mListView.setAdapter(mCurrentAdapter);
            mCurrentAdapter.notifyDataSetChanged();
        }

        private void search(String keyword) {
            if(keyword == null || keyword.length() <= 0) {
                showChannelList();
                return;
            }

            showSearchList();

            mChannelListSearchQuery = SendBird.queryChannelList(keyword);
            mChannelListSearchQuery.next(new ChannelListQuery.ChannelListQueryResult() {
                @Override
                public void onResult(Collection<Channel> channels) {
                    mSearchAdapter.clear();
                    mSearchAdapter.addAll(channels);
                    mSearchAdapter.notifyDataSetChanged();
                    if(channels.size() <= 0) {
                        Toast.makeText(getActivity(), "No channels were found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                }
            });
        }

        public static class SendBirdChannelAdapter extends BaseAdapter {
            private final LayoutInflater mInflater;
            private final ArrayList<Channel> mItemList;

            public SendBirdChannelAdapter(Context context) {
                mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mItemList = new ArrayList<Channel>();
            }

            @Override
            public int getCount() {
                return mItemList.size();
            }

            @Override
            public Channel getItem(int position) {
                return mItemList.get(position);
            }

            public void clear() {
                mItemList.clear();
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            public void add(Channel channel) {
                mItemList.add(channel);
                notifyDataSetChanged();
            }

            public void addAll(Collection<Channel> channels) {
                mItemList.addAll(channels);
                notifyDataSetChanged();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;

                if(convertView == null) {
                    viewHolder = new ViewHolder();

                    convertView = mInflater.inflate(R.layout.sendbird_view_channel, parent, false);
                    viewHolder.setView("selected_container", convertView.findViewById(R.id.selected_container));
                    viewHolder.getView("selected_container").setVisibility(View.GONE);
                    viewHolder.setView("img_thumbnail", convertView.findViewById(R.id.img_thumbnail));
                    viewHolder.setView("txt_topic", convertView.findViewById(R.id.txt_topic));
                    viewHolder.setView("txt_desc", convertView.findViewById(R.id.txt_desc));

                    convertView.setTag(viewHolder);
                }

                Channel item = getItem(position);
                viewHolder = (ViewHolder) convertView.getTag();
                displayUrlImage(viewHolder.getView("img_thumbnail", ImageView.class), item.getCoverUrl());
                viewHolder.getView("txt_topic", TextView.class).setText("#" + item.getUrlWithoutAppPrefix());
                viewHolder.getView("txt_desc", TextView.class).setText("" + item.getMemberCount() + ((item.getMemberCount() <= 1) ? " Member" : " Members"));

                if(item.getUrl().equals(SendBird.getChannelUrl())) {
                    viewHolder.getView("selected_container").setVisibility(View.VISIBLE);
                } else {
                    viewHolder.getView("selected_container").setVisibility(View.GONE);
                }

                return convertView;
            }

            private static class ViewHolder {
                private Hashtable<String, View> holder = new Hashtable<String, View>();

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

    private static void displayUrlImage(ImageView imageView, String url) {
        UrlDownloadAsyncTask.display(url, imageView);
    }

    private static class UrlDownloadAsyncTask extends AsyncTask<Void, Void, Object> {
        private static LRUCache cache = new LRUCache((int) (Runtime.getRuntime().maxMemory() / 16)); // 1/16th of the maximum memory.
        private final UrlDownloadAsyncTaskHandler handler;
        private String url;

        public static void download(String url, final File downloadFile, final Context context) {
            UrlDownloadAsyncTask task = new UrlDownloadAsyncTask(url, new UrlDownloadAsyncTaskHandler() {
                @Override
                public void onPreExecute() {
                    Toast.makeText(context, "Start downloading", Toast.LENGTH_SHORT).show();
                }

                @Override
                public Object doInBackground(File file) {
                    if(file == null) {
                        return null;
                    }

                    try {
                        BufferedInputStream in = null;
                        BufferedOutputStream out = null;

                        //create output directory if it doesn't exist
                        File dir = downloadFile.getParentFile();
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        in = new BufferedInputStream(new FileInputStream(file));
                        out = new BufferedOutputStream(new FileOutputStream(downloadFile));

                        byte[] buffer = new byte[1024 * 100];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        in.close();
                        out.flush();
                        out.close();

                        return downloadFile;
                    } catch(IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                public void onPostExecute(Object object, UrlDownloadAsyncTask task) {
                    if(object != null && object instanceof File) {
                        Toast.makeText(context, "Finish downloading: " + ((File)object).getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Error downloading", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        }

        public static void display(String url, final ImageView imageView) {
            UrlDownloadAsyncTask task = null;

            if(imageView.getTag() != null && imageView.getTag() instanceof UrlDownloadAsyncTask) {
                try {
                    task = (UrlDownloadAsyncTask) imageView.getTag();
                    task.cancel(true);
                } catch(Exception e) {}

                imageView.setTag(null);
            }

            task = new UrlDownloadAsyncTask(url, new UrlDownloadAsyncTaskHandler() {
                @Override
                public void onPreExecute() {
                    imageView.setImageResource(R.drawable.sendbird_img_placeholder);
                }

                @Override
                public Object doInBackground(File file) {
                    if(file == null) {
                        return null;
                    }

                    Bitmap bm = null;
                    try {
                        int targetHeight = 256;
                        int targetWidth = 256;

                        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
                        bin.mark(bin.available());

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(bin, null, options);

                        Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) >= Math.abs(options.outWidth - targetWidth);

                        if(options.outHeight * options.outWidth >= targetHeight * targetWidth) {
                            double sampleSize = scaleByHeight
                                    ? options.outHeight / targetHeight
                                    : options.outWidth / targetWidth;
                            options.inSampleSize = (int)Math.pow(2d, Math.floor(Math.log(sampleSize)/Math.log(2d)));
                        }

                        try {
                            bin.reset();
                        } catch(IOException e) {
                            bin = new BufferedInputStream(new FileInputStream(file));
                        }

                        // Do the actual decoding
                        options.inJustDecodeBounds = false;
                        bm = BitmapFactory.decodeStream(bin, null, options);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return bm;
                }

                @Override
                public void onPostExecute(Object object, UrlDownloadAsyncTask task) {
                    if(object != null && object instanceof Bitmap && imageView.getTag() == task) {
                        imageView.setImageBitmap((Bitmap)object);
                    }
                }
            });

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }

            imageView.setTag(task);
        }

        public UrlDownloadAsyncTask(String url, UrlDownloadAsyncTaskHandler handler) {
            this.handler = handler;
            this.url = url;
        }

        public interface UrlDownloadAsyncTaskHandler {
            public void onPreExecute();
            public Object doInBackground(File file);
            public void onPostExecute(Object object, UrlDownloadAsyncTask task);
        }

        @Override
        protected void onPreExecute() {
            if(handler != null) {
                handler.onPreExecute();
            }
        }

        protected Object doInBackground(Void... args) {
            File outFile = null;
            try {
                if(cache.get(url) != null && new File(cache.get(url)).exists()) { // Cache Hit
                    outFile = new File(cache.get(url));
                } else { // Cache Miss, Downloading a file from the url.
                    outFile = File.createTempFile("sendbird-download", ".tmp");
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile));

                    InputStream input = new BufferedInputStream(new URL(url).openStream());
                    byte[] buf = new byte[1024 * 100];
                    int read = 0;
                    while ((read = input.read(buf, 0, buf.length)) >= 0) {
                        outputStream.write(buf, 0, read);
                    }

                    outputStream.flush();
                    outputStream.close();
                    cache.put(url, outFile.getAbsolutePath());
                }



            } catch(IOException e) {
                e.printStackTrace();

                if(outFile != null) {
                    outFile.delete();
                }

                outFile = null;
            }


            if(handler != null) {
                return handler.doInBackground(outFile);
            }

            return outFile;
        }

        protected void onPostExecute(Object result) {
            if(handler != null) {
                handler.onPostExecute(result, this);
            }
        }

        private static class LRUCache {
            private final int maxSize;
            private int totalSize;
            private ConcurrentLinkedQueue<String> queue;
            private ConcurrentHashMap<String, String> map;

            public LRUCache(final int maxSize) {
                this.maxSize = maxSize;
                this.queue	= new ConcurrentLinkedQueue<String>();
                this.map	= new ConcurrentHashMap<String, String>();
            }

            public String get(final String key) {
                if (map.containsKey(key)) {
                    queue.remove(key);
                    queue.add(key);
                }

                return map.get(key);
            }

            public synchronized void put(final String key, final String value) {
                if(key == null || value == null) {
                    throw new NullPointerException();
                }

                if (map.containsKey(key)) {
                    queue.remove(key);
                }

                queue.add(key);
                map.put(key, value);
                totalSize = totalSize + getSize(value);

                while (totalSize >= maxSize) {
                    String expiredKey = queue.poll();
                    if (expiredKey != null) {
                        totalSize = totalSize - getSize(map.remove(expiredKey));
                    }
                }
            }

            private int getSize(String value) {
                return value.length();
            }
        }
    }
}
