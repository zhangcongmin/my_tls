package cn.talianshe.android.bean;


public class HotAssociationListData extends BaseListBean<HotAssociationListData.HotAssociationListInfo> {
    public class HotAssociationListInfo extends BaseListData<HotAssociation> {
    }

    public class HotAssociation {
        public String id;//成员id，学生表里的id
        public String associationName;//社团人数
        public double score;//社团评价
        public String associationLogo;//社团logo
    }
}
