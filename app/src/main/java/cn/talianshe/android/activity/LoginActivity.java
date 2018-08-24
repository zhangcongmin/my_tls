package cn.talianshe.android.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.wc.widget.dialog.IosDialog;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.app.AppConfig;
import cn.talianshe.android.bean.LoginData;
import cn.talianshe.android.bean.SchoolListData;
import cn.talianshe.android.bean.UserData;
import cn.talianshe.android.db.entity.UserInfo;
import cn.talianshe.android.db.util.UserEntityUtil;
import cn.talianshe.android.eventbus.AccountActivatedEvent;
import cn.talianshe.android.eventbus.FillPersonalInfoEvent;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.UserApiService;
import cn.talianshe.android.utils.PhoneFormatCheckUtil;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.UnderlineBtn;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author zcm
 * @ClassName: LoginActivity
 * @Description: 登录页
 * @date 2017/11/3 14:54
 */
public class LoginActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.underline_btn_account)
    UnderlineBtn underlineBtnAccount;
    @BindView(R.id.underline_btn_mobile)
    UnderlineBtn underlineBtnMobile;
    @BindView(R.id.ll_account)
    LinearLayout llAccount;
    @BindView(R.id.et_mobile)
    EditText etMobile;
    @BindView(R.id.ll_mobile)
    LinearLayout llMobile;
    @BindView(R.id.et_pwd)
    EditText etPwd;
    @BindView(R.id.tv_school)
    TextView tvSchool;
    @BindView(R.id.et_account)
    EditText etAccount;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.qq_login)
    ImageView qqLogin;
    @BindView(R.id.wechat_login)
    ImageView wechatLogin;
    @BindView(R.id.iv_watch_pwd)
    ImageView ivWatchPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
