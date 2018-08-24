package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MomentDetailData extends BaseBean<MomentDetailData.MomentDetailInfo> {

    public class MomentDetailInfo {

        public String publisherId;
        public String avatar;
        public String realname;
        public String isname;
        public String nickname;
        public String isnickname;
        @SerializedName("createtime")
        public long createTime;
        public String content;
        public String forwardContent;
        public String like;
        public String reviewer;
        public String isLike;
        public String dynamicsImgs;
        @SerializedName("reviewCount")
        public String commentCount;
        @SerializedName("forwarding")
        public String forwardCount;
        @SerializedName("givelike")
        public String likeCount;
    }
}
