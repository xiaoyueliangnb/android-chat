/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message.notification;

import static cn.wildfirechat.message.core.MessageContentType.CONTENT_TYPE_MUTE_MEMBER;

import android.os.Parcel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.remote.ChatManager;

@ContentTag(type = CONTENT_TYPE_MUTE_MEMBER, flag = PersistFlag.Persist)
public class GroupMuteMemberNotificationContent extends NotificationMessageContent {
    public String groupId;
    public String operator;
    // 操作类型，1禁言，0取消禁言
    public int type;
    public List<String> memberIds;

    @Override
    public String formatNotification(Message message) {
        StringBuilder sb = new StringBuilder();
        if (fromSelf) {
            sb.append("您");
        } else {
            sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, operator));
        }
        sb.append("把");
        if (memberIds != null) {
            for (int i = 0; i < memberIds.size() && i < 4; i++) {
                sb.append(" ");
                sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, memberIds.get(i)));
            }
            if (memberIds.size() > 4) {
                sb.append(" 等");
            }
        }
        if (type == 0) {
            sb.append("取消禁言");
        } else {
            sb.append("设置禁言");
        }

        return sb.toString();
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("g", groupId);
            objWrite.put("o", operator);
            objWrite.put("n", type + "");
            objWrite.put("ms", new JSONArray(memberIds));
            payload.binaryContent = objWrite.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return payload;
    }

    @Override
    public void decode(MessagePayload payload) {
        super.decode(payload);
        try {
            if (payload.binaryContent != null) {
                JSONObject jsonObject = new JSONObject(new String(payload.binaryContent));
                groupId = jsonObject.optString("g");
                operator = jsonObject.optString("o");
                type = Integer.parseInt(jsonObject.optString("n", "0"));
                memberIds = new ArrayList<>();
                JSONArray jsonArray = jsonObject.getJSONArray("ms");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        memberIds.add(jsonArray.getString(i));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.groupId);
        dest.writeString(this.operator);
        dest.writeInt(this.type);
        dest.writeStringList(this.memberIds);
    }

    public GroupMuteMemberNotificationContent() {
    }

    protected GroupMuteMemberNotificationContent(Parcel in) {
        super(in);
        this.groupId = in.readString();
        this.operator = in.readString();
        this.type = in.readInt();
        this.memberIds = in.createStringArrayList();
    }

    public static final Creator<GroupMuteMemberNotificationContent> CREATOR = new Creator<GroupMuteMemberNotificationContent>() {
        @Override
        public GroupMuteMemberNotificationContent createFromParcel(Parcel source) {
            return new GroupMuteMemberNotificationContent(source);
        }

        @Override
        public GroupMuteMemberNotificationContent[] newArray(int size) {
            return new GroupMuteMemberNotificationContent[size];
        }
    };
}
