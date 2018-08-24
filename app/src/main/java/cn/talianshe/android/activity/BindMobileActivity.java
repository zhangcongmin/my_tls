package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.db.util.UserEntityUtil;
import cn.talianshe.android.eventbus.AccountActivatedEvent;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.UserApiService;
import cn.talianshe.android.utils.PhoneFormatCheckUtil;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: BindMobileActivity
 * @Description: 绑定手机号
 * @date 2017/11/17 14:19
 */
public class BindMobileActivity extends BaseActivity {
    @BindView(R.id.tv_get_captcha)
    TextView tvGetCaptcha;
    @BindView(R.id.tv_mobile)
    TextView tvMobile;
    @BindView(R.id.btn_commit)
    Button btnCommit;
    @BindView(R.id.ll_change_mobile)
    LinearLayout llChangeMobile;
    @BindView(R.id.tv_first_bind_tip)
    TextView tvFirstBindTip;
    @BindView(R.id.et_mobile)
    EditText etMobile;
    @BindView(R.id.et_verification_code)
    EditText etVerificationCode;

    private int curType;
    private static final int TYPE_CHANGE_MOBILE = 1;
    private static final int TYPE_BIND_MOBILE = 2;
    private static final String BIND_MOBILE_TYPE = "bind_mobile_type";
    private static final String PASSWORD = "password";
    private static final String MOBILE = "mobile";
    private String password;
    private String mobile;

    public static Intent getBindMobileIntent(Context context, String password) {
        Intent intent = new Intent(context, BindMobileActivity.class);
        intent.putExtra(BIND_MOBILE_TYPE, TYPE_BIND_MOBILE);
        intent.putExtra(PASSWORD, password);
        return intent;
    }

    public static Intent getChangeMobileIntent(Context context, String mobile) {
        Intent intent = new Intent(context, BindMobileActivity.class);
        intent.putExtra(BIND_MOBILE_TYPE, TYPE_CHANGE_MOBILE);
        intent.putExtra(MOBILE, mobile);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_mobile);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        curType = getIntent().getIntExtra(BIND_MOBILE_TYPE, TYPE_CHANGE_MOBILE);
        if (curType == TYPE_BIND_MOBILE) {
            setTitle(R.string.bind_mobile);
            password = getIntent().getStringExtra(PASSWORD);
            tvFirstBindTip.setVisibility(View.VISIBLE);
            llChangeMobile.setVisibility(View.GONE);
        } else {
            setTitle(R.string.change_bind_mobile);
            mobile = getIntent().getStringExtra(MOBILE);
            tvMobile.setText(mobile);
            tvFirstBindTip.setVisibility(View.GONE);
            llChangeMobile.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.tv_get_captcha, R.id.btn_commit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_get_captcha:
                if (!PhoneFormatCheckUtil.isChinaPhoneLegal(etMobile.getText().toString())) {
                    MyToast.show(getString(R.string.mobile_illegal), this);
                    return;
                }
                swipeLayout.setRefreshing(true);
                sendVerificationCode(etMobile.getText().toString());
                break;
            case R.id.btn_commit:
                if (!PhoneFormatCheckUtil.isChinaPhoneLegal(etMobile.getText().toString())) {
                    MyToast.show(getString(R.string.mobile_illegal), this);
                    return;
                }
                if (TextUtils.isEmpty(etVerificationCode.getText().toString())) {
                    MyToast.show(getString(R.string.verification_code_null_tip), this);
                    return;
                }
                if (curType == TYPE_BIND_MOBILE) {
                    //绑定手机
                    activicationAccount(etMobile.getText().toString(), etVerificationCode.getText().toString(), password);
                } else {
                    //更换绑定手机
                    changeMobile(etMobile.getText().toString(), etVerificationCode.getText().toString(), password);
                }
                break;
        }
    }

    private void changeMobile(final String mobile, String verificationCode, String password) {
        swipeLayout.setRefreshing(true);
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                swipeLayout.setRefreshing(false);
                MyToast.show(R.string.change_mobile_success, BindMobileActivity.this);
                if (GlobalParams.USER_INFO != null) {
                    GlobalParams.USER_INFO.mobile = mobile;
                    UserEntityUtil.saveOrUpdateUserInfo(BindMobileActivity.this, GlobalParams.USER_INFO);
                }
                finish();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
            }
        };
        userApiService.changeMobile(mobile, verificationCode, password).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void activicationAccount(final String mobile, String verificationCode, String password) {
        swipeLayout.setRefreshing(true);
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                swipeLayout.setRefreshing(false);
                MyToast.show(R.string.activate_account_success, BindMobileActivity.this);
//                UserEntityUtil.saveOrUpdateUserInfo(BindMobileActivity.this,GlobalParams.USER_INFO);
//                startActivity(new Intent(BindMobileActivity.this,MainActivity.class));
                EventBus.getDefault().post(new AccountActivatedEvent(mobile, "1"));
                finish();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
            }
        };
        userApiService.activationAccount(mobile, verificationCode, password).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void sendVerificationCode(String mobile) {
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                swipeLayout.setRefreshing(false);
                myCountDownTime.start();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
            }
        };
        userApiService.sendVerificationCode(mobile).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private CountDownTimer myCountDownTime = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            tvGetCaptcha.setClickable(false);
            tvGetCaptcha.setFocusable(false);
            tvGetCaptcha.setTextColor(ContextCompat.getColor(BindMobileActivity.this, R.color.gray));
            tvGetCaptcha.setText(getString(R.string.captcha_count_down, millisUntilFinished / 1000));  //设置倒计时时间
            SpannableString spannableString = new SpannableString(tvGetCaptcha.getText().toString());  //获取按钮上的文字
            ForegroundColorSpan span = new ForegroundColorSpan(Color.RED);
            /**
             * public void setSpan(Object what, int start, int end, int flags) {
             * 主要是start跟end，start是起始位置,无论中英文，都算一个。
             * 从0开始计算起。end是结束位置，所以处理的文字，包含开始位置，但不包含结束位置。
             */
            spannableString.setSpan(span, 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);//将倒计时的时间设置为红色
            tvGetCaptcha.setText(spannableString);
        }

        @Override
        public void onFinish() {
            tvGetCaptcha.setText(R.string.reget_captcha);
            tvGetCaptcha.setClickable(true);//重新获得点击
        }
    };
}
