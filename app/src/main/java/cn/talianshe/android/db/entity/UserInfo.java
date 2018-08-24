package cn.talianshe.android.db.entity;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import cn.talianshe.android.net.GlobalParams;

/**
 * @author zcm
 * @ClassName: UserInfo
 * @Description: 用户token
 * @date 2017/12/8 16:44
 */
@Entity(nameInDb = "user")
public class UserInfo {

    public String token;
    /**
     * sex : 1
     * phone :
     * political : null
     * nickname : null
     * department : 金融系
     * qqopen_id :
     * weixinopen_id : null
     * avatar : /resources/zheng-admin/images/avatar.jpg
     * id : 36
     * school : 福州大学
     * studentId  : zcm
     * native : null
     * email : 123456@qq.com
     * grade : 科技
     * realname : 学生测试人员
     * major : 金融
     * isactivation : null
     * qq : null
     */

    @Id
    public Long id;
    public String sex; //0是女 1是男
    @SerializedName("phone")
    public String mobile;
    @SerializedName("political")
    public String politicalOutlook;
    public String nickname;
    public String department;
    @SerializedName("qqopen_id")
    public String qqopenId; //如果为0，说明未绑定
    @SerializedName("weixinopen_id")
    public String weixinopenId; //如果为0，说明未绑定
    public String avatar;
    public String school;
    public String schoolId;
    public String studentId;
    public String teacherId;
    public boolean isTeacher;
    @SerializedName("native")
    public String originPlace;
    public String email;
    public String grade;
    public String realname;
    public String major;
    @SerializedName("Identity")
    public String identity;

    @SerializedName("isactivation")
    public String isActivated; //是否激活，0代表未激活 1代表激活
    public String qq;

    @Generated(hash = 1180234021)
    public UserInfo(String token, Long id, String sex, String mobile, String politicalOutlook, String nickname, String department,
                    String qqopenId, String weixinopenId, String avatar, String school, String schoolId, String studentId, String teacherId,
                    boolean isTeacher, String originPlace, String email, String grade, String realname, String major, String identity,
                    String isActivated, String qq) {
        this.token = token;
        this.id = id;
        this.sex = sex;
        this.mobile = mobile;
        this.politicalOutlook = politicalOutlook;
        this.nickname = nickname;
        this.department = department;
        this.qqopenId = qqopenId;
        this.weixinopenId = weixinopenId;
        this.avatar = avatar;
        this.school = school;
        this.schoolId = schoolId;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.isTeacher = isTeacher;
        this.originPlace = originPlace;
        this.email = email;
        this.grade = grade;
        this.realname = realname;
        this.major = major;
        this.identity = identity;
        this.isActivated = isActivated;
        this.qq = qq;
    }

    @Generated(hash = 1279772520)
    public UserInfo() {
    }

    /**
     * 用户是否需要补充信息
     *
     * @return
     */
    public boolean needFillInfo() {
        if (isTeacher) {
            return TextUtils.isEmpty(identity);
        } else {
            if (TextUtils.isEmpty(department) || TextUtils.isEmpty(major) || TextUtils.isEmpty(grade) || TextUtils.isEmpty(identity))
                return true;
        }
        return false;
    }

    /**
     * 微信是否绑定
     *
     * @return
     */
    public boolean isWechatBind() {
        return !TextUtils.isEmpty(weixinopenId) && !"0".equals(weixinopenId);
    }

    /**
     * QQ是否绑定
     *
     * @return
     */
    public boolean isQQBind() {
        return !TextUtils.isEmpty(qqopenId) && !"0".equals(qqopenId);
    }

    /**
     * 账号是否激活
     *
     * @return
     */
    public boolean isActivated() {
        return "1".equals(isActivated);
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSex() {
        return this.sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPoliticalOutlook() {
        return this.politicalOutlook;
    }

    public void setPoliticalOutlook(String politicalOutlook) {
        this.politicalOutlook = politicalOutlook;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDepartment() {
        return this.department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getQqopenId() {
        return this.qqopenId;
    }

    public void setQqopenId(String qqopenId) {
        this.qqopenId = qqopenId;
    }

    public String getWeixinopenId() {
        return this.weixinopenId;
    }

    public void setWeixinopenId(String weixinopenId) {
        this.weixinopenId = weixinopenId;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSchool() {
        return this.school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSchoolId() {
        return this.schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getStudentId() {
        return this.studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTeacherId() {
        return this.teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public boolean getIsTeacher() {
        return this.isTeacher;
    }

    public void setIsTeacher(boolean isTeacher) {
        this.isTeacher = isTeacher;
    }

    public String getOriginPlace() {
        return this.originPlace;
    }

    public void setOriginPlace(String originPlace) {
        this.originPlace = originPlace;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGrade() {
        return this.grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRealname() {
        return this.realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getMajor() {
        return this.major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getIdentity() {
        return this.identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getIsActivated() {
        return this.isActivated;
    }

    public void setIsActivated(String isActivated) {
        this.isActivated = isActivated;
    }

    public String getQq() {
        return this.qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

}
