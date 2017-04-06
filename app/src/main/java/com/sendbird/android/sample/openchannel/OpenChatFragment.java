package com.sendbird.android.sample.openchannel;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.sendbird.android.*;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.utils.FileUtils;
import com.sendbird.android.sample.utils.MediaPlayerActivity;
import com.sendbird.android.sample.utils.PhotoViewerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class OpenChatFragment extends Fragment {

    private static final String LOG_TAG = OpenChatFragment.class.getSimpleName();

    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHAT";

    private static final int INTENT_REQUEST_CHOOSE_IMAGE = 300;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 13;

    static final String EXTRA_CHANNEL_URL = "CHANNEL_URL";

    private RecyclerView mRecyclerView;
    private OpenChatAdapter mChatAdapter;
    private LinearLayoutManager mLayoutManager;
    private View mRootLayout;
    private EditText mMessageEditText;
    private Button mMessageSendButton;
    private ImageButton mUploadFileButton;
    private View mCurrentEventLayout;
    private TextView mCurrentEventText;

    private OpenChannel mChannel;
    private String mChannelUrl;
    private PreviousMessageListQuery mPrevMessageListQuery;

    /**
     * To create an instance of this fragment, a Channel URL should be passed.
     */
    public static OpenChatFragment newInstance(@NonNull String channelUrl) {
        OpenChatFragment fragment = new OpenChatFragment();

        Bundle args = new Bundle();
        args.putString(OpenChannelListFragment.EXTRA_OPEN_CHANNEL_URL, channelUrl);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_open_chat, container, false);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        mRootLayout = rootView.findViewById(R.id.layout_open_chat_root);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_open_channel_chat);

        mCurrentEventLayout = rootView.findViewById(R.id.layout_open_chat_current_event);
        mCurrentEventText = (TextView) rootView.findViewById(R.id.text_open_chat_current_event);

        setUpChatAdapter();
        setUpRecyclerView();

        // Set up chat box
        mMessageSendButton = (Button) rootView.findViewById(R.id.button_open_channel_chat_send);
        mMessageEditText = (EditText) rootView.findViewById(R.id.edittext_chat_message);

        mMessageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserMessage(mMessageEditText.getText().toString());
                mMessageEditText.setText("");
            }
        });

        mUploadFileButton = (ImageButton) rootView.findViewById(R.id.button_open_channel_chat_upload);
        mUploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestImage();
            }
        });


        // Gets channel from URL user requested
        mChannelUrl = getArguments().getString(OpenChannelListFragment.EXTRA_OPEN_CHANNEL_URL);
        enterChannel(mChannelUrl);

        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission granted.
                    Snackbar.make(mRootLayout, "Storage permissions granted. You can now upload or download files.",
                            Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    // Permission denied.
                    Snackbar.make(mRootLayout, "Permissions denied.",
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.d(LOG_TAG, "data is null!");
                return;
            }
            showUploadConfirmDialog(data.getData());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set this as true to restart auto-background detection.
        // This means that you will be automatically disconnected from SendBird when your
        // app enters the background.
        SendBird.setAutoBackgroundDetection(true);

        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
                Log.d("CONNECTION", "OpenChatFragment onReconnectStarted()");
            }

            @Override
            public void onReconnectSucceeded() {
                Log.d("CONNECTION", "OpenChatFragment onReconnectSucceeded()");
            }

            @Override
            public void onReconnectFailed() {
                Log.d("CONNECTION", "OpenChatFragment onReconnectFailed()");
            }
        });

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                // Add new message to view
                if (baseChannel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.addFirst(baseMessage);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mChannel.exit(new OpenChannel.OpenChannelExitHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_open_chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open_chat_view_participants) {
            Intent intent = new Intent(getActivity(), ParticipantListActivity.class);
            intent.putExtra(EXTRA_CHANNEL_URL, mChannel.getUrl());
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpChatAdapter() {
        mChatAdapter = new OpenChatAdapter(getActivity());
        mChatAdapter.setOnItemClickListener(new OpenChatAdapter.OnItemClickListener() {
            @Override
            public void onUserMessageItemClick(UserMessage message) {
            }

            @Override
            public void onFileMessageItemClick(FileMessage message) {
                onFileMessageClicked(message);
            }

            @Override
            public void onAdminMessageItemClick(AdminMessage message) {
            }
        });

        mChatAdapter.setOnItemLongClickListener(new OpenChatAdapter.OnItemLongClickListener() {
            @Override
            public void onBaseMessageLongClick(final BaseMessage message) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete message?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMessage(message);
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChatAdapter);

        // Load more messages when user reaches the top of the current message list.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (mLayoutManager.findLastVisibleItemPosition() == mChatAdapter.getItemCount() - 1) {
                    loadNextMessageList(30);
                }
                Log.v(LOG_TAG, "onScrollStateChanged");
            }
        });
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
                    .setMessage("Download file?")
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

    private void showUploadConfirmDialog(final Uri uri) {
        new AlertDialog.Builder(getActivity())
                .setMessage("Upload file?")
                .setPositiveButton(R.string.upload, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {

                            // Specify two dimensions of thumbnails to generate
                            List<FileMessage.ThumbnailSize> thumbnailSizes = new ArrayList<>();
                            thumbnailSizes.add(new FileMessage.ThumbnailSize(240, 240));
                            thumbnailSizes.add(new FileMessage.ThumbnailSize(320, 320));

                            sendImageWithThumbnail(uri, thumbnailSizes);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void requestImage() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // If storage permissions are not granted, request permissions at run-time,
            // as per < API 23 guidelines.
            requestStoragePermissions();
        } else {
            Intent intent = new Intent();
            // Show only images, no videos or anything else
            intent.setType("image/* video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, "Select Media"), INTENT_REQUEST_CHOOSE_IMAGE);

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
            Snackbar.make(mRootLayout, "Storage access permissions are required to upload/download files.",
                    Snackbar.LENGTH_LONG)
                    .setAction("Okay", new View.OnClickListener() {
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

    /**
     * Enters an Open Channel.
     * <p>
     * A user must successfully enter a channel before being able to load or send messages
     * within the channel.
     *
     * @param channelUrl The URL of the channel to enter.
     */
    private void enterChannel(String channelUrl) {
        OpenChannel.getChannel(channelUrl, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(final OpenChannel openChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                // Enter the channel
                openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null) {
                            // Error!
                            e.printStackTrace();
                            return;
                        }

                        mChannel = openChannel;
                        loadInitialMessageList(30);

                        // Set action bar title to name of channel
                        ((OpenChannelActivity) getActivity()).setActionBarTitle(mChannel.getName());
                    }
                });
            }
        });
    }

    private void sendUserMessage(String text) {
        mChannel.sendUserMessage(text, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Log.e(LOG_TAG, e.toString());
                    Toast.makeText(
                            getActivity(),
                            "Send failed with error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                // Display sent message to RecyclerView
                mChatAdapter.addFirst(userMessage);
            }
        });
    }

    /**
     * Sends a File Message containing an image file.
     * Also requests thumbnails to be generated in specified sizes.
     *
     * @param uri The URI of the image, which in this case is received through an Intent request.
     */
    private void sendImageWithThumbnail(Uri uri, List<FileMessage.ThumbnailSize> thumbnailSizes) {
        Hashtable<String, Object> info = FileUtils.getFileInfo(getActivity(), uri);
        final String path = (String) info.get("path");
        final File file = new File(path);
        final String name = file.getName();
        final String mime = (String) info.get("mime");
        final int size = (Integer) info.get("size");

        if (path.equals("")) {
            Toast.makeText(getActivity(), "File must be located in local storage.", Toast.LENGTH_LONG).show();
        } else {
            // Send image with thumbnails in the specified dimensions
            mChannel.sendFileMessage(file, name, mime, size, "", null, thumbnailSizes, new BaseChannel.SendFileMessageHandler() {
                @Override
                public void onSent(FileMessage fileMessage, SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mChatAdapter.addFirst(fileMessage);
                }
            });
        }
    }

    /**
     * Replaces current message list with new list.
     * Should be used only on initial load.
     */
    private void loadInitialMessageList(int numMessages) {

        mPrevMessageListQuery = mChannel.createPreviousMessageListQuery();
        mPrevMessageListQuery.load(numMessages, true, new PreviousMessageListQuery.MessageListQueryResult() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                mChatAdapter.setMessageList(list);
            }
        });

    }

    /**
     * Loads messages and adds them to current message list.
     * <p>
     * A PreviousMessageListQuery must have been already initialized through {@link #loadInitialMessageList(int)}
     */
    private void loadNextMessageList(int numMessages) throws NullPointerException {

        if (mChannel == null) {
            throw new NullPointerException("Current channel instance is null.");
        }

        if (mPrevMessageListQuery == null) {
            throw new NullPointerException("Current query instance is null.");
        }


        mPrevMessageListQuery.load(numMessages, true, new PreviousMessageListQuery.MessageListQueryResult() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                for (BaseMessage message : list) {
                    mChatAdapter.addLast((message));
                }
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
        mChannel.deleteMessage(message, new BaseChannel.DeleteMessageHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // Error!

                    Toast.makeText(getActivity(),
                            "Error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                loadInitialMessageList(30);
            }
        });
    }
}
