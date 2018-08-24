package cn.talianshe.android.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author zcm
 * @ClassName: AssociationListData
 * @Description: 社团列表数据
 * @date 2017/12/13 13:25
 */
public class AssociationLabelListData extends BaseListBean<AssociationLabelListData.AssociationLabelInfoList> {
    public class AssociationLabelInfoList extends BaseListData<AssociationLabelInfo>{}
    public class AssociationLabelInfo {
        public String id;
        public String lableName;


    }
}
