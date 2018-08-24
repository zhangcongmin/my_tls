package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

public class MomentListData extends BaseListBean<MomentListData.MomentListInfo>{

    public class MomentListInfo extends BaseListData<MomentInfo>{
    }
    @Parcel
    public static class MomentInfo {
        @SerializedName("auto_id")
        public String id;
        @SerializedName("createtime")
        public String createTime;//创建时间
        @SerializedName("forwarding")
        public int forwardCount;//转发数量
        public int reviewCount;//评论数量
        public int givelike;//点赞数量
        public String publisherId;//发布人id
        public String publisher;//发布人姓名
        public String avatar;//发布人头像
        public String dynamicsImgs;//动态图片
        public String forwardContent;//转发内容
        public String content;//动态内容
        public String isLike;//是否点赞 1点赞 0没点赞
}
    }
