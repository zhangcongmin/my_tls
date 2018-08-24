package cn.talianshe.android.bean;


import org.parceler.Parcel;

public class ActivityPhotoListData extends BaseListBean<ActivityPhotoListData.ActivityPhotoListInfo> {
    public static class ActivityPhotoListInfo extends BaseListData<ActivityPhotoInfo> {
    }

    @Parcel
    public static class ActivityPhotoInfo {
        public int index;
        public String path;
        public int like;
        public String imgid;
    }
}
