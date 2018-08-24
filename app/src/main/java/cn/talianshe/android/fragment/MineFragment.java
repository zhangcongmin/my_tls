package cn.talianshe.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.activity.ActivityListActivity;
import cn.talianshe.android.activity.AssociationListActivity;
import cn.talianshe.android.activity.LoginActivity;
import cn.talianshe.android.activity.MessageCenterActivity;
import cn.talianshe.android.activity.PersonalInfoActivity;
import cn.talianshe.android.activity.QRGenerateActivity;
import cn.talianshe.android.activity.SecuritySettingActivity;
import cn.talianshe.android.bean.QRCodeType;
import cn.talianshe.android.db.entity.UserInfo;
import cn.talianshe.android.db.util.UserEntityUtil;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.ScaleImageView;

/**
 * @author zcm
 * @ClassName: MineFragment
 * @Description: 我的
 * @date 2017/12/7 18:30
 */
public class MineFragment extends BaseFragment {

    @BindView(R.id.iv_head)
    ScaleImageView ivHead;
    @BindView(R.id.iv_gender)
    ImageView ivGender;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_nick_name)
    TextView tvNickName;
    @BindView(R.id.ll_mine_info)
    LinearLayout llMineInfo;
    @BindView(R.id.tv_my_message)
    TextView tvMyMessage;
    @BindView(R.id.tv_my_association)
    TextView tvMyAssociation;
    @BindView(R.id.tv_my_activity)
    TextView tvMyActivity;
    @BindView(R.id.btn_exit_account)
    TextView btnExitAccount;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initData();
        super.onViewCreated(view, savedInstanceState);
    }

    private UserInfo userInfo;

    private void initData() {
        setTitle(R.string.personal_center);
        btnLeft.setVisibility(View.VISIBLE);
        btnLeft.setBackgroundResource(R.mipmap.setting);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TipDialogUtil.checkLogin(mActivity)) {
                    startActivity(new Intent(mActivity, SecuritySettingActivity.class));
                }
            }
        });

        btnRight.setVisibility(View.VISIBLE);
        btnRight.setBackgroundResource(R.mipmap.qr_code);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TipDialogUtil.checkLogin(mActivity)) {
                    QRGenerateActivity.GenerateEntity entity = new QRGenerateActivity.GenerateEntity();
                    entity.generateId = GlobalParams.USER_INFO.id+"";
                    entity.generateAvatar = TLSUrl.BASE_URL+GlobalParams.USER_INFO.avatar;
                    entity.name = GlobalParams.USER_INFO.realname;
                    entity.nickName = GlobalParams.USER_INFO.nickname;
                    entity.generateSecondStr = GlobalParams.USER_INFO.isTeacher?"teacher":"student";
                    startActivity(QRGenerateActivity.getQRGenerateIntent(mActivity, QRCodeType.PERSONAL,entity));
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        setPageData();
    }

    //设置页面数据
    private void setPageData() {
        userInfo = GlobalParams.USER_INFO;
        if (userInfo != null) {
            tvName.setText(userInfo.realname);
            if (!TextUtils.isEmpty(userInfo.avatar)) {
                Glide.with(mActivity)
                        .load(TLSUrl.BASE_URL + userInfo.avatar)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(ivHead);
            }
            ivGender.setVisibility(TextUtils.isEmpty(userInfo.sex)?View.GONE:View.VISIBLE);
            ivGender.setImageResource("0".equals(userInfo.sex) ? R.mipmap.female : R.mipmap.male);
            if (!TextUtils.isEmpty(userInfo.nickname)) {
                tvNickName.setText(getString(R.string.nickname_placeholder, userInfo.nickname));
            } else {
                tvNickName.setText("");
            }
            btnExitAccount.setVisibility(View.VISIBLE);
        } else {
            tvName.setText(R.string.unlogin);
            tvNickName.setText(R.string.login_tls);
            ivHead.setImageResource(R.mipmap.default_head);
            //说明未登录
            ivGender.setVisibility(View.GONE);
            btnExitAccount.setVisibility(View.GONE);
        }
    }

    @Override
    public int getContentViewResId() {
        return R.layout.fragment_mine;
    }

    @OnClick({R.id.ll_mine_info, R.id.tv_my_message, R.id.tv_my_association, R.id.tv_my_activity, R.id.btn_exit_account})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_mine_info:
                // TODO: 2017/12/7 个人信息页
                if (userInfo == null) {
                    startActivity(new Intent(mActivity, LoginActivity.class));
                } else {
                    startActivity(new Intent(mActivity, PersonalInfoActivity.class));
                }
                break;
            case R.id.tv_my_message:
                if (TipDialogUtil.checkLogin(mActivity)) {

                    startActivity(new Intent(mActivity, MessageCenterActivity.class));
                }
                break;
            case R.id.tv_my_association:
                if (TipDialogUtil.checkLogin(mActivity)) {
                    startActivity(AssociationListActivity.getFollowAssociationIntent(mActivity));
                }
                break;
            case R.id.tv_my_activity:
                if (TipDialogUtil.checkLogin(mActivity)) {
                    startActivity(ActivityListActivity.getFollowActivityIntent(mActivity));

                }
                break;
            case R.id.btn_exit_account:
                // TODO: 2017/12/7 退出账号
                if (userInfo != null) {
                    userInfo.token = null;
                    GlobalParams.TOKEN = null;
                    GlobalParams.USER_INFO = null;
                    UserEntityUtil.saveOrUpdateUserInfo(mActivity, userInfo);
                    setPageData();
                }
                break;
        }
    }
}
