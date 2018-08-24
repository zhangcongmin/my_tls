package cn.talianshe.android.activity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bruce.stickynavigationbar.bean.NavBean;
import com.bruce.stickynavigationbar.listener.NavListViewScrollListener;
import com.bruce.stickynavigationbar.view.StickNavHostSubject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.github.lzyzsd.randomcolor.RandomColor;
import com.jiang.android.indicatordialog.IndicatorBuilder;
import com.jiang.android.indicatordialog.IndicatorDialog;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.wc.widget.dialog.IosDialog;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.adapter.BannerVPAdapter;
import cn.talianshe.android.bean.ActivityChatListData;
import cn.talianshe.android.bean.ActivityDetailData;
import cn.talianshe.android.bean.ActivityInteractionData;
import cn.talianshe.android.bean.ActivityMemberListData;
import cn.talianshe.android.bean.ActivityPhotoListData;
import cn.talianshe.android.bean.ActivityScoreData;
import cn.talianshe.android.bean.AssociationActivityInfo;
import cn.talianshe.android.bean.Marquee;
import cn.talianshe.android.bean.NoticeBean;
import cn.talianshe.android.bean.QRCodeType;
import cn.talianshe.android.bean.SignTypeData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.UploadData;
import cn.talianshe.android.bean.VoteResultData;
import cn.talianshe.android.eventbus.ActivityInteractionEvent;
import cn.talianshe.android.eventbus.SignManageSettingFinishEvent;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.MultipartUtil;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.net.service.UploadLoadApiService;
import cn.talianshe.android.utils.DeleteFileUtil;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.utils.PhotoCropUtil;
import cn.talianshe.android.utils.StringUtils;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.utils.NameUtil;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.widget.CustomStarBar;
import cn.talianshe.android.widget.MarqueeTextView;
import cn.talianshe.android.widget.MyForwardDialog;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyScoreDialog;
import cn.talianshe.android.widget.MyStickyNavHost;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.RecyclerViewItemClickListener;
import cn.talianshe.android.widget.ScaleImageView;
import cn.talianshe.android.widget.ScrollViewPager;
import cn.talianshe.android.widget.UnderlineBtn;
import okhttp3.MediaType;
import okhttp3.MultipartBody;

/**
 * @author zcm
 * @ClassName: ActivityDetailActivity
 * @Description: 活动详情页
 * @date 2017/11/20 10:45
 */
public class ActivityDetailActivity extends BaseActivity implements MyStickyNavHost.TabItemClickListener {

    private static final int NAV_LENGTH = 4;
    private static final String EXTRA_ACTIVITY_ID = "extra_activity_id";
    @BindView(R.id.tv_follow)
    TextView tvFollow;
    @BindView(R.id.tv_multi)
    TextView tvMulti;
    @BindView(R.id.iv_like)
    ImageView ivLike;
    @BindView(R.id.ll_multi)
    LinearLayout llMulti;
    @BindView(R.id.et_content)
    EditText etContent;
    @BindView(R.id.ll_chat)
    LinearLayout llChat;
    private int STICKY_POSITION_IN_HEADER;

    @BindView(R.id.mSwipe_layout)
    SwipeRefreshLayout mSwipeLayout;
    @BindView(R.id.list_view)
    ListView mListView;
    @BindView(R.id.sticky_nav_layout)
    MyStickyNavHost stickyNavHostRoot;//根布局中的导航栏，表现为ListView上滑吸附在顶部
    MyStickyNavHost stickyNavHostHead;//添加在ListView的headerView中的导航栏

    private StickNavHostSubject stickNavHostSubject;//观察者，用于管理两个导航栏
    private SparseArray<NavBean> mNavs;             //导航栏的多个tab数据
    private NavListViewScrollListener scrollListener;//给ListView设置的滑动事件，里面处理了导航栏的显示与隐藏

    private String activityId;
    private MyActivityChatAdapter chatAdapter;
    private MyActivityInfodapter infoAdapter;
    private MyActivityMemberAdapter memberAdapter;
    private MyActivityWonderfulMomentAdapter photoAdapter;
    private MyChatAdapter myChatAdapter;
    private IndicatorDialog indicatorDialog;
    private RecyclerView rvChats;
    private MyWonderfulMomentAdapter woderfulMomentAdapter;


    public static Intent getActivityDetailIntent(Context context, String activityId) {
        Intent intent = new Intent(context, ActivityDetailActivity.class);
        intent.putExtra(EXTRA_ACTIVITY_ID, activityId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_detail);
        ButterKnife.bind(this);
        mSwipeLayout.setColorSchemeResources(R.color.theme_color);
        setSwipeRerefreshEnable(mSwipeLayout, true);
        initData();
        initView();

        initNavsView();//初始化导航栏view
        mSwipeLayout.setRefreshing(true);
        requestData();
    }

