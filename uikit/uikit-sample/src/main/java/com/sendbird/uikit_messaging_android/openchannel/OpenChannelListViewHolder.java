package com.sendbird.uikit_messaging_android.openchannel;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.OpenChannel;

abstract public class OpenChannelListViewHolder extends RecyclerView.ViewHolder {
    public OpenChannelListViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    abstract protected void bind(OpenChannel openChannel);
}
