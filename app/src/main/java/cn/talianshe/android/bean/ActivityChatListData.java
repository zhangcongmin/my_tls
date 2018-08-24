package cn.talianshe.android.bean;


import java.util.List;

public class ActivityChatListData extends BaseListBean<ActivityChatListData.ActivityChatListInfo> {
    public static class ActivityChatListInfo extends BaseListData<ActivityChatInfo> {
    }

    public static class ActivityChatInfo {

        /**
         * content : 哇哈哈1
         * id : 1
         * imgsList : [{"imgId":13,"imgPath":"\\resources\\zheng-admin\\upload\\images\\20171128110100054.jpg"},{"imgId":14,"imgPath":"\\resources\\zheng-admin\\upload\\images\\20171129113100009.jpg"}]
         * createtime  : 1513234627000
         * avatar : /resources/zheng-admin/upload/images/20171208175200047.jpg
         */

        public long id;
        public String content;
        public String realname;
        public String isname;
        public String nickname;
        public String isnickname;
        public long createtime;
        public String avatar;
        public List<ChatImg> imgsList;
    }

    public static class ChatImg {

        public String imgId;
        public String imgPath;
    }
}
