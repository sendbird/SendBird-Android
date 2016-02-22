package com.sendbird.android.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.MessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdEventHandler;
import com.sendbird.android.SendBirdFileUploadEventHandler;
import com.sendbird.android.model.BroadcastMessage;
import com.sendbird.android.model.Channel;
import com.sendbird.android.model.FileInfo;
import com.sendbird.android.model.FileLink;
import com.sendbird.android.model.Message;
import com.sendbird.android.model.MessageModel;
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
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class SendBirdChatActivity extends FragmentActivity {
    public static final int REQUEST_CHANNEL_LIST = 100;

    private SendBirdChatFragment mSendBirdChatFragment;
    private SendBirdChatAdapter mSendBirdChatAdapter;

    private ImageButton mBtnClose;
    private ImageButton mBtnSettings;
    private TextView mTxtChannelUrl;
    private View mTopBarContainer;
    private View mSettingsContainer;
    private Button mBtnLeave;
    private String mChannelUrl;
    private boolean mDoNotDisconnect;


    public static Bundle makeSendBirdArgs(String appKey, String uuid, String nickname, String channelUrl) {
        Bundle args = new Bundle();
        args.putString("appKey", appKey);
        args.putString("uuid", uuid);
        args.putString("nickname", nickname);
        args.putString("channelUrl", channelUrl);
        return args;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.sendbird_slide_in_from_bottom, R.anim.sendbird_slide_out_to_top);
        setContentView(R.layout.activity_sendbird_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initFragment();

        initUIComponents();
        initSendBird(getIntent().getExtras());

        SendBird.queryMessageList(mChannelUrl).prev(Long.MAX_VALUE, 50, new MessageListQuery.MessageListQueryResult() {
            @Override
            public void onResult(List<MessageModel> messageModels) {
                for (MessageModel model : messageModels) {
                    mSendBirdChatAdapter.addMessageModel(model);
                }


                mSendBirdChatAdapter.notifyDataSetChanged();
                mSendBirdChatFragment.mListView.setSelection(mSendBirdChatAdapter.getCount());
                SendBird.join(mChannelUrl);
                SendBird.connect(mSendBirdChatAdapter.getMaxMessageTimestamp());
            }

            @Override
            public void onError(Exception e) {

            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
        if(!mDoNotDisconnect) {
            SendBird.disconnect();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.sendbird_slide_in_from_top, R.anim.sendbird_slide_out_to_bottom);
    }

    private void initFragment() {
        mSendBirdChatFragment = new SendBirdChatFragment();

        mSendBirdChatAdapter = new SendBirdChatAdapter(this);
        mSendBirdChatFragment.setSendBirdChatAdapter(mSendBirdChatAdapter);

        mSendBirdChatFragment.setSendBirdChatHandler(new SendBirdChatFragment.SendBirdChatHandler() {

            @Override
            public void onChannelListClicked() {
                Intent intent = new Intent(SendBirdChatActivity.this, SendBirdChannelListActivity.class);
                intent.putExtras(getIntent().getExtras());
                startActivityForResult(intent, REQUEST_CHANNEL_LIST);
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdChatFragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CHANNEL_LIST) {
            if(resultCode == RESULT_OK && data != null) {
                mChannelUrl = data.getStringExtra("channelUrl");


                mSendBirdChatAdapter.clear();
                mSendBirdChatAdapter.notifyDataSetChanged();

                SendBird.queryMessageList(mChannelUrl).prev(Long.MAX_VALUE, 50, new MessageListQuery.MessageListQueryResult() {
                    @Override
                    public void onResult(List<MessageModel> messageModels) {
                        for(MessageModel model : messageModels) {
                            mSendBirdChatAdapter.addMessageModel(model);
                        }


                        mSendBirdChatAdapter.notifyDataSetChanged();
                        mSendBirdChatFragment.mListView.setSelection(mSendBirdChatAdapter.getCount());
                        SendBird.join(mChannelUrl);
                        SendBird.connect(mSendBirdChatAdapter.getMaxMessageTimestamp());
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
        }
    }

    private void initSendBird(Bundle extras) {
        String appKey = extras.getString("appKey");
        String uuid = extras.getString("uuid");
        String nickname = extras.getString("nickname");
        mChannelUrl = extras.getString("channelUrl");

        SendBird.init(this, appKey);
        SendBird.login(SendBird.LoginOption.build(uuid).setUserName(nickname));
        SendBird.setEventHandler(new SendBirdEventHandler() {
            @Override
            public void onConnect(Channel channel) {
                mTxtChannelUrl.setText("#" + channel.getUrlWithoutAppPrefix());
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
                mSendBirdChatAdapter.addMessageModel(message);
            }

            @Override
            public void onSystemMessageReceived(SystemMessage systemMessage) {
                mSendBirdChatAdapter.addMessageModel(systemMessage);
            }

            @Override
            public void onBroadcastMessageReceived(BroadcastMessage broadcastMessage) {
                mSendBirdChatAdapter.addMessageModel(broadcastMessage);
            }

            @Override
            public void onFileReceived(FileLink fileLink) {
                mSendBirdChatAdapter.addMessageModel(fileLink);
            }

            @Override
            public void onAllDataReceived(SendBird.SendBirdDataType type, int count) {
                mSendBirdChatAdapter.notifyDataSetChanged();
                mSendBirdChatFragment.mListView.setSelection(mSendBirdChatAdapter.getCount());
            }

            @Override
            public void onMessageDelivery(boolean sent, String message, String data, String id) {
                if (!sent) {
                    mSendBirdChatFragment.mEtxtMessage.setText(message);
                }
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

    private void initUIComponents() {
        mTopBarContainer = findViewById(R.id.top_bar_container);
        mTxtChannelUrl = (TextView)findViewById(R.id.txt_channel_url);

        mSettingsContainer = findViewById(R.id.settings_container);
        mSettingsContainer.setVisibility(View.GONE);

        mBtnLeave = (Button)findViewById(R.id.btn_leave);

        mBtnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsContainer.setVisibility(View.GONE);
                SendBird.leave(SendBird.getChannelUrl());
                finish();
            }
        });

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
                if(mSettingsContainer.getVisibility() != View.VISIBLE) {
                    mSettingsContainer.setVisibility(View.VISIBLE);
                } else {
                    mSettingsContainer.setVisibility(View.GONE);
                }
            }
        });

        resizeMenubar();
    }

    public static class SendBirdChatFragment extends Fragment {
        private static final int REQUEST_PICK_IMAGE = 100;

        private ListView mListView;
        private SendBirdChatAdapter mAdapter;
        private EditText mEtxtMessage;
        private Button mBtnSend;
        private ImageButton mBtnChannel;
        private ImageButton mBtnUpload;
        private ProgressBar mProgressBtnUpload;
        private SendBirdChatHandler mHandler;

        public static interface SendBirdChatHandler {
            public void onChannelListClicked();
        }

        public void setSendBirdChatHandler(SendBirdChatHandler handler) {
            mHandler = handler;
        }

        public SendBirdChatFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sendbird_fragment_chat, container, false);
            initUIComponents(rootView);
            return rootView;
        }


        private void initUIComponents(View rootView) {
            mListView = (ListView)rootView.findViewById(R.id.list);
            turnOffListViewDecoration(mListView);
            mListView.setAdapter(mAdapter);

            mBtnChannel = (ImageButton)rootView.findViewById(R.id.btn_channel);
            mBtnSend = (Button)rootView.findViewById(R.id.btn_send);
            mBtnUpload = (ImageButton)rootView.findViewById(R.id.btn_upload);
            mProgressBtnUpload = (ProgressBar)rootView.findViewById(R.id.progress_btn_upload);
            mEtxtMessage = (EditText)rootView.findViewById(R.id.etxt_message);

            mBtnSend.setEnabled(false);
            mBtnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    send();
                }
            });


            mBtnChannel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHandler != null) {
                        mHandler.onChannelListClicked();
                    }
                }
            });

            mBtnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE);
                }
            });

            mEtxtMessage.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_ENTER) {
                        if(event.getAction() == KeyEvent.ACTION_DOWN) {
                            send();
                        }
                        return true; // Do not hide keyboard.
                    }
                    return false;
                }
            });
            mEtxtMessage.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            mEtxtMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mBtnSend.setEnabled(s.length() > 0);
                }
            });
            mListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Helper.hideKeyboard(getActivity());
                    return false;
                }
            });
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if(scrollState == SCROLL_STATE_IDLE) {
                        if(view.getFirstVisiblePosition() == 0 && view.getChildCount() > 0 && view.getChildAt(0).getTop() == 0) {
                            SendBird.queryMessageList(SendBird.getChannelUrl()).prev(mAdapter.getMinMessageTimestamp(), 30, new MessageListQuery.MessageListQueryResult() {
                                @Override
                                public void onResult(List<MessageModel> messageModels) {
                                    for(MessageModel model : messageModels) {
                                        mAdapter.addMessageModel(model);
                                    }

                                    mAdapter.notifyDataSetChanged();
                                    mListView.setSelection(messageModels.size());
                                }

                                @Override
                                public void onError(Exception e) {
                                }
                            });
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                }
            });
        }

        private void showUploadProgress(boolean tf) {
            if(tf) {
                mBtnUpload.setEnabled(false);
                mBtnUpload.setVisibility(View.INVISIBLE);
                mProgressBtnUpload.setVisibility(View.VISIBLE);
            } else {
                mBtnUpload.setEnabled(true);
                mBtnUpload.setVisibility(View.VISIBLE);
                mProgressBtnUpload.setVisibility(View.GONE);
            }
        }

        private void turnOffListViewDecoration(ListView listView) {
            listView.setDivider(null);
            listView.setDividerHeight(0);
            listView.setHorizontalFadingEdgeEnabled(false);
            listView.setVerticalFadingEdgeEnabled(false);
            listView.setHorizontalScrollBarEnabled(false);
            listView.setVerticalScrollBarEnabled(true);
            listView.setSelector(new ColorDrawable(0x00ffffff));
            listView.setCacheColorHint(0x00000000); // For Gingerbread scrolling bug fix
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(resultCode == Activity.RESULT_OK) {
                if(requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null) {
                    upload(data.getData());
                }
            }
        }

        private void send() {
            SendBird.send(mEtxtMessage.getText().toString());
            mEtxtMessage.setText("");

            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Helper.hideKeyboard(getActivity());
            }
        }

        private void upload(Uri uri) {
            try {
                Cursor cursor = getActivity().getContentResolver().query(uri,
                        new String[] {
                                MediaStore.Images.Media.DATA,
                                MediaStore.Images.Media.MIME_TYPE,
                                MediaStore.Images.Media.SIZE,
                        },
                        null, null, null);
                cursor.moveToFirst();
                final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                final String mime = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                final int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                cursor.close();

                if(path == null) {
                    Toast.makeText(getActivity(), "Uploading file must be located in local storage.", Toast.LENGTH_LONG).show();
                } else {
                    showUploadProgress(true);
                    SendBird.uploadFile(new File(path), mime, size, "", new SendBirdFileUploadEventHandler() {
                        @Override
                        public void onUpload(FileInfo fileInfo, Exception e) {
                            showUploadProgress(false);
                            if (e != null) {
                                e.printStackTrace();
                                Toast.makeText(getActivity(), "Fail to upload the file.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            SendBird.sendFile(fileInfo);
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Fail to upload the file.", Toast.LENGTH_LONG).show();
            }
        }


        public void setSendBirdChatAdapter(SendBirdChatAdapter adapter) {
            mAdapter = adapter;
            if(mListView != null) {
                mListView.setAdapter(adapter);
            }
        }
    }

    public class SendBirdChatAdapter extends BaseAdapter {
        private static final int TYPE_UNSUPPORTED = 0;
        private static final int TYPE_MESSAGE = 1;
        private static final int TYPE_SYSTEM_MESSAGE = 2;
        private static final int TYPE_FILELINK = 3;
        private static final int TYPE_BROADCAST_MESSAGE = 4;

        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ArrayList<Object> mItemList;
        private long mMaxMessageTimestamp = Long.MIN_VALUE;
        private long mMinMessageTimestamp = Long.MAX_VALUE;

        public SendBirdChatAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItemList = new ArrayList<Object>();
        }

        private void updateMessageTimestamp(MessageModel model) {
            mMaxMessageTimestamp = mMaxMessageTimestamp < model.getTimestamp() ? model.getTimestamp() : mMaxMessageTimestamp;
            mMinMessageTimestamp = mMinMessageTimestamp > model.getTimestamp() ? model.getTimestamp() : mMinMessageTimestamp;
        }

        public long getMaxMessageTimestamp() {
            return mMaxMessageTimestamp == Long.MIN_VALUE ? Long.MAX_VALUE : mMaxMessageTimestamp;
        }

        public long getMinMessageTimestamp() {
            return mMinMessageTimestamp == Long.MAX_VALUE ? Long.MIN_VALUE : mMinMessageTimestamp;
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemList.get(position);
        }

        public void clear() {
            mMaxMessageTimestamp = Long.MIN_VALUE;
            mMinMessageTimestamp = Long.MAX_VALUE;
            mItemList.clear();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        public void addMessageModel(MessageModel model) {
            if(model.isPast()) {
                mItemList.add(0, model);
            } else {
                mItemList.add(model);
            }
            updateMessageTimestamp(model);
        }

        @Override
        public int getItemViewType(int position) {
            Object item = mItemList.get(position);
            if(item instanceof Message) {
                return TYPE_MESSAGE;
            } else if(item instanceof FileLink) {
                return TYPE_FILELINK;
            } else if(item instanceof SystemMessage) {
                return TYPE_SYSTEM_MESSAGE;
            } else if(item instanceof BroadcastMessage) {
                return TYPE_BROADCAST_MESSAGE;
            }

            return TYPE_UNSUPPORTED;
        }

        @Override
        public int getViewTypeCount() {
            return 5;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            final Object item = getItem(position);

            if(convertView == null || ((ViewHolder)convertView.getTag()).getViewType() != getItemViewType(position)) {
                viewHolder = new ViewHolder();
                viewHolder.setViewType(getItemViewType(position));

                switch(getItemViewType(position)) {
                    case TYPE_UNSUPPORTED:
                        convertView = new View(mInflater.getContext());
                        convertView.setTag(viewHolder);
                        break;
                    case TYPE_MESSAGE: {
                        TextView tv;

                        convertView = mInflater.inflate(R.layout.sendbird_view_message, parent, false);
                        tv = (TextView) convertView.findViewById(R.id.txt_message);
                        viewHolder.setView("message", tv);
                        viewHolder.setView("img_op_icon", (ImageView)convertView.findViewById(R.id.img_op_icon));
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_SYSTEM_MESSAGE: {
                        convertView = mInflater.inflate(R.layout.sendbird_view_system_message, parent, false);
                        viewHolder.setView("message", convertView.findViewById(R.id.txt_message));
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_BROADCAST_MESSAGE: {
                        convertView = mInflater.inflate(R.layout.sendbird_view_system_message, parent, false);
                        viewHolder.setView("message", convertView.findViewById(R.id.txt_message));
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_FILELINK: {
                        TextView tv;

                        convertView = mInflater.inflate(R.layout.sendbird_view_filelink, parent, false);
                        tv = (TextView) convertView.findViewById(R.id.txt_sender_name);
                        viewHolder.setView("txt_sender_name", tv);
                        viewHolder.setView("img_op_icon", (ImageView)convertView.findViewById(R.id.img_op_icon));

                        viewHolder.setView("img_file_container", convertView.findViewById(R.id.img_file_container));

                        viewHolder.setView("image_container", convertView.findViewById(R.id.image_container));
                        viewHolder.setView("img_thumbnail", convertView.findViewById(R.id.img_thumbnail));
                        viewHolder.setView("txt_image_name", convertView.findViewById(R.id.txt_image_name));
                        viewHolder.setView("txt_image_size", convertView.findViewById(R.id.txt_image_size));

                        viewHolder.setView("file_container", convertView.findViewById(R.id.file_container));
                        viewHolder.setView("txt_file_name", convertView.findViewById(R.id.txt_file_name));
                        viewHolder.setView("txt_file_size", convertView.findViewById(R.id.txt_file_size));

                        convertView.setTag(viewHolder);

                        break;
                    }
                }
            }


            viewHolder = (ViewHolder) convertView.getTag();
            switch(getItemViewType(position)) {
                case TYPE_UNSUPPORTED:
                    break;
                case TYPE_MESSAGE:
                    Message message = (Message)item;
                    if(message.isOpMessage()) {
                        viewHolder.getView("img_op_icon", ImageView.class).setVisibility(View.VISIBLE);
                        viewHolder.getView("message", TextView.class).setText(Html.fromHtml("&nbsp;&nbsp;&nbsp;<font color='#824096'><b>" + message.getSenderName() + "</b></font>: " + message.getMessage()));
                    } else {
                        viewHolder.getView("img_op_icon", ImageView.class).setVisibility(View.GONE);
                        viewHolder.getView("message", TextView.class).setText(Html.fromHtml("<font color='#824096'><b>" + message.getSenderName() + "</b></font>: " + message.getMessage()));
                    }
                    viewHolder.getView("message").setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("SendBird")
                                    .setMessage("Do you want to start 1:1 messaging with " + ((Message) item).getSenderName() + "?")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent data = new Intent();
                                            data.putExtra("userIds", new String[]{((Message) item).getSenderId()});
                                            setResult(RESULT_OK, data);
                                            mDoNotDisconnect = true;
                                            SendBirdChatActivity.this.finish();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    });
                    break;
                case TYPE_SYSTEM_MESSAGE:
                    SystemMessage systemMessage = (SystemMessage)item;
                    viewHolder.getView("message", TextView.class).setText(Html.fromHtml(systemMessage.getMessage()));
                    break;
                case TYPE_BROADCAST_MESSAGE:
                    BroadcastMessage broadcastMessage = (BroadcastMessage)item;
                    viewHolder.getView("message", TextView.class).setText(Html.fromHtml(broadcastMessage.getMessage()));
                    break;
                case TYPE_FILELINK:
                    FileLink fileLink = (FileLink)item;

                    if(fileLink.isOpMessage()) {
                        viewHolder.getView("img_op_icon", ImageView.class).setVisibility(View.VISIBLE);
                        viewHolder.getView("txt_sender_name", TextView.class).setText(Html.fromHtml("&nbsp;&nbsp;&nbsp;<font color='#824096'><b>" + fileLink.getSenderName() + "</b></font>: "));
                    } else {
                        viewHolder.getView("img_op_icon", ImageView.class).setVisibility(View.GONE);
                        viewHolder.getView("txt_sender_name", TextView.class).setText(Html.fromHtml("<font color='#824096'><b>" + fileLink.getSenderName() + "</b></font>: "));
                    }
                    if(fileLink.getFileInfo().getType().toLowerCase().startsWith("image")) {
                        viewHolder.getView("file_container").setVisibility(View.GONE);

                        viewHolder.getView("image_container").setVisibility(View.VISIBLE);
                        viewHolder.getView("txt_image_name", TextView.class).setText(fileLink.getFileInfo().getName());
                        viewHolder.getView("txt_image_size", TextView.class).setText(Helper.readableFileSize(fileLink.getFileInfo().getSize()));
                        displayUrlImage(viewHolder.getView("img_thumbnail", ImageView.class), fileLink.getFileInfo().getUrl());
                    } else {
                        viewHolder.getView("image_container").setVisibility(View.GONE);

                        viewHolder.getView("file_container").setVisibility(View.VISIBLE);
                        viewHolder.getView("txt_file_name", TextView.class).setText(fileLink.getFileInfo().getName());
                        viewHolder.getView("txt_file_size", TextView.class).setText("" + fileLink.getFileInfo().getSize());
                    }
                    viewHolder.getView("txt_sender_name").setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("SendBird")
                                    .setMessage("Do you want to start 1:1 messaging with " + ((FileLink) item).getSenderName() + "?")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent data = new Intent();
                                            data.putExtra("userIds", new String[]{((FileLink) item).getSenderId()});
                                            setResult(RESULT_OK, data);
                                            SendBirdChatActivity.this.finish();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    });
                    viewHolder.getView("img_file_container").setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("SendBird")
                                    .setMessage("Do you want to download this file?")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                downloadUrl((FileLink) item, mContext);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    });
                    break;
            }

            return convertView;
        }

        private class ViewHolder {
            private Hashtable<String, View> holder = new Hashtable<String, View>();
            private int type;

            public int getViewType() {
                return this.type;
            }

            public void setViewType(int type) {
                this.type = type;
            }
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

    private static void displayUrlImage(ImageView imageView, String url) {
        UrlDownloadAsyncTask.display(url, imageView);
    }

    private static void downloadUrl(FileLink fileLink, Context context) throws IOException {
        String url = fileLink.getFileInfo().getUrl();
        String name = fileLink.getFileInfo().getName();
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File downloadFile = File.createTempFile("SendBird", name.substring(name.lastIndexOf(".")), downloadDir);
        UrlDownloadAsyncTask.download(url, downloadFile, context);
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


    public static class Helper {
        public static String generateDeviceUUID(Context context) {
            String serial = android.os.Build.SERIAL;
            String androidID = Settings.Secure.ANDROID_ID;
            String deviceUUID = serial + androidID;

        /*
         * SHA-1
         */
            MessageDigest digest;
            byte[] result;
            try {
                digest = MessageDigest.getInstance("SHA-1");
                result = digest.digest(deviceUUID.getBytes("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02X", b));
            }

            return sb.toString();
        }

        public static void hideKeyboard(Activity activity) {
            if (activity == null || activity.getCurrentFocus() == null) {
                return;
            }

            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }

        public static String readableFileSize(long size) {
            if (size <= 0) return "0KB";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

    }
}
