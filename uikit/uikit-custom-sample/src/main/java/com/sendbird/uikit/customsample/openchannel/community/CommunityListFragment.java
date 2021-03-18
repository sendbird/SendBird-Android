package com.sendbird.uikit.customsample.openchannel.community;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.openchannel.OpenChannelListFragment;
import com.sendbird.uikit.log.Logger;


public class CommunityListFragment extends OpenChannelListFragment {
    public CommunityListFragment() {
        super(new CommunityListAdapter());
        setCustomTypeFilter(StringSet.SB_COMMUNITY_TYPE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.community_list_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        final MenuItem createMenuItem = menu.findItem(R.id.action_create_channel);
        View rootView = createMenuItem.getActionView();
        rootView.setOnClickListener(v -> onOptionsItemSelected(createMenuItem));
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create_channel && getActivity() != null) {
            Logger.d("++ create button clicked");
            Intent intent = new Intent(getActivity(), CreateCommunityActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
    }

    @Override
    protected void clickOpenChannelItem(OpenChannel openChannel) {
        if (getContext() == null || openChannel == null) return;
        startActivity(CommunityActivity.newIntent(getContext(), openChannel.getUrl()));
    }
}
