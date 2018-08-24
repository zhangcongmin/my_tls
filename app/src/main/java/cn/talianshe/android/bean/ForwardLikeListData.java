package cn.talianshe.android.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author zcm
 * @ClassName: ForwardLikeListData
 * @Description:
 * @date 2017/12/15 14:16
 */
public class ForwardLikeListData extends BaseListBean<ForwardLikeListData.ForwardLikeListInfo> {

    public class ForwardLikeListInfo extends BaseListData<ForwardCommentLikeInfo> {
    }

    public static class ForwardCommentLikeInfo {
        public String forwardId;//转发人id
        public String forwardAvatar ;//转发人头像
        @SerializedName("reviewerId")
        public String commentId;//点赞人id
        @SerializedName("reviewerAvatar")
        public String commentAvatar;//点赞人头像
        public String likeId;//点赞人id
        public String likeAvatar;//点赞人头像
        public String realname;
        public String isname;
        public String nickname;
        public String isnickname;
        public String createtime;
        public String forwardContent;
        @SerializedName("content")
        public String commentContent;
        public String isLike;
    }
}
