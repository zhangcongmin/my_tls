package cn.talianshe.android.bean;


import org.parceler.Parcel;

import java.util.ArrayList;

import cn.talianshe.android.eventbus.CostSouceEvent;

@Parcel
public class CreateActivityData {
    public String activityName;
    public String activityDesc;
    public String mSelectedLogoPhoto;
    public ArrayList<String> mSelectedBannerPhotos;
    public AssociationListData.AssociationInfo associationInfo;
    public ActivityTypeListData.ActivityType activityTypeInfo;
    public String num;
    public CostSouceEvent costSouceEvent;
}
