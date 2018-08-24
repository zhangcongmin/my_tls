package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.CreateActivityData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.UploadData;
import cn.talianshe.android.eventbus.AddressOrderTimeEvent;
import cn.talianshe.android.eventbus.PublishActivitySuccessEvent;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.MultipartUtil;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.net.service.UploadLoadApiService;
import cn.talianshe.android.utils.DeleteFileUtil;
import cn.talianshe.android.utils.StringUtils;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.widget.DateChooseWheelViewDialog;
import cn.talianshe.android.widget.MyEditTextDialog;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import okhttp3.MediaType;
import okhttp3.MultipartBody;

/**
 * @author zcm
 * @ClassName: PublishActivityTimeActivity
 * @Description: 活动发布时间
 * @date 2017/11/24 21:17
 */
public class PublishActivityTimeActivity extends BaseActivity {


    private static final String EXTRA_PARCEL = "extra_parcel";
    @BindView(R.id.tv_activity_start_time)
    TextView tvActivityStartTime;
    @BindView(R.id.tv_activity_end_time)
    TextView tvActivityEndTime;
    @BindView(R.id.tv_activity_registration_start_time)
    TextView tvActivityRegistrationStartTime;
    @BindView(R.id.tv_activity_registration_end_time)
    TextView tvActivityRegistrationEndTime;
    @BindView(R.id.tv_activity_address)
    TextView tvActivityAddress;
    @BindView(R.id.et_minutes)
    EditText etMinutes;
    @BindView(R.id.tv_remarks)
    TextView tvRemarks;

    private CreateActivityData activityData;
    private long startTime;
    private long endTime;
    private long registStartTime;
    private long registEndTime;

