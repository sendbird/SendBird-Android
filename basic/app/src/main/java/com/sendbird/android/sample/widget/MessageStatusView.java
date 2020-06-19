package com.sendbird.android.sample.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.sample.R;

public class MessageStatusView extends FrameLayout {
    private ImageView messageStatus;
    private ProgressBar progressBar;

    public MessageStatusView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MessageStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MessageStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_message_status, this, true);
        messageStatus = view.findViewById(R.id.image_message_status);
        progressBar = view.findViewById(R.id.progress);
    }

    public void drawMessageStatus(GroupChannel channel, BaseMessage message) {
        BaseMessage.SendingStatus status = message.getSendingStatus();

        switch (status) {
            case CANCELED:
            case FAILED:
                drawError();
                break;
            case SUCCEEDED:
                int unreadMemberCount = channel.getReadReceipt(message);
                int unDeliveredMemberCount = channel.getDeliveryReceipt(message);

                if (unreadMemberCount == 0) {
                    drawRead();
                } else if (unDeliveredMemberCount == 0) {
                    drawDelivered();
                } else {
                    drawSent();
                }
                break;
            default:
                drawProgress();
                break;
        }
    }

    private void drawError() {
        setProgress(false);
        messageStatus.setImageResource(R.drawable.icon_error_filled);
    }

    private void drawRead() {
        setProgress(false);
        messageStatus.setImageResource(R.drawable.icon_read);
    }

    private void drawSent() {
        setProgress(false);
        messageStatus.setImageResource(R.drawable.icon_sent);
    }

    private void drawDelivered() {
        setProgress(false);
        messageStatus.setImageResource(R.drawable.icon_delivered);
    }

    private void drawProgress() {
        setProgress(true);
    }

    private void setProgress(boolean isProgress) {
        if (isProgress) {
            messageStatus.setVisibility(GONE);
            progressBar.setVisibility(VISIBLE);
        } else {
            progressBar.setVisibility(GONE);
            messageStatus.setVisibility(VISIBLE);
        }
    }
}
