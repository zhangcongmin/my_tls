package cn.talianshe.android.net.service;

import cn.talianshe.android.bean.DepartMentMajorClassListData;
import cn.talianshe.android.bean.LoginData;
import cn.talianshe.android.bean.LongData;
import cn.talianshe.android.bean.PrivacySettingData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.UserData;
import cn.talianshe.android.net.TLSUrl;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * @author zcm
 * @ClassName: UserApiService
 * @Description: 用户相关api
 * @date 2017/11/28 19:04
 */
public interface UserApiService {
    @FormUrlEncoded
    @POST(TLSUrl.User.login)
    Observable<LoginData> loginByMobile(@Field("mobile") String mobile, @Field("password") String password);

    @FormUrlEncoded
    @POST(TLSUrl.User.login)
    Observable<LoginData> loginByAccount(@Field("username") String username, @Field("password") String password, @Field("schoolid") String schoolId);

    @FormUrlEncoded
    @POST(TLSUrl.User.otherLogin)
    Observable<LoginData> otherLogin(@Field("openid") String openid, @Field("type") String type);

    /**
     * 校验姓名密码
     */
    @FormUrlEncoded
    @POST(TLSUrl.User.activationName)
    Observable<StringData> activationName(@Field("name") String realName, @Field("password") String password);

    /**
     * 激活账号
     */
    @FormUrlEncoded
    @POST(TLSUrl.User.activationAccount)
    Observable<StringData> activationAccount(@Field("mobile") String mobile, @Field("vcode") String verificationCode, @Field("password") String password);

    /**
     * 更换绑定手机
     */
    @FormUrlEncoded
    @POST(TLSUrl.User.changeMobile)
    Observable<StringData> changeMobile(@Field("phone") String mobile, @Field("code") String verificationCode, @Field("password") String password);

    /**
     * 获取隐私设置
     */
    @POST(TLSUrl.User.getPrivacySetting)
    Observable<PrivacySettingData> getPrivacySetting();

    /**
     * 获取隐私设置
     */
    @FormUrlEncoded
    @POST(TLSUrl.User.setPrivacySetting)
    Observable<StringData> setPrivacySetting(@Field("isname") String isName, @Field("isNickname") String isNickname, @Field("isphone") String isMobile);

    /**
     * 修改密码
     */
    @FormUrlEncoded
    @POST(TLSUrl.User.changePassword)
    Observable<StringData> changePassword(@Field("oldpassword") String oldPassword, @Field("newpassword") String newpassword);

   /* *//**
     * 修改登录用户资料
     *//*
    @POST(TLSUrl.User.saveUserInfo)
    Observable<StringData> saveUserInfo(@Body MultipartBody multipartBody);
*/

    /**
     * 修改密码
     */
    @FormUrlEncoded
    @POST(TLSUrl.User.saveUserInfo)
    Observable<StringData> saveUserInfo(@Field("avatar") String avatarId, @Field("nickname") String nickname,
                                        @Field("Identity") String identityId, @Field("qq") String qq,
                                        @Field("email") String email, @Field("natives") String originPlace,
                                        @Field("political") String outLook, @Field("sex") String sex,
                                        @Field("departmentId") String departmentId,
                                        @Field("majorsId") String majorId, @Field("gradeId") String classId,
                                        @Field("phone") String phone);

    /**
     * 修改登录用户资料
     */
    @POST(TLSUrl.School.postPersonalMoment)
    Observable<StringData> postPersonalMoment(@Body MultipartBody multipartBody);

    @POST(TLSUrl.User.getUserInfo)
    Observable<UserData> getUserInfo();

    /**
     * @param openid 绑定qq，0代表解绑QQ
     * @return
     */
    @FormUrlEncoded
    @POST(TLSUrl.User.bindThirdAccount)
    Observable<StringData> bindQQ(@Field("qqopenid") String openid);

    /**
     * @param openid 绑定qq，0代表解绑QQ
     * @return
     */
    @FormUrlEncoded
    @POST(TLSUrl.User.bindThirdAccount)
    Observable<StringData> bindWechat(@Field("wxopenid") String openid);

    /**
     * 发送验证码
     */
    @FormUrlEncoded
    @POST(TLSUrl.Sms.sendVerificationCode)
    Observable<StringData> sendVerificationCode(@Field("phoneNumber") String mobile);
    /**
     * 忘记密码发送验证码
     */
    @FormUrlEncoded
    @POST(TLSUrl.Sms.forgetPwdSendVerificationCode)
    Observable<StringData> sendForgetPwdVerificationCode(@Field("phoneNumber") String mobile);

    /**
     * 重置密码
     */
    @FormUrlEncoded
    @POST(TLSUrl.User.resetPassword)
    Observable<StringData> resetPassword(@Field("mobile") String mobile, @Field("vcode") String verificationCode, @Field("password") String password);

    @POST(TLSUrl.User.getCollegeList)
    Observable<DepartMentMajorClassListData> getCollegeList();

    @FormUrlEncoded
    @POST(TLSUrl.User.checkTimeOut)
    Observable<StringData> checkTimeOut(@Field("time") String time);

    @FormUrlEncoded
    @POST(TLSUrl.User.departmentList)
    Observable<DepartMentMajorClassListData> getDepartmentList(@Field("collegeId") String collegeId);

    @FormUrlEncoded
    @POST(TLSUrl.User.majorList)
    Observable<DepartMentMajorClassListData> getMajorList(@Field("departmentId") String departmentId);

    @FormUrlEncoded
    @POST(TLSUrl.User.classList)
    Observable<DepartMentMajorClassListData> getClassList(@Field("majorsId") String majorId);

    @POST(TLSUrl.User.getTime)
    Observable<LongData> getTime();
}
