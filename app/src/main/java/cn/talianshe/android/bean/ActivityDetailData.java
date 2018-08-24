package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

public class ActivityDetailData extends BaseBean<ActivityDetailData.ActivityDetailInfo> {

    @Parcel
    public static class ActivityDetailInfo {

        /**
         * endtime : 1512018683000
         * signStarttime : 1510977727000
         * signEndtime : 1511948544000
         * starttime : 1512011480000
         * activityImg : [{"autoid":11,"attachmentcontent":null,"title":"221","businesskey":1,"extend":null,"realpath":"/resources/zheng-admin/assets/images/placeholder.png","likes":0,"busentityname":"activityBanner","seq":null,"status":1,"operationuserid":null,"operationtime":null,"createtime":1512308772000},{"autoid":12,"attachmentcontent":null,"title":"3231","businesskey":1,"extend":null,"realpath":"/resources/zheng-admin/assets/images/placehsfsfolder.png","likes":0,"busentityname":"activityBanner","seq":0,"status":1,"operationuserid":null,"operationtime":null,"createtime":1512308774000}]
         * associationId : 4
         * score : 3.2222
         * activityPlace : 体育馆
         * activityState : 1
         * associationLogo : /resources/zheng-admin/upload/images/20171217183900058.jpg
         * isCan : 1
         * id : 1
         * associationName : 社团名称
         * estimatedNumber : 30
         * level : 1
         * name : 福州大学
         * signtime : 30
         * topnews : [{"autoid":3,"type":"0","activityId":1,"associationId":1,"title":"测试社团公告","stick":1,"seq":4,"status":1,"operationuserid":3,"operationtime":1512544669000,"createtime":1512184072000,"content":null}]
         * counts : 6
         * activityType : 5
         * activityName : 活动名称
         * introduction : 活动简价
         */

        public long endtime;
        @SerializedName("signStarttime")
        public long registStarttime;
        @SerializedName("signEndtime")
        public long registEndtime;
        public long starttime;
        public String associationId;
        public double score;
        public String activityPlace;
        public int activityState;
        public String associationLogo;
        public int isCan;
        public int id;
        public String associationName;
        @SerializedName("estimatedNumber")
        public String maxNum;
        public int level;
        public String schoolName;
        public int signtime;
        @SerializedName("counts")
        public int participantNum;
        public String activityType;
        public String activityName;
        @SerializedName("introduction")
        public String desc;
        @SerializedName("activityImg")
        public List<ActivityImg> bannerList;
        @SerializedName("topnews")
        public List<NoticeBean> noticeList;
        public int isAttention;//是否关注 1、关注 0 未关注
        public int isVote; //当前时间没有互动  1.已投票 2.未投票
        public boolean isSignConfigure;//是否配置了签到

        public boolean isLike;
    }

    @Parcel
    public static class ActivityImg {
        /**
         * autoid : 11
         * attachmentcontent : null
         * title : 221
         * businesskey : 1
         * extend : null
         * realpath : /resources/zheng-admin/assets/images/placeholder.png
         * likes : 0
         * busentityname : activityBanner
         * seq : null
         * status : 1
         * operationuserid : null
         * operationtime : null
         * createtime : 1512308772000
         */

        @SerializedName("autoid")
        public int id;
        public String title;
        public String realpath;
    }

}