    private void initData() {
        activityId = getIntent().getStringExtra(EXTRA_ACTIVITY_ID);
        mSwipeLayout.setColorSchemeResources(R.color.theme_color);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO: 2017/12/16 清除数据
                chatListInfo = null;
                photoListInfo = null;
                memberListInfo = null;
                interactionInfo = null;
                requestData();
            }
        });
    }

    private ActivityDetailData.ActivityDetailInfo detailInfo;
    private ActivityInteractionData.ActivityInteractionInfo interactionInfo;

    //获取活动详情
    private void requestData() {
        HttpSubscriber httpSubscriber = new HttpSubscriber<ActivityDetailData>(this) {

            @Override
            public void onSuccess(ActivityDetailData data) {
                detailInfo = data.result;
                if (GlobalParams.getCurrentTimeStamp() > detailInfo.endtime) {
                    //说明当前时间是活动结束时间不需要获取互动配置
                    mSwipeLayout.setRefreshing(false);
                    setSwipeRerefreshEnable(mSwipeLayout, true);
                    initDefaultSelectedNav();
                    fillHeadData();
                } else {
                    getActivityInteraction();
//                    if(GlobalParams.TIME_STAMP > (detailInfo.starttime - detailInfo.signtime*60*1000)&&)
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                mSwipeLayout.setRefreshing(false);
                setSwipeRerefreshEnable(mSwipeLayout, true);
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityDetail(activityId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    //获取活动配置详情
    private void getActivityInteraction() {
        HttpSubscriber httpSubscriber = new HttpSubscriber<ActivityInteractionData>(this) {

            @Override
            public void onSuccess(ActivityInteractionData data) {
                interactionInfo = data.result;
                if (mSwipeLayout.isRefreshing()) {
                    initDefaultSelectedNav();
                    fillHeadData();
                }
                MyProgressDialog.dismiss();
                startInteractionService();
//                startService(ActivityInteractionService.getActivityInteractionService(ActivityDetailActivity.this, interactionInfo, detailInfo));
                mSwipeLayout.setRefreshing(false);
                setSwipeRerefreshEnable(mSwipeLayout, true);
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                if (mSwipeLayout.isRefreshing()) {
                    initDefaultSelectedNav();
                    fillHeadData();
                }
                mSwipeLayout.setRefreshing(false);
                setSwipeRerefreshEnable(mSwipeLayout, true);
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityInteraction(activityId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void initDefaultSelectedNav() {
        onTabItemSelected(NavBean.TYPE_SECOND);//通过此方法可进行切换tab
    }

    private void initNavsView() {
        initNavsData();
        stickyNavHostRoot.setTabItemClickListener(this);//设置点击回调
        stickyNavHostHead.setTabItemClickListener(this);//设置点击回调
        stickyNavHostRoot.setShowTopLine(false);

        stickNavHostSubject = new StickNavHostSubject();
        stickNavHostSubject.attachObserver(stickyNavHostRoot);//观察者模式
        stickNavHostSubject.attachObserver(stickyNavHostHead);

        NavBean[] sortedNavs = new NavBean[mNavs.size()];//指定导航栏的排列顺序
        sortedNavs[0] = mNavs.get(NavBean.TYPE_FIRST);
        sortedNavs[1] = mNavs.get(NavBean.TYPE_SECOND);
        sortedNavs[2] = mNavs.get(NavBean.TYPE_THIRD);
        sortedNavs[3] = mNavs.get(NavBean.TYPE_FOURTH);
        stickNavHostSubject.initTabData(sortedNavs);//四个tab添加到固定布局后可以做初始化

        scrollListener = new NavListViewScrollListener(stickyNavHostRoot, stickyNavHostHead);
        mListView.setOnScrollListener(scrollListener);//为listView设置滑动监听，内部处理了吸附view的显示与隐藏
    }

    protected void initNavsData() {
        mNavs = new SparseArray<>(NAV_LENGTH);
        chatAdapter = new MyActivityChatAdapter();
        mNavs.put(NavBean.TYPE_FIRST, new NavBean(NavBean.TYPE_FIRST, chatAdapter));
        infoAdapter = new MyActivityInfodapter();
        mNavs.put(NavBean.TYPE_SECOND, new NavBean(NavBean.TYPE_SECOND, infoAdapter));
        memberAdapter = new MyActivityMemberAdapter();
        mNavs.put(NavBean.TYPE_THIRD, new NavBean(NavBean.TYPE_THIRD, memberAdapter));
        photoAdapter = new MyActivityWonderfulMomentAdapter();
        mNavs.put(NavBean.TYPE_FOURTH, new NavBean(NavBean.TYPE_FOURTH, photoAdapter));
    }


    private void initView() {
        setTitle(R.string.activity_detail);
        //右侧分享按钮逻辑
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setBackgroundResource(R.mipmap.share_dot);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示分享弹框
                showShareDialog();
            }
        });
        stickyNavHostRoot.setVisibility(View.INVISIBLE);

        View headView = initHeadView();
        mListView.addHeaderView(headView);

        stickyNavHostHead = (MyStickyNavHost) LayoutInflater.from(this).inflate(R.layout.item_sticky_tab, null);
        stickyNavHostHead.setVisibility(View.VISIBLE);
        mListView.addHeaderView(stickyNavHostHead);
        STICKY_POSITION_IN_HEADER = mListView.getHeaderViewsCount();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (NavBean.TYPE_CURRENT) {
                    case NavBean.TYPE_FIRST:
                        break;
                    case NavBean.TYPE_SECOND:
                        break;
                    case NavBean.TYPE_THIRD:
                        break;
                    case NavBean.TYPE_FOURTH:
                        startActivity(new Intent(ActivityDetailActivity.this, MemberInfoActivity.class));
                        break;
                }
            }
        });

//        etContent.setFilters(StringUtils.getEmojiFilters());
    }


    /**
     * MANAGER_SHARE 管理者，活动未结束之前的分享
     */
    public enum SHARE_MODE {
        SHARE_ONLY, SHARE_MOMENT_SIGNMANAGE, SHARE_SIGNOUT, SHARE_REGISTCANCEL;
    }

    private class ShareData {
        public SHARE_MODE mode;
        public List<String> contents = new ArrayList<>();
        public List<Integer> contentIconIds = new ArrayList<>();

        public ShareData(SHARE_MODE mode) {
            this.mode = mode;
            fillData();
        }

        /*<integer-array name="activity_manager_icons">
                <item>@mipmap/dialog_share</item>
                <item>@mipmap/dialog_publish_moment</item>
                <item>@mipmap/dialog_sign_manage</item>
                <item>@mipmap/dialog_exit</item>
                <item>@mipmap/dialog_close_activity</item>
            </integer-array>
            <string-array name="activity_manager_strs">
                <item>@string/activity_share</item>
                <item>@string/activity_publish_moment</item>
                <item>@string/activity_sign_manage</item>
                <item>@string/activity_sign_exit</item>
                <item>@string/activity_exit</item>
            </string-array>*/
        private void fillData() {
            contents.clear();
            contentIconIds.clear();
            switch (mode) {
                case SHARE_ONLY:
//                    contents.add(getResources().getString(R.string.activity_share));
//                    contents.add(getResources().getString(R.string.activity_publish_moment));
//                    contents.add(getResources().getString(R.string.activity_sign_manage));
//                    contents.add(getResources().getString(R.string.activity_sign_exit));
//                    contents.add(getResources().getString(R.string.activity_exit));
//                    contentIconIds.add(R.mipmap.dialog_share);
//                    contentIconIds.add(R.mipmap.dialog_publish_moment);
//                    contentIconIds.add(R.mipmap.dialog_sign_manage);
//                    contentIconIds.add(R.mipmap.dialog_exit);
//                    contentIconIds.add(R.mipmap.dialog_close_activity);
                    contents.add(getResources().getString(R.string.activity_share));
                    contentIconIds.add(R.mipmap.dialog_share);

                    break;
                case SHARE_MOMENT_SIGNMANAGE:
                    contents.add(getResources().getString(R.string.activity_share));
                    contents.add(getResources().getString(R.string.activity_publish_moment));
                    contents.add(getResources().getString(R.string.activity_sign_manage));
                    contentIconIds.add(R.mipmap.dialog_share);
                    contentIconIds.add(R.mipmap.dialog_publish_moment);
                    contentIconIds.add(R.mipmap.dialog_sign_manage);
                    break;
                case SHARE_SIGNOUT:
                    contents.add(getResources().getString(R.string.activity_share));
                    contents.add(getResources().getString(R.string.activity_sign_exit));
                    contentIconIds.add(R.mipmap.dialog_share);
                    contentIconIds.add(R.mipmap.dialog_exit);


                    break;
                case SHARE_REGISTCANCEL:
                    contents.add(getResources().getString(R.string.activity_share));
                    contents.add(getResources().getString(R.string.activity_exit));
                    contentIconIds.add(R.mipmap.dialog_share);
                    contentIconIds.add(R.mipmap.dialog_close_activity);

                    break;
            }
        }
    }

    private void showShareDialog() {
        ShareData shareData;
        long currentTime = GlobalParams.getCurrentTimeStamp();
        if (detailInfo.isCan == 1) {
            //如果是管理员
            if (currentTime < detailInfo.endtime) {
                shareData = new ShareData(SHARE_MODE.SHARE_MOMENT_SIGNMANAGE);
            } else {
                if (detailInfo.activityState >= 3 || detailInfo.activityState == 1) {
                    shareData = new ShareData(SHARE_MODE.SHARE_ONLY);
                } else {
                    shareData = new ShareData(SHARE_MODE.SHARE_SIGNOUT);
                }
            }
        } else {
            if (detailInfo.activityState == 0) {
                //未报名用户
                shareData = new ShareData(SHARE_MODE.SHARE_ONLY);

            } else {
                //参加活动用户

                if (currentTime < detailInfo.registEndtime) {
                    //报名时间截止前,可取消报名
                    shareData = new ShareData(SHARE_MODE.SHARE_REGISTCANCEL);
                } else {
                    if (detailInfo.activityState != 2) {
                        //但是用户未签到，不显示签退
                        shareData = new ShareData(SHARE_MODE.SHARE_ONLY);
                    } else {
                        shareData = new ShareData(SHARE_MODE.SHARE_SIGNOUT);
                    }
                }

//
//                    if (currentTime > detailInfo.starttime && currentTime < detailInfo.endtime) {
//                    //活动进行时
//                    if (detailInfo.activityState != 2) {
//                        //但是用户未签到，不显示签退
//                        shareData = new ShareData(SHARE_MODE.SHARE_ONLY);
//                    } else {
//                        shareData = new ShareData(SHARE_MODE.SHARE_SIGNOUT);
//                    }
//                } else {
//                    if (detailInfo.activityState != 2) {
//                        //但是用户未签到，不显示签退
//                        shareData = new ShareData(SHARE_MODE.SHARE_ONLY);
//                    } else {
//                        shareData = new ShareData(SHARE_MODE.SHARE_SIGNOUT);
//                    }
//                    shareData = new ShareData(SHARE_MODE.SHARE_ONLY);
//                }
            }
        }
        RecyclerView.Adapter adapter = new ShareAdapter(shareData);
        //.arrowDrawable(BaseDrawable)        //custom arrow style if you needed
        indicatorDialog = new IndicatorBuilder(this)
                .width(DensityUtils.dipTopx(this, 110))
                .height(-1)
                .ArrowDirection(IndicatorBuilder.TOP)
                .bgColor(Color.parseColor("#66000000"))
                .dimEnabled(false)
                .gravity(IndicatorBuilder.GRAVITY_RIGHT)
                //.arrowDrawable(BaseDrawable)        //custom arrow style if you needed
                .radius(8)
                .ArrowRectage(0.8f)
                .layoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
                .adapter(adapter).create();
        indicatorDialog.setCanceledOnTouchOutside(true);
        indicatorDialog.show(btnRight);
    }

    private class ShareAdapter extends RecyclerView.Adapter {
        private ShareData shareData;

        public ShareAdapter(ShareData shareData) {
            this.shareData = shareData;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_share_view, parent, false);
//            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messge_center_list,parent,false);
//            TextView itemView = new TextView(parent.getContext());
            ShareViewHolder shareViewHolder = new ShareViewHolder(itemView, shareData);
            return shareViewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ShareViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return shareData.contents.size();
        }
    }


    public class ShareViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_share)
        TextView tvContent;
        @BindView(R.id.view_divider)
        View viewDivider;
        private ShareData shareData;

        public ShareViewHolder(View itemView, ShareData shareData) {
            super(itemView);
            this.shareData = shareData;
            ButterKnife.bind(this, itemView);
        }

        public void bindData(final int position) {
            tvContent.setText(shareData.contents.get(position));
            tvContent.setCompoundDrawablesWithIntrinsicBounds(shareData.contentIconIds.get(position), 0, 0, 0);
            viewDivider.setVisibility(position == shareData.contents.size() - 1 ? View.GONE : View.VISIBLE);
            itemView.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    indicatorDialog.dismiss();
                    switch (position) {
                        case 0:
                            //分享活动
                            MyForwardDialog dialog = new MyForwardDialog(ActivityDetailActivity.this, false).builder();
                            dialog.setResultListener(new MyForwardDialog.ResultListener() {
                                @Override
                                public void onResult(MyForwardDialog.ForwardType type) {
                                    UMShareListener umShareListener = new UMShareListener() {
                                        @Override
                                        public void onStart(SHARE_MEDIA share_media) {
                                            System.out.println("开始分享");
                                        }

                                        @Override
                                        public void onResult(SHARE_MEDIA share_media) {
                                            MyToast.show(R.string.share_success, ActivityDetailActivity.this);
                                        }

                                        @Override
                                        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                                            System.out.println("分享失败");
                                            MyToast.show(R.string.share_failed, ActivityDetailActivity.this);
                                            throwable.printStackTrace();
                                        }

                                        @Override
                                        public void onCancel(SHARE_MEDIA share_media) {
                                            System.out.println("分享取消");
//                                    MyToast.show(R.string.share_cancel,ActivityDetailActivity.this);

                                        }
                                    };
                                    UMWeb web;
                                    long currentTime = GlobalParams.getCurrentTimeStamp();
                                    if (currentTime > detailInfo.starttime && currentTime < detailInfo.endtime) {

                                        web = new UMWeb(TLSUrl.Forward.activityOngoingForwardUrl + detailInfo.id);
                                    } else {
                                        web = new UMWeb(TLSUrl.Forward.activityUnliveForwardUrl + detailInfo.id);
                                    }
                                    web.setTitle(detailInfo.activityName);//标题
                                    UMImage umImage;
                                    if (TextUtils.isEmpty(detailInfo.associationLogo)) {
                                        umImage = new UMImage(ActivityDetailActivity.this, R.mipmap.tls_logo);

                                    } else {
                                        umImage = new UMImage(ActivityDetailActivity.this, TLSUrl.BASE_URL + detailInfo.associationLogo);
                                    }
                                    umImage.compressFormat = Bitmap.CompressFormat.PNG;
                                    web.setThumb(umImage);  //缩略图
                                    web.setDescription(detailInfo.desc);//描述
                                    if (type == MyForwardDialog.ForwardType.TYPE_QZONG) {
                                        //QQ空间分享
                                        new ShareAction(ActivityDetailActivity.this)
                                                .setPlatform(SHARE_MEDIA.QZONE)//传入平台
                                                .withText("hello")//分享内容
                                                .withMedia(web)
                                                .setCallback(umShareListener)//回调监听器
                                                .share();
                                    } else if (type == MyForwardDialog.ForwardType.TYPE_WECHAT_MOMENT) {
                                        //微信朋友圈分享
                                        new ShareAction(ActivityDetailActivity.this)
                                                .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)//传入平台
                                                .withText("hello")//分享内容
                                                .withMedia(web)
                                                .setCallback(umShareListener)//回调监听器
                                                .share();
                                    }
                                }
                            });
                            dialog.show();
                            break;
                        case 1:
                            switch (shareData.mode) {
                                case SHARE_MOMENT_SIGNMANAGE:
                                    //发布动态
                                    AssociationActivityInfo info = new AssociationActivityInfo();
                                    info.activityId = activityId;
                                    info.activityName = detailInfo.activityName;
                                    info.associationName = detailInfo.associationName;
                                    info.associationId = detailInfo.associationId;
                                    startActivity(PostMomentActivity.getOfficialMomentIntent(v.getContext(), info));
                                    break;
                                case SHARE_SIGNOUT:
                                    //活动签退
                                    MyProgressDialog.show(ActivityDetailActivity.this, false);
                                    HttpSubscriber signOutSubscriber = new HttpSubscriber<StringData>(ActivityDetailActivity.this) {
                                        @Override
                                        public void onSuccess(StringData baseBean) {
                                            MyProgressDialog.dismiss();
                                            detailInfo.activityState = 3;
                                            setBottomViewState();
                                            MyToast.show(R.string.sign_out_success, ActivityDetailActivity.this);
                                        }

                                        @Override
                                        public void onError(String msg) {
                                            MyProgressDialog.dismiss();
                                            super.onError(msg);
                                        }
                                    };
                                    RequestEngine.getInstance().getServer(ActivityApiService.class).modifyActivity(activityId, "3", null, null, null).compose(RxSchedulersHelper.io_main()).subscribe(signOutSubscriber);

                                    break;
                                case SHARE_REGISTCANCEL:
                                    //取消报名
                                    //活动签退
                                    MyProgressDialog.show(ActivityDetailActivity.this, false);
                                    HttpSubscriber registCancelSubscriber = new HttpSubscriber<StringData>(ActivityDetailActivity.this) {
                                        @Override
                                        public void onSuccess(StringData baseBean) {
                                            MyProgressDialog.dismiss();
                                            detailInfo.activityState = 0;
                                            setBottomViewState();
                                            MyToast.show(R.string.cancel_regist_success, ActivityDetailActivity.this);
                                        }

                                        @Override
                                        public void onError(String msg) {
                                            MyProgressDialog.dismiss();
                                            super.onError(msg);
                                        }
                                    };
                                    RequestEngine.getInstance().getServer(ActivityApiService.class).modifyActivity(activityId, "0", null, null, null).compose(RxSchedulersHelper.io_main()).subscribe(registCancelSubscriber);

                                    break;
                            }
                            break;
                        case 2:
                            //签到管理,如果签到时间未开始，可以添加、修改签到配置
                            if (GlobalParams.getCurrentTimeStamp() < detailInfo.starttime - detailInfo.signtime * 60000) {
                                startActivity(SignModeActivity.getSignModeIntent(ActivityDetailActivity.this, detailInfo.isSignConfigure, activityId));
                            } else {
                                //开始扫码
                                if (detailInfo.isSignConfigure) {
                                    QRScanActivity.ScanType scanType = QRScanActivity.ScanType.ACTIVITY_SIGN;
                                    scanType.scanId = activityId;
                                    startActivity(QRScanActivity.getQRScanIntent(ActivityDetailActivity.this, scanType));
                                } else {
                                    startActivity(SignModeActivity.getSignModeIntent(ActivityDetailActivity.this, detailInfo.isSignConfigure, activityId));
                                }
                            }
                            break;
                    }
                }
            });
        }
    }

    private boolean isWallOpen = true;
    private static final int SHOW_SETTING_DIALOG = 1;
    private static final int GET_VOTE_RESULT = 2;
    private static final int START_SIGN = 3;
    private static final int ADD_VOTE_TIP_VIEW = 4;
    private static final int VOTE_END_TIME = 5;
    private static final int VOTE_COUNT_DOWN = 6;
    private static final int ACTIVITY_ON_GOING = 7;
    private static final int ACTIVITY_NOT_ON_GOING = 8;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_SETTING_DIALOG:
                    showSettingSignTypeDialog();
                    break;
                case GET_VOTE_RESULT:
                    getVoteResult((ActivityInteractionEvent) msg.obj);
                    break;
                case ADD_VOTE_TIP_VIEW:
                    addVoteTipView((ActivityInteractionEvent) msg.obj);
                    break;
                case VOTE_END_TIME:
                    llMiddleContent.removeAllViews();
                    break;
                case VOTE_COUNT_DOWN:
                    addVoteCountDownView((ActivityInteractionEvent) msg.obj);
                    break;
                case START_SIGN:
                    startSign();
                    break;
                case ACTIVITY_ON_GOING:
                    setActivityViewState();
                    break;
                case ACTIVITY_NOT_ON_GOING:
                    setActivityViewState();
                    break;
            }
        }
    };
    private int count = 0;
