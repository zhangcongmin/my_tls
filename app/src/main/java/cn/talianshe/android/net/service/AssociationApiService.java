package cn.talianshe.android.net.service;

import cn.talianshe.android.bean.AssociationDetailData;
import cn.talianshe.android.bean.AssociationLabelListData;
import cn.talianshe.android.bean.AssociationListData;
import cn.talianshe.android.bean.HotAssociationListData;
import cn.talianshe.android.bean.ManageAssociationListData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.UploadData;
import cn.talianshe.android.net.TLSUrl;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * @author zcm
 * @ClassName: AssociationApiService
 * @Description: 社团相关api
 * @date 2017/12/13 9:16
 */
public interface AssociationApiService {
    /**
     * 获取我管理的社团
     */
    @POST(TLSUrl.Association.manageAssociationList)
    Observable<AssociationListData> getManageAssociationList();

    /**
     * 获取热门的社团
     */
    @FormUrlEncoded
    @POST(TLSUrl.Association.hotAssociationList)
    Observable<HotAssociationListData> getHotAssociationList(@Field("schoolId")String schoolId, @Field("p")int page, @Field("size")int size);

    /**
     * 获取社团列表，也可以根据标签搜索列表,type为1代表加入2关注
     */
    @FormUrlEncoded
    @POST(TLSUrl.Association.associationList)
    Observable<AssociationListData> getAssociationList(@Field("p")int page, @Field("size")int size,@Field("schoolId")String schoolId,@Field("type")String type,@Field("infoName")String tag,@Field("associationName")String searchKey);

    /**
     * 获取社团列表，也可以根据标签搜索列表,type为1代表加入2关注
     */
    @FormUrlEncoded
    @POST(TLSUrl.Association.createAssociation)
    Observable<StringData> createAssociation(@Field("associationLogo")String logo, @Field("level")String level,
                                             @Field("associationName")String name, @Field("associationSlogan")String slogan,
                                             @Field("infoName")String labels,@Field("associationIntro")String desc,
                                             @Field("associationFunction")String function,@Field("activityIdea")String vision,
                                             @Field("developPlan")String plan,@Field("numLimit")String num,
                                             @Field("leaderTeacherId")int leaderId,@Field("leaderTeacherName")String leaderName,
                                             @Field("teacherId")String tutorIds
                                             );

    /**
     * 获取社团标签列表
     */
    @POST(TLSUrl.Association.associationLabel)
    Observable<AssociationLabelListData> getAssociationLabelList();


    /**
     * 获取社团详情
     */
    @FormUrlEncoded
    @POST(TLSUrl.Association.associationDetail)
    Observable<AssociationDetailData> getAssociationDetail(@Field("id") String associationId);

    /**
     * 申请加入社团
     */
    @FormUrlEncoded
    @POST(TLSUrl.Association.applyJionAssociation)
    Observable<StringData> applyJionAssociation(@Field("id") String associationId,@Field("applyReason")String applyReason);

    /**
     * 申请加入社团
     */
    @FormUrlEncoded
    @POST(TLSUrl.Association.leaveAssociation)
    Observable<StringData> leaveAssociation(@Field("id") String associationId);

    /**
     * 关注或取消关注社团 type 1:关注 0:取消关注
     */
    @FormUrlEncoded
    @POST(TLSUrl.Association.followAssociation)
    Observable<StringData> followAssociation(@Field("id") String associationId,@Field("type")String type);


}
