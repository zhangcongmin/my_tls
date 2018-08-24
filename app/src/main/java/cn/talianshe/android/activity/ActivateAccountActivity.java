package cn.talianshe.android.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.wc.widget.dialog.IosDialog;

import org.greenrobot.greendao.query.QueryBuilder;

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
import cn.talianshe.android.utils.PasswordCheckUtil;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: BindMobileActivity
 * @Description: 绑定手机号
 * @date 2017/11/17 14:19
 */
public class ActivateAccountActivity extends BaseActivity {
    @BindView(R.id.btn_commit)
    Button btnCommit;
    @BindView(R.id.et_real_name)
    EditText etRealName;
    @BindView(R.id.et_pwd)
    EditText etPwd;
    @BindView(R.id.et_pwd_again)
    EditText etPwdAgain;
    private String realName;
    private String password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_account);
        ButterKnife.bind(this);
        initData();

    }

    private void initData() {
        setTitle(R.string.activate_account);
    }

    @OnClick({R.id.btn_commit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_commit:
                //
                if(TextUtils.isEmpty(etRealName.getText().toString())){
                    MyToast.show(R.string.name_null_tip,this);
                    return;
                }
                if(!PasswordCheckUtil.checkPassword(etPwd.getText().toString())){
                    MyToast.show(R.string.password_invalid,this);
                    return;
                }
                if(TextUtils.isEmpty(etPwdAgain.getText().toString())){
                    MyToast.show(R.string.password_again_null_tip,this);
                    return;
                }
                if(!etPwd.getText().toString().equals(etPwdAgain.getText().toString())){
                    MyToast.show(R.string.password_unsame_tip,this);
                    etPwd.setText("");
                    etPwdAgain.setText("");
                    return;
                }
                activationName();
                break;
        }
    }

    private void activationName() {
        swipeLayout.setRefreshing(true);
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                swipeLayout.setRefreshing(false);
                startActivity(BindMobileActivity.getBindMobileIntent(ActivateAccountActivity.this,password));
                finish();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
            }
        };
        realName = etRealName.getText().toString();
        password = etPwd.getText().toString();
        userApiService.activationName(realName, password).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }
}
