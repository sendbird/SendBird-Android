package com.sendbird.uikit.customsample.openchannel.livestream;

import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.openchannel.OpenChannelListFragment;


public class LiveStreamListFragment extends OpenChannelListFragment {
    public LiveStreamListFragment() {
        super(new LiveStreamListAdapter());
        setCustomTypeFilter(StringSet.SB_LIVE_TYPE);
    }

    @Override
    protected void clickOpenChannelItem(OpenChannel openChannel) {
        if (getContext() == null || openChannel == null) return;
        startActivity(LiveStreamActivity.newIntent(getContext(), openChannel.getUrl()));
    }
}
