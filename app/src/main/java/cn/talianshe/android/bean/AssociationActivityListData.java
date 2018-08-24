package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

public class AssociationActivityListData extends BaseListBean<AssociationActivityListData.AssociationActivityListInfo> {
    public static class AssociationActivityListInfo extends BaseListData<AssociationActivity> {}

    public class AssociationActivity {

        /**
         * id : 5
         * endtime : 1513324579000
         * createtime : 1513229485000
         * starttime : 1513072558000
         * estimatedNumber : 80
         * activityPlace : 游泳馆
         * activityLogo : {}
         * counts : 0
         * activityName : 活动4
         * introduction : sfaasd
         */

        public String id;
        public long endtime;
        public long createtime;
        public long starttime;
        public String estimatedNumber;
        public String activityPlace;
        public ActivityLogo activityLogo;
        public int counts;
        public String activityName;
        public boolean isManager;

        public String level;
    }
        public class ActivityLogo {
            @SerializedName("autoid")
            public String imgId;
            @SerializedName("realpath")
            public String imgPath;
        }
}
