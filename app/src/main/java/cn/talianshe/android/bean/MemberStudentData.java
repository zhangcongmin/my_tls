package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

public class MemberStudentData extends BaseBean<MemberStudentData.StudentInfo> {
    public static class StudentInfo {
        public String id; //成员id
        public String realname;
        public String nickname;
        public String isname;
        public String isnickname;
        public String schoolName;
        public String majors;
        public String grade;
        @SerializedName("isphone")
        public String isMobile;
        @SerializedName("phone")
        public String mobile;
        public String avatar;
        public String sex;

    }
}
