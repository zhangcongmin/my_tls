package cn.talianshe.android.net.service;

import cn.talianshe.android.bean.ActivityChatListData;
import cn.talianshe.android.bean.ActivityDetailData;
import cn.talianshe.android.bean.ActivityInteractionData;
import cn.talianshe.android.bean.ActivityMemberListData;
import cn.talianshe.android.bean.ActivityPhotoListData;
import cn.talianshe.android.bean.ActivityScoreData;
import cn.talianshe.android.bean.ActivityTypeListData;
import cn.talianshe.android.bean.AssociationActivityListData;
import cn.talianshe.android.bean.AssociationAlbumListData;
import cn.talianshe.android.bean.PhotoDetailData;
import cn.talianshe.android.bean.RegionListData;
import cn.talianshe.android.bean.SignTypeData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.VoteResultData;
import cn.talianshe.android.net.TLSUrl;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface ActivityApiService {
    /**
     * 获取精彩活动列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.onGoingActivityList)
    Observable<AssociationActivityListData> getOnGoingActivityList(@Field("p") int page, @Field("size") int size, @Field("schoolId") String schoolId);

    /**
     * 获取精彩活动列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.recommendActivityList)
    Observable<AssociationActivityListData> getRecommendActivityList(@Field("p") int page, @Field("size") int size, @Field("schoolId") String schoolId);

    /**
     * 获取精彩活动列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.wonderfulActivity)
    Observable<AssociationActivityListData> getWonderfulActivityList(@Field("id") String associationId, @Field("p") int page, @Field("size") int size);

    /**
     * 获取社团相册列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.activityAlbum)
    Observable<AssociationAlbumListData> getAssociationAlbumList(@Field("id") String associationId, @Field("p") int page, @Field("size") int size);

    /**
     * 获取活动列表 type：1是关注 2是参加
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.activityList)
    Observable<AssociationActivityListData> getActivityList(@Field("p") int page, @Field("size") int size, @Field("type") String type, @Field("schoolId") String schoolId, @Field("activityName") String searchKey);

    /**
     * 获取活动发布权限
     */
    @POST(TLSUrl.Activitys.getActivityAuthorization)
    Observable<StringData> getActivityAuthorization();

    /**
     * 获取活动发布类型
     */
    @POST(TLSUrl.Activitys.getActivityType)
    Observable<ActivityTypeListData> getActivityTypeList();

    /**
     * 获取活动区域
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.regionList)
    Observable<RegionListData> getRegionList(@Field("schoolid") String schoolId);

    /**
     * 获取活动详情
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.activityDetail)
    Observable<ActivityDetailData> getActivityDetail(@Field("id") String activityId);

    /**
     * 获取活动互动配置
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.activityInteraction)
    Observable<ActivityInteractionData> getActivityInteraction(@Field("id") String activityId);

    /**
     * 获取活动聊天列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.activityChatList)
    Observable<ActivityChatListData> getActivityChatList(@Field("id") String activityId, @Field("p") int page, @Field("size") int size);

    /**
     * 获取活动成员列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.activityMemberList)
    Observable<ActivityMemberListData> getActivityMemberList(@Field("id") String activityId, @Field("p") int page, @Field("size") int size);

    /**
     * 获取活动相片列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.activityPhotoList)
    Observable<ActivityPhotoListData> getActivityPhotoList(@Field("id") String activityId, @Field("p") int page, @Field("size") int size, @Field("onlyId") String onlyId);

    /**
     * 签到配置增加或修改 修改时必传signid ,signType:1、签到码，2、扫码签到，3、手势签到
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.addSignType)
    Observable<StringData> addSignType(@Field("id") String activityId, @Field("signId") String signId, @Field("remark") String remark, @Field("signType") String signType);

    /**
     * 获取签到配置
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.getSignType)
    Observable<SignTypeData> getSignType(@Field("id") String activityId);

    /**
     * 活动照片详情
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.getPhotoDetail)
    Observable<PhotoDetailData> getPhotoDetail(@Field("id") String photoId, @Field("onlyId") String onlyId);

    /**
     * 活动照片点赞
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.photoLike)
    Observable<StringData> photoLike(@Field("id") String photoId, @Field("type") int type, @Field("onlyId") String onlyId);

    /**
     * 活动投票
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.voteActivity)
    Observable<StringData> voteActivity(@Field("activityId") String activityId, @Field("voteId") String voteId, @Field("selectId") String selectIds);

    /**
     * 活动投票
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.getVoteResult)
    Observable<VoteResultData> getVoteResult(@Field("id") String voteId);

    /**
     * type	登录人对活动状态	0、取消报名(即未报名)1、报名，2、签到，3、签退，4,、评价，5、关注，6、取消关注
     * evaluate	评价说明
     * score	评分	Integer
     * userdId	用户id	Long
     * 获取活动相片列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.modifyActivity)
    Observable<StringData> modifyActivity(@Field("id") String activityId, @Field("type") String type, @Field("evaluate") String evaluate, @Field("score") Integer score, @Field("userdId") String userdId);

    /**
     * type	登录人对活动状态	0、取消报名(即未报名)1、报名，2、签到，3、签退，4,、评价，5、关注，6、取消关注
     * evaluate	评价说明
     * score	评分	Integer
     * userdId	用户id	Long
     * 获取活动相片列表
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.getActivityScore)
    Observable<ActivityScoreData> getActivityScore(@Field("id") String activityId);

    /**
     * type	操作类型	string	Y	1、点赞，2、取消点赞
     * 点赞活动
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.doLikeActivity)
    Observable<StringData> doLikeActivity(@Field("id") String activityId, @Field("type") String type);

    /**
     * type	操作类型	string	Y	1、点赞，2、取消点赞
     * 点赞活动
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.doChatActivity)
    Observable<StringData> doChatActivity(@Field("id") String activityId, @Field("content") String content, @Field("imgsId") String imgsId);


    /**
     * 发布活动
     */
    @FormUrlEncoded
    @POST(TLSUrl.Activitys.publishActivity)
    Observable<StringData> publishActivity(@Field("activityName") String activityName, @Field("coverImg") String coverImgId,
                                           @Field("bannerImg") String bannerImgIds, @Field("associationId") String associationId,
                                           @Field("activityType") String activityType, @Field("estimatedNumber") String estimatedNumber,
                                           @Field("costSources") String costSources, @Field("estimatedCost") String estimatedCost,
                                           @Field("schoolCost") String schoolCost, @Field("shopCost") String businessCost,
                                           @Field("introduction") String desc, @Field("starttimes") String startTime,
                                           @Field("endtimes") String endTime, @Field("signStarttimes") String signStartTime,
                                           @Field("signEndtimes") String signEndTime, @Field("activityPlace") String activityAddress,
                                           @Field("signtime") String signTime, @Field("remark") String remark,
                                           @Field("fieldId") String addressId, @Field("fieldstarttime") String addressStartTime,
                                           @Field("fieldendtime") String addressEndTime
    );
}
