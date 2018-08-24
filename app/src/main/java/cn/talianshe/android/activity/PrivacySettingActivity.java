package cn.talianshe.android.activity;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.PrivacySettingData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.UserApiService;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: PrivacySettingActivity
 * @Description: 隐私设置
 * @date 2017/12/7 19:53
 */
public class PrivacySettingActivity extends BaseActivity {

    @BindView(R.id.switch_public_fullname)
    SwitchCompat switchPublicFullname;
    @BindView(R.id.switch_public_nickname)
    SwitchCompat switchPublicNickname;
    @BindView(R.id.switch_public_mobile)
    SwitchCompat switchPublicMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_setting);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.privacy_setting);
        getPrivacySetting();
    }

    private PrivacySettingData.PrivacySettingInfo settingInfo;

    private void getPrivacySetting() {
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        swipeLayout.setRefreshing(true);
        HttpSubscriber httpSubscriber = new HttpSubscriber<PrivacySettingData>(this) {
            @Override
            public void onSuccess(PrivacySettingData privacySettingData) {
                swipeLayout.setRefreshing(false);
                resetSwitchState(privacySettingData.result.isNamePublic(),privacySettingData.result.isNicknamePublic(),privacySettingData.result.isMobilePublic());
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
//                if(curCheckedButton != null){
//                    boolean namePublic = (curCheckedButton == switchPublicFullname) != switchPublicFullname.isChecked();
//                    boolean nickNamePublic = (curCheckedButton == switchPublicNickname) != switchPublicNickname.isChecked();
//                    boolean mobilePublic = (curCheckedButton == switchPublicMobile) != switchPublicMobile.isChecked();
//                    resetSwitchState(namePublic,nickNamePublic,mobilePublic);
//                }else{
//                }
                resetSwitchState(false,false,false);
            }
        };
        userApiService.getPrivacySetting().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void setPrivacySetting(boolean isNamePublic,boolean isNicknamePublic,boolean isMobilePublic) {
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        swipeLayout.setRefreshing(true);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                MyToast.show(R.string.setting_success,PrivacySettingActivity.this);
                swipeLayout.setRefreshing(false);
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                if(curCheckedButton != null){
                    boolean namePublic = (curCheckedButton == switchPublicFullname) != switchPublicFullname.isChecked();
                    boolean nickNamePublic = (curCheckedButton == switchPublicNickname) != switchPublicNickname.isChecked();
                    boolean mobilePublic = (curCheckedButton == switchPublicMobile) != switchPublicMobile.isChecked();
                    resetSwitchState(namePublic,nickNamePublic,mobilePublic);
                }else{
                    resetSwitchState(false,false,false);
                }
            }
        };
        userApiService.setPrivacySetting(isNamePublic?"1":"0",isNicknamePublic?"1":"0",isMobilePublic?"1":"0").compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void resetSwitchState(boolean namePublic, boolean nicknamePublic, boolean mobilePublic) {
        switchPublicFullname.setOnCheckedChangeListener(null);
        switchPublicNickname.setOnCheckedChangeListener(null);
        switchPublicMobile.setOnCheckedChangeListener(null);
        switchPublicFullname.setChecked(namePublic);
        switchPublicNickname.setChecked(nicknamePublic);
        switchPublicMobile.setChecked(mobilePublic);
        switchPublicFullname.setOnCheckedChangeListener(onCheckedChangeListener);
        switchPublicNickname.setOnCheckedChangeListener(onCheckedChangeListener);
        switchPublicMobile.setOnCheckedChangeListener(onCheckedChangeListener);

    }
    private View curCheckedButton;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            curCheckedButton = buttonView;
            setPrivacySetting(switchPublicFullname.isChecked(),switchPublicNickname.isChecked(),switchPublicMobile.isChecked());
        }
    };
}
