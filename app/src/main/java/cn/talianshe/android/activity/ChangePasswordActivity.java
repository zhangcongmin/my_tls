package cn.talianshe.android.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

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
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: ChangePasswordActivity
 * @Description: 修改密码
 * @date 2017/12/7 19:45
 */
public class ChangePasswordActivity extends BaseActivity {

    @BindView(R.id.et_old_pwd)
    EditText etOldPwd;
    @BindView(R.id.et_new_pwd)
    EditText etNewPwd;
    @BindView(R.id.et_new_pwd_again)
    EditText etNewPwdAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.change_password);
    }

    private void changePassword(String oldPassword, String newPassword) {
        swipeLayout.setRefreshing(true);
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                MyToast.show(R.string.setting_new_pwd_success, ChangePasswordActivity.this);
                swipeLayout.setRefreshing(false);
                finish();
            }
        };
        userApiService.changePassword(oldPassword, newPassword).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    @OnClick(R.id.btn_commit)
    public void onViewClicked() {
        if (TextUtils.isEmpty(etOldPwd.getText().toString())) {
            MyToast.show(R.string.old_password_null_tip, this);
            return;
        }
        if (TextUtils.isEmpty(etNewPwd.getText().toString())) {
            MyToast.show(R.string.new_password_null_tip, this);
            return;
        }
        if (TextUtils.isEmpty(etNewPwdAgain.getText().toString())) {
            MyToast.show(R.string.password_again_null_tip, this);
            return;
        }
        if (!PasswordCheckUtil.checkPassword(etNewPwd.getText().toString())) {
            MyToast.show(R.string.password_invalid, this);
            return;
        }
        if (!etNewPwd.getText().toString().equals(etNewPwdAgain.getText().toString())) {
            MyToast.show(R.string.password_unsame_tip, this);
            return;
        }
        changePassword(etOldPwd.getText().toString(), etNewPwd.getText().toString());
    }
}
