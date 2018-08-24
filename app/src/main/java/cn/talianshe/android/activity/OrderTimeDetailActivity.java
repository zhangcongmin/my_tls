package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.AddressOrderDetailData;
import cn.talianshe.android.bean.QRCodeType;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.widget.CustomStarBar;
import cn.talianshe.android.widget.MyProgressDialog;

/**
 * @author zcm
 * @ClassName: OrderTimeDetailActivity
 * @Description: 已经预约时间的场地时间详情
 * @date 2017/11/29 19:10
 */
public class OrderTimeDetailActivity extends BaseActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";
    @BindView(R.id.iv_association_logo)
    ImageView ivAssociationLogo;
    @BindView(R.id.tv_association_name)
    TextView tvAssociationName;
    @BindView(R.id.star_bar)
    CustomStarBar starBar;
    @BindView(R.id.tv_association_college)
    TextView tvAssociationCollege;
    @BindView(R.id.iv_qrcode)
    ImageView ivQrcode;
    @BindView(R.id.tv_leader)
    TextView tvLeader;
    @BindView(R.id.tv_vice_leader)
    TextView tvViceLeader;
    @BindView(R.id.tv_tutor)
    TextView tvTutor;
    @BindView(R.id.tv_order_time)
    TextView tvOrderTime;
    @BindView(R.id.tv_activity_theme)
    TextView tvActivityName;

    private String orderId;

    public static Intent getOrderDetailIntent(Context context, String orderId) {
        Intent intent = new Intent(context, OrderTimeDetailActivity.class);
        intent.putExtra(EXTRA_ORDER_ID, orderId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_time_detail);
        ButterKnife.bind(this);
        ivQrcode.setOnClickListener(this);
        initData();
    }

    private void initData() {
        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        requestData();
    }

    private AddressOrderDetailData.AddressOrderDetailInfo detailInfo;

    private void requestData() {
        MyProgressDialog.show(this);
        //获取活动类型
        HttpSubscriber httpSubscriber = new HttpSubscriber<AddressOrderDetailData>(this) {

            @Override
            public void onSuccess(AddressOrderDetailData detailData) {
                MyProgressDialog.dismiss();
                swipeLayout.setRefreshing(false);
                detailInfo = detailData.result;
                setTitle(detailInfo.addressName);
                tvAssociationName.setText(detailInfo.associationName);
                tvAssociationCollege.setText(detailInfo.schoolName + detailInfo.departmentName);
                starBar.setStarMark(detailInfo.score);
                tvActivityName.setText(detailInfo.activityName);
                tvAssociationName.setCompoundDrawablesWithIntrinsicBounds(1 == detailInfo.level ? R.mipmap.school_level : R.mipmap.college_level, 0, 0, 0);
                tvLeader.setText(detailInfo.leaderName);
                if (detailInfo.viceLeaderList != null && detailInfo.viceLeaderList.size() > 0) {
                    String viceLeader = "";
                    for (AddressOrderDetailData.ViceLeaderBean viceLeaderBean : detailInfo.viceLeaderList) {
                        viceLeader += viceLeaderBean.studentName + "、";
                    }
                    viceLeader = viceLeader.substring(0, viceLeader.length() - 1);
                    tvViceLeader.setText(viceLeader);
                }
                String tutorLeader = detailInfo.leaderTutorName;
                if (detailInfo.viceTutorList != null && detailInfo.viceTutorList.size() > 0) {
                    for (AddressOrderDetailData.ViceTutorBean tutorBean : detailInfo.viceTutorList) {
                        tutorLeader += "、" + tutorBean.name;
                    }
                }
                tvTutor.setText(tutorLeader);
                tvOrderTime.setText(TimeUtil.getActivityTime(detailInfo.starttime, detailInfo.endtime));
                if (!TextUtils.isEmpty(detailInfo.associationLogo)) {
                    RequestOptions options = new RequestOptions();
                    options.override(DensityUtils.dipTopx(OrderTimeDetailActivity.this, 52));
                    options.placeholder(R.mipmap.ic_img_thumbnail);
                    options.error(R.mipmap.ic_img_thumbnail);
                    Glide.with(OrderTimeDetailActivity.this).load(TLSUrl.BASE_URL + detailInfo.associationLogo).apply(options).into(ivAssociationLogo);
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).getAddressOrderDetail(orderId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }


    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.iv_qrcode:
                QRGenerateActivity.GenerateEntity entity = new QRGenerateActivity.GenerateEntity();
                entity.generateId = orderId;
                entity.generateAvatar = TLSUrl.BASE_URL + detailInfo.associationLogo;
                entity.name = detailInfo.associationName;
                startActivity(QRGenerateActivity.getQRGenerateIntent(this, QRCodeType.ASSOCIATION, entity));
                break;
        }

    }
}
