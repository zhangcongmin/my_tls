/**
 * @Title: TLSUrl.java
 * @Package com.naerju.network
 * @Description: TODO(用一句话描述该文件做什么)
 * @author think4
 * @date 2014-4-2 下午1:40:52
 * @version V1.0
 */
package cn.talianshe.android.net;

/**
 * @author zcm
 * @ClassName: TLSUrl
 * @Description: url连接表
 * @date 2017/11/28 15:11
 */
public class TLSUrl {

    // 测试
//    public static final String BASE_TEST_URL = "http://192.168.107.90:8010";
//    public static final String BASE_TEST_URL = "http://192.168.107.76:8010";
    public static final String BASE_TEST_URL = "http://test.dev.dkhs.com";

    // 开发
    public static final String BASE_DEV_URL = "http://120.77.84.116:8885";
//        public static final String BASE_DEV_URL = "http://172.16.9.92:8885";
    // 正式
    public static final String BASE_DEV_MAIN = "http://120.77.84.116:8885";
    // 服务器
    public static final String BASE_DEV_TAG = "http://120.77.84.116:8885";

    public static String BASE_URL = BASE_DEV_URL;

    public interface User {
        String login = "/interface/user/login";
        String activationName = "/interface/user/activationName";
        String activationAccount = "/interface/user/activation";
        String changeMobile = "/interface/user/replacePhone";
        String getPrivacySetting = "/interface/user/privacy";
        String setPrivacySetting = "/interface/user/doprivacy";
        String changePassword = "/interface/user/modifypassword";
        String saveUserInfo = "/interface/user/modifyinformation";
        String otherLogin = "/interface/user/otherlogin";
        String getUserInfo = "/interface/user/information";
        String bindThirdAccount = "/interface/user/binding";
        String resetPassword = "/interface/user/forgotPwd";
        String departmentList = "/interface/user/departmentlist";
        String majorList = "/interface/user/majorslist";
        String classList = "/interface/user/gradelist";
        String getTime = "/interface/user/gettime";
        String getCollegeList = "/interface/user/collegelist";
        String checkTimeOut = "/interface/user/timeout";
    }

    public interface Sms {
        String sendVerificationCode = "/sms/sendVcode";
        String forgetPwdSendVerificationCode = "/sms/sendVcodePhone";
    }

    public interface Upload {
        String uploadImage = "/fileUpload/upload";
    }

    public interface School {
        String schoolList = "/interface/school/schoollist";
        String messageList = "/interface/school/messagelist";
        String messageDetail = "/interface/school/messagedetail";
        String emptyMessage = "/interface/school/emptymessage";
        String contactAssociationList = "/interface/school/associationlist";
        String contactAssociationMemberList = "/interface/school/associationmemberlist";
        String studentDetail = "/interface/school/associationmemberdetail";
        String postPersonalMoment = "/interface/school/personaldynamics";
        String postOfficialMoment = "/interface/school/officialdynamics";
        String teacherList = "/interface/school/teacherlist";
        String teacherDetail = "/interface/school/teacherdetail";
        String momentList = "/interface/school/dynamicslist";
        String giveLike = "/interface/school/dodynamics";
        String momentDetail = "/interface/school/dynamicsdetail";
        String forwardList = "/interface/school/forwardList";
        String reviewerList = "/interface/school/reviewerList";
        String likeList = "/interface/school/likeList";
        String forwardMoment = "/interface/association/forward";
        String addressList = "/interface/school/activityfieldlist";
        String addressDetail = "/interface/school/fielddetail";
        String addressOrderList = "/interface/school/orderfieldlist";
        String addressOrderDetail = "/interface/school/orderfielddetail";
        String checkAddressOrder = "/interface/activity/judgeOrder";
        String hasUnreadMsg = "/interface/school/isUnread";
    }

    public interface Association {
        String manageAssociationList = "/interface/association/manageAssociation";
        String hotAssociationList = "/interface/association/hotassociationlist";
        String associationList = "/interface/association/associationlist";
        String createAssociation = "interface/association/createassociation";
        String associationLabel = "/interface/association/associationlabel";
        String associationMemberList = "/interface/association/getmember";
        String associationDetail = "/interface/association/associationdetail";
        String applyJionAssociation = "/interface/association/joinassociation";
        String leaveAssociation = "/interface/association/outassociation";
        String followAssociation = "/interface/association/doassociation";
        String manageAssociation = "/interface/association/manageAssociation";
    }

    public interface Activitys {
        String activityAlbum = "/interface/activity/activityalbum";
        String wonderfulActivity = "/interface/activity/wonderfulactivity";
        String activityList = "/interface/activity/getactivitylist";
        String onGoingActivityList = "/interface/activity/doactivitylist";
        String recommendActivityList = "/interface/activity/activitylist";
        String getActivityAuthorization = "/interface/activity/authorization";
        String publishActivity = "/interface/activity/releaseactivity";
        String getActivityType = "/interface/activity/activitytype";
        String regionList = "/interface/activity/region";
        String activityDetail = "/interface/activity/activitydetail";
        String activityInteraction = "/interface/activity/sceneInteractivity";
        String activityChatList = "/interface/activity/chatlist";
        String activityMemberList = "/interface/activity/activitymember";
        String activityPhotoList = "/interface/activity/wonderfulmoment";
        String modifyActivity = "/interface/activity/modifymemberstate";
        String getActivityScore = "/interface/activity/scoreAndEvaluate";
        String doLikeActivity = "/interface/activity/dolike";
        String doChatActivity = "/interface/activity/dochat";
        String voteActivity = "/interface/activity/vote";
        String getVoteResult = "/interface/activity/voteResult";
        String addSignType = "/interface/activity/addSignType";
        String getSignType = "/interface/activity/signType";
        String getPhotoDetail = "/interface/activity/wonderfuldetail";
        String photoLike = "/interface/activity/wonderfullike";
    }

    public interface Forward {
        String momentForwardUrl = BASE_DEV_URL + "/resources/talianshe/dynamic.html?id=";
        String activityOngoingForwardUrl = BASE_DEV_URL + "/resources/talianshe/ongoing_activities.html?id=";
        String activityUnliveForwardUrl = BASE_DEV_URL + "/resources/talianshe/activity.html?id=";
        String associationForwardUrl = BASE_DEV_URL + "/resources/talianshe/team.html?id=";
    }
}
