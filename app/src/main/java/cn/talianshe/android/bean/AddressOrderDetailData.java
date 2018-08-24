package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressOrderDetailData extends BaseBean<AddressOrderDetailData.AddressOrderDetailInfo> {

    public class AddressOrderDetailInfo {


        /**
         * endtime : 1512018683000
         * teacherList : [{"name":"阿凡达"},{"name":"小明"}]
         * schoolName : 福州大学
         * starttime : 1512011480000
         * presidentName : 学生测试人员
         * associationId : 4
         * score : 3.2222
         * associationLogo : /resources/zheng-admin/upload/images/20171217183900058.jpg
         * fieldId : 1
         * deputyList : [{"studentName":"学生测试人员"}]
         * departmentName : 测试
         * associationName : 社团名称
         * level : 1
         * leaderTeacherName : 主导老师
         * fieldName : 体育馆
         * activityName : 活动名称
         */

        public long endtime;
        public String schoolName;
        public long starttime;
        @SerializedName("presidentName")
        public String leaderName;
        public int associationId;
        public double score;
        public String associationLogo;
        @SerializedName("fieldId")
        public int addressId;
        public String departmentName;
        public String associationName;
        public int level;
        @SerializedName("leaderTeacherName")
        public String leaderTutorName;
        @SerializedName("fieldName")
        public String addressName;
        public String activityName;
        @SerializedName("teacherList")
        public List<ViceTutorBean> viceTutorList;
        @SerializedName("deputyList")
        public List<ViceLeaderBean> viceLeaderList;

    }

    public static class ViceTutorBean {
        /**
         * name : 阿凡达
         */

        public String name;
    }

    public static class ViceLeaderBean {
        /**
         * studentName : 学生测试人员
         */

        public String studentName;
    }
}
