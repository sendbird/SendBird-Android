package com.sendbird.android.sample.utils;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;

import java.util.List;

public class TextUtils {
    public static String getGroupChannelTitle(GroupChannel channel) {
        List<User> members = channel.getMembers();

        if (members.size() < 2) {
            return "No Members";
        } else if (members.size() == 2) {
            StringBuffer names = new StringBuffer();
            for (User member : members) {
                if (member.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    continue;
                }

                names.append(", " + member.getNickname());
            }
            return names.delete(0, 2).toString();
        } else {
            int count = 0;
            StringBuffer names = new StringBuffer();
            for (User member : members) {
                if (member.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    continue;
                }

                count++;
                names.append(", " + member.getNickname());

                if(count >= 10) {
                    break;
                }
            }
            return names.delete(0, 2).toString();
        }
    }
}
