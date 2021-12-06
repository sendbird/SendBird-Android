package com.sendbird.localcaching.sample.groupchannel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.MessageCollection;
import com.sendbird.android.MessageListParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.sendbird.android.handlers.GroupChannelContext;
import com.sendbird.android.handlers.MessageCollectionHandler;
import com.sendbird.android.handlers.MessageCollectionInitHandler;
import com.sendbird.android.handlers.MessageCollectionInitPolicy;
import com.sendbird.android.handlers.MessageContext;
import com.sendbird.android.handlers.RemoveFailedMessagesHandler;
import com.sendbird.localcaching.sample.R;
import com.sendbird.localcaching.sample.utils.FileUtils;
import com.sendbird.localcaching.sample.utils.MediaPlayerActivity;
import com.sendbird.localcaching.sample.utils.PhotoViewerActivity;
import com.sendbird.localcaching.sample.utils.PreferenceUtils;
import com.sendbird.localcaching.sample.utils.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class GroupChatFragment extends Fragment {

    private static final String LOG_TAG = GroupChatFragment.class.getSimpleName();

    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT_" + System.currentTimeMillis();

    private static final int STATE_NORMAL = 0;
    private static final int STATE_EDIT = 1;

    private static final String STATE_CHANNEL_URL = "STATE_CHANNEL_URL";
    private static final int INTENT_REQUEST_CHOOSE_MEDIA = 301;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 13;
    static final String EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL";
    static final String EXTRA_CHANNEL = "EXTRA_CHANNEL";

    private InputMethodManager mIMM;

    private RelativeLayout mRootLayout;
    private RecyclerView mRecyclerView;
    private GroupChatAdapter mChatAdapter;
    private LinearLayoutManager mLayoutManager;
    private EditText mMessageEditText;
    private Button mMessageSendButton;
    private ImageButton mUploadFileButton;
    private View mCurrentEventLayout;
    private TextView mCurrentEventText;
    private TextView mNewMessageText;

    private GroupChannel mChannel;
    private String mChannelUrl;

    private int mCurrentState = STATE_NORMAL;
    private BaseMessage mEditingMessage = null;

    // Local Caching
    private MessageCollection messageCollection;

    private long mLastRead;

    /**
     * To create an instance of this fragment, a Channel URL should be required.
     */
    public static GroupChatFragment newInstance(@NonNull String channelUrl) {
        GroupChatFragment fragment = new GroupChatFragment();

        Bundle args = new Bundle();
        args.putString(GroupChannelListFragment.EXTRA_GROUP_CHANNEL_URL, channelUrl);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIMM = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (savedInstanceState != null) {
            mChannelUrl = savedInstanceState.getString(STATE_CHANNEL_URL);
        } else {
            if (getArguments() != null) {
                mChannelUrl = getArguments().getString(GroupChannelListFragment.EXTRA_GROUP_CHANNEL_URL);
            }
        }

        Log.d(LOG_TAG, mChannelUrl);

        mLastRead = PreferenceUtils.getLastRead(mChannelUrl);
        mChatAdapter = new GroupChatAdapter(getActivity());
        setUpChatListAdapter();

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel channel, BaseMessage message) {
                // Local Caching
                // Replacement for MessageCollection.onNewMessage() in SyncManager SDK
                if (channel.getUrl().equals(messageCollection.getChannel().getUrl())) {
                    Log.d("GroupChatFragment", "onMessageReceived: message = " + message);
                    //show when the scroll position is bottom ONLY.
                    if (messageCollection.hasNext() ||
                            mLayoutManager.findFirstVisibleItemPosition() != 0) {
                        if (message instanceof UserMessage) {
                            if (!message.getSender().getUserId().equals(PreferenceUtils.getUserId())) {
                                mNewMessageText.setText("New Message = " + message.getSender().getNickname() + " : " + message.getMessage());
                                mNewMessageText.setVisibility(View.VISIBLE);
                            }
                        } else if (message instanceof FileMessage) {
                            if (!message.getSender().getUserId().equals(PreferenceUtils.getUserId())) {
                                mNewMessageText.setText("New Message = " + message.getSender().getNickname() + "Send a File");
                                mNewMessageText.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_chat, container, false);

        setRetainInstance(true);

        mRootLayout = (RelativeLayout) rootView.findViewById(R.id.layout_group_chat_root);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_group_chat);

        mCurrentEventLayout = rootView.findViewById(R.id.layout_group_chat_current_event);
        mCurrentEventText = (TextView) rootView.findViewById(R.id.text_group_chat_current_event);

        mMessageEditText = (EditText) rootView.findViewById(R.id.edittext_group_chat_message);
        mMessageSendButton = (Button) rootView.findViewById(R.id.button_group_chat_send);
        mUploadFileButton = (ImageButton) rootView.findViewById(R.id.button_group_chat_upload);
        mNewMessageText = (TextView) rootView.findViewById(R.id.text_group_chat_new_message);

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mMessageSendButton.setEnabled(true);
                } else {
                    mMessageSendButton.setEnabled(false);
                }
            }
        });

        mMessageSendButton.setEnabled(false);
        mMessageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentState == STATE_EDIT) {
                    String userInput = mMessageEditText.getText().toString();
                    if (userInput.length() > 0 && mEditingMessage != null) {
                        editMessage(mEditingMessage, userInput);
                    }
                    setState(STATE_NORMAL, null, -1);
                } else {
                    String userInput = mMessageEditText.getText().toString();
                    if (userInput.length() == 0) {
                        return;
                    }

                    sendUserMessage(userInput);
                    mMessageEditText.setText("");

                    if (messageCollection.hasNext()) {
                        createMessageCollection(mChannelUrl, Long.MAX_VALUE);
                    } else {
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }
        });

        mUploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMedia();
            }
        });
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    setTypingStatus(false);
                } else {
                    setTypingStatus(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mNewMessageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewMessageText.setVisibility(View.GONE);
                // Local Caching
                if (messageCollection.hasNext()) {
                    createMessageCollection(mChannelUrl, Long.MAX_VALUE);
                } else {
                    mRecyclerView.scrollToPosition(0);
                }
            }
        });
        setUpRecyclerView();
        setHasOptionsMenu(true);

        // Local Caching
        createMessageCollection(mChannelUrl, mLastRead);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mChatAdapter.setContext(getActivity()); // Glide bug fix (java.lang.IllegalArgumentException: You cannot start a load for a destroyed activity)`
    }

    @Override
    public void onPause() {
        setTypingStatus(false);
        displayTyping(null);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        // Local Caching
        if (messageCollection != null) {
            messageCollection.dispose();
        }
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_CHANNEL_URL, mChannelUrl);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_group_chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_group_channel_invite) {
            Intent intent = new Intent(getActivity(), InviteMemberActivity.class);
            intent.putExtra(EXTRA_CHANNEL_URL, mChannelUrl);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_group_channel_view_members) {
            Intent intent = new Intent(getActivity(), MemberListActivity.class);
            intent.putExtra(EXTRA_CHANNEL, mChannel.serialize());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Set this as true to restore background connection management.
        SendBird.setAutoBackgroundDetection(true);

        if (requestCode == INTENT_REQUEST_CHOOSE_MEDIA && resultCode == Activity.RESULT_OK) {
            // If user has successfully chosen the image, show a dialog to confirm upload.
            if (data == null) {
                Log.d(LOG_TAG, "data is null!");
                return;
            }

            sendFileWithThumbnail(data.getData());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((GroupChannelActivity) context).setOnBackPressedListener(new GroupChannelActivity.onBackPressedListener() {
            @Override
            public boolean onBack() {
                if (mCurrentState == STATE_EDIT) {
                    setState(STATE_NORMAL, null, -1);
                    return true;
                }

                mIMM.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
                return false;
            }
        });
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChatAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
                        // Local Caching
                        if (messageCollection != null && messageCollection.hasNext()) {
                            messageCollection.loadNext(new BaseChannel.GetMessagesHandler() {
                                @Override
                                public void onResult(List<BaseMessage> messages, SendBirdException e) {
                                    if (messages != null) {
                                        mChatAdapter.markAllMessagesAsRead();
                                        int insertedIndex = mChatAdapter.insertNextMessages(messages);
                                        mChatAdapter.notifyItemRangeInserted(insertedIndex, messages.size());
                                        updateLastSeenTimestamp(messages);
                                    }
                                }
                            });
                        }
                        mNewMessageText.setVisibility(View.GONE);
                    }

                    if (mLayoutManager.findLastVisibleItemPosition() == mChatAdapter.getItemCount() - 1) {
                        // Local Caching
                        if (messageCollection != null && messageCollection.hasPrevious()) {
                            messageCollection.loadPrevious(new BaseChannel.GetMessagesHandler() {
                                @Override
                                public void onResult(List<BaseMessage> messages, SendBirdException e) {
                                    if (messages != null) {
                                        int insertedIndex = mChatAdapter.insertPreviousMessages(messages);
                                        mChatAdapter.notifyItemRangeInserted(insertedIndex, messages.size());
                                    }
                                }
                            });
                        }
                    }

                }
            }
        });
    }

    private void setUpChatListAdapter() {
        mChatAdapter.setItemClickListener(new GroupChatAdapter.OnItemClickListener() {
            @Override
            public void onUserMessageItemClick(UserMessage message) {
                if (mChatAdapter.isFailedMessage(message) && !mChatAdapter.isResendingMessage(message)) {
                    retryFailedMessage(message);
                    return;
                }

                // Message is sending. Do nothing on click event.
                if (mChatAdapter.isTempMessage(message)) {
                    return;
                }

                if (message.getOgMetaData() != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getOgMetaData().getUrl()));
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(browserIntent);
                }
            }

            @Override
            public void onFileMessageItemClick(FileMessage message) {
                // Load media chooser and removeSucceededMessages the failed message from list.
                if (mChatAdapter.isFailedMessage(message)) {
                    retryFailedMessage(message);
                    return;
                }

                // Message is sending. Do nothing on click event.
                if (mChatAdapter.isTempMessage(message)) {
                    return;
                }


                onFileMessageClicked(message);
            }
        });

        mChatAdapter.setItemLongClickListener(new GroupChatAdapter.OnItemLongClickListener() {
            @Override
            public void onUserMessageItemLongClick(UserMessage message, int position) {
                if (message.getSender().getUserId().equals(PreferenceUtils.getUserId())) {
                    showMessageOptionsDialog(message, position);
                }
            }

            @Override
            public void onFileMessageItemLongClick(FileMessage message) {
            }

            @Override
            public void onAdminMessageItemLongClick(AdminMessage message) {
            }
        });
    }

    private void showMessageOptionsDialog(final BaseMessage message, final int position) {
        String[] options;

        if (message.getMessageId() == 0) {
            options = new String[]{getString(R.string.option_delete_message)};
        } else {
            options = new String[]{getString(R.string.option_edit_message), getString(R.string.option_delete_message)};
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options.length == 1) {
                    deleteMessage(message);
                } else {
                    if (which == 0) {
                        setState(STATE_EDIT, message, position);
                    } else if (which == 1) {
                        deleteMessage(message);
                    }
                }
            }
        });
        builder.create().show();
    }

    private void setState(int state, BaseMessage editingMessage, final int position) {
        switch (state) {
            case STATE_NORMAL:
                mCurrentState = STATE_NORMAL;
                mEditingMessage = null;

                mUploadFileButton.setVisibility(View.VISIBLE);
                mMessageSendButton.setText(getString(R.string.action_send_message));
                mMessageEditText.setText("");
                break;

            case STATE_EDIT:
                mCurrentState = STATE_EDIT;
                mEditingMessage = editingMessage;

                mUploadFileButton.setVisibility(View.GONE);
                mMessageSendButton.setText(getString(R.string.action_update_message));
                String messageString = editingMessage.getMessage();
                if (messageString == null) {
                    messageString = "";
                }
                mMessageEditText.setText(messageString);
                if (messageString.length() > 0) {
                    mMessageEditText.setSelection(0, messageString.length());
                }

                mMessageEditText.requestFocus();
                mMessageEditText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIMM.showSoftInput(mMessageEditText, 0);

                        mRecyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRecyclerView.scrollToPosition(position);
                            }
                        }, 500);
                    }
                }, 100);
                break;
        }
    }

    private void createMessageCollection(final String channelUrl, long startingPoint) {
        // Local Caching
        MessageListParams messageListParams = new MessageListParams();
        messageListParams.setReverse(true);
        messageListParams.setPreviousResultSize(30);
        messageListParams.setNextResultSize(30);

        GroupChannel.getChannel(channelUrl, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(getContext(), getString(R.string.get_channel_failed), Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().onBackPressed();
                        }
                    }, 1000);
                } else {
                    if (messageCollection != null) {
                        messageCollection.dispose();
                    }
                    mChannel = groupChannel;
                    updateActionBarTitle();
                    mChatAdapter.setChannel(mChannel);
                    mChatAdapter.clear();

                    messageCollection = new MessageCollection.Builder(mChannel, messageListParams)
                            .setStartingPoint(startingPoint)
                            .setMessageCollectionHandler(messageCollectionHandler)
                            .build();
                    fetchInitialMessages();
                }
            }
        });
    }

    private void fetchInitialMessages() {
        // Local Caching
        if (messageCollection == null) return;

        messageCollection.initialize(MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API, new MessageCollectionInitHandler() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onCacheResult(@Nullable List<BaseMessage> cachedList, @Nullable SendBirdException e) {
                if (e != null) {
                    // Error in fetching from the db.
                    Log.d("GroupChatFragment", "db error: " + e);
                }

                if (cachedList != null) {
                    mChatAdapter.insertSucceededMessages(cachedList);
                }

                // Display pending/failed messages.
                List<BaseMessage> pendingMessages = messageCollection.getPendingMessages();
                if (!pendingMessages.isEmpty()) {
                    mChatAdapter.insertSucceededMessages(pendingMessages);
                }
                List<BaseMessage> failedMessages = messageCollection.getFailedMessages();
                if (!failedMessages.isEmpty()) {
                    mChatAdapter.insertFailedMessages(failedMessages);
                }
                mChatAdapter.notifyDataSetChanged();

                int tsIndex = mChatAdapter.indexOfMessage(messageCollection.getStartingPoint());
                if (tsIndex >= 0) {
                    mLayoutManager.scrollToPositionWithOffset(tsIndex, mRecyclerView.getHeight() / 2);
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onApiResult(@Nullable List<BaseMessage> apiResultList, @Nullable SendBirdException e) {
                if (e != null || apiResultList == null) {
                    // Error in fetching from the server.
                    Log.d("GroupChatFragment", "api error: " + e);
                    return;
                }

                mChatAdapter.markAllMessagesAsRead();

                mChatAdapter.clear();
                mChatAdapter.insertSucceededMessages(apiResultList);
                updateLastSeenTimestamp(apiResultList);

                // Display pending/failed messages.
                List<BaseMessage> pendingMessages = messageCollection.getPendingMessages();
                if (!pendingMessages.isEmpty()) {
                    mChatAdapter.insertSucceededMessages(pendingMessages);
                }
                List<BaseMessage> failedMessages = messageCollection.getFailedMessages();
                if (!failedMessages.isEmpty()) {
                    mChatAdapter.insertFailedMessages(failedMessages);
                }
                mChatAdapter.notifyDataSetChanged();

                int tsIndex = mChatAdapter.indexOfMessage(messageCollection.getStartingPoint());
                if (tsIndex >= 0) {
                    mLayoutManager.scrollToPositionWithOffset(tsIndex, mRecyclerView.getHeight() / 2);
                }
            }
        });
    }

    private void retryFailedMessage(final BaseMessage message) {
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.request_retry_send_message))
                .setPositiveButton(R.string.resend_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            if (message instanceof UserMessage) {
                                // Local Caching
                                mChannel.resendUserMessage((UserMessage) message, null);
                            } else if (message instanceof FileMessage) {
                                // Local Caching
                                // Failed messages will contain MessageParams which were originally used in first try.
                                FileMessageParams params = ((FileMessage) message).getMessageParams();
                                if (params != null) {
                                    mChannel.resendFileMessage((FileMessage) message, params.getFile(), (BaseChannel.ResendFileMessageHandler) null);
                                }
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.delete_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            // Local Caching
                            messageCollection.removeFailedMessages(Collections.singletonList(message), new RemoveFailedMessagesHandler() {
                                @Override
                                public void onResult(List<String> requestIds, SendBirdException e) {
                                    if (e == null) {
                                        mChatAdapter.removeFailedMessages(Collections.singletonList(message));
                                    }
                                }
                            });
                        }
                    }
                }).show();
    }

    /**
     * Display which users are typing.
     * If more than two users are currently typing, this will state that "multiple users" are typing.
     *
     * @param typingUsers The list of currently typing users.
     */
    private void displayTyping(List<User> typingUsers) {
        if (typingUsers != null && typingUsers.size() > 0) {
            mCurrentEventLayout.setVisibility(View.VISIBLE);
            String string;

            if (typingUsers.size() == 1) {
                string = String.format(getString(R.string.user_typing), typingUsers.get(0).getNickname());
            } else if (typingUsers.size() == 2) {
                string = String.format(getString(R.string.two_users_typing), typingUsers.get(0).getNickname(), typingUsers.get(1).getNickname());
            } else {
                string = getString(R.string.users_typing);
            }
            mCurrentEventText.setText(string);
        } else {
            mCurrentEventLayout.setVisibility(View.GONE);
        }
    }

    private void requestMedia() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // If storage permissions are not granted, request permissions at run-time,
            // as per < API 23 guidelines.
            requestStoragePermissions();
        } else {
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, "Select Media"), INTENT_REQUEST_CHOOSE_MEDIA);

            // Set this as false to maintain connection
            // even when an external Activity is started.
            SendBird.setAutoBackgroundDetection(false);
        }
    }

    private void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(mRootLayout, getString(R.string.request_external_storage),
                    Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.accept_permission), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PERMISSION_WRITE_EXTERNAL_STORAGE);
                        }
                    })
                    .show();
        } else {
            // Permission has not been granted yet. Request it directly.
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void onFileMessageClicked(FileMessage message) {
        String type = message.getType().toLowerCase();
        if (type.startsWith("image")) {
            Intent i = new Intent(getActivity(), PhotoViewerActivity.class);
            i.putExtra("url", message.getUrl());
            i.putExtra("type", message.getType());
            startActivity(i);
        } else if (type.startsWith("video")) {
            Intent intent = new Intent(getActivity(), MediaPlayerActivity.class);
            intent.putExtra("url", message.getUrl());
            startActivity(intent);
        } else {
            showDownloadConfirmDialog(message);
        }
    }

    private void showDownloadConfirmDialog(final FileMessage message) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // If storage permissions are not granted, request permissions at run-time,
            // as per < API 23 guidelines.
            requestStoragePermissions();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.request_download_file))
                    .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                FileUtils.downloadFile(getActivity(), message.getUrl(), message.getName());
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null).show();
        }

    }

    private void updateActionBarTitle() {
        String title = "";

        if (mChannel != null) {
            title = TextUtils.getGroupChannelTitle(mChannel);
        }

        // Set action bar title to name of channel
        if (getActivity() != null) {
            ((GroupChannelActivity) getActivity()).setActionBarTitle(title);
        }
    }

    private void sendUserMessage(String text) {
        if (mChannel == null) {
            return;
        }
        // Local Caching
        mChannel.sendUserMessage(text, null);
    }

    /**
     * Notify other users whether the current user is typing.
     *
     * @param typing Whether the user is currently typing.
     */
    private void setTypingStatus(boolean typing) {
        if (mChannel == null) {
            return;
        }

        if (typing) {
            mChannel.startTyping();
        } else {
            mChannel.endTyping();
        }
    }

    /**
     * Sends a File Message containing an image file.
     * Also requests thumbnails to be generated in specified sizes.
     *
     * @param uri The URI of the image, which in this case is received through an Intent request.
     */
    private void sendFileWithThumbnail(Uri uri) {
        if (mChannel == null) {
            return;
        }

        // Specify two dimensions of thumbnails to generate
        List<FileMessage.ThumbnailSize> thumbnailSizes = new ArrayList<>();
        thumbnailSizes.add(new FileMessage.ThumbnailSize(240, 240));
        thumbnailSizes.add(new FileMessage.ThumbnailSize(320, 320));

        Hashtable<String, Object> info = FileUtils.getFileInfo(getActivity(), uri);

        if (info == null || info.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.wrong_file_info), Toast.LENGTH_LONG).show();
            return;
        }
        final String name;
        if (info.containsKey("name")) {
            name = (String) info.get("name");
        } else {
            name = "Sendbird File";
        }
        final String path = (String) info.get("path");
        final File file = new File(path);
        final String mime = (String) info.get("mime");
        final int size = (int) info.get("size");

        if (path == null || path.equals("")) {
            Toast.makeText(getActivity(), getString(R.string.wrong_file_path), Toast.LENGTH_LONG).show();
        } else {
            // Local Caching
            mChannel.sendFileMessage(file, name, mime, size, "", null, thumbnailSizes, (BaseChannel.SendFileMessageHandler) null);
        }
    }

    private void editMessage(final BaseMessage message, String editedMessage) {
        if (mChannel == null) {
            return;
        }

        mChannel.updateUserMessage(message.getMessageId(), editedMessage, null, null, new BaseChannel.UpdateUserMessageHandler() {
            @Override
            public void onUpdated(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                      // Error!
                    Toast.makeText(getActivity(), getString(R.string.sendbird_error_with_code, e.getCode(), e.getMessage()), Toast.LENGTH_SHORT).show();
                    return;
                }
                // Local Caching
                // Updated message will be delivered by MessageCollectionHandler.onMessagesUpdated().
            }
        });
    }

    /**
     * Deletes a message within the channel.
     * Note that users can only delete messages sent by oneself.
     *
     * @param message The message to delete.
     */
    private void deleteMessage(final BaseMessage message) {
        if (message.getMessageId() == 0) {
            // Local Caching
            messageCollection.removeFailedMessages(Collections.singletonList(message), new RemoveFailedMessagesHandler() {
                @Override
                public void onResult(List<String> requestIds, SendBirdException e) {
                    if (e != null) {
                        mChatAdapter.removeFailedMessages(Collections.singletonList(message));
                    }
                }
            });
        } else {
            if (mChannel == null) {
                return;
            }

            mChannel.deleteMessage(message, new BaseChannel.DeleteMessageHandler() {
                @Override
                public void onResult(SendBirdException e) {
                    if (e != null) {
                        // Error!
                        Toast.makeText(getActivity(), getString(R.string.sendbird_error_with_code, e.getCode(), e.getMessage()), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Local Caching
                    // Deleted message will be delivered by MessageCollectionHandler.onMessagesDeleted().
                }
            });
        }
    }

    private void updateLastSeenTimestamp(List<BaseMessage> messages) {
        long lastSeenTimestamp = mLastRead == Long.MAX_VALUE ? 0 : mLastRead;
        for (BaseMessage message : messages) {
            if (lastSeenTimestamp < message.getCreatedAt()) {
                lastSeenTimestamp = message.getCreatedAt();
            }
        }

        if (lastSeenTimestamp > mLastRead) {
            PreferenceUtils.setLastRead(mChannelUrl, lastSeenTimestamp);
            mLastRead = lastSeenTimestamp;
        }
    }

    // Local Caching
    private final MessageCollectionHandler messageCollectionHandler = new MessageCollectionHandler() {
        @Override
        public void onMessagesAdded(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
            Log.d("GroupChatFragment", "onMessagesAdded: source=" + context.getCollectionEventSource() + ", messages=" + messages);
            switch (context.getMessagesSendingStatus()) {
                case SUCCEEDED: {
                    mChatAdapter.markAllMessagesAsRead();
                    boolean isScrollBottom = mLayoutManager.findFirstVisibleItemPosition() == 0;
                    mChatAdapter.insertSucceededMessages(messages);
                    mChatAdapter.notifyDataSetChanged();

                    if (isScrollBottom) {
                        mLayoutManager.scrollToPosition(0);
                    }
                    updateLastSeenTimestamp(messages);
                }
                break;
                case PENDING: {
                    // from send
                    int insertedIndex = mChatAdapter.insertNextMessages(messages);
                    mChatAdapter.notifyItemRangeInserted(insertedIndex, messages.size());
                    mLayoutManager.scrollToPosition(0);
                }
                break;
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onMessagesUpdated(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
            Log.d("GroupChatFragment", "onMessagesUpdated: source=" + context.getCollectionEventSource() + ", messages=" + messages);
            switch (context.getMessagesSendingStatus()) {
                case SUCCEEDED:
                    mChatAdapter.updateSucceededMessages(messages);
                    updateLastSeenTimestamp(messages);
                    break;
                case PENDING:
                    // from resend
                    mChatAdapter.removeFailedMessages(messages);
                    int insertedIndex = mChatAdapter.insertNextMessages(messages);
                    mChatAdapter.notifyItemRangeInserted(insertedIndex, messages.size());
                    mLayoutManager.scrollToPosition(0);
                    break;
                case FAILED:
                    // from send/resend failure
                    mChatAdapter.removeSucceededMessages(messages);
                    mChatAdapter.insertFailedMessages(messages);
                    mChatAdapter.notifyDataSetChanged();
                    break;
                case CANCELED:
                    mChatAdapter.removeSucceededMessages(messages);
                    break;
            }
        }

        @Override
        public void onMessagesDeleted(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
            Log.d("GroupChatFragment", "onMessagesDeleted: source=" + context.getCollectionEventSource() + ", messages=" + messages);
            switch (context.getMessagesSendingStatus()) {
                case SUCCEEDED:
                    mChatAdapter.removeSucceededMessages(messages);
                    updateLastSeenTimestamp(messages);
                    break;
                case PENDING:
                    mChatAdapter.removeSucceededMessages(messages);
                    break;
                case FAILED:
                    mChatAdapter.removeFailedMessages(messages);
                    break;
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onChannelUpdated(@NonNull GroupChannelContext context, @NonNull GroupChannel channel) {
            Log.d("GroupChatFragment", "onChannelUpdated: source=" + context.getCollectionEventSource());
            mChannel = channel;

            switch (context.getCollectionEventSource()) {
                case EVENT_READ_RECEIPT_UPDATED:
                case EVENT_DELIVERY_RECEIPT_UPDATED:
                    mChatAdapter.notifyDataSetChanged();
                    break;
                case EVENT_TYPING_STATUS_UPDATED:
                    List<User> typingUsers = channel.getTypingUsers();
                    displayTyping(typingUsers);
                    break;
                default:
                    updateActionBarTitle();
                    break;
            }
        }

        @Override
        public void onChannelDeleted(@NonNull GroupChannelContext context, @NonNull String channelUrl) {
            Log.d("GroupChatFragment", "onChannelDeleted: source=" + context.getCollectionEventSource());
            Toast.makeText(getContext(), getString(R.string.channel_deleted), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getActivity().onBackPressed();
                }
            }, 1000);
        }

        @Override
        public void onHugeGapDetected() {
            Log.d("GroupChatFragment", "onHugeGapDetected");
            // dispose current message collection
            long defaultStartingPoint = Long.MAX_VALUE;
            if (messageCollection != null) {
                defaultStartingPoint = messageCollection.getStartingPoint();
                messageCollection.dispose();
            }

            int position = mLayoutManager.findFirstVisibleItemPosition();
            if (position >= 0) {
                // set starting point to current message if possible
                final BaseMessage message = mChatAdapter.getItem(position);
                Log.d("GroupChatFragement", "founded first visible message = " + message);
                createMessageCollection(mChannelUrl, message != null ? message.getCreatedAt() : defaultStartingPoint);
            } else {
                createMessageCollection(mChannelUrl, messageCollection.getStartingPoint());
            }
        }
    };
}
