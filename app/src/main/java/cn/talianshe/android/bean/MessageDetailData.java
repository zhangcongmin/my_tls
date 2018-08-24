package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

public class MessageDetailData extends BaseBean<MessageDetailData.MessageDetailInfo> {

    public class MessageDetailInfo {
        public String id;
        public String content;
        @SerializedName("reception")
        public String url;
        @SerializedName("createtime")
        public long createTime;
        public String title;
        @SerializedName("imgsList")
        public List<MsgImage> imgList;
    }
    public class MsgImage{
        public String imgId;
        public String imgPath;
    }
}
