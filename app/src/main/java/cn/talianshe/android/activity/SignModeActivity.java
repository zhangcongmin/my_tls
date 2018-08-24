package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.SignTypeData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.eventbus.SignManageSettingFinishEvent;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.widget.FixedEditText;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: SignModeActivity
 * @Description: 签到方式
 * @date 2017/12/6 13:39
 */
public class SignModeActivity extends BaseActivity {

    private static final String EXTRA_IS_CONFIGURE = "extra_is_configure";
    private static final String EXTRA_ACTIVITY_ID = "extra_activity_id";
    @BindView(R.id.tv_code)
    TextView tvCode;
    @BindView(R.id.tv_scan)
    TextView tvScan;
    @BindView(R.id.tv_gesture)
    TextView tvGesture;
    @BindView(R.id.et_desc)
    FixedEditText etDesc;
    private boolean hasConfigure;
    private String activityId;

    public static Intent getSignModeIntent(Context context, boolean hasConfigure, String activityId) {
        Intent intent = new Intent(context, SignModeActivity.class);
        intent.putExtra(EXTRA_IS_CONFIGURE, hasConfigure);
        intent.putExtra(EXTRA_ACTIVITY_ID, activityId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_mode);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.sign_mode);
        tvCode.setVisibility(View.INVISIBLE);
        tvGesture.setVisibility(View.INVISIBLE);
        tvScan.setVisibility(View.VISIBLE);
        tvScan.setSelected(true);
        etDesc.setFixedText(getString(R.string.sign_desc_tip));
        activityId = getIntent().getStringExtra(EXTRA_ACTIVITY_ID);
        hasConfigure = getIntent().getBooleanExtra(EXTRA_IS_CONFIGURE, false);
        if (hasConfigure) {
            getSignType();
        }
    }

    @OnClick({R.id.tv_code, R.id.tv_scan, R.id.tv_gesture, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_code:
                System.out.println("tv_code");
                break;
            case R.id.tv_scan:
                break;
            case R.id.tv_gesture:
                System.out.println("tv_gesture");
                break;
            case R.id.btn_save:
                if (TextUtils.isEmpty(etDesc.getText().toString())) {
                    MyToast.show(R.string.sign_remarks_null_tip, this);
                    return;
                }
                if (hasConfigure && signTypeInfo.remark.equals(etDesc.getText().toString())) {
                    MyToast.show(R.string.sign_remarks_same_tip, this);
                    return;
                }
                setSignType();
                break;
        }
        System.out.println(etDesc.getText().toString());
    }

    private SignTypeData.SignTypeInfo signTypeInfo;

    //获取签到配置详情
    private void getSignType() {
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<SignTypeData>(this) {

            @Override
            public void onSuccess(SignTypeData data) {
                MyProgressDialog.dismiss();
                signTypeInfo = data.result;
                etDesc.setText(signTypeInfo.remark);
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getSignType(activityId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void setSignType() {
        MyProgressDialog.show(this, false);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {

            @Override
            public void onSuccess(StringData data) {
                MyProgressDialog.dismiss();
                MyToast.show(R.string.setting_sign_type_success, SignModeActivity.this);
                EventBus.getDefault().post(new SignManageSettingFinishEvent());
                finish();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).addSignType(activityId, signTypeInfo == null ? null : signTypeInfo.id, etDesc.getText().toString(), "2").compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }
}
