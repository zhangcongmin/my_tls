package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.Result;
import com.google.zxing.client.android.AutoScannerView;
import com.google.zxing.client.android.BaseCaptureActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.QRCodeType;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.net.service.UserApiService;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: CostSourceActivity
 * @Description: 费用来源
 * @date 2017/11/24 15:52
 */
public class QRScanActivity extends BaseCaptureActivity {

    private static final String TAG = "QRScanActivity";
    private static final String EXTRA_SCAN_TYPE = "extra_scan_type";
    @BindView(R.id.iv_flash_light)
    ImageView ivFlashLight;
    @BindView(R.id.preview_view)
    SurfaceView previewView;
    @BindView(R.id.btn_left)
    ImageButton btnLeft;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.ll_center)
    LinearLayout llCenter;
    @BindView(R.id.btn_right)
    TextView btnRight;
    @BindView(R.id.ll_title_bar)
    LinearLayout llTitleBar;
    @BindView(R.id.autoscanner_view)
    AutoScannerView autoscannerView;

    /**
     * 扫码方式
     */
    public enum ScanType {
        ACTIVITY_SIGN, NORMAL;
        public String scanId;
    }

    private ScanType scanType;

    public static Intent getQRScanIntent(Context context, ScanType scanType) {
        Intent intent = new Intent(context, QRScanActivity.class);
        intent.putExtra(EXTRA_SCAN_TYPE, scanType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.qr_code_scan);
        scanType = (ScanType) getIntent().getSerializableExtra(EXTRA_SCAN_TYPE);
        switch (scanType) {
            case ACTIVITY_SIGN:
                //活动签到扫描
                break;
            case NORMAL:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoscannerView.setCameraManager(getCameraManager());
    }

    @Override
    public SurfaceView getSurfaceView() {
        return previewView == null ? ((SurfaceView) findViewById(R.id.preview_view)) : previewView;
    }

    @Override
    public void dealDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        String result = rawResult.getText();
        Log.i(TAG, "result:" + result);
        final String[] strings = result.split(",");
        if (strings != null && strings.length >= 3) {

            MyProgressDialog.show(QRScanActivity.this, false);
            final HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(QRScanActivity.this) {
                @Override
                public void onSuccess(StringData data) {
                    MyProgressDialog.dismiss();
                    if ("success".equals(data.msg)) {
                        //二维码有效
                        switch (scanType) {
                            case ACTIVITY_SIGN:
                                //活动签到扫描
                                QRCodeType codeType = QRCodeType.ACTIVITY_SIGN;
                                String activityId = strings[3].replace(codeType.getSecondQrCodePrefix(), "");
                                if (!scanType.scanId.equals(activityId)) {
                                    MyToast.show(R.string.activity_unsame, QRScanActivity.this);
                                    break;
                                }
                                String userId = strings[2].replace(codeType.getQrCodePrefix(), "");
                                System.out.println("活动id：" + scanType.scanId + ",用户id：" + userId);
                                if (!userId.equals(signSuccessUserId)) {
                                    startSign(scanType.scanId, userId);
                                }
                                break;
                            case NORMAL:
                                QRCodeType qrCodeType = QRCodeType.getEnumFromString(strings[1]);
                                if (qrCodeType != null) {
                                    vibrate();
                                    switch (qrCodeType) {
                                        case ACTIVITY:
                                            String mActivityId = strings[2].replace(qrCodeType.getQrCodePrefix(), "");
                                            startActivity(ActivityDetailActivity.getActivityDetailIntent(QRScanActivity.this, mActivityId));
                                            break;
                                        case ASSOCIATION:
                                            String mAssociationId = strings[2].replace(qrCodeType.getQrCodePrefix(), "");
                                            startActivity(AssociationDetailActivity.getAssociationDetailIntent(QRScanActivity.this, mAssociationId));
                                            break;
                                        case PERSONAL:
                                            String mUserId = strings[2].replace(qrCodeType.getQrCodePrefix(), "");
                                            boolean isTeacher = strings[3].replace(qrCodeType.getSecondQrCodePrefix(), "").equals("teacher");

                                            startActivity(MemberInfoActivity.getMemberInfoIntent(QRScanActivity.this, isTeacher ? MemberInfoActivity.TYPE_TEACHER : MemberInfoActivity.TYPE_STUDENT, mUserId));
                                            break;
                                    }
                                }
                                break;
                        }
                    } else {
                        MyToast.show(data.result, QRScanActivity.this);
                    }
//                    mQRCodeView.startSpot();
                    reScan();

                }

                @Override
                public void onError(String msg) {
                    MyProgressDialog.dismiss();
                    reScan();
                    super.onError(msg);
                }
            };
            RequestEngine.getInstance().getServer(UserApiService.class).checkTimeOut(strings[0]).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else {
            MyToast.show(getString(R.string.unknow_qr_code, result), this);
            reScan();
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private String signSuccessUserId;

    private void startSign(String activityId, final String userId) {
        MyProgressDialog.show(this, false);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData baseBean) {
                MyProgressDialog.dismiss();
                signSuccessUserId = userId;
                MyToast.show(getString(R.string.sign_success_holder, userId), QRScanActivity.this);
            }

            @Override
            public void onError(String msg) {
                MyProgressDialog.dismiss();
                super.onError(msg);
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).modifyActivity(activityId, "2", null, null, userId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

    }

    @OnClick({R.id.iv_flash_light, R.id.btn_left})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.iv_flash_light) {
            if (ivFlashLight.isSelected()) {
                ivFlashLight.setSelected(false);
                closeFlashLight();
            } else {
                ivFlashLight.setSelected(true);
                openFlashLight();
            }
        } else if (view.getId() == R.id.btn_left) {
            finish();
        }
    }
}
