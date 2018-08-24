package cn.talianshe.android.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.wc.widget.dialog.IosDialog;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.UserData;
import cn.talianshe.android.db.DBManager;
import cn.talianshe.android.db.DaoMaster;
import cn.talianshe.android.db.UserInfoDao;
import cn.talianshe.android.db.entity.UserInfo;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.UserApiService;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: SecuritySettingActivity
 * @Description: 安全设置
 * @date 2017/12/7 19:13
 */
public class SecuritySettingActivity extends BaseActivity {

    @BindView(R.id.tv_mobile)
    TextView tvMobile;
    @BindView(R.id.ll_bind_mobile)
    LinearLayout llBindMobile;
    @BindView(R.id.switch_qq)
    SwitchCompat switchQQ;
    @BindView(R.id.switch_wechat)
    SwitchCompat switchWechat;
    @BindView(R.id.tv_privacy_setting)
    TextView tvPrivacySetting;
    @BindView(R.id.tv_change_password)
    TextView tvChangePassword;
    @BindView(R.id.tv_abount_tls)
    TextView tvAbountTls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_setting);
        ButterKnife.bind(this);
    }

    private UserInfo userInfo;
    private void initData() {
        userInfo = GlobalParams.USER_INFO;
        if(!TextUtils.isEmpty(userInfo.mobile)){
            tvMobile.setText(userInfo.mobile);
        }
        setTitle(R.string.security_setting);
        setSwitchStatus();
    }

    private void setSwitchStatus() {
        switchQQ.setOnCheckedChangeListener(null);
        switchWechat.setOnCheckedChangeListener(null);
        switchWechat.setChecked(userInfo.isWechatBind());
        switchQQ.setChecked(userInfo.isQQBind());
        switchWechat.setOnCheckedChangeListener(bindWechatCheckListener);
        switchQQ.setOnCheckedChangeListener(bindQQCheckListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    private CompoundButton.OnCheckedChangeListener bindWechatCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            showBindConfirmDialog(buttonView.getContext(), isChecked, SHARE_MEDIA.WEIXIN);
        }
    };

    private CompoundButton.OnCheckedChangeListener bindQQCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            showBindConfirmDialog(buttonView.getContext(), isChecked, SHARE_MEDIA.QQ);
        }
    };

    private void showBindConfirmDialog(final Context context, final boolean isChecked, SHARE_MEDIA share_media) {
        curMedia = share_media;
        //设置点击事件
        Dialog dialog = new IosDialog.Builder(context)
                .setMessage(share_media == SHARE_MEDIA.WEIXIN ? (isChecked ? R.string.open_wechat : R.string.unbind_wechat) : (isChecked ? R.string.open_qq : R.string.unbind_qq)).setMessageColor(ContextCompat.getColor(context, R.color.dark_gray)).setMessageSize(15)
                .setNegativeButtonColor(ContextCompat.getColor(context, R.color.gray))
                .setNegativeButtonSize(16)
                .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        dialog.dismiss();
                        resetSwitchButton(curMedia);
                    }
                })
                .setPositiveButtonColor(ContextCompat.getColor(context, R.color.theme_color))
                .setPositiveButtonSize(16)
                .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {

                            dialog.dismiss();
                        if (isChecked) {
                            startOAuth(context, curMedia);

                        } else {
                            //解绑
                            bindRequest(false, curMedia, null);

                        }
                    }
                }).build();
        dialog.show();
    }

    private String openid;
    private SHARE_MEDIA curMedia;

    private void startOAuth(Context context, SHARE_MEDIA media) {
        UMShareConfig config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);

        UMShareAPI umShareAPI = UMShareAPI.get(context);
        umShareAPI.setShareConfig(config);
        UMAuthListener authListener = new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                bindRequest(true, share_media, map);
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                resetSwitchButton(share_media);
                MyToast.show(R.string.oauth_failed, SecuritySettingActivity.this);
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                resetSwitchButton(share_media);
                MyToast.show(R.string.oauth_cancel, SecuritySettingActivity.this);
            }
        };
        umShareAPI.getPlatformInfo(this, media, authListener);
    }

    private void bindRequest(final boolean isBind, SHARE_MEDIA share_media, Map<String, String> map) {
        curMedia = share_media;
        openid = isBind ?map.get("unionid"):"0";
        swipeLayout.setRefreshing(true);
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(SecuritySettingActivity.this) {
            @Override
            public void onSuccess(StringData stringData) {
                MyToast.show(isBind ? R.string.bind_success : R.string.unbind_success, SecuritySettingActivity.this);
                if (curMedia == SHARE_MEDIA.WEIXIN) {
                    userInfo.weixinopenId = openid;
                } else {
                    userInfo.qqopenId = openid;
                }
                updateUserInfo();
                setSwitchStatus();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
//                MyToast.show(R.string.bind_failed, SecuritySettingActivity.this);
                MyToast.show(msg, SecuritySettingActivity.this);
                resetSwitchButton(curMedia);
            }
        };
        if (curMedia == SHARE_MEDIA.WEIXIN) {
            userApiService.bindWechat(openid).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else {
            userApiService.bindQQ(openid).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        }
    }

    private void resetSwitchButton(SHARE_MEDIA share_media) {
        if (share_media == SHARE_MEDIA.WEIXIN) {
            switchWechat.setOnCheckedChangeListener(null);
            switchWechat.setChecked(!switchWechat.isChecked());
            switchWechat.setOnCheckedChangeListener(bindWechatCheckListener);
        } else {
            switchQQ.setOnCheckedChangeListener(null);
            switchQQ.setChecked(!switchQQ.isChecked());
            switchQQ.setOnCheckedChangeListener(bindQQCheckListener);
        }
    }

    private void updateUserInfo() {
        UserInfo userInfo = GlobalParams.USER_INFO;
        userInfo.token = GlobalParams.TOKEN;
        UserInfoDao userInfoDao = new DaoMaster(DBManager.getInstance(SecuritySettingActivity.this).getWritableDatabase()).newSession().getUserInfoDao();
        QueryBuilder<UserInfo> queryBuilder = userInfoDao.queryBuilder();
        queryBuilder.where(UserInfoDao.Properties.Id.eq(String.valueOf(userInfo.id)));
        UserInfo dbUserInfo = queryBuilder.unique();
        if (dbUserInfo != null) {
            userInfoDao.update(userInfo);
        } else {
            userInfoDao.insert(userInfo);
        }
        if (GlobalParams.USER_INFO != null) {
            userInfo.token = null;
            userInfoDao.update(GlobalParams.USER_INFO);
        }
        GlobalParams.USER_INFO = userInfo;
        swipeLayout.setRefreshing(false);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode,resultCode,data);
    }

    @OnClick({R.id.ll_bind_mobile, R.id.tv_privacy_setting, R.id.tv_change_password, R.id.tv_abount_tls})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_bind_mobile:
                if(TextUtils.isEmpty(userInfo.mobile)){
                    MyToast.show(R.string.no_bind_mobile_tip,this);
                    return;
                }
                startActivity(BindMobileActivity.getChangeMobileIntent(this,userInfo.mobile));
                break;
            case R.id.tv_privacy_setting:
                startActivity(new Intent(this, PrivacySettingActivity.class));
                break;
            case R.id.tv_change_password:
                startActivity(new Intent(this, ChangePasswordActivity.class));
                break;
            case R.id.tv_abount_tls:
                startActivity(new Intent(this, AboutUsActivity.class));
                break;
        }
    }
}
