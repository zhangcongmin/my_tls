package cn.talianshe.android.bean;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

/**
 * @author zcm
 * @ClassName: AssociationListData
 * @Description: 社团列表数据
 * @date 2017/12/13 13:25
 */
public class AssociationListData extends BaseListBean<AssociationListData.AssociationInfoList> {
    public class AssociationInfoList extends BaseListData<AssociationInfo>{}
    @Parcel
    public static class AssociationInfo{
        public String id;
        public String associationName;
        public String associationLogo;
        public String schoolName;
        public String departmentName;
        public double score;
        public int level;//社团级别 0:院级 1:校级
        public String dutyName;//社团职位
        @SerializedName("infoList")
        public List<Label> labels;


    }
    @Parcel
    public static class Label {
        @SerializedName("infoId")
        public String labelId;
        @SerializedName("lableName")
        public String lableName;
    }
}
