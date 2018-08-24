package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

public class MemberTutorData extends BaseBean<MemberTutorData.TutorInfo> {

    public static class TutorInfo {
        public String id;
        public String avatar;
        public String realname;
        @SerializedName("TeacherId")
        public String teacherId;
        public String titleName;
        public String schoolName;
        public String workExperience;
        public String sex;
    }
}
