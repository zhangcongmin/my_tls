package cn.talianshe.android.bean;

import org.parceler.Parcel;

public class ActivityTypeListData extends BaseListBean<ActivityTypeListData.ActivityTypeListInfo> {
    public class ActivityTypeListInfo extends BaseListData<ActivityType>{

    }
    @Parcel
    public static class ActivityType{
        public String id;
        public String typeName;
    }
}
