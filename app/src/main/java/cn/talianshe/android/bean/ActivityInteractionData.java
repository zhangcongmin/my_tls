package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

public class ActivityInteractionData extends BaseBean<ActivityInteractionData.ActivityInteractionInfo> {
    @Parcel
    public static class ActivityInteractionInfo {


        /**
         * sceneInteractivity : {"autoId":1,"activityId":1,"curtain":"/resources/zheng-admin/assets/images/1.jpg","typeGroup":"2","content":null,"outsideChain":"1","ischat":1,"chatPermissions":0,"commentModeration":0,"countdownEffect":5,"status":12,"seq":null,"operationuserid":3,"operationtime":1511774645000,"voteList":[],"onwallList":[{"autoId":7,"activityId":1,"title":"标112","beginDate":1511971380000,"endDate":1543680000000,"instruction":"哈哈","textFiltering":"哈哈","seq":null,"status":1,"operationuserid":1,"operationtime":null,"createtime":1513146785000,"sceneInteractivityId":1}],"list":[{"content":"哇**1","id":1,"imgsList":[{"imgId":207,"imgPath":"\\resources\\zheng-admin\\upload\\images\\20171129113100009.jpg"}],"createtime ":1513234627000,"avatar":"/resources/zheng-admin/upload/images/20171208175200047.jpg"},{"content":"哇**2","id":2,"imgsList":[],"createtime ":1513234627000,"avatar":"/resources/zheng-admin/upload/images/20171208175200047.jpg"},{"content":"哇**3","id":3,"imgsList":[],"createtime ":1513234628000,"avatar":"/resources/zheng-admin/upload/images/20171208175200047.jpg"},{"content":"哇**4","id":6,"imgsList":[],"createtime ":1513234629000,"avatar":"/resources/zheng-admin/upload/images/20171208175200047.jpg"}],"createtime":1511517085000}
         */

        public SceneInteractivityBean sceneInteractivity;
    }
    @Parcel
    public static class SceneInteractivityBean {
        /**
         * autoId : 1
         * activityId : 1
         * curtain : /resources/zheng-admin/assets/images/1.jpg
         * typeGroup : 2
         * content : null
         * outsideChain : 1
         * ischat : 1
         * chatPermissions : 0
         * commentModeration : 0
         * countdownEffect : 5
         * status : 12
         * seq : null
         * operationuserid : 3
         * operationtime : 1511774645000
         * voteList : []
         * onwallList : [{"autoId":7,"activityId":1,"title":"标112","beginDate":1511971380000,"endDate":1543680000000,"instruction":"哈哈","textFiltering":"哈哈","seq":null,"status":1,"operationuserid":1,"operationtime":null,"createtime":1513146785000,"sceneInteractivityId":1}]
         * list : [{"content":"哇**1","id":1,"imgsList":[{"imgId":207,"imgPath":"\\resources\\zheng-admin\\upload\\images\\20171129113100009.jpg"}],"createtime ":1513234627000,"avatar":"/resources/zheng-admin/upload/images/20171208175200047.jpg"},{"content":"哇**2","id":2,"imgsList":[],"createtime ":1513234627000,"avatar":"/resources/zheng-admin/upload/images/20171208175200047.jpg"},{"content":"哇**3","id":3,"imgsList":[],"createtime ":1513234628000,"avatar":"/resources/zheng-admin/upload/images/20171208175200047.jpg"},{"content":"哇**4","id":6,"imgsList":[],"createtime ":1513234629000,"avatar":"/resources/zheng-admin/upload/images/20171208175200047.jpg"}]
         * createtime : 1511517085000
         */

        @SerializedName("autoId")
        public int id;//活动投票id
        public int activityId;
        @SerializedName("curtain")
        public String curScreenPath;
        public String typeGroup;
        public String content;
        public String outsideChain;
        public int ischat;
        public int chatPermissions;
        public int commentModeration;
        public int status;
        public long createtime;
        public List<VoteListBean> voteList;
        public List<ListBean> list;
        public List<OnwallListBean> onwallList;
    }
    @Parcel
    public static class VoteListBean{
        public int countdownEffect;//倒计时时间
        @SerializedName("autoId")
        public String id;
        public String activityId;
        @SerializedName("operationuserid")
        public String operationuserId;
        public String title;
        @SerializedName("begenDate")
        public long beginDate;
        public long endDate;
        public int votingRights;
        public int choose;//0单选 1多选
        public String status;
        public String sceneInteractivityId;
        @SerializedName("vote")
        public boolean isVote;
        @SerializedName("voteselectList")
        public List<VoteResultListBean> voteSelectList;
    }
    @Parcel
    public static class VoteResultListBean{
        @SerializedName("autoid")
        public String id;
        public String voteId;
        public String name;
        public long operationuserid;
        public long operationtime;
        public long createtime;
    }
    @Parcel
    public static class OnwallListBean {
        /**
         * autoId : 7
         * activityId : 1
         * title : 标112
         * beginDate : 1511971380000
         * endDate : 1543680000000
         * instruction : 哈哈
         * textFiltering : 哈哈
         * seq : null
         * status : 1
         * operationuserid : 1
         * operationtime : null
         * createtime : 1513146785000
         * sceneInteractivityId : 1
         */

        public int autoId;
        public int activityId;
        public String title;
        public long beginDate;
        public long endDate;
        public String instruction;
        public String textFiltering;
        public int status;
        public int operationuserid;
        public long operationtime;
        public long createtime;
        public int sceneInteractivityId;
    }
    @Parcel
    public static class ListBean {
        /**
         * content : 哇**1
         * id : 1
         * imgsList : [{"imgId":207,"imgPath":"\\resources\\zheng-admin\\upload\\images\\20171129113100009.jpg"}]
         * createtime  : 1513234627000
         * avatar : /resources/zheng-admin/upload/images/20171208175200047.jpg
         */

        public String content;
        public int id;
        public long createtime;
        public String avatar;
        public List<ImgsListBean> imgsList;

    }
    @Parcel
    public static class ImgsListBean {

        public int imgId;
        public String imgPath;
    }
}
