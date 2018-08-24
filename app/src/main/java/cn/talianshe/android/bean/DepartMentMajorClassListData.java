package cn.talianshe.android.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author zcm
 * @ClassName: SchoolListData
 * @Description: 院系、专业、班级列表数据
 * @date 2017/12/11 15:45
 */
public class DepartMentMajorClassListData extends BaseListBean<DepartMentMajorClassListData.DepartMentMajorClassListInfo> {
    public class DepartMentMajorClassListInfo extends BaseListData<DepartmentMajorClassInfo> {
    }

    public static class DepartmentMajorClassInfo {

        public String collegeId;
        public String collegeName;
        public String departmentId;
        public String departmentName;
        @SerializedName("gradeId")
        public String classId;
        public String gradeName;
        public String year;
        @SerializedName("majorsId")
        public String majorId;
        @SerializedName("majorsName")
        public String majorName;
        public String className;
    }
}
