package cn.talianshe.android.bean;

import org.parceler.Parcel;

public class RegionListData extends BaseListBean<RegionListData.RegionListInfo> {
    public class RegionListInfo extends BaseListData<RegionInfo>{

    }
    @Parcel
    public static class RegionInfo {
        public String id;
        public String regionName;
    }
}
