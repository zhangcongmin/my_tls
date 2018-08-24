package cn.talianshe.android.bean;


public class ManageAssociationListData extends BaseListBean<ManageAssociationListData.ManageAssociationListInfo> {
    public class ManageAssociationListInfo extends BaseListData<ManageAssociation> {
    }

    public class ManageAssociation {
        public String id;//成员id，学生表里的id
        public String associationName;//社团人数
    }
}