//        setListeners();
        if (AppConfig.isDebug) {
            TipDialogUtil.showSelectUrlDialog(this);
        }
        initData();
    }

    private void initData() {
        ivWatchPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                }
                return true;
            }
        });
    }

    @OnClick({R.id.underline_btn_account, R.id.ll_choose_school, R.id.underline_btn_mobile, R.id.btn_login, R.id.qq_login, R.id.wechat_login, R.id.tv_forget_password})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.underline_btn_account:
                if (!underlineBtnAccount.isChecked()) {
                    //显示账号输入界面，隐藏手机输入界面
                    underlineBtnAccount.setChecked(true);
                    underlineBtnMobile.setChecked(false);
                    llAccount.setVisibility(View.VISIBLE);
                    llMobile.setVisibility(View.GONE);
                }
                break;
            case R.id.underline_btn_mobile:
                if (!underlineBtnMobile.isChecked()) {
                    //显示手机输入界面，隐藏账号输入界面
                    underlineBtnMobile.setChecked(true);
                    underlineBtnAccount.setChecked(false);
                    llMobile.setVisibility(View.VISIBLE);
                    llAccount.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_login:
                //登录
                if (checkSignData()) {
                    startLogin(underlineBtnAccount.isChecked());
                }
                break;
            case R.id.qq_login:
            case R.id.wechat_login:
                final SHARE_MEDIA media;
                if (view.getId() == R.id.qq_login) {
                    media = SHARE_MEDIA.QQ;
                } else {
                    media = SHARE_MEDIA.WEIXIN;
                }

                UMShareConfig config = new UMShareConfig();
                config.isNeedAuthOnGetUserInfo(true);

                final UMShareAPI umShareAPI = UMShareAPI.get(this);
                umShareAPI.setShareConfig(config);
                if (umShareAPI.isAuthorize(this, media)) {
                    umShareAPI.deleteOauth(this, media, new UMAuthListener() {
                        @Override
                        public void onStart(SHARE_MEDIA share_media) {

                        }

                        @Override
                        public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                            startOAuth(media, umShareAPI);
                        }

                        @Override
                        public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {

                            MyToast.show("删除授权出错", LoginActivity.this);
                        }

                        @Override
                        public void onCancel(SHARE_MEDIA share_media, int i) {
                            MyToast.show("删除授权取消", LoginActivity.this);

                        }
                    });
                } else {
                    startOAuth(media, umShareAPI);
                }
                break;
            case R.id.ll_choose_school:
                startActivity(new Intent(this, ChooseSchoolActivity.class));
                break;
            case R.id.tv_forget_password:
                startActivity(new Intent(this, ResetPasswordActivity.class));
                break;
        }
    }

    private void startOAuth(SHARE_MEDIA media, UMShareAPI umShareAPI) {
        UMAuthListener authListener = new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                swipeLayout.setRefreshing(true);
                UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
                HttpSubscriber httpSubscriber = new HttpSubscriber<LoginData>(LoginActivity.this) {
                    @Override
                    public void onSuccess(LoginData loginInfoBaseBean) {
                        //登录成功，再次获取用户信息，未激活的话弹框激活
                        GlobalParams.TOKEN = loginInfoBaseBean.result;
                        getUserInfo();
                    }
                };
                userApiService.otherLogin(map.get("unionid"), share_media == SHARE_MEDIA.QQ ? "0" : "1").compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
                System.out.println("map" + map.toString());
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                MyToast.show("授权失败", LoginActivity.this);
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                MyToast.show("授权取消", LoginActivity.this);
            }
        };
        umShareAPI.getPlatformInfo(this, media, authListener);
    }

    /**
     * 判断登录数据是否合法
     */
    private boolean checkSignData() {
        if (underlineBtnMobile.isChecked()) {
            //手机登录
            boolean mobileLegal = PhoneFormatCheckUtil.isChinaPhoneLegal(etMobile.getText().toString());
            if (!mobileLegal) {
                MyToast.show(getString(R.string.mobile_illegal), this);
                return false;
            }
        } else {
            //账号登录判断
            if (TextUtils.isEmpty(tvSchool.getText().toString())) {
                MyToast.show(getString(R.string.please_choose_school), this);
                return false;
            }
        }
        if (etPwd.getText().length() < 6) {
            MyToast.show(getString(R.string.please_input_right_pwd), this);
            return false;
        }
        return true;
    }

    private void startLogin(boolean isAccountLogin) {
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<LoginData>(this) {
            @Override
            public void onSuccess(LoginData loginInfoBaseBean) {
                //登录成功，再次获取用户信息，未激活的话弹框激活
                GlobalParams.TOKEN = loginInfoBaseBean.result;
                getUserInfo();
            }
        };
        swipeLayout.setRefreshing(true);
        if (isAccountLogin) {
            String userName = etAccount.getText().toString();
            String pwd = etPwd.getText().toString();
            userApiService.loginByAccount(userName, pwd, schoolId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else {
            String mobile = etMobile.getText().toString();
            String pwd = etPwd.getText().toString();
            userApiService.loginByMobile(mobile, pwd).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        }
    }

    private UserInfo userInfo;

    private void getUserInfo() {
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<UserData>(this) {
            @Override
            public void onSuccess(UserData userData) {
                userInfo = userData.result;
                swipeLayout.setRefreshing(false);
                GlobalParams.SCHOOL_ID = userInfo.schoolId;
                if (!userInfo.isActivated()) {
                    //设置点击事件
                    Dialog dialog = new IosDialog.Builder(LoginActivity.this)
                            .setTitle(R.string.account_unactivated).setTitleColor(ContextCompat.getColor(LoginActivity.this, R.color.dark_gray))
                            .setMessage(R.string.activate_immediately).setMessageColor(ContextCompat.getColor(LoginActivity.this, R.color.gray)).setMessageSize(15)
                            .setNegativeButtonColor(ContextCompat.getColor(LoginActivity.this, R.color.dark_gray))
                            .setNegativeButtonSize(16)
                            .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                                @Override
                                public void onClick(IosDialog dialog, View v) {
                                    dialog.dismiss();
//                                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
//                                    finish();
                                }
                            })
                            .setPositiveButtonColor(ContextCompat.getColor(LoginActivity.this, R.color.theme_color))
                            .setPositiveButtonSize(16)
                            .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                                @Override
                                public void onClick(IosDialog dialog, View v) {
                                    dialog.dismiss();
                                    // TODO: 2017/12/8 打开绑定账号界面
                                    startActivity(new Intent(LoginActivity.this, ActivateAccountActivity.class));
                                }
                            }).build();
                    dialog.show();
                    dialog.setCancelable(false);
                } else {
                    saveUserInfo(userInfo);
//                    TaliansheApplication.getInstance().finishAllActivity();
                    if (checkFillInfo()) {
                        startMainActivity();
                    }
                }
            }
        };
        userApiService.getUserInfo().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Subscribe
    public void onReceiveAccountActivatedEvent(AccountActivatedEvent event) {
        userInfo.mobile = event.mobile;
        userInfo.isActivated = event.isActivated;
        saveUserInfo(userInfo);
        if(checkFillInfo()){
            startMainActivity();
        }
    }

    @Subscribe
    public void onReceiveFillPersonalInfoEvent(FillPersonalInfoEvent event) {
        startMainActivity();
    }

    private boolean checkFillInfo() {
        if (GlobalParams.USER_INFO.needFillInfo()) {

            Dialog fillInfoDialog = new IosDialog.Builder(this)
                    .setMessage(R.string.fill_personal_info_tip).setMessageColor(ContextCompat.getColor(this, R.color.dark_gray)).setMessageSize(15)
                    .setNegativeButtonColor(ContextCompat.getColor(this, R.color.gray))
                    .setNegativeButtonSize(16)
                    .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                        @Override
                        public void onClick(IosDialog dialog, View v) {
                            dialog.dismiss();

                            startMainActivity();
                        }
                    })
                    .setPositiveButtonColor(ContextCompat.getColor(this, R.color.theme_color))
                    .setPositiveButtonSize(16)
                    .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                        @Override
                        public void onClick(IosDialog dialog, View v) {
                            dialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, PersonalInfoActivity.class));
                        }
                    }).build();
            fillInfoDialog.setCancelable(false);
            fillInfoDialog.show();
            return false;
        } else {
            return true;
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }


    private void saveUserInfo(UserInfo userInfo) {
        userInfo.token = GlobalParams.TOKEN;
        GlobalParams.SCHOOL_ID = userInfo.schoolId;
        UserEntityUtil.saveOrUpdateUserInfo(LoginActivity.this, userInfo);
        if (GlobalParams.USER_INFO != null) {
            GlobalParams.USER_INFO.token = null;
            UserEntityUtil.saveOrUpdateUserInfo(LoginActivity.this, GlobalParams.USER_INFO);
        }
        GlobalParams.USER_INFO = userInfo;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GlobalParams.TOKEN = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    private String schoolId;

    @Subscribe
    public void onReceiveSchoolInfoEvent(SchoolListData.School school) {
        tvSchool.setText(school.name);
        schoolId = school.schoolId;
    }

}