//    private ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
//        @Override
//        public void onGlobalLayout() {
//            System.out.println("添加了一个view或者移除了一个view,触发点...  " + count);
//            int childCount = llMiddleContent.getChildCount();
//            System.out.println("childCount: " + childCount);
//            int actualHeight = 0;
//            for (int i = 0; i < childCount; i++) {
//                actualHeight += llMiddleContent.getChildAt(i).getHeight();
//            }
//            System.out.println("actualHeight: " + actualHeight);
//            if (actualHeight == llMiddleContent.getHeight()) {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        llMiddleContent.removeViewAt(0);
//
//                    }
//                });
////                llMiddleContent.requestLayout();
//                return;
//            }
//            llMiddleContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//        }
//    };

    //----------------头布局相关----------------
    private void fillHeadData() {
        ActivityDetailData.ActivityDetailInfo info = this.detailInfo;
        tvActivityName.setText(info.activityName);
        tvActivityAddress.setText(info.activityPlace);
        tvActivityTime.setText(TimeUtil.getActivityTime(info.starttime, info.endtime));
        tvActivityNum.setText(info.participantNum + "/" + info.maxNum);
        tvAssociationCollege.setText(info.schoolName);
        tvAssociationName.setText(info.associationName);
        tvActivityNameMiddle.setText(info.activityName);
        starBar.setStarMark(info.score);
        if (info.noticeList != null && info.noticeList.size() > 0) {
            List<Marquee> list = new ArrayList<>();
            for (NoticeBean notice : info.noticeList) {
                Marquee marquee = new Marquee();
                marquee.setTitle(notice.title);
                list.add(marquee);
            }
            tvMarquee.startWithList(list);
            tvMarquee.setOnItemClickListener(new MarqueeTextView.OnItemClickListener() {
                @Override
                public void onItemClick(int position, View textView) {
                    startActivity(TlsWebViewActivity.getNoticeIntent(ActivityDetailActivity.this, detailInfo.noticeList.get(position)));

                }
            });
        }
        if (info.bannerList != null && info.bannerList.size() > 0) {
            loadBannerData(info.bannerList);
        }
        setActivityViewState();
    }

    private void setActivityViewState() {
        setTags();
        long currentTime = GlobalParams.getCurrentTimeStamp();
        if (currentTime > detailInfo.starttime && currentTime < detailInfo.endtime) {
            llTop.setVisibility(View.GONE);
            rlLive.setVisibility(View.VISIBLE);
            if (interactionInfo != null && !TextUtils.isEmpty(interactionInfo.sceneInteractivity.curScreenPath)) {
                RequestOptions options = new RequestOptions().error(R.mipmap.ic_img_thumbnail).placeholder(R.mipmap.ic_img_thumbnail);
                Glide.with(this).load(TLSUrl.BASE_URL + interactionInfo.sceneInteractivity.curScreenPath).apply(options).thumbnail(0.1f).into(ivScreen);
            }

        } else {
            llTop.setVisibility(View.VISIBLE);
            rlLive.setVisibility(View.GONE);
        }
        setBottomViewState();
    }

    private void fillOnGoingView() {
        long currentTime = GlobalParams.getCurrentTimeStamp();
        if (currentTime > detailInfo.starttime && currentTime < detailInfo.endtime) {
            if (interactionInfo != null && interactionInfo.sceneInteractivity.voteList != null && interactionInfo.sceneInteractivity.voteList.size() > 0) {
                for (ActivityInteractionData.VoteListBean voteListBean : interactionInfo.sceneInteractivity.voteList) {
                    if (currentTime > voteListBean.beginDate && currentTime < voteListBean.endDate) {
                        // TODO: 2017/12/19 判断是否已投票，已投票显示投票结果 ，否则显示投票界面
                    }

                }
            }

        }
    }

    private Timer timer;

    private void setBottomViewState() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        ivLike.setImageResource(detailInfo.isLike ? R.mipmap.up : R.mipmap.no_up);
        tvFollow.setText(detailInfo.isAttention == 1 ? R.string.has_follow : R.string.follow);
        long currentTime = GlobalParams.getCurrentTimeStamp();
        if (detailInfo.activityState == 0) {
            //未报名
            if (currentTime > detailInfo.registEndtime) {
                //已超过报名时间
                tvMulti.setClickable(false);
                tvMulti.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
                if (currentTime > detailInfo.endtime) {
                    tvMulti.setText(R.string.activity_has_end);
                } else {
                    tvMulti.setText(R.string.regist_time_end);
                }
            } else {
                if (currentTime < detailInfo.registStarttime) {
                    tvMulti.setClickable(false);
                    tvMulti.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
                } else {

                    tvMulti.setClickable(true);
                    tvMulti.setBackgroundColor(ContextCompat.getColor(this, R.color.theme_color));
                }
                tvMulti.setText(R.string.regist);
            }
        } else if (detailInfo.activityState == 1) {
            //已报名
            if (currentTime < detailInfo.endtime) {
                if (currentTime < detailInfo.starttime - detailInfo.signtime * 60 * 1000) {
                    //显示倒计时
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            //一分钟执行一次
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tvMulti.setClickable(false);
                                    tvMulti.setBackgroundColor(ContextCompat.getColor(ActivityDetailActivity.this, R.color.light_gray));
                                    tvMulti.setText(getString(R.string.has_regist_holder, TimeUtil.getActivityLeftTime(GlobalParams.getCurrentTimeStamp(), detailInfo.starttime)));
                                }
                            });
                        }
                    };
                    timer = new Timer();
                    timer.schedule(task, 0, 1000);

                } else {
                    if (detailInfo.isSignConfigure) {
                        tvMulti.setClickable(true);
                        tvMulti.setBackgroundColor(ContextCompat.getColor(this, R.color.theme_color));
                        tvMulti.setText(R.string.sign);
                    } else {
                        tvMulti.setClickable(false);
                        tvMulti.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
                        tvMulti.setText(R.string.sign);
                    }
                }
            } else {
                tvMulti.setClickable(false);
                tvMulti.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
                tvMulti.setText(R.string.activity_has_end);
            }
        } else if (detailInfo.activityState == 2) {
            //已签到
            tvMulti.setClickable(false);
            tvMulti.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
            tvMulti.setText(R.string.has_sign);
        } else if (detailInfo.activityState == 3) {
            //已签退
            tvMulti.setClickable(true);
            tvMulti.setBackgroundColor(ContextCompat.getColor(this, R.color.theme_color));
            tvMulti.setText(R.string.comment);
        } else if (detailInfo.activityState == 4) {
            //已评价
            tvMulti.setClickable(true);
            tvMulti.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
            tvMulti.setText(R.string.has_comment);
        }
    }

    private void setTags() {
        llTags.removeAllViews();
        if (detailInfo.activityState == 0 || detailInfo.isCan == 1) {
            //说明未报名，什么都不显示
            llTags.setVisibility(View.GONE);
        } else {
            llTags.setVisibility(View.VISIBLE);
            switch (detailInfo.activityState) {
                case 4:
                    TextView tv4 = new TextView(this);
                    tv4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    tv4.setBackgroundResource(R.drawable.theme_round_stroke_bg);
                    tv4.setTextColor(ContextCompat.getColor(this, R.color.theme_color));
                    tv4.setTextSize(10);
                    tv4.setText(R.string.has_comment);
                    llTags.addView(tv4, 0);
                case 3:
                    TextView tv3 = new TextView(this);
                    tv3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    tv3.setBackgroundResource(R.drawable.theme_round_stroke_bg);
                    tv3.setTextColor(ContextCompat.getColor(this, R.color.theme_color));
                    tv3.setTextSize(10);
                    tv3.setText(R.string.has_sign_out);
                    llTags.addView(tv3, 0);
                case 2:
                    TextView tv2 = new TextView(this);
                    tv2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    tv2.setBackgroundResource(R.drawable.theme_round_stroke_bg);
                    tv2.setTextColor(ContextCompat.getColor(this, R.color.theme_color));
                    tv2.setTextSize(10);
                    tv2.setText(R.string.has_sign);
                    llTags.addView(tv2, 0);
                case 1:
                    TextView tv1 = new TextView(this);
                    tv1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    tv1.setBackgroundResource(R.drawable.theme_round_stroke_bg);
                    tv1.setTextColor(ContextCompat.getColor(this, R.color.theme_color));
                    tv1.setTextSize(10);
                    tv1.setText(R.string.has_regist);
                    llTags.addView(tv1, 0);
                    break;
            }
        }
    }

    /**
     * 加载banner轮播图数据
     *
     * @param bannerList
     */
    private void loadBannerData(List<ActivityDetailData.ActivityImg> bannerList) {
        List<ImageView> imageViews = new ArrayList<>();
        if (bannerList.size() > 0) {
            if (bannerList.size() > 1) {
                ActivityDetailData.ActivityImg firstBanner = bannerList.get(0);
                ImageView imageViewFirst = new ImageView(this);
                imageViewFirst.setLayoutParams(scrollVP.getLayoutParams());
                imageViewFirst.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViews.add(imageViewFirst);
                RequestOptions optionsFirst = new RequestOptions();
                optionsFirst.override(scrollVP.getLayoutParams().width, scrollVP.getLayoutParams().height);
                optionsFirst.placeholder(R.mipmap.ic_img_thumbnail_large);
                optionsFirst.error(R.mipmap.ic_img_failure_large);
                Glide.with(this).load(TLSUrl.BASE_URL + firstBanner.realpath).apply(optionsFirst).thumbnail(0.1f).into(imageViewFirst);

                for (ActivityDetailData.ActivityImg banner : bannerList) {
                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(scrollVP.getLayoutParams());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageViews.add(imageView);
                    RequestOptions options = new RequestOptions();
                    options.override(scrollVP.getLayoutParams().width, scrollVP.getLayoutParams().height);
                    options.placeholder(R.mipmap.ic_img_thumbnail_large);
                    options.error(R.mipmap.ic_img_failure_large);
                    Glide.with(this).load(TLSUrl.BASE_URL + banner.realpath).apply(options).thumbnail(0.1f).into(imageView);

                }
                ActivityDetailData.ActivityImg lastBanner = bannerList.get(bannerList.size() - 1);
                ImageView imageViewLast = new ImageView(this);
                imageViewLast.setLayoutParams(scrollVP.getLayoutParams());
                imageViewLast.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViews.add(imageViewLast);
                RequestOptions options = new RequestOptions();
                options.override(scrollVP.getLayoutParams().width, scrollVP.getLayoutParams().height);
                options.placeholder(R.mipmap.ic_img_thumbnail_large);
                options.error(R.mipmap.ic_img_failure_large);
                Glide.with(this).load(TLSUrl.BASE_URL + lastBanner.realpath).apply(options).thumbnail(0.1f).into(imageViewLast);
            } else {
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(scrollVP.getLayoutParams());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViews.add(imageView);
                RequestOptions options = new RequestOptions();
                options.override(scrollVP.getLayoutParams().width, scrollVP.getLayoutParams().height);
                options.placeholder(R.mipmap.ic_img_thumbnail_large);
                options.error(R.mipmap.ic_img_failure_large);
                Glide.with(this).load(TLSUrl.BASE_URL + bannerList.get(0).realpath).apply(options).thumbnail(0.1f).into(imageView);

            }
        } else {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(scrollVP.getLayoutParams());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews.add(imageView);
            RequestOptions options = new RequestOptions();
            options.override(scrollVP.getLayoutParams().width, scrollVP.getLayoutParams().height);
            options.placeholder(R.mipmap.ic_img_thumbnail_large);
            options.error(R.mipmap.ic_img_failure_large);
            Glide.with(this).load(TLSUrl.BASE_URL + bannerList.get(0).realpath).apply(options).into(imageView);

        }

        scrollVP.setCanScroll(false);
        scrollVP.setAdapter(new BannerVPAdapter(imageViews));
        scrollVP.setCurrentItem(1);
        scrollVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //正确数据的大小
                int size = 3;

                if (position == 0) {
                    //切换到最后一个页面
                    scrollVP.setCurrentItem(size, false);
                } else if (position == size + 1) {
                    //切换到第一个页面
                    scrollVP.setCurrentItem(1, false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        autoLoadBannerHandler.sendEmptyMessageDelayed(0, 3000);
    }

    private Handler autoLoadBannerHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int currentItem = scrollVP.getCurrentItem();
            if (currentItem != scrollVP.getAdapter().getCount() - 1) {
                scrollVP.setCurrentItem(currentItem + 1);
            } else {
                scrollVP.setCurrentItem(0);
            }
            this.sendEmptyMessageDelayed(0, 3000);
        }
    };

    ScrollViewPager scrollVP;
    TextView tvActivityName;
    TextView tvActivityAddress;
    TextView tvActivityTime;
    TextView tvActivityNum;
    ImageView ivQrcode;
    LinearLayout llTags;
    LinearLayout llActivityState;
    LinearLayout llTop;
    MarqueeTextView tvMarquee;
    LinearLayout llNotice;
    ScaleImageView ivScreen;
    TextView tvActivityNameMiddle;
    LinearLayout llMiddleContent;
    View viewLocation;
    RelativeLayout rlLive;
    TextView tvAssociationCollege;
    TextView tvAssociationName;
    CustomStarBar starBar;

    private View initHeadView() {
        View view = LayoutInflater.from(this).inflate(R.layout.view_activity_detail_head, null);
        /*
        //上墙逻辑
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                llMiddleContent.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
                if (count == 20)
                    return;
                TextView tv = new TextView(ActivityDetailActivity.this);
                if (count < 5) {
                    tv.setText("测试我是一个很长的数据测试我是一个很长的数据测试我是一个很长的数据测试我是一个很长的数据测试我是一个很长的数据" + count);

                } else if (count > 12 && count < 17) {

                    tv.setText("测试我是一个很长的数据测试我是一个很长的数据测试我是一个很长的数据测试我是一个很长的数据测试我是一个很长的数据" + count);
                } else {
                    tv.setText("测试" + count);

                }
                llMiddleContent.addView(tv);
                count++;
                handler.postDelayed(this, new Random().nextInt(3000));
            }
        }, 500);*/

        /*
        handler.post(new Runnable() {
            @Override
            public void run() {
                llMiddleContent.removeAllViews();
                RecyclerView rv = new RecyclerView(ActivityDetailActivity.this);
                llMiddleContent.addView(rv);
                rv.setLayoutManager(new LinearLayoutManager(ActivityDetailActivity.this));
                rv.setAdapter(new MyChatAdapter());
            }
        });*/
        scrollVP = view.findViewById(R.id.scrollVP);
        tvActivityName = view.findViewById(R.id.tv_activity_name);
        tvActivityAddress = view.findViewById(R.id.tv_activity_address);
        tvActivityTime = view.findViewById(R.id.tv_activity_time);
        tvActivityNum = view.findViewById(R.id.tv_activity_num);
        ivQrcode = view.findViewById(R.id.iv_qrcode);
        llTags = view.findViewById(R.id.ll_tags);
        llActivityState = view.findViewById(R.id.ll_activity_state);
        llTop = view.findViewById(R.id.ll_top);
        tvMarquee = view.findViewById(R.id.tv_marquee);
        llNotice = view.findViewById(R.id.ll_notice);
        ivScreen = view.findViewById(R.id.iv_screen);
        tvActivityNameMiddle = view.findViewById(R.id.tv_activity_name_middle);
        llMiddleContent = view.findViewById(R.id.ll_middle_content);
        viewLocation = view.findViewById(R.id.view_location);
        rlLive = view.findViewById(R.id.rl_live);
        tvAssociationCollege = view.findViewById(R.id.tv_association_college);
        tvAssociationName = view.findViewById(R.id.tv_association_name);
        starBar = view.findViewById(R.id.starBar);
        llNotice.setOnClickListener(this);
        ivQrcode.setOnClickListener(this);
        tvMarquee.setOnClickListener(this);
//        view.findViewById(R.id.iv_qrcode).setOnClickListener(this);
        int width = getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams layoutParams = scrollVP.getLayoutParams();
        if (layoutParams == null)
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, layoutParams.WRAP_CONTENT);
        layoutParams.width = width;
        layoutParams.height = width * 2 / 5;
        scrollVP.setLayoutParams(layoutParams);
        return view;
    }
    //----------------头布局相关----------------


    @Override
    public void onTabItemSelected(@NavBean.TYPE int type) {
        NavBean currNav = mNavs.get(type);
        stickNavHostSubject.setSelectedType(type);//事件分发给注册者，注册者进行相应的变化
        if (currNav.type == NavBean.TYPE_CURRENT)//等于当前选中的tab，可以屏蔽掉
            return;
        NavBean.TYPE_CURRENT = currNav.type;
        scrollListener.setNav(currNav);
        setItemChecked(currNav.type);
        requestTabData(currNav.type);
        mListView.setAdapter(currNav.adapter);
        if (stickyNavHostRoot.getVisibility() == View.VISIBLE) {//吸附在顶部的rootView正在展示
            if (currNav.getFirstVisibleItem() < STICKY_POSITION_IN_HEADER)
                mListView.setSelectionFromTop(STICKY_POSITION_IN_HEADER, stickyNavHostRoot.getHeight() - 2);
            else
                mListView.setSelectionFromTop(currNav.getFirstVisibleItem(), currNav.getTopDistance());
        } else {//吸附在顶部的rootView没有展示，说明在切换导航栏的时候是不需要进行滑动的，保持上次的位置即可
            if (currNav.getFirstVisibleItem() < STICKY_POSITION_IN_HEADER)
                mListView.setSelectionFromTop(NavBean.firstVisibleItemUniversal, NavBean.topDistanceUniversal);
            else
                mListView.setSelectionFromTop(currNav.getFirstVisibleItem(), currNav.getTopDistance());

        }
    }

    private void setItemChecked(int position) {
        for (int i = 0; i < tabItems1.length; i++) {
            UnderlineBtn btnTab = tabItems1[i].getView().findViewById(R.id.btn_tab);
            btnTab.setChecked(i == position);
        }
        for (int i = 0; i < tabItems2.length; i++) {
            UnderlineBtn btnTab = tabItems2[i].getView().findViewById(R.id.btn_tab);
            btnTab.setChecked(i == position);
        }
        if (position == 0) {
            //如果是未报名，则不可以聊天
            if (detailInfo.activityState == 0 || GlobalParams.getCurrentTimeStamp() > detailInfo.endtime) {
                llChat.setVisibility(View.GONE);
            } else {
                llChat.setVisibility(View.VISIBLE);
            }
            llMulti.setVisibility(View.GONE);
        } else {
            llMulti.setVisibility(View.VISIBLE);
            llChat.setVisibility(View.GONE);
        }
    }

    private MyStickyNavHost.TabItem[] tabItems1;
    private MyStickyNavHost.TabItem[] tabItems2;

    private int[] tabStrs = {R.string.come_tochat, R.string.activity_desc, R.string.activity_member, R.string.wonderful_moment};

    @Override
    public void onTabAddFinish(MyStickyNavHost.TabItem[] items) {
        if (tabItems1 == null) {
            this.tabItems1 = items;
        } else {
            this.tabItems2 = items;
        }
        for (int i = 0; i < items.length; i++) {

            UnderlineBtn btnTab = items[i].getView().findViewById(R.id.btn_tab);
            btnTab.setChecked(i == 0);
            btnTab.setText(tabStrs[i]);
        }
    }


    private Dialog voteDialog;
    private ActivityInteractionData.VoteListBean curVoteListBean;

    private void showVoteDialog(ActivityInteractionEvent event) {
        curVoteListBean = interactionInfo.sceneInteractivity.voteList.get(event.voteNoteIndex);
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.view_dialog_vote, null);
        contentView.findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (voteDialog != null) {
                    voteDialog.dismiss();
                    voteDialog = null;
                }
            }
        });
        contentView.findViewById(R.id.tv_vote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (voteDialog != null) {
                    if (selectedVoteIds.size() == 0) {
                        MyToast.show(R.string.vote_null_tip, ActivityDetailActivity.this);
                        return;
                    }
                    MyProgressDialog.show(ActivityDetailActivity.this, false);
                    HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(ActivityDetailActivity.this) {
                        @Override
                        public void onSuccess(StringData baseBean) {
                            voteDialog.dismiss();
                            voteDialog = null;
//                            myChatAdapter.notifyDataSetChanged();
                            //成功之后再次获取配置
                            getActivityInteraction();
                        }

                        @Override
                        public void onError(String msg) {
                            MyProgressDialog.dismiss();
                            super.onError(msg);
                        }
                    };
                    String selectIds = "";
                    for (String selectId : selectedVoteIds) {
                        selectIds += selectId + ",";
                    }
                    selectIds = selectIds.substring(0, selectIds.length() - 1);
                    RequestEngine.getInstance().getServer(ActivityApiService.class).voteActivity(activityId, curVoteListBean.id, selectIds).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

                }
            }
        });
        selectedVoteIds.clear();
        RecyclerView rvVotes = contentView.findViewById(R.id.rv_votes);
        rvVotes.setLayoutManager(new LinearLayoutManager(this));
        rvVotes.setAdapter(new VoteAdapter(curVoteListBean));
        rvVotes.addOnItemTouchListener(new RecyclerViewItemClickListener(rvVotes) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                System.out.println("条目点击了");
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {

            }
        });
        voteDialog = new Dialog(this, R.style.DialogCenterStyle);
        voteDialog.setContentView(contentView);
        setDialogFullScreenWidth(voteDialog);
        voteDialog.show();
    }

    private class VoteAdapter extends RecyclerView.Adapter {
        private ActivityInteractionData.VoteListBean voteListBean;

        public VoteAdapter(ActivityInteractionData.VoteListBean voteListBean) {
            this.voteListBean = voteListBean;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            /*<CheckBox
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="@null"
            android:button="@drawable/vote_gou_selector"
            android:text="大排档大排档大排档大排档大排档"
            android:gravity="center_vertical"
            android:textColor="@color/dark_gray"
            android:textSize="12sp"
                    />*/
            CheckBox checkBox = new CheckBox(parent.getContext());
            checkBox.setBackground(null);
            checkBox.setButtonDrawable(R.drawable.vote_gou_selector);
            checkBox.setGravity(Gravity.CENTER_VERTICAL);
            checkBox.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.dark_gray));
            checkBox.setTextSize(12);
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = DensityUtils.dipTopx(parent.getContext(), 5);
            layoutParams.bottomMargin = DensityUtils.dipTopx(parent.getContext(), 5);
            checkBox.setLayoutParams(layoutParams);
            VoteViewHolder holder = new VoteViewHolder(checkBox);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((VoteViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            if (voteListBean != null && voteListBean.voteSelectList != null) {
                return voteListBean.voteSelectList.size();
            } else {
                return 0;
            }
        }
    }

    private List<String> selectedVoteIds = new ArrayList<>();
    private CheckBox curCb;

    public class VoteViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cb;

        public VoteViewHolder(CheckBox itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            cb = itemView;
        }

        public void bindData(int position) {
            final ActivityInteractionData.VoteResultListBean voteBean = curVoteListBean.voteSelectList.get(position);
            cb.setText(voteBean.name);
            cb.setTag(position);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (curVoteListBean.choose == 1) {
                        //多选
                        if (isChecked) {
                            selectedVoteIds.add(voteBean.id);

                        } else {
                            selectedVoteIds.remove(voteBean.id);
                        }

                    } else {
                        //单选
                        if (isChecked) {
                            if (curCb != null) {
                                curCb.setOnCheckedChangeListener(null);
                                curCb.setChecked(false);
                                int index = (int) curCb.getTag();
                                ActivityInteractionData.VoteResultListBean voteBean = curVoteListBean.voteSelectList.get(index);
                                selectedVoteIds.remove(voteBean.id);
                                curCb.setOnCheckedChangeListener(this);
                            }
                            curCb = (CheckBox) buttonView;
                            int index = (int) curCb.getTag();
                            ActivityInteractionData.VoteResultListBean voteBean = curVoteListBean.voteSelectList.get(index);
                            selectedVoteIds.add(voteBean.id);
                        } else {
                            curCb = (CheckBox) buttonView;
                            curCb.setOnCheckedChangeListener(null);
                            curCb.setChecked(true);
                            curCb.setOnCheckedChangeListener(this);
                        }

                    }
                }
            });

        }
    }


    private List<Integer> randomColors = new ArrayList<>();

    private void initRandomColors(int itemCount) {
        int start = (int) Long.parseLong("ff3232ff", 16);
        int end = (int) Long.parseLong("ffffff32", 16);
        RandomColor randomColor = new RandomColor();
        int[] ints = randomColor.randomColor(itemCount);
        Random random = new Random((int) (end - start));
        for (int i = 0; i < itemCount; i++) {
            int color;
            do {
                color = randomColor.randomColor(50, RandomColor.SaturationType.MONOCHROME, RandomColor.Luminosity.DARK);
            }
            while (randomColors.contains(color));
            randomColors.add(ints[i]);
        }
    }

    private class VoteResultAdapter extends RecyclerView.Adapter {

        private VoteResultData.VoteResultInfo resultInfo;

        public VoteResultAdapter(VoteResultData.VoteResultInfo resultInfo) {
            this.resultInfo = resultInfo;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ActivityDetailActivity.this).inflate(R.layout.item_activity_detail_head_vote_result, null);
            VoteResultViewHolder holder = new VoteResultViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((VoteResultViewHolder) holder).bindData(position, resultInfo.voteResults.get(position));
        }

        @Override
        public int getItemCount() {
            if (resultInfo != null && resultInfo.voteResults != null) {
                return resultInfo.voteResults.size();
            }
            return 0;
        }
    }

    public class VoteResultViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.pb_result)
        ProgressBar pbResult;
        @BindView(R.id.tv_vote_name)
        TextView tvVoteName;
        @BindView(R.id.tv_vote_num)
        TextView tvVoteNum;

        public VoteResultViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(int position, VoteResultData.VoteResult voteResult) {
            Drawable progressDrawable = pbResult.getProgressDrawable();
            if (progressDrawable instanceof LayerDrawable) {
                LayerDrawable layerDrawable = (LayerDrawable) progressDrawable;
//                ClipDrawable d = (ClipDrawable) layerDrawable.findDrawableByLayerId(android.R.id.progress);
//                int[] state = d.getState();
//                layerDrawable.setDrawableByLayerId(android.R.id.progress,null);
//                System.out.println(d);
            }
            setProgressBg(pbResult, voteResult, position);
            tvVoteName.setText(voteResult.name + ":");
            tvVoteNum.setText(voteResult.voteCount + "人");
        }

        private void setProgressBg(ProgressBar progressBar, VoteResultData.VoteResult voteResult, int postion) {
//            progressBar.setProgressDrawable(null);
//            Drawable colorDrawable = getResources().getDrawable(R.color.colorPrimary);
            ColorDrawable colorDrawable = new ColorDrawable(randomColors.get(postion));
            ClipDrawable clipDrawable = new ClipDrawable(colorDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
            Drawable drawable = new ColorDrawable(Color.parseColor("#DAE4E8"));
            Drawable[] layers = new Drawable[]{drawable, clipDrawable};
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            layerDrawable.setDrawableByLayerId(android.R.id.background, drawable);
//            layerDrawable.setDrawableByLayerId(android.R.id.progress,null);
            layerDrawable.setDrawableByLayerId(android.R.id.progress, clipDrawable);
            progressBar.setProgressDrawableTiled(layerDrawable);
//            progressBar.setProgressDrawable(layerDrawable);
            progressBar.setMax(voteResult.totalCount);
            progressBar.setProgress(0);
            progressBar.setProgress(voteResult.voteCount);

        }
    }

    private Dialog signNoticeDialog;
    private TextView tvSignNotice;
    private TextView tvSignImmediate;

    private void showSignNoticeDialog() {
        if (signNoticeDialog == null) {
            View contentView = LayoutInflater.from(this).inflate(
                    R.layout.view_dialog_sign_notice, null);
            tvSignNotice = contentView.findViewById(R.id.tv_sign_notice);
            signNoticeDialog = new Dialog(this, R.style.DialogCenterStyle);
            signNoticeDialog.setContentView(contentView);
            contentView.findViewById(R.id.tv_sign_immediate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signNoticeDialog.dismiss();
                    if (detailInfo.isCan == 1) {
                        //管理员签到，直接签到，签到完打开二维码扫描
                        MyProgressDialog.show(ActivityDetailActivity.this, false);
                        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(ActivityDetailActivity.this) {
                            @Override
                            public void onSuccess(StringData baseBean) {
                                MyProgressDialog.dismiss();
                                detailInfo.activityState = 2;
                                setBottomViewState();
                                Dialog qrScanDialog = new IosDialog.Builder(ActivityDetailActivity.this)
                                        .setMessage(R.string.activity_manager_qr_scan_tip).setMessageColor(ContextCompat.getColor(ActivityDetailActivity.this, R.color.gray)).setMessageSize(15)
                                        .setNegativeButtonColor(ContextCompat.getColor(ActivityDetailActivity.this, R.color.gray))
                                        .setNegativeButtonSize(16)
                                        .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                                            @Override
                                            public void onClick(IosDialog dialog, View v) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setPositiveButtonColor(ContextCompat.getColor(ActivityDetailActivity.this, R.color.theme_color))
                                        .setPositiveButtonSize(16)
                                        .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                                            @Override
                                            public void onClick(IosDialog dialog, View v) {
                                                QRScanActivity.ScanType scanType = QRScanActivity.ScanType.ACTIVITY_SIGN;
                                                scanType.scanId = activityId;
                                                startActivity(QRScanActivity.getQRScanIntent(ActivityDetailActivity.this, scanType));
                                                dialog.dismiss();
                                            }
                                        }).build();
                                qrScanDialog.show();
                            }

                            @Override
                            public void onError(String msg) {
                                MyProgressDialog.dismiss();
                                super.onError(msg);
                            }
                        };
                        RequestEngine.getInstance().getServer(ActivityApiService.class).modifyActivity(activityId, "2", null, null, GlobalParams.USER_INFO.id + "").compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

                    } else {
                        //普通活动成员签到，打开二维码
                        QRGenerateActivity.GenerateEntity entity = new QRGenerateActivity.GenerateEntity();
                        entity.generateId = GlobalParams.USER_INFO.id + "";
                        entity.generateSecondStr = activityId;
                        entity.generateAvatar = TLSUrl.BASE_URL + GlobalParams.USER_INFO.avatar;
                        entity.name = GlobalParams.USER_INFO.realname;
                        entity.nickName = GlobalParams.USER_INFO.nickname;
                        startActivity(QRGenerateActivity.getQRGenerateIntent(ActivityDetailActivity.this, QRCodeType.ACTIVITY_SIGN, entity));
                    }
                }
            });
            if (signTypeInfo != null)
                tvSignNotice.setText(signTypeInfo.remark);
            setDialogFullScreenWidth(signNoticeDialog);
        }
        signNoticeDialog.show();
    }

    private MyScoreDialog scoreDialog;

    private void showScoreDialog(int starMark, String content) {
        scoreDialog = new MyScoreDialog(this, starMark, content).builder();
        scoreDialog.setResultListener(new MyScoreDialog.EditTextResultListener() {
            @Override
            public void onResult(String result, int star) {
                MyProgressDialog.show(ActivityDetailActivity.this, false);
                HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(ActivityDetailActivity.this) {
                    @Override
                    public void onSuccess(StringData baseBean) {
                        detailInfo.activityState = 4;
                        setBottomViewState();
                        MyProgressDialog.dismiss();
                        MyToast.show(R.string.score_success, ActivityDetailActivity.this);
                    }

                    @Override
                    public void onError(String msg) {
                        MyProgressDialog.dismiss();
                        super.onError(msg);
                    }
                };
                RequestEngine.getInstance().getServer(ActivityApiService.class).modifyActivity(activityId, "4", result, star, null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

            }
        });
        scoreDialog.show();
    }

    private void showConfirmRegistDialog() {
        Dialog registDialog = new IosDialog.Builder(this)
                .setMessage(R.string.confirm_regist_activity).setMessageColor(ContextCompat.getColor(this, R.color.gray)).setMessageSize(15)
                .setNegativeButtonColor(ContextCompat.getColor(this, R.color.gray))
                .setNegativeButtonSize(16)
                .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButtonColor(ContextCompat.getColor(this, R.color.theme_color))
                .setPositiveButtonSize(16)
                .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        MyProgressDialog.show(ActivityDetailActivity.this, false);
                        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(ActivityDetailActivity.this) {
                            @Override
                            public void onSuccess(StringData baseBean) {
                                detailInfo.activityState = 1;
                                MyProgressDialog.dismiss();
                                MyToast.show(R.string.regist_success, ActivityDetailActivity.this);
                                setBottomViewState();
                            }

                            @Override
                            public void onError(String msg) {
                                MyProgressDialog.dismiss();
                                super.onError(msg);
                            }
                        };
                        RequestEngine.getInstance().getServer(ActivityApiService.class).modifyActivity(activityId, "1", null, null, null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
                        dialog.dismiss();
                    }
                }).build();
        registDialog.show();
    }

    public final static int RCODE_PICK_PICTURE = 700;
    public final static int RCODE_TAKE_PHOTO = 800;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RCODE_TAKE_PHOTO) {
                takePhotoBack();
            }
        }
    }

    private String mCompresImgPath;

    private void takePhotoBack() {
        mCompresImgPath = PhotoCropUtil.compressImage(this, mCurrentPhotoPath);
        MultipartBody.Builder builder = MultipartUtil.filesToMultipartBodyBuilder("file", new String[]{mCompresImgPath}, MediaType.parse("multipart/form-data"));
        MyProgressDialog.show(this, false);
        HttpSubscriber uploadSubscriber = new HttpSubscriber<UploadData>(this) {
            @Override
            public void onSuccess(final UploadData data) {
                final String imgId = data.result.id;
                HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(ActivityDetailActivity.this) {
                    @Override
                    public void onSuccess(StringData baseBean) {
                        DeleteFileUtil.delete(mCompresImgPath);
                        DeleteFileUtil.delete(mCurrentPhotoPath);
                        MyProgressDialog.dismiss();
                        ActivityChatListData.ActivityChatInfo info = new ActivityChatListData.ActivityChatInfo();
                        info.id = GlobalParams.USER_INFO.id;
                        info.createtime = System.currentTimeMillis();
                        info.avatar = GlobalParams.USER_INFO.avatar;
                        info.realname = GlobalParams.USER_INFO.realname;
                        info.imgsList = new ArrayList<>();
                        ActivityChatListData.ChatImg chatImg = new ActivityChatListData.ChatImg();
                        chatImg.imgId = data.result.id;
                        chatImg.imgPath = data.result.path;
                        info.imgsList.add(chatImg);

                        chatInfoList.add(info);
                        myChatAdapter.notifyDataSetChanged();
                        if(chatInfoList.size() > 0)
                            rvChats.scrollToPosition(chatInfoList.size()-1);
                    }

                    @Override
                    public void onError(String msg) {
                        MyProgressDialog.dismiss();
                        super.onError(msg);
                    }
                };
                RequestEngine.getInstance().getServer(ActivityApiService.class).doChatActivity(activityId, etContent.getText().toString(), imgId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(UploadLoadApiService.class).uploadImage(builder.build()).compose(RxSchedulersHelper.io_main()).subscribe(uploadSubscriber);

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, photoFile.getAbsolutePath());
                uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            } else {
                uri = Uri.fromFile(photoFile);
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        uri);
                startActivityForResult(takePictureIntent, RCODE_TAKE_PHOTO);
            }
        }
    }

    String mCurrentPhotoPath;

    //create photo file to take photo
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), GlobalParams.TEMP_PIC_DIR);

        if (!storageDir.exists())
            storageDir.mkdir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @OnClick({R.id.tv_multi, R.id.iv_like, R.id.btn_send_message, R.id.tv_follow, R.id.iv_post_img})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_multi:
                if (detailInfo.activityState == 1) {
                    // TODO: 2017/12/19 获取签到说明
                    getSignType();
                } else if (detailInfo.activityState == 3) {
                    showScoreDialog(0, null);
                } else if (detailInfo.activityState == 0) {
                    //报名
                    showConfirmRegistDialog();
                } else if (detailInfo.activityState == 4) {
                    //已评价
                    getScoreData();
                }
                break;
            case R.id.iv_post_img:
                if (TipDialogUtil.checkLogin(this)) {
                    dispatchTakePictureIntent();
                }

                break;
            case R.id.btn_send_message:
                if (TextUtils.isEmpty(etContent.getText().toString())) {
                    MyToast.show(R.string.chat_content, this);
                    return;
                }
                if (etContent.getText().toString().length() > 200) {
                    MyToast.show(R.string.chat_content_limit, this);
                    return;
                }
                if (TipDialogUtil.checkLogin(this)) {
                    MyProgressDialog.show(this, false);
                    HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
                        @Override
                        public void onSuccess(StringData baseBean) {
                            MyProgressDialog.dismiss();
                            ActivityChatListData.ActivityChatInfo info = new ActivityChatListData.ActivityChatInfo();
                            info.id = GlobalParams.USER_INFO.id;
                            info.createtime = System.currentTimeMillis();
                            info.avatar = GlobalParams.USER_INFO.avatar;
                            info.realname = GlobalParams.USER_INFO.realname;
                            info.content = etContent.getText().toString();
                            chatInfoList.add(info);
                            etContent.setText("");
                            myChatAdapter.notifyDataSetChanged();
                            if(chatInfoList.size() > 0)
                                rvChats.scrollToPosition(chatInfoList.size()-1);
                        }

                        @Override
                        public void onError(String msg) {
                            MyProgressDialog.dismiss();
                            super.onError(msg);
                        }
                    };
                    RequestEngine.getInstance().getServer(ActivityApiService.class).doChatActivity(activityId, etContent.getText().toString(), null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
                }
                break;
            case R.id.iv_like:
                if (TipDialogUtil.checkLogin(this)) {
                    MyProgressDialog.show(this, false);
                    HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
                        @Override
                        public void onSuccess(StringData baseBean) {
                            MyProgressDialog.dismiss();
                            detailInfo.isLike = !detailInfo.isLike;
                            ivLike.setImageResource(detailInfo.isLike ? R.mipmap.up : R.mipmap.no_up);
                        }

                        @Override
                        public void onError(String msg) {
                            MyProgressDialog.dismiss();
                            super.onError(msg);
                        }
                    };
                    RequestEngine.getInstance().getServer(ActivityApiService.class).doLikeActivity(activityId, detailInfo.isLike ? "2" : "1").compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
                }
                break;
            case R.id.tv_follow:
                if (TipDialogUtil.checkLogin(this)) {
                    MyProgressDialog.show(this, false);
                    HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
                        @Override
                        public void onSuccess(StringData baseBean) {
                            MyProgressDialog.dismiss();
                            detailInfo.isAttention = detailInfo.isAttention == 1 ? 0 : 1;
                            tvFollow.setText(detailInfo.isAttention == 1 ? R.string.has_follow : R.string.follow);
                            MyToast.show(detailInfo.isAttention == 1 ? R.string.follow_toast : R.string.has_follow_toast, ActivityDetailActivity.this);
                        }

                        @Override
                        public void onError(String msg) {
                            MyProgressDialog.dismiss();
                            super.onError(msg);
                        }
                    };
                    RequestEngine.getInstance().getServer(ActivityApiService.class).modifyActivity(activityId, detailInfo.isAttention == 1 ? "6" : "5", null, 0, null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

                }
                break;
        }
    }

    private void setDialogFullScreenWidth(Dialog dialog) {
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.gravity = Gravity.CENTER;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        // 设置Dialog最小宽度为屏幕宽度
        lp.width = display.getWidth();
        dialogWindow.setAttributes(lp);
    }

    private class MyActivityInfodapter extends BaseAdapter {
        @Override
        public int getCount() {
            return detailInfo == null ? 0 : 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_activity_detail_desc, null);
            TextView tvActivityDesc = convertView.findViewById(R.id.tv_activity_desc);
            TextView tvActivityType = convertView.findViewById(R.id.tv_activity_type);
            TextView tvActivityRegistEndTime = convertView.findViewById(R.id.tv_activity_registration_end_time);
            TextView tvActivitySignTime = convertView.findViewById(R.id.tv_sign_time);
            tvActivityDesc.setText(getString(R.string.activity_desc_holder, detailInfo.desc));
            tvActivityType.setText(getString(R.string.activity_type_holder, detailInfo.activityType));
            tvActivityRegistEndTime.setText(getString(R.string.activity_regist_end_time, TimeUtil.getDateHourMinuteTime(detailInfo.registEndtime)));
            tvActivitySignTime.setText(getString(R.string.sign_time_holder, detailInfo.signtime));
            return convertView;
        }

    }

    private class MyActivityChatAdapter extends BaseAdapter {
        @Override
        public int getCount() {

            return 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_activity_detail_tab_chat, null);
            rvChats = convertView.findViewById(R.id.rv_chats);
//            rvChats.setLayoutManager(new GridLayoutManager(parent.getContext(), 3));
            rvChats.setLayoutManager(new LinearLayoutManager(parent.getContext()));
            myChatAdapter = new MyChatAdapter();
            rvChats.setAdapter(myChatAdapter);
            if(chatInfoList.size() > 0)
                rvChats.scrollToPosition(chatInfoList.size()-1);
            return convertView;
        }

    }

    private class MyChatAdapter extends RecyclerView.Adapter {


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_detail_chat, parent, false);
            ChatViewHolder holder = new ChatViewHolder(itemView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ChatViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return chatInfoList.size();
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.iv_chat_head)
        ScaleImageView ivChatHead;
        @BindView(R.id.tv_chat_name)
        TextView tvChatName;
        @BindView(R.id.tv_chat_content)
        TextView tvChatContent;
        @BindView(R.id.iv_chat_img)
        ImageView ivChatImg;

        ChatViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(int position) {

            final ActivityChatListData.ActivityChatInfo info = chatInfoList.get(position);
            if (!TextUtils.isEmpty(info.avatar)) {
                RequestOptions options = new RequestOptions().placeholder(R.mipmap.ic_img_thumbnail).error(R.mipmap.ic_img_failure).dontAnimate();
                Glide.with(itemView.getContext())
                        .load(TLSUrl.BASE_URL + info.avatar)
                        .apply(options)
                        .into(ivChatHead);
            } else {
                ivChatHead.setImageResource(R.mipmap.default_head);
            }
            if (!TextUtils.isEmpty(info.content)) {
                //说明聊天内容不为空
                tvChatContent.setText(info.content);
                tvChatContent.setVisibility(View.VISIBLE);
                ivChatImg.setVisibility(View.GONE);
            } else if (info.imgsList != null && info.imgsList.size() > 0) {
                tvChatContent.setVisibility(View.GONE);
                ivChatImg.setVisibility(View.VISIBLE);
                RequestOptions options = new RequestOptions().placeholder(R.mipmap.ic_img_thumbnail).error(R.mipmap.ic_img_thumbnail).dontAnimate();
                Glide.with(itemView.getContext())
                        .load(TLSUrl.BASE_URL + info.imgsList.get(0).imgPath)
                        .apply(options)
                        .thumbnail(0.1f)
                        .into(ivChatImg);
            }
            tvTime.setText(TimeUtil.getWeiboTime(info.createtime));
            tvChatName.setText(NameUtil.getName(info.realname, info.nickname, info.isname, info.isnickname));
            ivChatImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (info.imgsList != null && info.imgsList.size() > 0) {
//                        ArrayList<PhotoBean> photoList = new ArrayList<>();
//                        PhotoBean photoBean = new PhotoBean();
//                        photoBean.imgUrl = TLSUrl.BASE_URL + info.imgsList.get(0).imgPath;
//                        photoList.add(photoBean);
//                        ActivityPhotoViewActivity.startPhotoViewActivity(ActivityDetailActivity.this, photoList, v, 0);
                        ActivityPhotoListData.ActivityPhotoInfo photoInfo = new ActivityPhotoListData.ActivityPhotoInfo();
                        photoInfo.imgid = info.imgsList.get(0).imgId;
                        photoInfo.path = info.imgsList.get(0).imgPath;
                        ActivityPhotoViewActivity.startPhotoViewActivity(ActivityDetailActivity.this, photoInfo, v);
                    }
                }
            });
        }
    }


    private class MyActivityMemberAdapter extends BaseAdapter {
        @Override
        public int getCount() {

            return memberInfoList.size() > 0 ? 1 : 0;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_activity_detail_tab_member, null);
            RecyclerView rvMembers = convertView.findViewById(R.id.rv_members);
            rvMembers.setLayoutManager(new GridLayoutManager(parent.getContext(), 5));
            rvMembers.setAdapter(new MyMemberAdapter());
            return convertView;
        }

    }

    private class MyMemberAdapter extends RecyclerView.Adapter {


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_detail_member, parent, false);
            MemberViewHolder holder = new MemberViewHolder(itemView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MemberViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return memberInfoList.size();
        }
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_member_head)
        ScaleImageView ivMemberHead;
        @BindView(R.id.tv_member_sign_state)
        TextView tvMemberSignState;

        MemberViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(int position) {
            ActivityMemberListData.ActivityMemberInfo info = memberInfoList.get(position);
            if (!TextUtils.isEmpty(info.avatar)) {
                RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.mipmap.ic_img_thumbnail).dontAnimate();
                Glide.with(itemView.getContext())
                        .load(TLSUrl.BASE_URL + info.avatar)
                        .apply(options)
                        .into(ivMemberHead);

            } else {
                ivMemberHead.setImageResource(R.mipmap.ic_img_thumbnail);
            }
            tvMemberSignState.setSelected(info.type > 1);
            tvMemberSignState.setTextColor(info.type > 1 ? ContextCompat.getColor(itemView.getContext(), R.color.theme_color) : ContextCompat.getColor(itemView.getContext(), R.color.light_gray));
