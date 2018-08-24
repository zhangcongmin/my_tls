package cn.talianshe.android.net.service;

import cn.talianshe.android.bean.ActivityAddressListData;
import cn.talianshe.android.bean.AddressDetailData;
import cn.talianshe.android.bean.AddressOrderDetailData;
import cn.talianshe.android.bean.AddressOrderListData;
import cn.talianshe.android.bean.AssociationDetailData;
import cn.talianshe.android.bean.BooleanData;
import cn.talianshe.android.bean.ForwardLikeListData;
import cn.talianshe.android.bean.MemberStudentData;
import cn.talianshe.android.bean.ContactAssociationListData;
import cn.talianshe.android.bean.AssociationMemberListData;
import cn.talianshe.android.bean.MessageDetailData;
import cn.talianshe.android.bean.MessageListData;
import cn.talianshe.android.bean.MomentDetailData;
import cn.talianshe.android.bean.MomentListData;
import cn.talianshe.android.bean.SchoolListData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.MemberTutorData;
import cn.talianshe.android.bean.TutorListData;
import cn.talianshe.android.net.TLSUrl;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * @author zcm
 * @ClassName: SchoolApiService
 * @Description: 学校相关api
 * @date 2017/12/11 15:10
 */
public interface SchoolApiService {
    @POST(TLSUrl.School.schoolList)
    Observable<SchoolListData> getSchoolList();

    /**
     * 获取通讯录社团列表
     */
    @POST(TLSUrl.School.contactAssociationList)
    Observable<ContactAssociationListData> getContactAssociationList();

    /**
     * 获取社团成员列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.Association.associationMemberList)
    Observable<AssociationMemberListData> getAssociationMemberList(@Field("id") String associationId,@Field("p")int page,@Field("size")int size);

    /**
     * 获取消息列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.messageList)
    Observable<MessageListData> getMessageList(@Field("p")int page, @Field("size")int size);
    /**
     * 获取消息详情
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.messageDetail)
    Observable<MessageDetailData> getMessageDetail(@Field("id")String id);

    /**
     * 清空消息，传null全清，传id单个清
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.emptyMessage)
    Observable<StringData> emptyMessage(@Field("id")String id);

    /**
     * 获取通讯录成员详情
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.studentDetail)
    Observable<MemberStudentData> getStudentDetail(@Field("id") String memberId);

    /**
     * 发布个人动态
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.postPersonalMoment)
    Observable<StringData> postPersonalMoment(@Field("content") String content, @Field("imgs") String imgs, @Field("range") String range, @Field("usersId") String userIds);

    /**
     * 发布官方动态
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.postOfficialMoment)
    Observable<StringData> postOfficialMoment(@Field("content") String content, @Field("imgs") String imgs, @Field("associationid") String associationId, @Field("activityid") String activityId);

    /**
     * 获取导师列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.teacherList)
    Observable<TutorListData> getTeacherList(@Field("p")int p,@Field("size")int size);

    /**
     * 获取导师详情
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.teacherDetail)
    Observable<MemberTutorData> getTeacherDetail(@Field("id")String id);
    /**
     * 获取动态列表
     * type:1全部 2个人
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.momentList)
    Observable<MomentListData> getMomentList(@Field("p")int p, @Field("size")int size, @Field("type")String type); /**

     /**
     * 操作动态
     * type:1、点赞，2、转发(已去掉)，3、评论，4、取消点赞
     * review:评论
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.giveLike)
    Observable<StringData> operateMoment(@Field("id")String momentId, @Field("review")String comment, @Field("type")String type);
    /**
     * 获取动态详情
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.momentDetail)
    Observable<MomentDetailData> getMomentDetail(@Field("id")String momentId);

    /**
     * 获取评论人列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.reviewerList)
    Observable<ForwardLikeListData> getCommentList(@Field("id")String momentId);

    /**
     * 获取转发人列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.forwardList)
    Observable<ForwardLikeListData> getForwardList(@Field("id")String momentId);

    /**
     * 获取点赞人列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.likeList)
    Observable<ForwardLikeListData> getLikeList(@Field("id")String momentId);
    /**
     * 转发动态
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.forwardMoment)
    Observable<StringData> forwardMoment(@Field("id")String momentId,@Field("forwardContent")String forwardContent);

    /**
     * 场地列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.addressList)
    Observable<ActivityAddressListData> addressList(@Field("p")int page,@Field("size")int size,@Field("region")String regionId,@Field("Id")String schoolId);

    /**
     * 场地详情
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.addressDetail)
    Observable<AddressDetailData> getAddressDetail(@Field("id")String addressId);

    /**
     * 场地详情
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.addressOrderList)
    Observable<AddressOrderListData> getAddressOrderList(@Field("id")String addressId, @Field("p")int page, @Field("size")int size);

    /**
     * 场地详情
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.addressOrderDetail)
    Observable<AddressOrderDetailData> getAddressOrderDetail(@Field("id")String orderId);

    /**
     * 场地预约校验
     */
    @FormUrlEncoded
    @POST(TLSUrl.School.checkAddressOrder)
    Observable<BooleanData> checkAddressOrder(@Field("fieldId")String id,@Field("fieldstarttime")String startTime,@Field("fieldendtime")String endTime);

    /**
     * 是否有未读消息
     */
    @POST(TLSUrl.School.hasUnreadMsg)
    Observable<BooleanData> hasUnreadMsg();

}
