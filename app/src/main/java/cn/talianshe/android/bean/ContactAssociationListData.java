package cn.talianshe.android.bean;

/**
 * @author zcm
 * @ClassName: ${CLASS}
 * @Description:
 * @date 2017/12/12 15:55
 */

public class ContactAssociationListData extends BaseListBean<ContactAssociationListData.ContactAssociationListInfo>{

    public class ContactAssociationListInfo extends BaseListData<ContactAssociation>{
    }
    public class ContactAssociation{
        public String id;
        public String associationName;//社团名称
        public String associationSum;//社团人数
    }
}