//            tvMemberSignState.setTextColor(info.type > 1);
//            tvMemberName.setText(NameUtil.getName(info.realname, info.nickname, info.isname, info.isnickname));
//            tvMemberName.setSelected(info.type > 1);
//            tvMemberName.setBackgroundResource(info.type > 1 ? R.drawable.theme_round_stroke_bg : R.drawable.light_gray_round_stroke_bg);

        }
    }

    private class MyActivityWonderfulMomentAdapter extends BaseAdapter {
        @Override
        public int getCount() {

            return photoInfoList.size() > 0 ? 1 : 0;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_activity_detail_tab_wonderful_moment, null);
            RecyclerView rvWonderfulMoments = convertView.findViewById(R.id.rv_wonderful_moments);
            rvWonderfulMoments.setLayoutManager(new GridLayoutManager(parent.getContext(), 3));
            woderfulMomentAdapter = new MyWonderfulMomentAdapter();
            rvWonderfulMoments.setAdapter(woderfulMomentAdapter);
            return convertView;
        }

    }

    private class MyWonderfulMomentAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_detail_wonderful_momnet, parent, false);
            WonderfulViewHolder holder = new WonderfulViewHolder(itemView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((WonderfulViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return photoInfoList.size();
        }
    }

    public class WonderfulViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_photo)
        ScaleImageView ivPhoto;
        @BindView(R.id.tv_up_num)
        TextView tvUpNum;
