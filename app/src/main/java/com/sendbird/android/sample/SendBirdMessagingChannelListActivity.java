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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.MessagingChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdEventHandler;
import com.sendbird.android.SendBirdNotificationHandler;
import com.sendbird.android.model.BroadcastMessage;
import com.sendbird.android.model.Channel;
import com.sendbird.android.model.FileLink;
import com.sendbird.android.model.Mention;
import com.sendbird.android.model.Message;
import com.sendbird.android.model.MessagingChannel;
import com.sendbird.android.model.ReadStatus;
import com.sendbird.android.model.SystemMessage;
import com.sendbird.android.model.TypeStatus;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class SendBirdMessagingChannelListActivity extends FragmentActivity {
    private SendBirdMessagingChannelListFragment mSendBirdMessagingChannelListFragment;
    private SendBirdMessagingChannelAdapter mSendBirdMessagingChannelAdapter;

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
        setContentView(R.layout.activity_sendbird_messaging_channel_list);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initFragment();
        initUIComponents();

        initSendBird(getIntent().getExtras());
        Toast.makeText(this, "Long press the channel to leave.", Toast.LENGTH_LONG).show();
    }

    private void initSendBird(Bundle extras) {
        if(extras != null) {
            String appKey = extras.getString("appKey");
            String uuid = extras.getString("uuid");
            String nickname = extras.getString("nickname");

            SendBird.init(this, appKey);
            SendBird.login(SendBird.LoginOption.build(uuid).setUserName(nickname));

            SendBird.registerNotificationHandler(new SendBirdNotificationHandler() {
                @Override
                public void onMessagingChannelUpdated(MessagingChannel messagingChannel) {
                    mSendBirdMessagingChannelAdapter.replace(messagingChannel);
                }

                @Override
                public void onMentionUpdated(Mention mention) {

                }
            });

            SendBird.setEventHandler(new SendBirdEventHandler() {
                @Override
                public void onConnect(Channel channel) {

                }

                @Override
                public void onError(int code) {
                    Log.e("SendBird", "Error code: " + code);
                }

                @Override
                public void onChannelLeft(Channel channel) {

                }

                @Override
                public void onMessageReceived(Message message) {

                }

                @Override
                public void onSystemMessageReceived(SystemMessage systemMessage) {

                }

                @Override
                public void onBroadcastMessageReceived(BroadcastMessage broadcastMessage) {

                }

                @Override
                public void onFileReceived(FileLink fileLink) {

                }

                @Override
                public void onReadReceived(ReadStatus readStatus) {

                }

                @Override
                public void onTypeStartReceived(TypeStatus typeStatus) {

                }

                @Override
                public void onTypeEndReceived(TypeStatus typeStatus) {

                }

                @Override
                public void onAllDataReceived(SendBird.SendBirdDataType sendbirdDataType, int i) {

                }

                @Override
                public void onMessageDelivery(boolean b, String s, String s2, String s3) {

                }

                @Override
                public void onMessagingStarted(MessagingChannel messagingChannel) {

                }

                @Override
                public void onMessagingUpdated(MessagingChannel messagingChannel) {

                }

                @Override
                public void onMessagingEnded(MessagingChannel messagingChannel) {
                }

                @Override
                public void onAllMessagingEnded() {
                }

                @Override
                public void onMessagingHidden(MessagingChannel messagingChannel) {

                }

                @Override
                public void onAllMessagingHidden() {
                }
            });

        }
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
        SendBird.join("");
        SendBird.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SendBird.disconnect();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.sendbird_slide_in_from_top, R.anim.sendbird_slide_out_to_bottom);
    }

    private void initFragment() {
        mSendBirdMessagingChannelAdapter = new SendBirdMessagingChannelAdapter(this);

        mSendBirdMessagingChannelListFragment = new SendBirdMessagingChannelListFragment();
        mSendBirdMessagingChannelListFragment.setSendBirdMessagingChannelAdapter(mSendBirdMessagingChannelAdapter);
        mSendBirdMessagingChannelListFragment.setSendBirdMessagingChannelListHandler(new SendBirdMessagingChannelListFragment.SendBirdMessagingChannelListHandler() {
            @Override
            public void onMessagingChannelSelected(MessagingChannel messagingChannel) {
                Intent data = new Intent();
                data.putExtra("channelUrl", messagingChannel.getUrl());
                setResult(RESULT_OK, data);
                finish();
            }

        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdMessagingChannelListFragment)
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
                new AlertDialog.Builder(SendBirdMessagingChannelListActivity.this)
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

    public static class SendBirdMessagingChannelListFragment extends Fragment {
        private SendBirdMessagingChannelListHandler mHandler;
        private ListView mListView;
        private SendBirdMessagingChannelAdapter mAdapter;
        private Channel mCurrentChannel;
        private MessagingChannelListQuery mMessagingChannelListQuery;

        public static interface SendBirdMessagingChannelListHandler {
            public void onMessagingChannelSelected(MessagingChannel channel);
        }

        public void setSendBirdMessagingChannelAdapter(SendBirdMessagingChannelAdapter adapter) {
            mAdapter = adapter;
            if(mListView != null) {
                mListView.setAdapter(adapter);
            }
        }

        public void setSendBirdMessagingChannelListHandler(SendBirdMessagingChannelListHandler handler) {
            mHandler = handler;
        }

        public SendBirdMessagingChannelListFragment() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sendbird_fragment_messaging_channel_list, container, false);
            initUIComponents(rootView);
            return rootView;

        }
        private void initUIComponents(View rootView) {
            mListView = (ListView)rootView.findViewById(R.id.list);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MessagingChannel channel = mAdapter.getItem(position);
                    if(mHandler != null) {
                        mHandler.onMessagingChannelSelected(channel);
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
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    final MessagingChannel channel = mAdapter.getItem(position);
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Leave")
                            .setMessage("Do you want to leave this channel?")
                            .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mAdapter.remove(position);
                                    mAdapter.notifyDataSetChanged();
                                    SendBird.endMessaging(channel.getUrl());
                                }
                            })
                            .setNeutralButton("Hide", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mAdapter.remove(position);
                                    mAdapter.notifyDataSetChanged();
                                    SendBird.hideMessaging(channel.getUrl());
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create().show();
                    return true;
                }
            });
            mListView.setAdapter(mAdapter);
        }

        private void loadMoreChannels() {
            if(mMessagingChannelListQuery == null) {
                mMessagingChannelListQuery = SendBird.queryMessagingChannelList();
                mMessagingChannelListQuery.setLimit(30);
            }

            if(mMessagingChannelListQuery.isLoading()) {
                return;
            }

            if(mMessagingChannelListQuery.hasNext()) {
                mMessagingChannelListQuery.next(new MessagingChannelListQuery.MessagingChannelListQueryResult() {
                    @Override
                    public void onResult(List<MessagingChannel> messagingChannels) {
                        mAdapter.addAll(messagingChannels);
                    }

                    @Override
                    public void onError(int i) {
                    }
                });
            }
        }


        @Override
        public void onResume() {
            super.onResume();
            try {
                mCurrentChannel = SendBird.getCurrentChannel();
                mAdapter.setCurrentChannelId(mCurrentChannel.getId());
            } catch (IOException e) {
            }

            mAdapter.notifyDataSetChanged();
        }

    }

    public static class SendBirdMessagingChannelAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ArrayList<MessagingChannel> mItemList;
        private long mCurrentChannelId;

        public static List<MessagingChannel> sortMessagingChannels(List<MessagingChannel> messagingChannels) {
            Collections.sort(messagingChannels, new Comparator<MessagingChannel>() {
                @Override
                public int compare(MessagingChannel lhs, MessagingChannel rhs) {
                    long lhsv = lhs.getLastMessageTimestamp();
                    long rhsv = rhs.getLastMessageTimestamp();
                    return (lhsv == rhsv) ? 0 : (lhsv < rhsv) ? 1 : -1;
                }
            });

            return messagingChannels;
        }


        public SendBirdMessagingChannelAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItemList = new ArrayList<MessagingChannel>();
        }

        public void setCurrentChannelId(long channelId) {
            mCurrentChannelId = channelId;
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public MessagingChannel getItem(int position) {
            return mItemList.get(position);
        }

        public void clear() {
            mItemList.clear();
        }

        public MessagingChannel remove(int index) {
            return mItemList.remove(index);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void add(MessagingChannel channel) {
            mItemList.add(channel);
            notifyDataSetChanged();
        }

        public void addAll(List<MessagingChannel> channels) {
            mItemList.addAll(channels);
            notifyDataSetChanged();
        }

        public void replace(MessagingChannel newChannel) {
            for(MessagingChannel oldChannel : mItemList) {
                if(oldChannel.getId() == newChannel.getId()) {
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

            if(convertView == null) {
                viewHolder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.sendbird_view_messaging_channel, parent, false);
                viewHolder.setView("selected_container", convertView.findViewById(R.id.selected_container));
                viewHolder.getView("selected_container").setVisibility(View.GONE);
                viewHolder.setView("img_thumbnail", convertView.findViewById(R.id.img_thumbnail));
                viewHolder.setView("txt_topic", convertView.findViewById(R.id.txt_topic));
                viewHolder.setView("txt_member_count", convertView.findViewById(R.id.txt_member_count));
                viewHolder.setView("txt_unread_count", convertView.findViewById(R.id.txt_unread_count));
                viewHolder.setView("txt_date", convertView.findViewById(R.id.txt_date));
                viewHolder.setView("txt_desc", convertView.findViewById(R.id.txt_desc));

                convertView.setTag(viewHolder);
            }

            MessagingChannel item = getItem(position);
            viewHolder = (ViewHolder) convertView.getTag();
            displayUrlImage(viewHolder.getView("img_thumbnail", ImageView.class), getDisplayCoverImageUrl(item.getMembers()));
            viewHolder.getView("txt_topic", TextView.class).setText(getDisplayMemberNames(item.getMembers()));

            if(item.getUnreadMessageCount() > 0) {
                viewHolder.getView("txt_unread_count", TextView.class).setVisibility(View.VISIBLE);
                viewHolder.getView("txt_unread_count", TextView.class).setText("" + item.getUnreadMessageCount());
            } else {
                viewHolder.getView("txt_unread_count", TextView.class).setVisibility(View.INVISIBLE);
            }

            if(item.isGroupMessagingChannel()) {
                viewHolder.getView("txt_member_count", TextView.class).setVisibility(View.VISIBLE);
                viewHolder.getView("txt_member_count", TextView.class).setText("" + item.getMemberCount());
            } else {
                viewHolder.getView("txt_member_count", TextView.class).setVisibility(View.GONE);
            }

            if(item.hasLastMessage()) {
                Message message = item.getLastMessage();
                viewHolder.getView("txt_date", TextView.class).setText(getDisplayTimeOrDate(mContext, message.getTimestamp()));
                viewHolder.getView("txt_desc", TextView.class).setText("" + message.getMessage());
            } else {
                viewHolder.getView("txt_date", TextView.class).setText("");
                viewHolder.getView("txt_desc", TextView.class).setText("");
            }

            if(mCurrentChannelId == item.getId()) {
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

    private static String getDisplayCoverImageUrl(List<MessagingChannel.Member> members) {
        for(MessagingChannel.Member member : members) {
            if(member.getId().equals(SendBird.getUserId())) {
                continue;
            }

            return member.getImageUrl();
        }

        return "";
    }
    private static String getDisplayMemberNames(List<MessagingChannel.Member> members) {
        if(members.size() < 2) {
            return "No Members";
        }

        StringBuffer names = new StringBuffer();
        for(MessagingChannel.Member member : members) {
            if(member.getId().equals(SendBird.getUserId())) {
                continue;
            }

            names.append(", " + member.getName());
        }
        return names.delete(0, 2).toString();
    }

    private static String getDisplayTimeOrDate(Context context, long milli) {
        Date date = new Date(milli);

        if(System.currentTimeMillis() - milli > 60 * 60 * 24 * 1000l) {
            return DateFormat.getDateFormat(context).format(date);
        } else {
            return DateFormat.getTimeFormat(context).format(date);
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
                    } else {
                        imageView.setImageResource(R.drawable.sendbird_img_placeholder);
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
