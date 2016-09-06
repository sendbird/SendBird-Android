package com.sendbird.android.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class SendBirdOpenChatActivity extends FragmentActivity {
    private SendBirdChatFragment mSendBirdChatFragment;

    private View mTopBarContainer;
    private View mSettingsContainer;
    private String mChannelUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.sendbird_slide_in_from_bottom, R.anim.sendbird_slide_out_to_top);
        setContentView(R.layout.activity_sendbird_open_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mChannelUrl = getIntent().getStringExtra("channel_url");
        if (mChannelUrl == null || mChannelUrl.length() <= 0) {
            finish();
            return;
        }

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
        mSendBirdChatFragment = new SendBirdChatFragment();
        Bundle args = new Bundle();
        args.putString("channel_url", mChannelUrl);
        mSendBirdChatFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mSendBirdChatFragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Helper.MY_PERMISSION_REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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

        findViewById(R.id.btn_participants).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsContainer.setVisibility(View.GONE);
                Intent intent = new Intent(SendBirdOpenChatActivity.this, SendBirdParticipantListActivity.class);
                intent.putExtra("channel_url", mChannelUrl);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_blocked_users).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsContainer.setVisibility(View.GONE);
                Intent intent = new Intent(SendBirdOpenChatActivity.this, SendBirdBlockedUserListActivity.class);
                startActivity(intent);
            }
        });

        resizeMenubar();
    }

    public static class SendBirdChatFragment extends Fragment {
        private static final int REQUEST_PICK_IMAGE = 100;
        private static final String identifier = "SendBirdOpenChat";

        private ListView mListView;
        private SendBirdChatAdapter mAdapter;
        private EditText mEtxtMessage;
        private Button mBtnSend;
        private ImageButton mBtnUpload;
        private ProgressBar mProgressBtnUpload;
        private String mChannelUrl;
        private OpenChannel mOpenChannel;
        private PreviousMessageListQuery mPrevMessageListQuery;
        private boolean mIsUploading;

        public SendBirdChatFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.sendbird_fragment_open_chat, container, false);

            mChannelUrl = getArguments().getString("channel_url");

            initUIComponents(rootView);

            enterChannel(mChannelUrl);

            return rootView;
        }

        @Override
        public void onPause() {
            super.onPause();
            if (!mIsUploading) {
                SendBird.removeChannelHandler(identifier);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (!mIsUploading) {
                SendBird.addChannelHandler(identifier, new SendBird.ChannelHandler() {
                    @Override
                    public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                        if (baseChannel.getUrl().equals(mChannelUrl)) {
                            mAdapter.appendMessage(baseMessage);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onMessageDeleted(BaseChannel channel, long msgId) {
                        if (channel.getUrl().equals(mChannelUrl)) {
                            boolean deleteMsg = false;

                            for (int i = 0; i < mAdapter.getCount(); i++) {
                                BaseMessage msg = (BaseMessage) mAdapter.getItem(i);
                                if (msg.getMessageId() == msgId) {
                                    mAdapter.delete(msg);
                                    deleteMsg = true;
                                    break;
                                }
                            }

                            if (deleteMsg) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

                loadPrevMessages(true);
            } else {
                mIsUploading = false;

                /**
                 * Set this as true to restart auto-background detection,
                 * when your Activity is shown again after the external Activity is finished.
                 */
                SendBird.setAutoBackgroundDetection(true);
            }
        }

        @Override
        public void onDestroy() {
            exitChannel();
            super.onDestroy();
        }

        private void loadPrevMessages(final boolean refresh) {
            if (mOpenChannel == null) {
                return;
            }

            if (refresh || mPrevMessageListQuery == null) {
                mPrevMessageListQuery = mOpenChannel.createPreviousMessageListQuery();
            }

            if (mPrevMessageListQuery.isLoading()) {
                return;
            }

            if (!mPrevMessageListQuery.hasMore()) {
                return;
            }

            mPrevMessageListQuery.load(30, true, new PreviousMessageListQuery.MessageListQueryResult() {
                @Override
                public void onResult(List<BaseMessage> list, SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (refresh) {
                        mAdapter.clear();
                    }

                    for (BaseMessage message : list) {
                        mAdapter.insertMessage(message);
                    }
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(list.size());
                }
            });
        }

        private void enterChannel(String channelUrl) {
            OpenChannel.getChannel(channelUrl, new OpenChannel.OpenChannelGetHandler() {
                @Override
                public void onResult(final OpenChannel openChannel, SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                        @Override
                        public void onResult(SendBirdException e) {
                            if (e != null) {
                                Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            mOpenChannel = openChannel;
                            ((TextView) getActivity().findViewById(R.id.txt_channel_name)).setText(openChannel.getName());

                            loadPrevMessages(true);
                        }
                    });
                }
            });
        }

        private void exitChannel() {
            if (mOpenChannel != null) {
                mOpenChannel.exit(null);
            }
        }

        private void initUIComponents(View rootView) {
            mAdapter = new SendBirdChatAdapter(getActivity());

            mListView = (ListView) rootView.findViewById(R.id.list);
            turnOffListViewDecoration(mListView);
            mListView.setAdapter(mAdapter);

            mBtnSend = (Button) rootView.findViewById(R.id.btn_send);
            mBtnUpload = (ImageButton) rootView.findViewById(R.id.btn_upload);
            mProgressBtnUpload = (ProgressBar) rootView.findViewById(R.id.progress_btn_upload);
            mEtxtMessage = (EditText) rootView.findViewById(R.id.etxt_message);

            mBtnSend.setEnabled(false);
            mBtnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    send();
                }
            });

            mBtnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Helper.requestReadWriteStoragePermissions(getActivity())) {
                        mIsUploading = true;

                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE);

                        /**
                         * Set this as false to maintain SendBird connection,
                         * even when an external Activity is started.
                         */
                        SendBird.setAutoBackgroundDetection(false);
                    }
                }
            });

            mEtxtMessage.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
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
                    if (scrollState == SCROLL_STATE_IDLE) {
                        if (view.getFirstVisiblePosition() == 0 && view.getChildCount() > 0 && view.getChildAt(0).getTop() == 0) {
                            loadPrevMessages(false);
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                }
            });
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Select")
                            .setItems(new String[]{"Delete Message", "Block User"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            final BaseMessage msg0 = (BaseMessage) mAdapter.getItem(position);
                                            mOpenChannel.deleteMessage(msg0, new BaseChannel.DeleteMessageHandler() {
                                                @Override
                                                public void onResult(SendBirdException e) {
                                                    if (e != null) {
                                                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }

                                                    Toast.makeText(getActivity(), "Message deleted.", Toast.LENGTH_SHORT).show();
                                                    // Message will be deleted at ChannelHandler.
                                                }
                                            });
                                            break;

                                        case 1:
                                            BaseMessage msg1 = (BaseMessage) mAdapter.getItem(position);
                                            User target = null;

                                            if (msg1 instanceof AdminMessage) {
                                                Toast.makeText(getActivity(), "Admin message can not be deleted.", Toast.LENGTH_SHORT).show();
                                                return;
                                            } else if (msg1 instanceof UserMessage) {
                                                target = ((UserMessage) msg1).getSender();
                                            } else if (msg1 instanceof FileMessage) {
                                                target = ((FileMessage) msg1).getSender();
                                            }

                                            SendBird.blockUser(target, new SendBird.UserBlockHandler() {
                                                @Override
                                                public void onBlocked(User user, SendBirdException e) {
                                                    if (e != null) {
                                                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }

                                                    Toast.makeText(getActivity(), user.getNickname() + " is blocked.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            break;
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null).create().show();

                    return true;
                }
            });
        }

        private void showUploadProgress(boolean tf) {
            if (tf) {
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
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null) {
                    upload(data.getData());
                }
            }
        }

        private void send() {
            mOpenChannel.sendUserMessage(mEtxtMessage.getText().toString(), new BaseChannel.SendUserMessageHandler() {
                @Override
                public void onSent(UserMessage userMessage, SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mAdapter.appendMessage(userMessage);
                    mAdapter.notifyDataSetChanged();

                    mEtxtMessage.setText("");
                }
            });

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Helper.hideKeyboard(getActivity());
            }
        }

        private void upload(Uri uri) {
            Hashtable<String, Object> info = Helper.getFileInfo(getActivity(), uri);
            final String path = (String) info.get("path");
            final File file = new File(path);
            final String name = file.getName();
            final String mime = (String) info.get("mime");
            final int size = (Integer) info.get("size");

            if (path == null) {
                Toast.makeText(getActivity(), "Uploading file must be located in local storage.", Toast.LENGTH_LONG).show();
            } else {
                showUploadProgress(true);
                mOpenChannel.sendFileMessage(file, name, mime, size, "", new BaseChannel.SendFileMessageHandler() {
                    @Override
                    public void onSent(FileMessage fileMessage, SendBirdException e) {
                        showUploadProgress(false);
                        if (e != null) {
                            Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mAdapter.appendMessage(fileMessage);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    public static class SendBirdChatAdapter extends BaseAdapter {
        private static final int TYPE_UNSUPPORTED = 0;
        private static final int TYPE_USER_MESSAGE = 1;
        private static final int TYPE_FILE_MESSAGE = 2;
        private static final int TYPE_ADMIN_MESSAGE = 3;

        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ArrayList<Object> mItemList;

        public SendBirdChatAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItemList = new ArrayList<>();
        }

        public void delete(Object message) {
            mItemList.remove(message);
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
            mItemList.clear();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void insertMessage(BaseMessage message) {
            mItemList.add(0, message);
        }

        public void appendMessage(BaseMessage message) {
            mItemList.add(message);
        }

        @Override
        public int getItemViewType(int position) {
            Object item = mItemList.get(position);
            if (item instanceof UserMessage) {
                return TYPE_USER_MESSAGE;
            } else if (item instanceof FileMessage) {
                return TYPE_FILE_MESSAGE;
            } else if (item instanceof AdminMessage) {
                return TYPE_ADMIN_MESSAGE;
            }

            return TYPE_UNSUPPORTED;
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            final Object item = getItem(position);

            if (convertView == null || ((ViewHolder) convertView.getTag()).getViewType() != getItemViewType(position)) {
                viewHolder = new ViewHolder();
                viewHolder.setViewType(getItemViewType(position));

                switch (getItemViewType(position)) {
                    case TYPE_UNSUPPORTED:
                        convertView = new View(mInflater.getContext());
                        convertView.setTag(viewHolder);
                        break;
                    case TYPE_USER_MESSAGE: {
                        TextView tv;

                        convertView = mInflater.inflate(R.layout.sendbird_view_open_user_message, parent, false);
                        tv = (TextView) convertView.findViewById(R.id.txt_message);
                        viewHolder.setView("message", tv);
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_ADMIN_MESSAGE: {
                        convertView = mInflater.inflate(R.layout.sendbird_view_admin_message, parent, false);
                        viewHolder.setView("message", convertView.findViewById(R.id.txt_message));
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_FILE_MESSAGE: {
                        TextView tv;

                        convertView = mInflater.inflate(R.layout.sendbird_view_open_file_message, parent, false);
                        tv = (TextView) convertView.findViewById(R.id.txt_sender_name);
                        viewHolder.setView("txt_sender_name", tv);

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
            switch (getItemViewType(position)) {
                case TYPE_UNSUPPORTED:
                    break;
                case TYPE_USER_MESSAGE:
                    final UserMessage message = (UserMessage) item;
                    viewHolder.getView("message", TextView.class).setText(Html.fromHtml("<font color='#824096'><b>" + message.getSender().getNickname() + "</b></font>: " + message.getMessage()));
                    break;
                case TYPE_ADMIN_MESSAGE:
                    AdminMessage adminMessage = (AdminMessage) item;
                    viewHolder.getView("message", TextView.class).setText(Html.fromHtml(adminMessage.getMessage()));
                    break;
                case TYPE_FILE_MESSAGE:
                    final FileMessage fileLink = (FileMessage) item;

                    viewHolder.getView("txt_sender_name", TextView.class).setText(Html.fromHtml("<font color='#824096'><b>" + fileLink.getSender().getNickname() + "</b></font>: "));
                    if (fileLink.getType().toLowerCase().startsWith("image")) {
                        viewHolder.getView("file_container").setVisibility(View.GONE);

                        viewHolder.getView("image_container").setVisibility(View.VISIBLE);
                        viewHolder.getView("txt_image_name", TextView.class).setText(fileLink.getName());
                        viewHolder.getView("txt_image_size", TextView.class).setText(Helper.readableFileSize(fileLink.getSize()));
                        Helper.displayUrlImage(viewHolder.getView("img_thumbnail", ImageView.class), fileLink.getUrl());
                    } else {
                        viewHolder.getView("image_container").setVisibility(View.GONE);

                        viewHolder.getView("file_container").setVisibility(View.VISIBLE);
                        viewHolder.getView("txt_file_name", TextView.class).setText(fileLink.getName());
                        viewHolder.getView("txt_file_size", TextView.class).setText(Helper.readableFileSize(fileLink.getSize()));
                    }
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
                                                Helper.downloadUrl(fileLink.getUrl(), fileLink.getName(), mContext);
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
            private Hashtable<String, View> holder = new Hashtable<>();
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
}