//        @BindView(R.id.tv_down_num)
//        TextView tvDownNum;

        WonderfulViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(int position) {
            final ActivityPhotoListData.ActivityPhotoInfo info = photoInfoList.get(position);
            info.index = position;
            if (!TextUtils.isEmpty(info.path)) {
                RequestOptions options = new RequestOptions().placeholder(R.mipmap.ic_img_thumbnail).error(R.mipmap.ic_img_failure).dontAnimate();
                Glide.with(itemView.getContext())
                        .load(TLSUrl.BASE_URL + info.path)
                        .apply(options)
                        .thumbnail(0.1f)
                        .into(ivPhoto);

            } else {
                ivPhoto.setImageResource(R.mipmap.ic_img_thumbnail);
            }
            tvUpNum.setText(info.like + "");
            ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityPhotoViewActivity.startPhotoViewActivity(ActivityDetailActivity.this, info, v);

                }
            });
        }
    }
    @Subscribe
    public void onReceiveActivityPhotoUpEvent(ActivityPhotoListData.ActivityPhotoInfo photoInfo){

        ActivityPhotoListData.ActivityPhotoInfo activityPhotoInfo = photoListInfo.list.remove(photoInfo.index);
        activityPhotoInfo.like = photoInfo.like;
        photoListInfo.list.add(photoInfo.index,activityPhotoInfo);
        woderfulMomentAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NavBean.TYPE_CURRENT = -1;
        handler.removeCallbacksAndMessages(null);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (myInteractionTimer != null) {
            myInteractionTimer.cancel();
            myInteractionTimer = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_notice:
                //打开公告列表
                startActivity(NoticeListActivity.getNoticeListIntent(this, detailInfo.noticeList));
                break;
            case R.id.iv_qrcode:
                //生成二维码
                QRGenerateActivity.GenerateEntity entity = new QRGenerateActivity.GenerateEntity();
                entity.generateId = activityId;
                entity.generateAvatar = TLSUrl.BASE_URL + detailInfo.bannerList.get(0).realpath;
                entity.name = detailInfo.activityName;
                startActivity(QRGenerateActivity.getQRGenerateIntent(this, QRCodeType.ACTIVITY, entity));
                break;
            default:
                super.onClick(view);
                break;
        }
    }

    private ActivityChatListData.ActivityChatListInfo chatListInfo;
    private ActivityMemberListData.ActivityMemberListInfo memberListInfo;
    private ActivityPhotoListData.ActivityPhotoListInfo photoListInfo;
    private List<ActivityChatListData.ActivityChatInfo> chatInfoList = new ArrayList<>();
    private List<ActivityMemberListData.ActivityMemberInfo> memberInfoList = new ArrayList<>();
    private List<ActivityPhotoListData.ActivityPhotoInfo> photoInfoList = new ArrayList<>();

    private void requestTabData(int position) {
        if (position == 0) {
            //说明是活动列表请求
            if (chatListInfo != null)
                return;
            requesChatListData();
        } else if (position == 1) {
            //说明是活动简介不需要请求数据
            return;
        } else if (position == 2) {
            //说明是成员列表请求
            if (memberListInfo != null)
                return;
            requesMemberListData();
        } else {
            //说明是精彩瞬间列表请求
            if (photoListInfo != null)
                return;
            requesPhotoListData();
        }

    }


    private void requesChatListData() {
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<ActivityChatListData>(this) {
            @Override
            public void onSuccess(ActivityChatListData listData) {
                chatListInfo = listData.result;
                chatInfoList = chatListInfo.list;
                Collections.reverse(chatInfoList);
                chatAdapter.notifyDataSetChanged();
                MyProgressDialog.dismiss();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityChatList(activityId, 0, 0).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void requesPhotoListData() {
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<ActivityPhotoListData>(this) {
            @Override
            public void onSuccess(ActivityPhotoListData listData) {
                photoListInfo = listData.result;
                photoInfoList = photoListInfo.list;
                photoAdapter.notifyDataSetChanged();
                MyProgressDialog.dismiss();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityPhotoList(activityId, 0, 0, null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void requesMemberListData() {
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<ActivityMemberListData>(this) {
            @Override
            public void onSuccess(ActivityMemberListData listData) {
                memberListInfo = listData.result;
                memberInfoList = memberListInfo.list;
                memberAdapter.notifyDataSetChanged();
                MyProgressDialog.dismiss();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityMemberList(activityId, 0, 0).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    @Subscribe
    public void onReceiveSignManageSettingFinishEvent(SignManageSettingFinishEvent event) {
        detailInfo.isSignConfigure = true;
    }

    @Subscribe
    public void onReceiveActivityInteractionEvent(final ActivityInteractionEvent event) {
        //// TODO: 2017/12/19
        switch (event.curInteractionType) {
            case START_TIME:
            case REGIST_START_TIME:
            case REGIST_END_TIME:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setBottomViewState();
                    }
                });
                break;
            case SIGN_TIME:
                if (detailInfo.activityState == 1) {
                    //说明报名了，签到时间已经开始但是还没签到
//                    tvMulti.setClickable(true);
//                    tvMulti.setBackgroundColor(ContextCompat.getColor(this,R.color.theme_color));
                    if (detailInfo.isSignConfigure) {
                        //如果配置了签到才可以开始签到
                        if (GlobalParams.getCurrentTimeStamp() < detailInfo.endtime) {
                            handler.sendEmptyMessage(START_SIGN);
                        }
                    } else {
                        if (detailInfo.isCan == 1) {
                            //如果是管理员，没有配置签到方式
                            Message msg = new Message();
                            msg.what = SHOW_SETTING_DIALOG;
                            msg.obj = event;
                            handler.sendMessage(msg);
                        }
                    }

                }
                break;
            case VOTE:
                // TODO: 2017/12/19 投票弹框
                if (detailInfo.isVote == 1) {
                    //说明已投票获取投票结果
                    handler.sendEmptyMessage(GET_VOTE_RESULT);

                } else {
                    //未投票，弹框让用户投票
                    Message msg = new Message();
                    msg.what = ADD_VOTE_TIP_VIEW;
                    msg.obj = event;
                    handler.sendMessage(msg);
                }
                break;
        }
    }

    private void startSign() {
        //活动未结束之前才可以签到
        tvMulti.setText(R.string.sign);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        long currentTime = GlobalParams.getCurrentTimeStamp();
                        if (currentTime < detailInfo.starttime) {
                            tvMulti.setText(getString(R.string.sign_holder, TimeUtil.getActivityLeftTime(currentTime, detailInfo.starttime)));
                            tvMulti.setClickable(true);
                            tvMulti.setBackgroundColor(ContextCompat.getColor(ActivityDetailActivity.this, R.color.theme_color));
                        } else {
                            if (currentTime < detailInfo.endtime) {
                                tvMulti.setText(R.string.sign);
                            } else {
                                tvMulti.setText(R.string.activity_has_end);
                                tvMulti.setClickable(false);
                                tvMulti.setBackgroundColor(ContextCompat.getColor(ActivityDetailActivity.this, R.color.light_gray));
                            }
                        }
                    }
                });
            }
        };
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.schedule(task, 0, 1000);
        // TODO: 2017/12/19 获取签到说明
        getSignType();
    }

    private void addVoteTipView(final ActivityInteractionEvent event) {
        llMiddleContent.removeAllViews();
        View view = LayoutInflater.from(ActivityDetailActivity.this).inflate(R.layout.view_activity_detail_vote, null);
        llMiddleContent.addView(view);
        view.findViewById(R.id.btn_vote_immediate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVoteDialog(event);
            }
        });
    }

    private int countDownNum;

    private void addVoteCountDownView(final ActivityInteractionEvent event) {
        llMiddleContent.removeAllViews();
        View view = LayoutInflater.from(ActivityDetailActivity.this).inflate(R.layout.view_count_down, null);
        llMiddleContent.addView(view);
        TextView tvCountDownTitle = view.findViewById(R.id.tv_count_down_title);
        final TextView tvCountDownNum = view.findViewById(R.id.tv_count_down_num);
        countDownNum = 0;
        countDownNum = event.countDownNum;
        tvCountDownNum.setText(countDownNum + "");
        tvCountDownTitle.setText(R.string.vote_count_down);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                countDownNum--;
                if (countDownNum > 0) {
                    tvCountDownNum.setText(countDownNum + "");
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void getVoteResult(ActivityInteractionEvent event) {
        MyProgressDialog.show(this, false);
        HttpSubscriber httpSubscriber = new HttpSubscriber<VoteResultData>(ActivityDetailActivity.this) {
            @Override
            public void onSuccess(VoteResultData resultData) {
                MyProgressDialog.dismiss();
                //添加投票结果界面
                llMiddleContent.removeAllViews();
                View view = LayoutInflater.from(ActivityDetailActivity.this).inflate(R.layout.view_activity_detail_head_vote_result, null);
                llMiddleContent.addView(view);
                ((TextView) view.findViewById(R.id.tv_vote_title)).setText(resultData.result.voteTitle);
                RecyclerView rvVoteResults = view.findViewById(R.id.rv_vote_results);
                rvVoteResults.setLayoutManager(new LinearLayoutManager(ActivityDetailActivity.this));
                VoteResultAdapter adapter = new VoteResultAdapter(resultData.result);
                initRandomColors(adapter.getItemCount());
                rvVoteResults.setAdapter(adapter);
            }

            @Override
            public void onError(String msg) {
                MyProgressDialog.dismiss();
                super.onError(msg);
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getVoteResult(interactionInfo.sceneInteractivity.voteList.get(event.voteNoteIndex).id).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void showSettingSignTypeDialog() {
        Dialog setttngSignTypeDialog = new IosDialog.Builder(this)
                .setMessage(R.string.has_not_setting_sign_type).setMessageColor(ContextCompat.getColor(this, R.color.gray)).setMessageSize(15)
                .setNegativeButtonColor(ContextCompat.getColor(this, R.color.gray))
                .setNegativeButtonSize(16)
                .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButtonColor(ContextCompat.getColor(this, R.color.theme_color))
                .setPositiveButtonSize(16)
                .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        startActivity(SignModeActivity.getSignModeIntent(ActivityDetailActivity.this, detailInfo.isSignConfigure, activityId));
                        dialog.dismiss();
                    }
                }).build();
        setttngSignTypeDialog.show();
    }

    private SignTypeData.SignTypeInfo signTypeInfo;

    //获取评论
    private void getScoreData() {
        MyProgressDialog.show(this, false);
        HttpSubscriber httpSubscriber = new HttpSubscriber<ActivityScoreData>(this) {

            @Override
            public void onSuccess(ActivityScoreData data) {
                MyProgressDialog.dismiss();
                showScoreDialog((int) data.result.score, data.result.evaluate);
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                mSwipeLayout.setRefreshing(false);
                setSwipeRerefreshEnable(mSwipeLayout, true);
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityScore(activityId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    //获取签到配置详情
    private void getSignType() {
        HttpSubscriber httpSubscriber = new HttpSubscriber<SignTypeData>(this) {

            @Override
            public void onSuccess(SignTypeData data) {
                MyProgressDialog.dismiss();
                signTypeInfo = data.result;
                showSignNoticeDialog();

            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                mSwipeLayout.setRefreshing(false);
                setSwipeRerefreshEnable(mSwipeLayout, true);
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getSignType(activityId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private Timer myInteractionTimer;
    private MyInteractionTimerTask myInteractionTimerTask;
    private boolean isSignTimeNotice = false;
    private boolean isRegistStartTimeNotice = false;
    private boolean isRegistEndTimeNotice = false;
    private boolean isStartTimeNotice = false;
    private boolean isEndTimeNotice = false;
    private List<Boolean> voteNoticeList;
    private List<Boolean> voteNoticeEndList;
    private List<Boolean> voteCountDownList;

    private void startInteractionService() {
        handler.removeCallbacksAndMessages(null);
        isSignTimeNotice = false;
        isRegistStartTimeNotice = false;
        isRegistEndTimeNotice = false;
        isStartTimeNotice = false;
        isEndTimeNotice = false;
        if (myInteractionTimer != null) {
            myInteractionTimer.cancel();
            myInteractionTimer = null;
            myInteractionTimerTask = null;
        }
        if (interactionInfo.sceneInteractivity.voteList != null && interactionInfo.sceneInteractivity.voteList.size() > 0) {
            voteNoticeList = new ArrayList<>();
            voteNoticeEndList = new ArrayList<>();
            voteCountDownList = new ArrayList<>();
            for (int i = 0; i < interactionInfo.sceneInteractivity.voteList.size(); i++) {
                voteNoticeList.add(false);
                voteNoticeEndList.add(false);
                voteCountDownList.add(false);
            }
        }
        myInteractionTimer = new Timer();
        myInteractionTimerTask = new MyInteractionTimerTask();
        //每秒钟执行一次
        myInteractionTimer.schedule(myInteractionTimerTask, 0, 1000);
    }


    private class MyInteractionTimerTask extends TimerTask {

        @Override
        public void run() {
            long currentTime = GlobalParams.getCurrentTimeStamp();
            if (currentTime < detailInfo.endtime) {
                if (currentTime > (detailInfo.starttime - detailInfo.signtime * 60 * 1000)) {
                    //签到时间
                    //说明当前都是签到时间
                    if (!isSignTimeNotice) {
                        isSignTimeNotice = true;
                        ActivityInteractionEvent event = new ActivityInteractionEvent();
                        event.isSignTimeNotice = isSignTimeNotice;
                        event.curInteractionType = ActivityInteractionEvent.InteractionType.SIGN_TIME;
//                        EventBus.getDefault().post(event);
                        if (detailInfo.activityState == 1) {
                            //说明报名了，签到时间已经开始但是还没签到
//                    tvMulti.setClickable(true);
//                    tvMulti.setBackgroundColor(ContextCompat.getColor(this,R.color.theme_color));
                            if (detailInfo.isSignConfigure) {
                                //如果配置了签到才可以开始签到
                                if (GlobalParams.getCurrentTimeStamp() < detailInfo.endtime) {
                                    handler.sendEmptyMessage(START_SIGN);
                                }
                            } else {
                                if (detailInfo.isCan == 1) {
                                    //如果是管理员，没有配置签到方式
                                    Message msg = new Message();
                                    msg.what = SHOW_SETTING_DIALOG;
                                    msg.obj = event;
                                    handler.sendMessage(msg);
                                }
                            }
                        }

                    }
                    if (currentTime > detailInfo.starttime) {
                        if (!isStartTimeNotice) {
                            isStartTimeNotice = true;
                            handler.sendEmptyMessage(ACTIVITY_ON_GOING);
                        }
                        //说明是互动进行时，判断投票配置
                        if (interactionInfo != null && interactionInfo.sceneInteractivity != null) {
                            //互动配置不为空，如果有投票
                            if (interactionInfo.sceneInteractivity.voteList != null && interactionInfo.sceneInteractivity.voteList.size() > 0) {
                                for (int i = 0; i < interactionInfo.sceneInteractivity.voteList.size(); i++) {

                                    ActivityInteractionData.VoteListBean voteListBean = interactionInfo.sceneInteractivity.voteList.get(i);
                                    ActivityInteractionEvent event = new ActivityInteractionEvent();
                                    event.voteNoticeList = voteNoticeList;
                                    event.voteNoteIndex = i;
                                    event.curInteractionType = ActivityInteractionEvent.InteractionType.VOTE;
                                    if (currentTime > voteListBean.beginDate && currentTime < voteListBean.endDate) {
                                        if (!voteNoticeList.get(i)) {
                                            //说明当前是投票时间,并且没有投过票
                                            voteNoticeList.remove(i);
                                            voteNoticeList.add(i, true);
//                                        EventBus.getDefault().post(event);
                                            if (voteListBean.isVote) {
                                                //说明已投票获取投票结果
                                                Message msg = new Message();
                                                msg.what = GET_VOTE_RESULT;
                                                msg.obj = event;
                                                handler.sendMessage(msg);

                                            } else {
                                                //未投票，弹框让用户投票
                                                ActivityInteractionData.VoteListBean voteInfo = interactionInfo.sceneInteractivity.voteList.get(event.voteNoteIndex);
                                                if (voteInfo.votingRights == 0) {
                                                    if (detailInfo.activityState > 0) {
                                                        Message msg = new Message();
                                                        msg.what = ADD_VOTE_TIP_VIEW;
                                                        msg.obj = event;
                                                        handler.sendMessage(msg);
                                                    }
                                                } else if (voteInfo.votingRights == 1) {
                                                    if (detailInfo.activityState > 1) {
                                                        Message msg = new Message();
                                                        msg.what = ADD_VOTE_TIP_VIEW;
                                                        msg.obj = event;
                                                        handler.sendMessage(msg);
                                                    }
                                                }
                                            }
                                        }
                                    } else if (currentTime > voteListBean.endDate) {
                                        if (!voteNoticeEndList.get(i)) {
                                            voteNoticeEndList.remove(i);
                                            voteNoticeEndList.add(i, true);
                                            handler.sendEmptyMessage(VOTE_END_TIME);
                                        }
                                    } else if (currentTime < voteListBean.beginDate) {
                                        ActivityInteractionData.VoteListBean voteInfo = interactionInfo.sceneInteractivity.voteList.get(event.voteNoteIndex);

                                        if (!voteCountDownList.get(i) && voteInfo.countdownEffect > 0) {
                                            //说明设置了倒计时配置
                                            if (currentTime > (voteListBean.beginDate - voteInfo.countdownEffect * 1000)) {
                                                voteCountDownList.remove(i);
                                                voteCountDownList.add(i, true);
                                                int countDownNum = ((int) (voteListBean.beginDate - currentTime)) / 1000;
                                                event.countDownNum = countDownNum;
                                                if (voteInfo.votingRights == 0) {
                                                    Message msg = new Message();
                                                    msg.what = VOTE_COUNT_DOWN;
                                                    msg.obj = event;
                                                    handler.sendMessage(msg);
                                                } else if (voteInfo.votingRights == 1) {
                                                    if (detailInfo.activityState > 1) {
                                                        Message msg = new Message();
                                                        msg.what = VOTE_COUNT_DOWN;
                                                        msg.obj = event;
                                                        handler.sendMessage(msg);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {

                    if (currentTime > detailInfo.registStarttime) {
                        if (!isRegistStartTimeNotice) {
                            isRegistStartTimeNotice = true;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setBottomViewState();
                                }
                            });
                        }
                        if (currentTime > detailInfo.registEndtime) {
                            if (!isRegistEndTimeNotice) {
                                isRegistEndTimeNotice = true;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        setBottomViewState();
                                    }
                                });
                            }
                        }
                    }

                }

            } else {
                if (!isEndTimeNotice) {
                    isEndTimeNotice = true;
                    handler.sendEmptyMessage(ACTIVITY_NOT_ON_GOING);
                }
            }

        }
    }
}
