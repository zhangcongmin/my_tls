package cn.talianshe.android.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.socialize.UMShareAPI;
import com.wc.widget.dialog.IosDialog;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.app.TaliansheApplication;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.db.entity.UserInfo;
import cn.talianshe.android.eventbus.PublishActivitySuccessEvent;
import cn.talianshe.android.fragment.BaseFragment;
import cn.talianshe.android.fragment.ContactsFragment;
import cn.talianshe.android.fragment.HomeFragment;
import cn.talianshe.android.fragment.MineFragment;
import cn.talianshe.android.fragment.MomentFragment;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.ScrollViewPager;

public class MainActivity extends BaseActivity {

    @BindView(R.id.view_pager)
    ScrollViewPager viewPager;
    @BindView(R.id.tv_home)
    TextView tvHome;
    @BindView(R.id.tv_moment)
    TextView tvMoment;
    @BindView(R.id.ib_plus)
    ImageButton ibPlus;
    @BindView(R.id.tv_contacts)
    TextView tvContacts;
    @BindView(R.id.tv_mine)
    TextView tvMine;

    private static final int INDEX_HOME_TAB = 0;
    private static final int INDEX_MOMENT_TAB = 1;
    private static final int INDEX_CONTACTS_TAB = 2;
    private static final int INDEX_MINE_TAB = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        TaliansheApplication.getInstance().getTimeStamp();
        init();
    }

    List<TextView> tabs = new ArrayList<>();

    private void init() {
        getUserInfo();
        hideTitleBar();
        initViewPager();
        tabs.add(INDEX_HOME_TAB, tvHome);
        tabs.add(INDEX_MOMENT_TAB, tvMoment);
        tabs.add(INDEX_CONTACTS_TAB, tvContacts);
        tabs.add(INDEX_MINE_TAB, tvMine);
        //手动初始化选中的tab
        tvHome.setSelected(true);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setTabSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void getUserInfo() {
        UserInfo userInfo = GlobalParams.USER_INFO;
        if(userInfo !=null){
            GlobalParams.TOKEN = userInfo.token;
        }
//        TipDialogUtil.checkFillInfo(this);
    }

    private int selectedTabIndex = -1;

    /**
     * 根据索引设置选中的tab
     *
     * @param tabIndex
     */
    private void setTabSelected(int tabIndex) {
        //选中相同tab不做任何操作
        if (selectedTabIndex == tabIndex)
            return;
        selectedTabIndex = tabIndex;
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setSelected(i == selectedTabIndex);
        }
    }

    public void initViewPager() {
//        viewPager.setCanScroll(false);
        ArrayList<BaseFragment> fragments = new ArrayList<>();
        fragments.add(INDEX_HOME_TAB, new HomeFragment());
        fragments.add(INDEX_MOMENT_TAB, new MomentFragment());
        fragments.add(INDEX_CONTACTS_TAB, new ContactsFragment());
        fragments.add(INDEX_MINE_TAB, new MineFragment());
        viewPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager(), fragments));
        viewPager.setOffscreenPageLimit(4);
        viewPager.setCanScroll(false);
    }

    class TabsPagerAdapter extends FragmentPagerAdapter {
        private List<BaseFragment> fragments;

        public TabsPagerAdapter(FragmentManager fm, List<BaseFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return (fragments == null || fragments.size() == 0) ? null : fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments == null ? 0 : fragments.size();
        }
    }

    @OnClick({R.id.tv_home, R.id.tv_moment, R.id.ib_plus, R.id.tv_contacts, R.id.tv_mine})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ib_plus:
                //显示+号按钮弹框
                showPlusDialog();
                break;
            case R.id.tv_home:
                if (!view.isSelected()) {
                    viewPager.setCurrentItem(0, false);
                }
                break;
            case R.id.tv_moment:
                if (!view.isSelected()) {
                    if (TipDialogUtil.checkLogin(this)) {
                        viewPager.setCurrentItem(1, false);
                    }
                }
                break;
            case R.id.tv_contacts:
                if (!view.isSelected()) {
                    if (TipDialogUtil.checkLogin(this)) {
                        viewPager.setCurrentItem(2, false);
                    }
                }
                break;
            case R.id.tv_mine:
                if (!view.isSelected()) {
                    viewPager.setCurrentItem(3, false);
                }
                break;
        }
    }

    private AlertDialog plusDialog;

    private void showPlusDialog() {
        if (plusDialog == null) {
            plusDialog = new AlertDialog.Builder(this, R.style.PlusDialogStyle).create();
            plusDialog.show();
            Window window = plusDialog.getWindow();
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setGravity(Gravity.CENTER);
            window.setContentView(R.layout.view_dialog_plus);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
            PlusClickListener plusClickListener = new PlusClickListener();
            plusDialog.findViewById(R.id.tv_create_association).setOnClickListener(plusClickListener);
            plusDialog.findViewById(R.id.tv_publish_activity).setOnClickListener(plusClickListener);
            plusDialog.findViewById(R.id.tv_apply_job).setOnClickListener(plusClickListener);
            plusDialog.findViewById(R.id.tv_post_moment).setOnClickListener(plusClickListener);
            plusDialog.findViewById(R.id.ib_cancel).setOnClickListener(plusClickListener);
        } else {
            plusDialog.show();
        }
    }

    private class PlusClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_create_association:
                    if (TipDialogUtil.checkLogin(MainActivity.this) && TipDialogUtil.checkFillInfo(MainActivity.this)) {
                        if(GlobalParams.USER_INFO.isTeacher){
                            //教师端没有创建社团的权限
                            MyToast.show(R.string.teacher_no_authorized,MainActivity.this);
                            return;
                        }
                        startActivity(new Intent(MainActivity.this, CreateAssociationDataActivity.class));
                    }
                    break;
                case R.id.tv_publish_activity:
                    if (TipDialogUtil.checkLogin(MainActivity.this) && TipDialogUtil.checkFillInfo(MainActivity.this)) {
                        if(GlobalParams.USER_INFO.isTeacher){
                            //教师端没有发布活动的权限
                            MyToast.show(R.string.teacher_no_authorized,MainActivity.this);
                            return;
                        }
                        checkPublishOAuth();
                    }
                    break;
                case R.id.tv_apply_job:
                    MyToast.show(R.string.function_is_not_open, MainActivity.this);
                    break;
                case R.id.tv_post_moment:
                    if (TipDialogUtil.checkLogin(MainActivity.this) && TipDialogUtil.checkFillInfo(MainActivity.this)) {
                        startActivity(PostMomentActivity.getPersonalMomentIntent(MainActivity.this));
                    }
                    break;
                default:
                    break;
            }
            plusDialog.dismiss();
        }
    }

    private void checkPublishOAuth() {
        MyProgressDialog.show(this, false);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                MyProgressDialog.dismiss();
                if ("1".equals(stringData.result)) {
                    //有发布权限
                    startActivity(new Intent(MainActivity.this, PublishActivityActivity.class));
                } else {
//                    startActivity(new Intent(MainActivity.this,PublishActivityActivity.class));
                    //没有发布权限
                    MyToast.show(R.string.publish_activity_no_auth, MainActivity.this);
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityAuthorization().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Subscribe
    public void onReceivePublishActivitySuccessEvent(PublishActivitySuccessEvent event){
        MyToast.show(R.string.publish_activity_success, this);
    }
}
