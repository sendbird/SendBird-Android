package com.sendbird.localcaching.sample.utils;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.sendbird.localcaching.sample.utils.SyncManagerUtils.getMyUserId;

public class TextUtils {
    public static String getGroupChannelTitle(GroupChannel channel) {
        List<Member> members = channel.getMembers();

        String myUserId = getMyUserId();

        if (members.size() < 2 || myUserId == null) {
            return "No Members";
        } else if (members.size() == 2) {
            StringBuilder names = new StringBuilder();
            for (Member member : members) {
                if (member.getUserId().equals(myUserId)) {
                    continue;
                }

                names.append(", ").append(member.getNickname());
            }
            return names.delete(0, 2).toString();
        } else {
            int count = 0;
            StringBuilder names = new StringBuilder();
            for (User member : members) {
                if (member.getUserId().equals(myUserId)) {
                    continue;
                }

                count++;
                names.append(", ").append(member.getNickname());

                if(count >= 10) {
                    break;
                }
            }
            return names.delete(0, 2).toString();
        }
    }

    /**
     * Calculate MD5
     * @param data
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String generateMD5(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(data.getBytes());
        byte[] messageDigest = digest.digest();

        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) hexString.append(Integer.toHexString(0xFF & b));

        return hexString.toString();
    }

    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0;
    }
}
