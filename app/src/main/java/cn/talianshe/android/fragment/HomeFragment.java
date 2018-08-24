package cn.talianshe.android.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import cn.talianshe.android.R;
import cn.talianshe.android.activity.ChooseSchoolActivity;
import cn.talianshe.android.activity.MessageCenterActivity;
import cn.talianshe.android.activity.OrderTimeActivity;
import cn.talianshe.android.activity.QRScanActivity;
import cn.talianshe.android.adapter.HomeAdapter;
import cn.talianshe.android.bean.AssociationActivityListData;
import cn.talianshe.android.bean.BooleanData;
import cn.talianshe.android.bean.HotAssociationListData;
import cn.talianshe.android.bean.SchoolListData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.db.entity.UserInfo;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.net.service.AssociationApiService;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author zcm
 * @ClassName: ${CLASS}
 * @Description:
 * @date 2017/11/8 19:08
 */

public class HomeFragment extends BaseFragment implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.rv_home)
    RecyclerView rvHome;
    private HomeAdapter homeAdapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initData();

        super.onViewCreated(view, savedInstanceState);
    }

    private UserInfo userInfo;

    private void initData() {
        userInfo = GlobalParams.USER_INFO;
        btnLeft.setImageResource(R.mipmap.qr_scan);
        btnRight.setBackgroundResource(R.mipmap.message);
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TipDialogUtil.checkLogin(mActivity)) {
                    startActivity(new Intent(mActivity, MessageCenterActivity.class));
                }
            }
        });
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(QRScanActivity.getQRScanIntent(mActivity, QRScanActivity.ScanType.NORMAL));
            }
        });
        setTitle(R.string.choose_school);
        tvTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black, getActivity().getTheme()));
        tvTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.mipmap.location_black, getActivity().getTheme()), null, getResources().getDrawable(R.mipmap.arrow_right, getActivity().getTheme()), null);
        if (null != GlobalParams.USER_INFO) {
            setTitle(GlobalParams.USER_INFO.school);
        }

        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == GlobalParams.USER_INFO) {
                    startActivity(ChooseSchoolActivity.getChooseSchoolIntent(mActivity,true));
                }
            }
        });

        rvHome.setLayoutManager(new LinearLayoutManager(getActivity()));
        homeAdapter = new HomeAdapter(getActivity());
        rvHome.setAdapter(homeAdapter);
        swipeLayout.setEnabled(true);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
        requestData();
    }


    @Override
    public int getContentViewResId() {
        return R.layout.fragment_home;
    }

    private int requestCount = 0;
    private int availablelRequestCount = 3;
    private Handler handler = new Handler();

    public void requestData() {
        requestCount = 0;
        swipeLayout.setRefreshing(true);
        getHotAssociationList();
        getOnGoingActivityList();
        getRecommendActivityList();

    }

    private void checkHasUnreadMsg() {
        final HttpSubscriber httpSubscriber = new HttpSubscriber<BooleanData>(mActivity) {

            @Override
            public void onSuccess(BooleanData booleanData) {
                if (btnRight != null) {
                    if (booleanData.result != null && booleanData.result) {
                        btnRight.setBackgroundResource(R.mipmap.message_unread);
                    } else {
                        btnRight.setBackgroundResource(R.mipmap.message);
                    }
                    if (!TextUtils.isEmpty(GlobalParams.TOKEN)) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkHasUnreadMsg();
                            }
                        }, 20000);
                    }
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                if (!TextUtils.isEmpty(GlobalParams.TOKEN)) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkHasUnreadMsg();
                        }
                    }, 20000);
                }
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).hasUnreadMsg().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }


    private void getHotAssociationList() {
        AssociationApiService associationApiService = RequestEngine.getInstance(false).getServer(AssociationApiService.class);
        HttpSubscriber hotAssociationSubscribe = new HttpSubscriber<HotAssociationListData>(mActivity) {
            @Override
            public void onSuccess(HotAssociationListData listData) {
                List<HotAssociationListData.HotAssociation> result = listData.result.list;
                homeAdapter.setHotAssociationList(result);
                homeAdapter.notifyDataSetChanged();
                requestCount++;
                if (requestCount == availablelRequestCount) {
                    swipeLayout.setRefreshing(false);
                }
            }
        };
        associationApiService.getHotAssociationList(TextUtils.isEmpty(GlobalParams.SCHOOL_ID) ? (userInfo == null ? null : userInfo.schoolId) : GlobalParams.SCHOOL_ID, 0, 10).compose(RxSchedulersHelper.io_main()).subscribe(hotAssociationSubscribe);
    }

    private AssociationActivityListData.AssociationActivityListInfo onGoingActivityListInfo;

    private void getOnGoingActivityList() {
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationActivityListData>(mActivity) {
            @Override
            public void onSuccess(AssociationActivityListData listData) {
                onGoingActivityListInfo = listData.result;
                requestCount++;
                if (requestCount == availablelRequestCount) {
                    swipeLayout.setRefreshing(false);
                }
                homeAdapter.setOnGoingActivityListInfo(onGoingActivityListInfo);
                homeAdapter.notifyDataSetChanged();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getOnGoingActivityList(0, 10, GlobalParams.SCHOOL_ID).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private AssociationActivityListData.AssociationActivityListInfo recommendActivityListInfo;

    private void getRecommendActivityList() {
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationActivityListData>(mActivity) {
            @Override
            public void onSuccess(AssociationActivityListData listData) {
                recommendActivityListInfo = listData.result;
                requestCount++;
                if (requestCount == availablelRequestCount) {
                    swipeLayout.setRefreshing(false);
                }
                homeAdapter.setRecommendActivityListInfo(recommendActivityListInfo);
                homeAdapter.notifyDataSetChanged();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getRecommendActivityList(0, 0, GlobalParams.SCHOOL_ID).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    @Override
    public void onStart() {
        super.onStart();
        requestCodeQRCodePermissions();
        if (!TextUtils.isEmpty(GlobalParams.TOKEN)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    checkHasUnreadMsg();
                }
            });
        }
    }

    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    //    private boolean isAlertWindowPermission = false;
    private boolean isRequestPermissionDenied = false;

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};
//        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, 该权限在6.0只能手动开启
//        isAlertWindowPermission = false;
        isRequestPermissionDenied = false;
        if (!EasyPermissions.hasPermissions(mActivity, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.tls_need_permission), REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!EasyPermissions.hasPermissions(mActivity, perms)) {
//
//                EasyPermissions.requestPermissions(this, getString(R.string.tls_need_permission), REQUEST_CODE_QRCODE_PERMISSIONS, perms);
//            } else {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !Settings.canDrawOverlays(mActivity)) {
//                    isAlertWindowPermission = true;
//                    new AppSettingsDialog.Builder(this).setTitle(R.string.perssion_denied).setRationale(R.string.alert_window_permission_should_open).build().show();
//                }
//            }
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        System.out.println("权限赋予了");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_CANCELED) {
//            //如果还是没有对应权限
//            if (!isAlertWindowPermission && isRequestPermissionDenied) {
//                System.out.println("权限走cancel了");
//                mActivity.finish();
//            }
//        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (perms != null && perms.size() > 0) {
            isRequestPermissionDenied = true;
            String permissionTip = "塔联社使用需要";
            if (perms.contains(Manifest.permission.CAMERA)) {
                permissionTip += "拍照、";
            }
            if (perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE) || perms.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissionTip += "文件存储、";
            }
            if (perms.contains(Manifest.permission.RECEIVE_SMS) || perms.contains(Manifest.permission.READ_SMS)) {
                permissionTip += "短信读取权限、";
            }
            if (perms.contains(Manifest.permission.READ_PHONE_STATE) || perms.contains(Manifest.permission.CALL_PHONE)) {
                permissionTip += "读取手机信息、";
            }
            if (perms.contains(Manifest.permission.ACCESS_COARSE_LOCATION) || perms.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                permissionTip += "获取gps定位、";
            }
            permissionTip = permissionTip.substring(0, permissionTip.length() - 1) + "等权限，否则将无法使用";
            new AppSettingsDialog.Builder(this).setTitle(R.string.perssion_denied).setRationale(permissionTip).build().show();
        }
        System.out.println("权限被拒绝了");
    }


    @Subscribe
    public void onReceiveSchoolInfoEvent(SchoolListData.School school) {
        if (TextUtils.isEmpty(GlobalParams.TOKEN) || school.isFromHome) {
            GlobalParams.SCHOOL_ID = school.schoolId;
            setTitle(school.name);
            requestData();
        }
    }


    @Override
    public void onStop() {
        handler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
