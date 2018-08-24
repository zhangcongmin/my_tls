package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import cn.talianshe.android.utils.JianPinUtil;
import cn.talianshe.android.utils.PinYinUtil;

public class MessageListData extends BaseListBean<MessageListData.MessageListInfo> {

    public class MessageListInfo extends BaseListData<MessageInfo> {
    }

    public class MessageInfo{
        @SerializedName("autoId")
        public String id;
        @SerializedName("createtime")
        public long createTime;//
        public int reades;//是否阅读过(1未读;0已读)
        public String title;
        public String content;
        public String htmlContent;
        public String messageAvatar;

    }
}