    public static Intent getTimeIntent(Context context, CreateActivityData data) {
        Intent intent = new Intent(context, PublishActivityTimeActivity.class);
        intent.putExtra(EXTRA_PARCEL, Parcels.wrap(data));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_activity_time);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.publish_activity);
        activityData = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PARCEL));
    }


    private DateChooseWheelViewDialog datePicker;

    private int curId;

    @OnClick({R.id.ll_activity_start_time, R.id.ll_activity_end_time, R.id.ll_activity_registration_start_time, R.id.ll_activity_registration_end_time, R.id.ll_activity_address, R.id.ll_remarks})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_activity_start_time:
            case R.id.ll_activity_end_time:
            case R.id.ll_activity_registration_start_time:
            case R.id.ll_activity_registration_end_time:
                curId = view.getId();
                if (datePicker == null) {
                    datePicker = new DateChooseWheelViewDialog(this, new DateChooseWheelViewDialog.DateChooseInterface() {
                        @Override
                        public void getDateTime(String time, boolean longTimeChecked, long longTime) {
                            System.out.println(time);
                            switch (curId) {
                                case R.id.ll_activity_start_time:
                                    if (endTime != 0 && endTime < longTime) {
                                        MyToast.show(R.string.activity_starttime_less_endtime, PublishActivityTimeActivity.this);
                                        return;
                                    }
                                    startTime = longTime;
                                    tvActivityStartTime.setText(time);
                                    break;
                                case R.id.ll_activity_end_time:
                                    if (startTime != 0 && startTime > longTime) {
                                        MyToast.show(R.string.activity_starttime_more_endtime, PublishActivityTimeActivity.this);
                                        return;
                                    }
                                    endTime = longTime;
                                    tvActivityEndTime.setText(time);
                                    break;
                                case R.id.ll_activity_registration_start_time:
                                    if (registEndTime != 0 && registEndTime < longTime) {
                                        MyToast.show(R.string.activity_regist_starttime_less_endtime, PublishActivityTimeActivity.this);
                                        return;
                                    }
                                    registStartTime = longTime;
                                    tvActivityRegistrationStartTime.setText(time);
                                    break;
                                case R.id.ll_activity_registration_end_time:
                                    if (registStartTime != 0 && registStartTime > longTime) {
                                        MyToast.show(R.string.activity_regist_starttime_more_endtime, PublishActivityTimeActivity.this);
                                        return;
                                    }
                                    registEndTime = longTime;
                                    tvActivityRegistrationEndTime.setText(time);
                                    break;
                            }
                        }
                    });
                }
                datePicker.showDateChooseDialog();
                break;
            case R.id.ll_activity_address:
                startActivity(new Intent(this, ChooseActivityAddressActivity.class));
                break;
            case R.id.ll_remarks:
                showEditDialog("", getString(R.string.remarks), "最多可输入200个字符", 4, 200);
                break;
        }
    }


    private void showEditDialog(String defaultText, String info, String hint, int lines, int maxLength) {
        MyEditTextDialog dialog = new MyEditTextDialog(this, info, defaultText, hint, lines, maxLength).builder();
        dialog.setResultListener(new MyEditTextDialog.EditTextResultListener() {
            @Override
            public void onResult(String result) {
                if (!TextUtils.isEmpty(result)) {
                    tvRemarks.setText(result);
                }
            }
        });
        dialog.show();
    }

    private AddressOrderTimeEvent addressOrderTimeEvent;

    @Subscribe
    public void onReceiveAddressOrderTimeEvent(AddressOrderTimeEvent event) {
        this.addressOrderTimeEvent = event;
        tvActivityAddress.setText(event.addressName);
    }

    @OnClick(R.id.btn_next_step)
    public void onViewClicked() {
        if (startTime == 0 || endTime == 0) {
            MyToast.show(R.string.pls_setting_activity_time, this);
            return;
        }
        if (registStartTime == 0 || registEndTime == 0) {
            MyToast.show(R.string.pls_setting_activity_regist_time, this);
            return;
        }
        if (addressOrderTimeEvent == null) {
            MyToast.show(R.string.input_activity_address_tip, this);
            return;
        }
        if (TextUtils.isEmpty(etMinutes.getText().toString())) {
            MyToast.show(R.string.pls_finish_sign_time, this);
            return;
        }
        uploadActivityLogo();
    }

    private String logoImgId;

    /**
     * 上传logo
     */
    private void uploadActivityLogo() {
        logoImgId = null;
        MyProgressDialog.show(this, false);
        HttpSubscriber uploadLogoSubscriber = new HttpSubscriber<UploadData>(this) {
            @Override
            public void onSuccess(UploadData uploadData) {
                System.out.println("path:" + uploadData.result.path);
                logoImgId = uploadData.result.id;
                uploadActivityBanner();
            }

            @Override
            public void onError(Throwable e) {
                MyProgressDialog.dismiss();
                MyToast.show(R.string.publish_activity_failed_cause_upload_img, PublishActivityTimeActivity.this);
                super.onError(e);
            }
        };
        MultipartBody.Builder builder = MultipartUtil.filesToMultipartBodyBuilder("file", new String[]{activityData.mSelectedLogoPhoto}, MediaType.parse("multipart/form-activityData"));
        RequestEngine.getInstance().getServer(UploadLoadApiService.class).uploadImage(builder.build()).compose(RxSchedulersHelper.io_main()).subscribe(uploadLogoSubscriber);
    }

    /**
     * 上传banner图片
     */
    private void uploadActivityBanner() {
        final UploadLoadApiService uploadLoadApiService = RequestEngine.getInstance().getServer(UploadLoadApiService.class);
        mUploadBannerImgs.clear();
        bannerImgIds = "";
        MultipartBody.Builder builder = MultipartUtil.filesToMultipartBodyBuilder("file", new String[]{activityData.mSelectedBannerPhotos.get(0)}, MediaType.parse("multipart/form-activityData"));
        HttpSubscriber uploadSubscriber = new RepeatSubscribe(this, uploadLoadApiService);
        uploadLoadApiService.uploadImage(builder.build()).compose(RxSchedulersHelper.io_main()).subscribe(uploadSubscriber);

    }

    /**
     * 上传的轮播图片id集合
     */
    private ArrayList<String> mUploadBannerImgs = new ArrayList<>();
    /**
     * 上传的图片id整合字符串
     */
    private String bannerImgIds = "";

    public class RepeatSubscribe extends HttpSubscriber<UploadData> {
        UploadLoadApiService uploadLoadApiService;

        public RepeatSubscribe(Context context, UploadLoadApiService uploadLoadApiService) {
            super(context);
            this.uploadLoadApiService = uploadLoadApiService;
        }

        @Override
        public void onSuccess(UploadData uploadData) {
            System.out.println("path:" + uploadData.result.path);
            mUploadBannerImgs.add(uploadData.result.id);
            int totalImgCount = activityData.mSelectedBannerPhotos.size();
            if (mUploadBannerImgs.size() == totalImgCount) {
                //说明全部上传成功
                for (String id : mUploadBannerImgs) {
                    bannerImgIds += id + ",";
                }
                bannerImgIds = bannerImgIds.substring(0, bannerImgIds.length() - 1);
                publishActivity();
            } else {
                MultipartBody.Builder builder = MultipartUtil.filesToMultipartBodyBuilder("file", new String[]{activityData.mSelectedBannerPhotos.get(mUploadBannerImgs.size())}, MediaType.parse("multipart/form-activityData"));
                HttpSubscriber mSubscriber = new RepeatSubscribe(context, uploadLoadApiService);
                uploadLoadApiService.uploadImage(builder.build()).compose(RxSchedulersHelper.io_main()).subscribe(mSubscriber);
            }
        }

        @Override
        public void onError(Throwable e) {
            MyProgressDialog.dismiss();
            MyToast.show(R.string.publish_activity_failed_cause_upload_img, PublishActivityTimeActivity.this);
            super.onError(e);
        }

    }

    private void publishActivity() {
        //先上传logo和宣传图再发布

        //获取活动类型
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {

            @Override
            public void onSuccess(StringData data) {
                MyProgressDialog.dismiss();
                MyToast.show(R.string.publish_activity_success, PublishActivityTimeActivity.this);
                EventBus.getDefault().post(new PublishActivitySuccessEvent());
                finish();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        String remarks = getString(R.string.input_remarks_tip).equals(tvRemarks.getText().toString()) ? null : tvRemarks.getText().toString();
        RequestEngine.getInstance().
                getServer(ActivityApiService.class).
                publishActivity(activityData.activityName, logoImgId,
                        bannerImgIds, activityData.associationInfo.id,
                        activityData.activityTypeInfo.id,
                        activityData.num, activityData.costSouceEvent.getCostSourceName(),
                        StringUtils.moneyFormat(activityData.costSouceEvent.costSelf), StringUtils.moneyFormat(activityData.costSouceEvent.costSchool),
                        StringUtils.moneyFormat(activityData.costSouceEvent.costBusiness), activityData.activityDesc,
                        TimeUtil.getStringDate(startTime), TimeUtil.getStringDate(endTime),
                        TimeUtil.getStringDate(registStartTime), TimeUtil.getStringDate(registEndTime),
                        addressOrderTimeEvent.addressName, etMinutes.getText().toString(),
                        remarks, addressOrderTimeEvent.addressId,
                        TimeUtil.getStringDate(addressOrderTimeEvent.startTime),
                        TimeUtil.getStringDate(addressOrderTimeEvent.endTime)).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

    }
}
