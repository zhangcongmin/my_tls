package cn.talianshe.android.bean;


public class ActivityMemberListData extends BaseListBean<ActivityMemberListData.ActivityMemberListInfo> {
    public static class ActivityMemberListInfo extends BaseListData<ActivityMemberInfo> {
    }

    public class ActivityMemberInfo {

        /**
         * id : 33
         * isname : 0
         * isnickname : 1
         * nickname : 昵称
         * realname : 学生测试人员
         * avatar : /resources/zheng-admin/upload/images/20171216/20171216222522_807.jpg
         * type : 4
         */

        public int id;
        public String isname;
        public String isnickname;
        public String nickname;
        public String realname;
        public String avatar;
        public int type;
    }
}
