package com.sendbird.syncmanager.sample.groupchannel;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.sendbird.syncmanager.sample.R;
import com.sendbird.syncmanager.sample.utils.DateUtils;
import com.sendbird.syncmanager.sample.utils.ImageUtils;
import com.sendbird.syncmanager.sample.utils.PreferenceUtils;
import com.sendbird.syncmanager.sample.utils.SyncManagerUtils;
import com.sendbird.syncmanager.sample.utils.UrlPreviewInfo;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;


class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String URL_PREVIEW_CUSTOM_TYPE = "url_preview";

    private static final int VIEW_TYPE_USER_MESSAGE_ME = 10;
    private static final int VIEW_TYPE_USER_MESSAGE_OTHER = 11;
    private static final int VIEW_TYPE_FILE_MESSAGE_ME = 20;
    private static final int VIEW_TYPE_FILE_MESSAGE_OTHER = 21;
    private static final int VIEW_TYPE_FILE_MESSAGE_IMAGE_ME = 22;
    private static final int VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER = 23;
    private static final int VIEW_TYPE_FILE_MESSAGE_VIDEO_ME = 24;
    private static final int VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER = 25;
    private static final int VIEW_TYPE_ADMIN_MESSAGE = 30;

    private Context mContext;
    private HashMap<FileMessage, CircleProgressBar> mFileMessageMap;
    private GroupChannel mChannel;
    private List<BaseMessage> mMessageList;
    private final List<BaseMessage> mFailedMessageList;
    private Set<String> mResendingMessageSet;

    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    private Hashtable<String, Uri> mTempFileMessageUriTable = new Hashtable<>();

    interface OnItemLongClickListener {
        void onUserMessageItemLongClick(UserMessage message, int position);

        void onFileMessageItemLongClick(FileMessage message);

        void onAdminMessageItemLongClick(AdminMessage message);
    }

    interface OnItemClickListener {
        void onUserMessageItemClick(UserMessage message);

        void onFileMessageItemClick(FileMessage message);
    }


    GroupChatAdapter(Context context) {
        mContext = context;
        mFileMessageMap = new HashMap<>();
        mMessageList = new ArrayList<>();
        mFailedMessageList = new ArrayList<>();
        mResendingMessageSet = new HashSet<>();
    }

    void setContext(Context context) {
        mContext = context;
    }

    /**
     * Inflates the correct layout according to the View Type.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                View myUserMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_user_me, parent, false);
                return new MyUserMessageHolder(myUserMsgView);
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                View otherUserMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_user_other, parent, false);
                return new OtherUserMessageHolder(otherUserMsgView);
            case VIEW_TYPE_ADMIN_MESSAGE:
                View adminMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_admin, parent, false);
                return new AdminMessageHolder(adminMsgView);
            case VIEW_TYPE_FILE_MESSAGE_ME:
                View myFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_me, parent, false);
                return new MyFileMessageHolder(myFileMsgView);
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                View otherFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_other, parent, false);
                return new OtherFileMessageHolder(otherFileMsgView);
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                View myImageFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_image_me, parent, false);
                return new MyImageFileMessageHolder(myImageFileMsgView);
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                View otherImageFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_image_other, parent, false);
                return new OtherImageFileMessageHolder(otherImageFileMsgView);
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                View myVideoFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_video_me, parent, false);
                return new MyVideoFileMessageHolder(myVideoFileMsgView);
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                View otherVideoFileMsgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_group_chat_file_video_other, parent, false);
                return new OtherVideoFileMessageHolder(otherVideoFileMsgView);

            default:
                return null;

        }
    }

    /**
     * Binds variables in the BaseMessage to UI components in the ViewHolder.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseMessage message = getMessage(position);
        if (message == null) {
            return;
        }

        boolean isContinuous = false;
        boolean isNewDay = false;
        boolean isTempMessage = false;
        boolean isFailedMessage = false;
        Uri tempFileMessageUri = null;

        // If there is at least one item preceding the current one, check the previous message.
        if (position < mMessageList.size() + mFailedMessageList.size() - 1) {
            BaseMessage prevMessage = getMessage(position + 1);

            // If the date of the previous message is different, display the date before the message,
            // and also set isContinuous to false to show information such as the sender's nickname
            // and profile image.
            if (!DateUtils.hasSameDate(message.getCreatedAt(), prevMessage.getCreatedAt())) {
                isNewDay = true;
                isContinuous = false;
            } else {
                isContinuous = isContinuous(message, prevMessage);
            }
        } else if (position == mFailedMessageList.size() + mMessageList.size() - 1) {
            isNewDay = true;
        }

        isTempMessage = isTempMessage(message);
        tempFileMessageUri = getTempFileMessageUri(message);
        isFailedMessage = isFailedMessage(message);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                ((MyUserMessageHolder) holder).bind(mContext, (UserMessage) message, mChannel, isContinuous, isNewDay, isTempMessage, isFailedMessage, mItemClickListener, mItemLongClickListener, position);
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                ((OtherUserMessageHolder) holder).bind(mContext, (UserMessage) message, mChannel, isNewDay, isContinuous, mItemClickListener, mItemLongClickListener, position);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                ((AdminMessageHolder) holder).bind(mContext, (AdminMessage) message, mChannel, isNewDay);
                break;
            case VIEW_TYPE_FILE_MESSAGE_ME:
                ((MyFileMessageHolder) holder).bind(mContext, (FileMessage) message, mChannel, isNewDay, isTempMessage, isFailedMessage, tempFileMessageUri, mItemClickListener);
                break;
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                ((OtherFileMessageHolder) holder).bind(mContext, (FileMessage) message, mChannel, isNewDay, isContinuous, mItemClickListener);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                ((MyImageFileMessageHolder) holder).bind(mContext, (FileMessage) message, mChannel, isNewDay, isTempMessage, isFailedMessage, tempFileMessageUri, mItemClickListener);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                ((OtherImageFileMessageHolder) holder).bind(mContext, (FileMessage) message, mChannel, isNewDay, isContinuous, mItemClickListener);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                ((MyVideoFileMessageHolder) holder).bind(mContext, (FileMessage) message, mChannel, isNewDay, isTempMessage, isFailedMessage, tempFileMessageUri, mItemClickListener);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                ((OtherVideoFileMessageHolder) holder).bind(mContext, (FileMessage) message, mChannel, isNewDay, isContinuous, mItemClickListener);
                break;
            default:
                break;
        }
    }

    /**
     * Declares the View Type according to the type of message and the sender.
     */
    @Override
    public int getItemViewType(int position) {
        BaseMessage message = getMessage(position);
        if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage) message;
            // If the sender is current user
            if (userMessage.getSender().getUserId().equals(getMyUserId())) {
                return VIEW_TYPE_USER_MESSAGE_ME;
            } else {
                return VIEW_TYPE_USER_MESSAGE_OTHER;
            }
        } else if (message instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) message;
            if (fileMessage.getType().toLowerCase().startsWith("image")) {
                // If the sender is current user
                if (fileMessage.getSender().getUserId().equals(getMyUserId())) {
                    return VIEW_TYPE_FILE_MESSAGE_IMAGE_ME;
                } else {
                    return VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER;
                }
            } else if (fileMessage.getType().toLowerCase().startsWith("video")) {
                if (fileMessage.getSender().getUserId().equals(getMyUserId())) {
                    return VIEW_TYPE_FILE_MESSAGE_VIDEO_ME;
                } else {
                    return VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER;
                }
            } else {
                if (fileMessage.getSender().getUserId().equals(getMyUserId())) {
                    return VIEW_TYPE_FILE_MESSAGE_ME;
                } else {
                    return VIEW_TYPE_FILE_MESSAGE_OTHER;
                }
            }
        } else if (message instanceof AdminMessage) {
            return VIEW_TYPE_ADMIN_MESSAGE;
        }

        return -1;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size() + mFailedMessageList.size();
    }

    private BaseMessage getMessage(int position) {
        if (position < mFailedMessageList.size()) {
            return mFailedMessageList.get(position);
        } else if (position < mFailedMessageList.size() + mMessageList.size()) {
            return mMessageList.get(position - mFailedMessageList.size());
        } else {
            return null;
        }
    }

    void setChannel(GroupChannel channel) {
        mChannel = channel;
    }

    public boolean isTempMessage(BaseMessage message) {
        return message.getMessageId() == 0;
    }

    public boolean isFailedMessage(BaseMessage message) {
        if (message == null) {
            return false;
        }

        return mFailedMessageList.contains(message);
    }

    public boolean isResendingMessage(BaseMessage message) {
        if (message == null) {
            return false;
        }

        return mResendingMessageSet.contains(getRequestId(message));
    }

    public Uri getTempFileMessageUri(BaseMessage message) {
        if (!isTempMessage(message)) {
            return null;
        }

        if (!(message instanceof FileMessage)) {
            return null;
        }

        return mTempFileMessageUriTable.get(((FileMessage) message).getRequestId());
    }

    void setFileProgressPercent(FileMessage message, int percent) {
        BaseMessage msg;
        for (int i = mMessageList.size() - 1; i >= 0; i--) {
            msg = mMessageList.get(i);
            if (msg instanceof FileMessage) {
                if (message.getRequestId().equals(((FileMessage)msg).getRequestId())) {
                    CircleProgressBar circleProgressBar = mFileMessageMap.get(message);
                    if (circleProgressBar != null) {
                        circleProgressBar.setProgress(percent);
                    }
                    break;
                }
            }
        }
    }

    void addTempFileMessageInfo(FileMessage message, Uri uri) {
        mTempFileMessageUriTable.put(message.getRequestId(), uri);
    }

    void insertSucceededMessages(List<BaseMessage> messages) {
        for (BaseMessage message : messages) {
            int index = SyncManagerUtils.findIndexOfMessage(mMessageList, message);
            mMessageList.add(index, message);
        }

        notifyDataSetChanged();
    }

    void updateSucceededMessages(List<BaseMessage> messages) {
        for (BaseMessage message : messages) {
            int index = SyncManagerUtils.getIndexOfMessage(mMessageList, message);
            if (index != -1) {
                mMessageList.set(index, message);
                notifyItemChanged(index);
            }
        }
    }

    void removeSucceededMessages(List<BaseMessage> messages) {
        for (BaseMessage message : messages) {
            int index = SyncManagerUtils.getIndexOfMessage(mMessageList, message);
            if (index != -1) {
                mMessageList.remove(index);
            }
        }

        notifyDataSetChanged();
    }

    private String getRequestId(BaseMessage message) {
        if (message instanceof UserMessage) {
            return ((UserMessage) message).getRequestId();
        } else if (message instanceof FileMessage) {
            return ((FileMessage) message).getRequestId();
        }

        return "";
    }

    public void insertFailedMessages(List<BaseMessage> messages) {
        synchronized (mFailedMessageList) {
            for (BaseMessage message : messages) {
                String requestId = getRequestId(message);
                if (requestId.isEmpty()) {
                    continue;
                }

                mResendingMessageSet.add(requestId);
                mFailedMessageList.add(message);
            }

            Collections.sort(mFailedMessageList, new Comparator<BaseMessage>() {
                @Override
                public int compare(BaseMessage m1, BaseMessage m2) {
                    long x = m1.getCreatedAt();
                    long y = m2.getCreatedAt();

                    return (x < y) ? 1 : ((x == y) ? 0 : -1);
                }
            });
        }

        notifyDataSetChanged();
    }

    void updateFailedMessages(List<BaseMessage> messages) {
        synchronized (mFailedMessageList) {
            for (BaseMessage message : messages) {
                String requestId = getRequestId(message);
                if (requestId.isEmpty()) {
                    continue;
                }

                mResendingMessageSet.remove(requestId);
            }
        }

        notifyDataSetChanged();
    }

    void removeFailedMessages(List<BaseMessage> messages) {
        synchronized (mFailedMessageList) {
            for (BaseMessage message : messages) {
                String requestId = getRequestId(message);
                mResendingMessageSet.remove(requestId);
                mFailedMessageList.remove(message);
            }
        }

        notifyDataSetChanged();
    }

    void clear() {
        mMessageList.clear();
        mFailedMessageList.clear();
        notifyDataSetChanged();
    }

    /**
     * Notifies that the user has read all (previously unread) messages in the channel.
     * Typically, this would be called immediately after the user enters the chat and loads
     * its most recent messages.
     */
    public void markAllMessagesAsRead() {
        if (mChannel != null) {
            mChannel.markAsRead();
        }
        notifyDataSetChanged();
    }

    public int getLastReadPosition(long lastRead) {
        for (int i = 0; i < mMessageList.size(); i++) {
            if (mMessageList.get(i).getCreatedAt() == lastRead) {
                return i + mFailedMessageList.size();
            }
        }

        return 0;
    }

    public void setItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    public void setItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * Checks if the current message was sent by the same person that sent the preceding message.
     * <p>
     * This is done so that redundant UI, such as sender nickname and profile picture,
     * does not have to displayed when not necessary.
     */
    private boolean isContinuous(BaseMessage currentMsg, BaseMessage precedingMsg) {
        // null check
        if (currentMsg == null || precedingMsg == null) {
            return false;
        }

        if (currentMsg instanceof AdminMessage && precedingMsg instanceof AdminMessage) {
            return true;
        }

        User currentUser = null, precedingUser = null;

        if (currentMsg instanceof UserMessage) {
            currentUser = ((UserMessage) currentMsg).getSender();
        } else if (currentMsg instanceof FileMessage) {
            currentUser = ((FileMessage) currentMsg).getSender();
        }

        if (precedingMsg instanceof UserMessage) {
            precedingUser = ((UserMessage) precedingMsg).getSender();
        } else if (precedingMsg instanceof FileMessage) {
            precedingUser = ((FileMessage) precedingMsg).getSender();
        }

        // If admin message or
        return !(currentUser == null || precedingUser == null)
                && currentUser.getUserId().equals(precedingUser.getUserId());


    }

    private String getMyUserId() {
        if (SendBird.getCurrentUser() == null) {
            return PreferenceUtils.getUserId();
        }

        return SendBird.getCurrentUser().getUserId();
    }

    private class BaseViewHolder extends RecyclerView.ViewHolder {
        private TextView dateText;

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.text_group_chat_date);
        }

        void bind(BaseMessage message, boolean isNewDay) {
            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
            } else {
                dateText.setVisibility(View.GONE);
            }
        }
    }

    private class AdminMessageHolder extends BaseViewHolder {
        private TextView messageText;

        AdminMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_group_chat_message);
        }

        void bind(Context context, AdminMessage message, GroupChannel channel, boolean isNewDay) {
            super.bind(message, isNewDay);
            messageText.setText(message.getMessage());
        }
    }

    private class MyUserMessageHolder extends BaseViewHolder {
        TextView messageText, editedText, timeText, readReceiptText;
        ViewGroup urlPreviewContainer;
        TextView urlPreviewSiteNameText, urlPreviewTitleText, urlPreviewDescriptionText;
        ImageView urlPreviewMainImageView;
        View padding;

        MyUserMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_group_chat_message);
            editedText = (TextView) itemView.findViewById(R.id.text_group_chat_edited);
            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);

            urlPreviewContainer = (ViewGroup) itemView.findViewById(R.id.url_preview_container);
            urlPreviewSiteNameText = (TextView) itemView.findViewById(R.id.text_url_preview_site_name);
            urlPreviewTitleText = (TextView) itemView.findViewById(R.id.text_url_preview_title);
            urlPreviewDescriptionText = (TextView) itemView.findViewById(R.id.text_url_preview_description);
            urlPreviewMainImageView = (ImageView) itemView.findViewById(R.id.image_url_preview_main);

            // Dynamic padding that can be hidden or shown based on whether the message is continuous.
            padding = itemView.findViewById(R.id.view_group_chat_padding);
        }

        void bind(Context context, final UserMessage message, GroupChannel channel, boolean isContinuous, boolean isNewDay, boolean isTempMessage, boolean isFailedMessage, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener, final int position) {
            super.bind(message, isNewDay);
            messageText.setText(message.getMessage());
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            if (message.getUpdatedAt() > 0) {
                editedText.setVisibility(View.VISIBLE);
            } else {
                editedText.setVisibility(View.GONE);
            }

            if (message.getRequestState() == UserMessage.RequestState.PENDING
                    || isFailedMessage && mResendingMessageSet.contains(message.getRequestId())) {
                readReceiptText.setText(R.string.message_sending);
                readReceiptText.setVisibility(View.VISIBLE);
            } else if (isTempMessage) {
                readReceiptText.setText(R.string.message_failed);
                readReceiptText.setVisibility(View.VISIBLE);
            } else {

                // Since setChannel is set slightly after adapter is created
                if (channel != null) {
                    int readReceipt = channel.getReadReceipt(message);
                    if (readReceipt > 0) {
                        readReceiptText.setVisibility(View.VISIBLE);
                        readReceiptText.setText(String.valueOf(readReceipt));
                    } else {
                        readReceiptText.setVisibility(View.INVISIBLE);
                    }
                }
            }

            // If continuous from previous message, removeSucceededMessages extra padding.
            if (isContinuous) {
                padding.setVisibility(View.GONE);
            } else {
                padding.setVisibility(View.VISIBLE);
            }

            urlPreviewContainer.setVisibility(View.GONE);
            if (message.getCustomType().equals(URL_PREVIEW_CUSTOM_TYPE)) {
                try {
                    urlPreviewContainer.setVisibility(View.VISIBLE);
                    final UrlPreviewInfo previewInfo = new UrlPreviewInfo(message.getData());
                    urlPreviewSiteNameText.setText("@" + previewInfo.getSiteName());
                    urlPreviewTitleText.setText(previewInfo.getTitle());
                    urlPreviewDescriptionText.setText(previewInfo.getDescription());
                    ImageUtils.displayImageFromUrl(context, previewInfo.getImageUrl(), urlPreviewMainImageView, null);
                } catch (JSONException e) {
                    urlPreviewContainer.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }

            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onUserMessageItemClick(message);
                    }
                });
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onUserMessageItemLongClick(message, position);
                        return true;
                    }
                });
            }
        }
    }

    private class OtherUserMessageHolder extends BaseViewHolder {
        TextView messageText, editedText, nicknameText, timeText, readReceiptText;
        ImageView profileImage;

        ViewGroup urlPreviewContainer;
        TextView urlPreviewSiteNameText, urlPreviewTitleText, urlPreviewDescriptionText;
        ImageView urlPreviewMainImageView;

        public OtherUserMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_group_chat_message);
            editedText = (TextView) itemView.findViewById(R.id.text_group_chat_edited);
            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            nicknameText = (TextView) itemView.findViewById(R.id.text_group_chat_nickname);
            profileImage = (ImageView) itemView.findViewById(R.id.image_group_chat_profile);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);

            urlPreviewContainer = (ViewGroup) itemView.findViewById(R.id.url_preview_container);
            urlPreviewSiteNameText = (TextView) itemView.findViewById(R.id.text_url_preview_site_name);
            urlPreviewTitleText = (TextView) itemView.findViewById(R.id.text_url_preview_title);
            urlPreviewDescriptionText = (TextView) itemView.findViewById(R.id.text_url_preview_description);
            urlPreviewMainImageView = (ImageView) itemView.findViewById(R.id.image_url_preview_main);
        }


        void bind(Context context, final UserMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener, final int position) {
            super.bind(message, isNewDay);

            // Since setChannel is set slightly after adapter is created
            if (channel != null) {
                int readReceipt = channel.getReadReceipt(message);
                if (readReceipt > 0) {
                    readReceiptText.setVisibility(View.VISIBLE);
                    readReceiptText.setText(String.valueOf(readReceipt));
                } else {
                    readReceiptText.setVisibility(View.INVISIBLE);
                }
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
                profileImage.setVisibility(View.INVISIBLE);
                nicknameText.setVisibility(View.GONE);
            } else {
                profileImage.setVisibility(View.VISIBLE);
                ImageUtils.displayRoundImageFromUrl(context, message.getSender().getProfileUrl(), profileImage);

                nicknameText.setVisibility(View.VISIBLE);
                nicknameText.setText(message.getSender().getNickname());
            }

            messageText.setText(message.getMessage());
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            if (message.getUpdatedAt() > 0) {
                editedText.setVisibility(View.VISIBLE);
            } else {
                editedText.setVisibility(View.GONE);
            }

            urlPreviewContainer.setVisibility(View.GONE);
            if (message.getCustomType().equals(URL_PREVIEW_CUSTOM_TYPE)) {
                try {
                    urlPreviewContainer.setVisibility(View.VISIBLE);
                    UrlPreviewInfo previewInfo = new UrlPreviewInfo(message.getData());
                    urlPreviewSiteNameText.setText("@" + previewInfo.getSiteName());
                    urlPreviewTitleText.setText(previewInfo.getTitle());
                    urlPreviewDescriptionText.setText(previewInfo.getDescription());
                    ImageUtils.displayImageFromUrl(context, previewInfo.getImageUrl(), urlPreviewMainImageView, null);
                } catch (JSONException e) {
                    urlPreviewContainer.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }


            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onUserMessageItemClick(message);
                    }
                });
            }
            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onUserMessageItemLongClick(message, position);
                        return true;
                    }
                });
            }
        }
    }

    private class MyFileMessageHolder extends BaseViewHolder {
        TextView fileNameText, timeText, readReceiptText;
        CircleProgressBar circleProgressBar;

        public MyFileMessageHolder(View itemView) {
            super(itemView);

            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            fileNameText = (TextView) itemView.findViewById(R.id.text_group_chat_file_name);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
            circleProgressBar = (CircleProgressBar) itemView.findViewById(R.id.circle_progress);
        }

        void bind(Context context, final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isTempMessage, boolean isFailedMessage, Uri tempFileMessageUri, final OnItemClickListener listener) {
            bind(message, isNewDay);

            fileNameText.setText(message.getName());
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));


            if (message.getRequestState() == FileMessage.RequestState.PENDING
                    || isFailedMessage && mResendingMessageSet.contains(message.getRequestId())) {
                readReceiptText.setText(R.string.message_sending);
                readReceiptText.setVisibility(View.GONE);

                circleProgressBar.setVisibility(View.VISIBLE);
                mFileMessageMap.put(message, circleProgressBar);
            } else if (isTempMessage) {
                readReceiptText.setText(R.string.message_failed);
                readReceiptText.setVisibility(View.VISIBLE);

                circleProgressBar.setVisibility(View.GONE);
                mFileMessageMap.remove(message);
            } else {
                circleProgressBar.setVisibility(View.GONE);
                mFileMessageMap.remove(message);

                if (channel != null) {
                    int readReceipt = channel.getReadReceipt(message);
                    if (readReceipt > 0) {
                        readReceiptText.setVisibility(View.VISIBLE);
                        readReceiptText.setText(String.valueOf(readReceipt));
                    } else {
                        readReceiptText.setVisibility(View.INVISIBLE);
                    }
                }

            }

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onFileMessageItemClick(message);
                    }
                });
            }
        }
    }

    private class OtherFileMessageHolder extends BaseViewHolder {
        TextView nicknameText, timeText, fileNameText, fileSizeText, readReceiptText;
        ImageView profileImage;

        public OtherFileMessageHolder(View itemView) {
            super(itemView);

            nicknameText = (TextView) itemView.findViewById(R.id.text_group_chat_nickname);
            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            fileNameText = (TextView) itemView.findViewById(R.id.text_group_chat_file_name);
//            fileSizeText = (TextView) itemView.findViewById(R.id.text_group_chat_file_size);

            profileImage = (ImageView) itemView.findViewById(R.id.image_group_chat_profile);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
        }

        void bind(Context context, final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous, final OnItemClickListener listener) {
            super.bind(message, isNewDay);

            fileNameText.setText(message.getName());
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));
//            fileSizeText.setText(String.valueOf(message.getSize()));

            // Since setChannel is set slightly after adapter is created, check if null.
            if (channel != null) {
                int readReceipt = channel.getReadReceipt(message);
                if (readReceipt > 0) {
                    readReceiptText.setVisibility(View.VISIBLE);
                    readReceiptText.setText(String.valueOf(readReceipt));
                } else {
                    readReceiptText.setVisibility(View.INVISIBLE);
                }
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
                profileImage.setVisibility(View.INVISIBLE);
                nicknameText.setVisibility(View.GONE);
            } else {
                profileImage.setVisibility(View.VISIBLE);
                ImageUtils.displayRoundImageFromUrl(context, message.getSender().getProfileUrl(), profileImage);

                nicknameText.setVisibility(View.VISIBLE);
                nicknameText.setText(message.getSender().getNickname());
            }

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onFileMessageItemClick(message);
                    }
                });
            }
        }
    }

    /**
     * A ViewHolder for file messages that are images.
     * Displays only the image thumbnail.
     */
    private class MyImageFileMessageHolder extends BaseViewHolder {
        TextView timeText, readReceiptText;
        ImageView fileThumbnailImage;
        CircleProgressBar circleProgressBar;

        public MyImageFileMessageHolder(View itemView) {
            super(itemView);

            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            fileThumbnailImage = (ImageView) itemView.findViewById(R.id.image_group_chat_file_thumbnail);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
            circleProgressBar = (CircleProgressBar) itemView.findViewById(R.id.circle_progress);
        }

        void bind(Context context, final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isTempMessage, boolean isFailedMessage, Uri tempFileMessageUri, final OnItemClickListener listener) {
            super.bind(message, isNewDay);

            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            if (message.getRequestState() == FileMessage.RequestState.PENDING
                    || isFailedMessage && mResendingMessageSet.contains(message.getRequestId())) {
                readReceiptText.setText(R.string.message_failed);
                readReceiptText.setVisibility(View.VISIBLE);

                circleProgressBar.setVisibility(View.GONE);
                mFileMessageMap.remove(message);
            } else if (isTempMessage) {
                readReceiptText.setText(R.string.message_sending);
                readReceiptText.setVisibility(View.GONE);

                circleProgressBar.setVisibility(View.VISIBLE);
                mFileMessageMap.put(message, circleProgressBar);
            } else {
                circleProgressBar.setVisibility(View.GONE);
                mFileMessageMap.remove(message);

                // Since setChannel is set slightly after adapter is created, check if null.
                if (channel != null) {
                    int readReceipt = channel.getReadReceipt(message);
                    if (readReceipt > 0) {
                        readReceiptText.setVisibility(View.VISIBLE);
                        readReceiptText.setText(String.valueOf(readReceipt));
                    } else {
                        readReceiptText.setVisibility(View.INVISIBLE);
                    }
                }
            }

            if (isTempMessage && tempFileMessageUri != null) {
                ImageUtils.displayImageFromUrl(context, tempFileMessageUri.toString(), fileThumbnailImage, null);
            } else {
                // Get thumbnails from FileMessage
                ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size() > 0) {
                    if (message.getType().toLowerCase().contains("gif")) {
                        ImageUtils.displayGifImageFromUrl(context, message.getUrl(), fileThumbnailImage, thumbnails.get(0).getUrl(), fileThumbnailImage.getDrawable());
                    } else {
                        ImageUtils.displayImageFromUrl(context, thumbnails.get(0).getUrl(), fileThumbnailImage, fileThumbnailImage.getDrawable());
                    }
                } else {
                    if (message.getType().toLowerCase().contains("gif")) {
                        ImageUtils.displayGifImageFromUrl(context, message.getUrl(), fileThumbnailImage, (String) null, fileThumbnailImage.getDrawable());
                    } else {
                        ImageUtils.displayImageFromUrl(context, message.getUrl(), fileThumbnailImage, fileThumbnailImage.getDrawable());
                    }
                }
            }

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onFileMessageItemClick(message);
                    }
                });
            }
        }
    }

    private class OtherImageFileMessageHolder extends BaseViewHolder {

        TextView timeText, nicknameText, readReceiptText;
        ImageView profileImage, fileThumbnailImage;

        public OtherImageFileMessageHolder(View itemView) {
            super(itemView);

            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            nicknameText = (TextView) itemView.findViewById(R.id.text_group_chat_nickname);
            fileThumbnailImage = (ImageView) itemView.findViewById(R.id.image_group_chat_file_thumbnail);
            profileImage = (ImageView) itemView.findViewById(R.id.image_group_chat_profile);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
        }

        void bind(Context context, final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous, final OnItemClickListener listener) {
            super.bind(message, isNewDay);
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            // Since setChannel is set slightly after adapter is created, check if null.
            if (channel != null) {
                int readReceipt = channel.getReadReceipt(message);
                if (readReceipt > 0) {
                    readReceiptText.setVisibility(View.VISIBLE);
                    readReceiptText.setText(String.valueOf(readReceipt));
                } else {
                    readReceiptText.setVisibility(View.INVISIBLE);
                }
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
                profileImage.setVisibility(View.INVISIBLE);
                nicknameText.setVisibility(View.GONE);
            } else {
                profileImage.setVisibility(View.VISIBLE);
                ImageUtils.displayRoundImageFromUrl(context, message.getSender().getProfileUrl(), profileImage);

                nicknameText.setVisibility(View.VISIBLE);
                nicknameText.setText(message.getSender().getNickname());
            }

            // Get thumbnails from FileMessage
            ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();

            // If thumbnails exist, get smallest (first) thumbnail and display it in the message
            if (thumbnails.size() > 0) {
                if (message.getType().toLowerCase().contains("gif")) {
                    ImageUtils.displayGifImageFromUrl(context, message.getUrl(), fileThumbnailImage, thumbnails.get(0).getUrl(), fileThumbnailImage.getDrawable());
                } else {
                    ImageUtils.displayImageFromUrl(context, thumbnails.get(0).getUrl(), fileThumbnailImage, fileThumbnailImage.getDrawable());
                }
            } else {
                if (message.getType().toLowerCase().contains("gif")) {
                    ImageUtils.displayGifImageFromUrl(context, message.getUrl(), fileThumbnailImage, (String) null, fileThumbnailImage.getDrawable());
                } else {
                    ImageUtils.displayImageFromUrl(context, message.getUrl(), fileThumbnailImage, fileThumbnailImage.getDrawable());
                }
            }

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onFileMessageItemClick(message);
                    }
                });
            }
        }
    }

    /**
     * A ViewHolder for file messages that are videos.
     * Displays only the video thumbnail.
     */
    private class MyVideoFileMessageHolder extends BaseViewHolder {
        TextView timeText, readReceiptText;
        ImageView fileThumbnailImage;
        CircleProgressBar circleProgressBar;

        public MyVideoFileMessageHolder(View itemView) {
            super(itemView);

            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            fileThumbnailImage = (ImageView) itemView.findViewById(R.id.image_group_chat_file_thumbnail);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
            circleProgressBar = (CircleProgressBar) itemView.findViewById(R.id.circle_progress);
        }

        void bind(Context context, final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isTempMessage, boolean isFailedMessage, Uri tempFileMessageUri, final OnItemClickListener listener) {
            super.bind(message, isNewDay);

            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            if (message.getRequestState() == FileMessage.RequestState.PENDING
                    || isFailedMessage && mResendingMessageSet.contains(message.getRequestId())) {
                readReceiptText.setText(R.string.message_failed);
                readReceiptText.setVisibility(View.VISIBLE);

                circleProgressBar.setVisibility(View.GONE);
                mFileMessageMap.remove(message);
            } else if (isTempMessage) {
                readReceiptText.setText(R.string.message_sending);
                readReceiptText.setVisibility(View.GONE);

                circleProgressBar.setVisibility(View.VISIBLE);
                mFileMessageMap.put(message, circleProgressBar);
            } else {
                circleProgressBar.setVisibility(View.GONE);
                mFileMessageMap.remove(message);

                // Since setChannel is set slightly after adapter is created, check if null.
                if (channel != null) {
                    int readReceipt = channel.getReadReceipt(message);
                    if (readReceipt > 0) {
                        readReceiptText.setVisibility(View.VISIBLE);
                        readReceiptText.setText(String.valueOf(readReceipt));
                    } else {
                        readReceiptText.setVisibility(View.INVISIBLE);
                    }
                }
            }

            if (isTempMessage && tempFileMessageUri != null) {
                ImageUtils.displayImageFromUrl(context, tempFileMessageUri.toString(), fileThumbnailImage, null);
            } else {
                // Get thumbnails from FileMessage
                ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size() > 0) {
                    ImageUtils.displayImageFromUrl(context, thumbnails.get(0).getUrl(), fileThumbnailImage, fileThumbnailImage.getDrawable());
                }
            }

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onFileMessageItemClick(message);
                    }
                });
            }
        }
    }

    private class OtherVideoFileMessageHolder extends BaseViewHolder {

        TextView timeText, nicknameText, readReceiptText;
        ImageView profileImage, fileThumbnailImage;

        public OtherVideoFileMessageHolder(View itemView) {
            super(itemView);

            timeText = (TextView) itemView.findViewById(R.id.text_group_chat_time);
            nicknameText = (TextView) itemView.findViewById(R.id.text_group_chat_nickname);
            fileThumbnailImage = (ImageView) itemView.findViewById(R.id.image_group_chat_file_thumbnail);
            profileImage = (ImageView) itemView.findViewById(R.id.image_group_chat_profile);
            readReceiptText = (TextView) itemView.findViewById(R.id.text_group_chat_read_receipt);
        }

        void bind(Context context, final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous, final OnItemClickListener listener) {
            super.bind(message, isNewDay);
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            // Since setChannel is set slightly after adapter is created, check if null.
            if (channel != null) {
                int readReceipt = channel.getReadReceipt(message);
                if (readReceipt > 0) {
                    readReceiptText.setVisibility(View.VISIBLE);
                    readReceiptText.setText(String.valueOf(readReceipt));
                } else {
                    readReceiptText.setVisibility(View.INVISIBLE);
                }
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
                profileImage.setVisibility(View.INVISIBLE);
                nicknameText.setVisibility(View.GONE);
            } else {
                profileImage.setVisibility(View.VISIBLE);
                ImageUtils.displayRoundImageFromUrl(context, message.getSender().getProfileUrl(), profileImage);

                nicknameText.setVisibility(View.VISIBLE);
                nicknameText.setText(message.getSender().getNickname());
            }

            // Get thumbnails from FileMessage
            ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();

            // If thumbnails exist, get smallest (first) thumbnail and display it in the message
            if (thumbnails.size() > 0) {
                ImageUtils.displayImageFromUrl(context, thumbnails.get(0).getUrl(), fileThumbnailImage, fileThumbnailImage.getDrawable());
            }

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onFileMessageItemClick(message);
                    }
                });
            }
        }
    }
}



