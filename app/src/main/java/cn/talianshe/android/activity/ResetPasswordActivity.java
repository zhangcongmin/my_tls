package cn.talianshe.android.activity;

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
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.UserApiService;
import cn.talianshe.android.utils.PasswordCheckUtil;
import cn.talianshe.android.utils.PhoneFormatCheckUtil;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: ResetPasswordActivity
 * @Description: 重置密码页
 * @date 2017/11/17 14:28
 */
public class ResetPasswordActivity extends BaseActivity {
    @BindView(R.id.tv_get_captcha)
    TextView tvGetCaptcha;
    @BindView(R.id.btn_commit)
    Button btnCommit;
    @BindView(R.id.et_mobile)
    EditText etMobile;
    @BindView(R.id.et_verification_code)
    EditText etVerificationCode;
    @BindView(R.id.et_pwd)
    EditText etPwd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);
        initData();

    }

    private void initData() {
        setTitle(R.string.reset_password);
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
                if(TextUtils.isEmpty(etVerificationCode.getText().toString())){
                    MyToast.show(R.string.verification_code_null_tip,this);
                    return;
                }
                if(TextUtils.isEmpty(etPwd.getText().toString())){
                    MyToast.show(R.string.password_null_tip,this);
                    return;
                }
                if(!PasswordCheckUtil.checkPassword(etPwd.getText().toString())){
                    MyToast.show(R.string.password_invalid,this);
                    return;
                }
                resetPassword(etMobile.getText().toString(),etVerificationCode.getText().toString(),etPwd.getText().toString());
                break;
        }
    }

    private void resetPassword(String mobile,String verificationCode,String password) {
        UserApiService userApiService = RequestEngine.getInstance(false).getServer(UserApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                swipeLayout.setRefreshing(false);
                MyToast.show(R.string.reset_password_success,ResetPasswordActivity.this);
                finish();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
            }
        };
        swipeLayout.setRefreshing(true);
        userApiService.resetPassword(mobile,verificationCode,password).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void sendVerificationCode(String mobile) {
        UserApiService userApiService = RequestEngine.getInstance(false).getServer(UserApiService.class);
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
        userApiService.sendForgetPwdVerificationCode(mobile).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }


    private CountDownTimer myCountDownTime = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            tvGetCaptcha.setClickable(false);
            tvGetCaptcha.setFocusable(false);
            tvGetCaptcha.setTextColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.gray));
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
