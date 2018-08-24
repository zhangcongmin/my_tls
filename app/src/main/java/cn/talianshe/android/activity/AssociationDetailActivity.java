package cn.talianshe.android.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bruce.stickynavigationbar.bean.NavBean;
import com.bruce.stickynavigationbar.listener.NavListViewScrollListener;
import com.bruce.stickynavigationbar.view.StickNavHostSubject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jiang.android.indicatordialog.IndicatorBuilder;
import com.jiang.android.indicatordialog.IndicatorDialog;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.wc.widget.dialog.IosDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.adapter.BannerVPAdapter;
import cn.talianshe.android.bean.AssociationActivityListData;
import cn.talianshe.android.bean.AssociationAlbumListData;
import cn.talianshe.android.bean.AssociationDetailData;
import cn.talianshe.android.bean.AssociationMemberListData;
import cn.talianshe.android.bean.Marquee;
import cn.talianshe.android.bean.NoticeBean;
import cn.talianshe.android.bean.QRCodeType;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.net.service.AssociationApiService;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.CustomStarBar;
import cn.talianshe.android.widget.MarqueeTextView;
import cn.talianshe.android.widget.MyEditTextDialog;
import cn.talianshe.android.widget.MyForwardDialog;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyStickyNavHost;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.ScaleImageView;
import cn.talianshe.android.widget.ScrollViewPager;
import cn.talianshe.android.widget.UnderlineBtn;

/**
 * @author zcm
 * @ClassName: AssociationDetailActivity
 * @Description: 社团详情页
 * @date 2017/11/20 10:45
 */
public class AssociationDetailActivity extends BaseActivity implements MyStickyNavHost.TabItemClickListener {

    private static final int NAV_LENGTH = 4;
    private static final String EXTRA_ASSOCIATION_ID = "extra_association_id";
    @BindView(R.id.tv_follow)
    TextView tvFollow;
    @BindView(R.id.tv_multi)
    TextView tvMulti;
    @BindView(R.id.ll_multi)
    LinearLayout llMulti;
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

    private String associationId;
    private MyAssociationInfoAdapter descAdapter;
    private MyAssociationActivityAdapter activityAdapter;
    private MyAssociationPhotoAdapter albumAdapter;
    private MyAssociationMemberAdapter memberAdapter;

    public static Intent getAssociationDetailIntent(Context context, String associationId) {
        Intent intent = new Intent(context, AssociationDetailActivity.class);
        intent.putExtra(EXTRA_ASSOCIATION_ID, associationId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_association_detail);
        ButterKnife.bind(this);

        initData();
        initView();

        initNavsView();//初始化导航栏view

//        initDefaultSelectedNav();//设置默认选择的导航tab
        mSwipeLayout.setRefreshing(true);
//        mSwipeLayout.setEnabled(false);
        llMulti.setVisibility(View.GONE);
        requestData();
    }

    private AssociationApiService associationApiService;
    private AssociationDetailData.AssociationDetailInfo detailInfo;

    private void requestData() {
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationDetailData>(this) {
            @Override
            public void onSuccess(AssociationDetailData data) {
                mSwipeLayout.setRefreshing(false);
                setSwipeRerefreshEnable(mSwipeLayout, true);
                llMulti.setVisibility(View.VISIBLE);
                detailInfo = data.result;
                //四个tab的数据初始化
                UnderlineBtn btnTab1 = tabItems1[1].getView().findViewById(R.id.btn_tab);
                UnderlineBtn btnTab2 = tabItems1[2].getView().findViewById(R.id.btn_tab);
                UnderlineBtn btnTab3 = tabItems1[3].getView().findViewById(R.id.btn_tab);
                UnderlineBtn btnOtherTab1 = tabItems2[1].getView().findViewById(R.id.btn_tab);
                UnderlineBtn btnOtherTab2 = tabItems2[2].getView().findViewById(R.id.btn_tab);
                UnderlineBtn btnOtherTab3 = tabItems2[3].getView().findViewById(R.id.btn_tab);
                btnTab1.setText(getString(R.string.activity_tab_holder, detailInfo.activityCount));
                btnOtherTab1.setText(getString(R.string.activity_tab_holder, detailInfo.activityCount));

                btnTab2.setText(getString(R.string.album_tab_holder, detailInfo.albumCount));
                btnOtherTab2.setText(getString(R.string.album_tab_holder, detailInfo.albumCount));

                btnTab3.setText(getString(R.string.member_tab_holder, detailInfo.memberCount));
                btnOtherTab3.setText(getString(R.string.member_tab_holder, detailInfo.memberCount));
                //填充顶部数据
                fillHeadData(detailInfo);
                //底部已加入社团、关注数据
                setMultiState(detailInfo.isJoin == 1, detailInfo.isApply);
                setFollowState(detailInfo.isfollow == 1);
                //填充数据到第一个基本信息页
                initDefaultSelectedNav();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        associationApiService = RequestEngine.getInstance().getServer(AssociationApiService.class);
        associationApiService.getAssociationDetail(associationId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void initData() {
        associationId = getIntent().getStringExtra(EXTRA_ASSOCIATION_ID);
        mSwipeLayout.setColorSchemeResources(R.color.theme_color);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO: 2017/12/16 清除数据
                activityListInfo = null;
                albumListInfo = null;
                memberListInfo = null;
                requestData();
            }
        });
    }

    private void setMultiState(boolean hasJion, boolean isApply) {
        if (hasJion) {
            tvMulti.setClickable(false);
            tvMulti.setText(R.string.has_jion_association);
            tvMulti.setBackgroundColor(ContextCompat.getColor(AssociationDetailActivity.this, R.color.light_gray));
        } else {
            if (isApply) {
                tvMulti.setClickable(false);
                tvMulti.setText(R.string.has_apply_jion_association);
                tvMulti.setBackgroundColor(ContextCompat.getColor(AssociationDetailActivity.this, R.color.light_gray));
            } else {
                tvMulti.setClickable(true);
                tvMulti.setText(R.string.apply_jion_association);
                tvMulti.setBackgroundColor(ContextCompat.getColor(AssociationDetailActivity.this, R.color.theme_color));
            }
        }
    }

    private void setFollowState(boolean hasFollow) {
        if (hasFollow) {
            tvFollow.setText(R.string.has_follow);
        } else {
            tvFollow.setText(R.string.follow);
        }
    }

    private void initDefaultSelectedNav() {
        onTabItemSelected(NavBean.TYPE_FIRST);//通过此方法可进行切换tab
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
        descAdapter = new MyAssociationInfoAdapter();
        mNavs.put(NavBean.TYPE_FIRST, new NavBean(NavBean.TYPE_FIRST, descAdapter));
        activityAdapter = new MyAssociationActivityAdapter();
        mNavs.put(NavBean.TYPE_SECOND, new NavBean(NavBean.TYPE_SECOND, activityAdapter));
        albumAdapter = new MyAssociationPhotoAdapter();
        mNavs.put(NavBean.TYPE_THIRD, new NavBean(NavBean.TYPE_THIRD, albumAdapter));
        memberAdapter = new MyAssociationMemberAdapter();
        mNavs.put(NavBean.TYPE_FOURTH, new NavBean(NavBean.TYPE_FOURTH, memberAdapter));
    }

    private void initView() {
        setTitle(R.string.association_detail);
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
//                        startActivity(new Intent(AssociationDetailActivity.this,MemberInfoActivity.class));
                        startActivity(MemberInfoActivity.getMemberInfoIntent(AssociationDetailActivity.this, MemberInfoActivity.TYPE_STUDENT, "-1"));
                        break;
                }
            }
        });
    }

    //----------------头布局相关----------------
    private ScrollViewPager scrollVP;
    private LinearLayout llNotice;
    private ImageView ivQrcode;
    ImageView ivAssociationLogo;
    TextView tvAssociationName;
    TextView tvAssociationSchool;
    CustomStarBar starBar;
    private MarqueeTextView tvMarquee;

    private void fillHeadData(AssociationDetailData.AssociationDetailInfo info) {
        llNotice.setOnClickListener(this);
        ivQrcode.setOnClickListener(this);
        if (!TextUtils.isEmpty(info.associationDetail.associationLogo)) {
            RequestOptions options = new RequestOptions();
            options.override(this.getDrawable(R.mipmap.all_association).getIntrinsicWidth());
            options.placeholder(R.mipmap.ic_img_thumbnail_small);
            options.error(R.mipmap.ic_img_failure_small);
            Glide.with(this).load(TLSUrl.BASE_URL + info.associationDetail.associationLogo).apply(options).into(ivAssociationLogo);
        } else {
            ivAssociationLogo.setImageResource(R.mipmap.all_association);
        }
        tvAssociationName.setText(info.associationDetail.associationName);
        tvAssociationSchool.setText(info.associationDetail.level == 1 ? info.schoolName : info.schoolName + info.departmentName);
        starBar.setStarMark(info.associationDetail.score);
        tvAssociationName.setCompoundDrawablesWithIntrinsicBounds(info.associationDetail.level == 1 ? R.mipmap.school_level : R.mipmap.college_level, 0, 0, 0);
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
                    startActivity(TlsWebViewActivity.getNoticeIntent(AssociationDetailActivity.this, detailInfo.noticeList.get(position)));
                }
            });
        }
        if (info.bannerList != null) {
            loadBannerData(info.bannerList);
        }
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

    /**
     * 加载banner轮播图数据
     *
     * @param bannerList
     */
    private void loadBannerData(final List<AssociationDetailData.BannerListBean> bannerList) {

        List<ImageView> imageViews = new ArrayList<>();
        if (bannerList.size() > 0) {

            if (bannerList.size() > 1) {
                AssociationDetailData.BannerListBean firstBanner = bannerList.get(0);
                ImageView imageViewFirst = new ImageView(this);
                imageViewFirst.setLayoutParams(scrollVP.getLayoutParams());
                imageViewFirst.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViews.add(imageViewFirst);
                RequestOptions optionsFirst = new RequestOptions();
                optionsFirst.override(scrollVP.getLayoutParams().width, scrollVP.getLayoutParams().height);
                optionsFirst.placeholder(R.mipmap.ic_img_thumbnail_large);
                optionsFirst.error(R.mipmap.ic_img_failure_large);
                Glide.with(this).load(TLSUrl.BASE_URL + firstBanner.imgPath).apply(optionsFirst).thumbnail(0.1f).into(imageViewFirst);

                for (AssociationDetailData.BannerListBean banner : bannerList) {
                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(scrollVP.getLayoutParams());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageViews.add(imageView);
                    RequestOptions options = new RequestOptions();
                    options.override(scrollVP.getLayoutParams().width, scrollVP.getLayoutParams().height);
                    options.placeholder(R.mipmap.ic_img_thumbnail_large);
                    options.error(R.mipmap.ic_img_failure_large);
                    Glide.with(this).load(TLSUrl.BASE_URL + banner.imgPath).apply(options).thumbnail(0.1f).into(imageView);

                }
                AssociationDetailData.BannerListBean lastBanner = bannerList.get(bannerList.size() - 1);
                ImageView imageViewLast = new ImageView(this);
                imageViewLast.setLayoutParams(scrollVP.getLayoutParams());
                imageViewLast.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViews.add(imageViewLast);
                RequestOptions options = new RequestOptions();
                options.override(scrollVP.getLayoutParams().width, scrollVP.getLayoutParams().height);
                options.placeholder(R.mipmap.ic_img_thumbnail_large);
                options.error(R.mipmap.ic_img_failure_large);
                Glide.with(this).load(TLSUrl.BASE_URL + lastBanner.imgPath).apply(options).thumbnail(0.1f).into(imageViewLast);
            } else {
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(scrollVP.getLayoutParams());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViews.add(imageView);
                RequestOptions options = new RequestOptions();
                options.override(scrollVP.getLayoutParams().width, scrollVP.getLayoutParams().height);
                options.placeholder(R.mipmap.ic_img_thumbnail_large);
                options.error(R.mipmap.ic_img_failure_large);
                Glide.with(this).load(TLSUrl.BASE_URL + bannerList.get(0).imgPath).apply(options).thumbnail(0.1f).into(imageView);

            }
        } else {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(R.mipmap.association_default_banner);
            imageViews.add(imageView);
        }

        scrollVP.setCanScroll(false);
        scrollVP.setAdapter(new BannerVPAdapter(imageViews));
        if (imageViews.size() > 1) {

            scrollVP.setCurrentItem(1);
            scrollVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    //正确数据的大小
                    int size = bannerList.size();
                    if (bannerList.size() > 1) {
                        if (position == 0) {
                            //切换到最后一个页面
                            scrollVP.setCurrentItem(size, false);
                        } else if (position == size + 1) {
                            //切换到第一个页面
                            scrollVP.setCurrentItem(1, false);
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            autoLoadBannerHandler.sendEmptyMessageDelayed(0, 3000);
        }
    }

    private View initHeadView() {
        View view = LayoutInflater.from(this).inflate(R.layout.view_association_detail_head, null);
        llNotice = view.findViewById(R.id.ll_notice);
        tvMarquee = view.findViewById(R.id.tv_marquee);
        ivQrcode = view.findViewById(R.id.iv_qrcode);
        ivAssociationLogo = view.findViewById(R.id.iv_association_logo);
        tvAssociationName = view.findViewById(R.id.tv_association_name);
        starBar = view.findViewById(R.id.starBar);
        tvAssociationSchool = view.findViewById(R.id.tv_association_school);
        scrollVP = view.findViewById(R.id.scrollVP);
        int width = getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams layoutParams = scrollVP.getLayoutParams();
        if (layoutParams == null)
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        if (currNav.type == NavBean.TYPE_FOURTH) {
            if (detailInfo.isJoin != 1) {
                //说明用户未加入社团，不让查看成员列表
                MyToast.show(R.string.member_info_no_oauth_tip, getApplicationContext());
                return;
            }
        }
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
    }

    private MyStickyNavHost.TabItem[] tabItems1;
    private MyStickyNavHost.TabItem[] tabItems2;

    private int[] tabStrs = {R.string.base_info, R.string.activity, R.string.photos, R.string.member};

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


    private class MyAssociationActivityAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (activityListInfo == null || activityListInfo.list == null) {
                return 0;
            } else {
                return activityListInfo.list.size();
            }
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
            ActivityItemViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_association_tab_activity, null);
                viewHolder = new ActivityItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ActivityItemViewHolder) convertView.getTag();
            }
            viewHolder.bindData(position);
            return convertView;
        }
    }

    private class MyAssociationMemberAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (memberListInfo == null || memberListInfo.list == null) {
                return 0;
            } else {
                return memberListInfo.list.size();
            }
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
            MemberItemViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_association_detail_members, null);
                viewHolder = new MemberItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MemberItemViewHolder) convertView.getTag();
            }
            viewHolder.bindData(position);
            return convertView;
        }

    }

    public class MemberItemViewHolder {
        private View itemView;
        @BindView(R.id.iv_head)
        ScaleImageView ivHead;
        @BindView(R.id.iv_gender)
        ImageView ivGender;
        @BindView(R.id.tv_member_name)
        TextView tvMemberName;
        @BindView(R.id.tv_member_info)
        TextView tvMemberInfo;


        public MemberItemViewHolder(View itemView) {
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }

        public void bindData(final int position) {
            //圆形裁剪
            final AssociationMemberListData.AssociationMember info = memberListInfo.list.get(position);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(MemberInfoActivity.getMemberInfoIntent(AssociationDetailActivity.this, MemberInfoActivity.TYPE_STUDENT, info.id));
                }
            });
            ivGender.setVisibility(TextUtils.isEmpty(info.sex)?View.GONE:View.VISIBLE);
            ivGender.setImageResource("0".equals(info.sex) ? R.mipmap.female : R.mipmap.male);

            tvMemberName.setText("1".equals(info.isnickname) ? ("1".equals(info.isname) ? info.realname + "(" + info.nickname + ")" : info.nickname) : info.realname);
            tvMemberInfo.setText(TextUtils.isEmpty(info.dutyName) ? getString(R.string.member) + "/" + info.schoolName + info.departmentName : info.dutyName + info.schoolName + info.departmentName);
            if (!TextUtils.isEmpty(info.avatar)) {
                RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
                options.placeholder(R.mipmap.default_head);
                options.error(R.mipmap.ic_img_thumbnail);
                Glide.with(itemView.getContext())
                        .load(TLSUrl.BASE_URL + info.avatar)
                        .apply(options)
                        .into(ivHead);
            }
        }
    }

    private class MyAssociationInfoAdapter extends BaseAdapter {
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_association_tab_info, null);
            TextView tvLeader = convertView.findViewById(R.id.tv_leader);
            TextView tvViceLeader = convertView.findViewById(R.id.tv_vice_leader);
            TextView tvTutor = convertView.findViewById(R.id.tv_tutor);
            TextView tvViceTutor = convertView.findViewById(R.id.tv_vice_tutor);
            TextView tvCreator = convertView.findViewById(R.id.tv_creator);
            TextView tvCreateTime = convertView.findViewById(R.id.tv_create_time);
            TextView tvDesc = convertView.findViewById(R.id.tv_desc);
            TextView tvReadNum = convertView.findViewById(R.id.tv_read_num);

            tvLeader.setText(getString(R.string.association_leader_holder, detailInfo.associationDetail.managerName));
            if (TextUtils.isEmpty(detailInfo.deputyName)) {
                tvViceLeader.setVisibility(View.GONE);
            } else {
                tvViceLeader.setText(getString(R.string.association_vice_leader_holder, detailInfo.deputyName));

            }
            tvTutor.setText(getString(R.string.association_tutor_holder, detailInfo.associationDetail.leaderTeacherName));
            if (TextUtils.isEmpty(detailInfo.teacherNames)) {
                tvViceTutor.setVisibility(View.GONE);
            } else {
                tvViceTutor.setText(getString(R.string.association_vice_tutor_holder, detailInfo.teacherNames));
            }
            tvCreator.setText(getString(R.string.association_creator_holder, detailInfo.associationDetail.creatorName));
            tvDesc.setText(getString(R.string.association_desc_holder, detailInfo.associationDetail.associationDesc));
            tvReadNum.setText(getString(R.string.association_read_num_holder, detailInfo.associationDetail.readNum));
            tvCreateTime.setText(getString(R.string.association_create_time_holder, TimeUtil.getDateTime(detailInfo.associationDetail.createtime)));
            return convertView;
        }

    }

    private class MyAssociationPhotoAdapter extends BaseAdapter {
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_association_tab_photos, null);
            RecyclerView rvPhotos = convertView.findViewById(R.id.rv_photos);
            rvPhotos.setLayoutManager(new GridLayoutManager(parent.getContext(), 3));
            rvPhotos.setAdapter(new MyPhotoAdapter());
            return convertView;
        }

    }

    private class MyPhotoAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_association_detail_photos, parent, false);
            PhotoViewHolder holder = new PhotoViewHolder(itemView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((PhotoViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            if (albumListInfo == null || albumListInfo.list == null) {
                return 0;
            } else {
                return albumListInfo.list.size();
            }
        }

    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image_view)
        ImageView imageView;
        @BindView(R.id.tv_album_count)
        TextView tvAlbumCount;
        @BindView(R.id.tv_activity_name)
        TextView tvActivityName;

        PhotoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(int position) {
            final AssociationAlbumListData.AssociationAlbum info = albumListInfo.list.get(position);
            tvActivityName.setText(info.activityName);
            tvAlbumCount.setText(info.counts);
            if (!TextUtils.isEmpty(info.cover)) {
                RequestOptions options = new RequestOptions().placeholder(R.mipmap.ic_img_thumbnail).error(R.mipmap.ic_img_thumbnail).dontAnimate();
                Glide.with(itemView.getContext())
                        .load(TLSUrl.BASE_URL + info.cover)
                        .apply(options)
                        .thumbnail(0.1f)
                        .into(imageView);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(AlbumListActivity.getAlbumIntent(AssociationDetailActivity.this, info.id + ""));
                }
            });
        }
    }


    public class ActivityItemViewHolder {
        private final View view;

        @BindView(R.id.iv_activity_logo)
        ImageView ivActivityLogo;
        @BindView(R.id.iv_activity_state)
        ImageView ivActivityState;
        @BindView(R.id.rl_activity_logo)
        RelativeLayout rlActivityLogo;
        @BindView(R.id.tv_activity_name)
        TextView tvActivityName;
        @BindView(R.id.tv_activity_address)
        TextView tvActivityAddress;
        @BindView(R.id.tv_activity_time)
        TextView tvActivityTime;
        @BindView(R.id.tv_activity_num)
        TextView tvActivityNum;

        ActivityItemViewHolder(View view) {
            ButterKnife.bind(this, view);
            this.view = view;
            int width = getResources().getDisplayMetrics().widthPixels;
            ViewGroup.LayoutParams layoutParams = ivActivityLogo.getLayoutParams();
            if (layoutParams == null)
                layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.width = width;
            layoutParams.height = width * 2 / 5;
            ivActivityLogo.setLayoutParams(layoutParams);

        }

        public void bindData(int position) {
            final AssociationActivityListData.AssociationActivity info = activityListInfo.list.get(position);
            long currentTime = GlobalParams.getCurrentTimeStamp();
            if (info.starttime < currentTime && info.endtime > currentTime) {
                //说明是进行中活动
                ivActivityState.setImageResource(R.mipmap.activity_start);
            } else if (info.endtime < currentTime) {
                //已结束
                ivActivityState.setImageResource(R.mipmap.activity_end);
            } else {
                ivActivityState.setImageResource(R.mipmap.activity_unstart);
            }
            tvActivityName.setText(info.activityName);
            tvActivityName.setCompoundDrawablesWithIntrinsicBounds("1".equals(info.level) ? R.mipmap.school_level : R.mipmap.college_level, 0, 0, 0);
            tvActivityAddress.setText(info.activityPlace);
            tvActivityNum.setText(info.counts + "/" + info.estimatedNumber);
            tvActivityTime.setText(TimeUtil.getActivityTime(info.starttime, info.endtime));
            if (!TextUtils.isEmpty(info.activityLogo.imgPath)) {
                RequestOptions optionsFirst = new RequestOptions();
                optionsFirst.override(ivActivityLogo.getLayoutParams().width, ivActivityLogo.getLayoutParams().height);
                optionsFirst.placeholder(R.mipmap.ic_img_thumbnail);
                optionsFirst.error(R.mipmap.ic_img_failure);
                Glide.with(getApplicationContext()).load(TLSUrl.BASE_URL + info.activityLogo.imgPath).apply(optionsFirst).thumbnail(0.1f).into(ivActivityLogo);
            } else {
                ivActivityLogo.setImageResource(R.mipmap.ic_img_thumbnail);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(ActivityDetailActivity.getActivityDetailIntent(AssociationDetailActivity.this, info.id));
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NavBean.TYPE_CURRENT = -1;
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
                entity.generateId = associationId;
                entity.generateAvatar = TLSUrl.BASE_URL + detailInfo.associationDetail.associationLogo;
                entity.name = detailInfo.associationDetail.associationName;
                startActivity(QRGenerateActivity.getQRGenerateIntent(this, QRCodeType.ASSOCIATION, entity));
                break;
            default:
                super.onClick(view);
                break;
        }
    }

    private int[] dialogResIds;
    private String[] dialogContents;
    private IndicatorDialog indicatorDialog;

    private void showShareDialog() {
        TypedArray ar = getResources().obtainTypedArray(R.array.association_jion_icons);
        int len = ar.length();
        dialogResIds = new int[len];
        for (int i = 0; i < len; i++)
            dialogResIds[i] = ar.getResourceId(i, 0);
        ar.recycle();
        dialogContents = getResources().getStringArray(R.array.association_jion_strs);
        RecyclerView.Adapter adapter = new ShareAdapter(detailInfo.isJoin);
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
        private int isJion;

        public ShareAdapter(int isJoin) {
            this.isJion = isJoin;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_share_view, parent, false);
//            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messge_center_list,parent,false);
//            TextView itemView = new TextView(parent.getContext());
            ShareViewHolder shareViewHolder = new ShareViewHolder(itemView);
            return shareViewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ShareViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return isJion == 1 ? 2 : 1;
        }
    }

    public class ShareViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_share)
        TextView tvContent;
        @BindView(R.id.view_divider)
        View viewDivider;

        public ShareViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(final int position) {
            tvContent.setText(dialogContents[position]);
            tvContent.setCompoundDrawablesWithIntrinsicBounds(dialogResIds[position], 0, 0, 0);
            viewDivider.setVisibility(position == dialogContents.length - 1 ? View.GONE : View.VISIBLE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    indicatorDialog.dismiss();
                    if (position == 0) {
                        //分享微信、QQ
                        MyForwardDialog dialog = new MyForwardDialog(AssociationDetailActivity.this, false).builder();
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
                                        MyToast.show(R.string.share_success, AssociationDetailActivity.this);
                                    }

                                    @Override
                                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                                        System.out.println("分享失败");
                                        MyToast.show(R.string.share_failed, AssociationDetailActivity.this);
                                        throwable.printStackTrace();
                                    }

                                    @Override
                                    public void onCancel(SHARE_MEDIA share_media) {
                                        System.out.println("分享取消");
//                                        MyToast.show(R.string.share_cancel,AssociationDetailActivity.this);

                                    }
                                };
                                UMWeb web = new UMWeb(TLSUrl.Forward.associationForwardUrl + associationId);
                                web.setTitle(detailInfo.associationDetail.associationName);//标题
                                UMImage umImage = new UMImage(AssociationDetailActivity.this, R.mipmap.tls_logo);
                                umImage.compressFormat = Bitmap.CompressFormat.PNG;
                                web.setThumb(umImage);  //缩略图
                                web.setDescription(detailInfo.associationDetail.associationDesc);//描述
                                if (type == MyForwardDialog.ForwardType.TYPE_QZONG) {
                                    //QQ空间分享
                                    new ShareAction(AssociationDetailActivity.this)
                                            .setPlatform(SHARE_MEDIA.QZONE)//传入平台
                                            .withText("hello")//分享内容
                                            .withMedia(web)
                                            .setCallback(umShareListener)//回调监听器
                                            .share();
                                } else if (type == MyForwardDialog.ForwardType.TYPE_WECHAT_MOMENT) {
                                    //微信朋友圈分享
                                    new ShareAction(AssociationDetailActivity.this)
                                            .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)//传入平台
                                            .withText("hello")//分享内容
                                            .withMedia(web)
                                            .setCallback(umShareListener)//回调监听器
                                            .share();
                                }
                            }
                        });
                        dialog.show();
                    } else {

                        if (detailInfo.associationDetail.managerId == GlobalParams.USER_INFO.id) {
                            MyToast.show(R.string.association_leader_cannot_leave, AssociationDetailActivity.this);
                            return;
                        }
                        //设置点击事件
                        Dialog dialog = new IosDialog.Builder(v.getContext())
                                .setMessage(R.string.confirm_leave_association).setMessageColor(ContextCompat.getColor(v.getContext(), R.color.dark_gray)).setMessageSize(15)
                                .setNegativeButtonColor(ContextCompat.getColor(v.getContext(), R.color.gray))
                                .setNegativeButtonSize(16)
                                .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                                    @Override
                                    public void onClick(IosDialog dialog, View v) {
                                        dialog.dismiss();
                                        //doSomething
                                    }
                                })
                                .setPositiveButtonColor(ContextCompat.getColor(v.getContext(), R.color.theme_color))
                                .setPositiveButtonSize(16)
                                .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                                    @Override
                                    public void onClick(IosDialog dialog, View v) {
                                        dialog.dismiss();
                                        leaveAssociation();
                                    }
                                }).build();
                        dialog.show();

                    }
                }
            });
        }
    }

    @OnClick({R.id.tv_follow, R.id.tv_multi})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_follow:
                if (TipDialogUtil.checkLogin(this)) {
                    if (GlobalParams.USER_INFO.isTeacher) {
                        MyToast.show(R.string.teacher_no_authorized, this);
                        return;
                    }
                    followAssociation();
                }
                break;
            case R.id.tv_multi:
                if (TipDialogUtil.checkLogin(this)) {
                    if (GlobalParams.USER_INFO.isTeacher) {
                        MyToast.show(R.string.teacher_no_authorized, this);
                        return;
                    }
                    if (detailInfo.isJoin == 1) {
                        if (detailInfo.associationDetail.managerId == GlobalParams.USER_INFO.id) {
                            MyToast.show(R.string.association_leader_cannot_leave, AssociationDetailActivity.this);
                            return;
                        }
                        leaveAssociation();
                    } else {
                        if (TipDialogUtil.checkFillInfo(this)) {
                            if(detailInfo.associationDetail.numLimit > 0 && detailInfo.associationDetail.numLimit == detailInfo.memberCount){
                                MyToast.show(R.string.association_member_full_tip,this);
                                return;
                            }
                            MyEditTextDialog dialog = new MyEditTextDialog(this, "入团理由", "", "最多可输入200个字符", 4, 200).builder();
                            dialog.setResultListener(new MyEditTextDialog.EditTextResultListener() {
                                @Override
                                public void onResult(String result) {
                                    if (TextUtils.isEmpty(result)) {
                                        MyToast.show(R.string.apply_jion_association_reason_null_tip, AssociationDetailActivity.this);
                                    }
                                    applyJionAssociation(result);
                                }
                            });
                            dialog.show();
                        }
                    }
                }
                break;
        }
    }

    private void leaveAssociation() {
        MyProgressDialog.show(this, false);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData data) {
                MyProgressDialog.dismiss();
                MyToast.show(R.string.leave_association_success, getApplicationContext());
                detailInfo.isApply = false;
                setMultiState(false, detailInfo.isApply);
            }

            @Override
            public void onError(String msg) {
                MyProgressDialog.dismiss();
                super.onError(msg);
            }
        };
        RequestEngine.getInstance().getServer(AssociationApiService.class).leaveAssociation(associationId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void applyJionAssociation(String reason) {
        MyProgressDialog.show(this, false);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData data) {
                MyProgressDialog.dismiss();
                detailInfo.isApply = true;
                setMultiState(detailInfo.isJoin == 1, detailInfo.isApply);
                MyToast.show(R.string.apply_association_success, getApplicationContext());
            }

            @Override
            public void onError(String msg) {
                MyProgressDialog.dismiss();
                super.onError(msg);
            }
        };
        RequestEngine.getInstance().getServer(AssociationApiService.class).applyJionAssociation(associationId, reason).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void followAssociation() {
        MyProgressDialog.show(this, false);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData data) {
                MyProgressDialog.dismiss();
                detailInfo.isfollow = detailInfo.isfollow == 1 ? 0 : 1;
                MyToast.show(detailInfo.isfollow == 1 ? R.string.has_follow : R.string.has_cancel_follow, getApplicationContext());
                setFollowState(detailInfo.isfollow == 1);
            }

            @Override
            public void onError(String msg) {
                MyProgressDialog.dismiss();
                super.onError(msg);
            }
        };
        RequestEngine.getInstance().getServer(AssociationApiService.class).followAssociation(associationId, detailInfo.isfollow == 1 ? "0" : "1").compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private AssociationActivityListData.AssociationActivityListInfo activityListInfo;
    private AssociationAlbumListData.AssociationAlbumListInfo albumListInfo;
    private AssociationMemberListData.AssociationMemberListInfo memberListInfo;

    private void requestTabData(int position) {
        if (position == 0) {
            //说明是基本信息不需要请求数据
            return;
        } else if (position == 1) {
            //说明是活动列表请求
            if (activityListInfo != null)
                return;
            requesActivityListData();
        } else if (position == 2) {
            //说明是相册列表请求
            if (albumListInfo != null)
                return;
            requesAlbumListData();
        } else {
            //说明是成员列表请求
            if (memberListInfo != null)
                return;
            requesMemberListData();
        }

    }

    private void requesActivityListData() {
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationActivityListData>(this) {
            @Override
            public void onSuccess(AssociationActivityListData listData) {
                activityListInfo = listData.result;
                activityAdapter.notifyDataSetChanged();
                MyProgressDialog.dismiss();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getWonderfulActivityList(associationId, 0, 0).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void requesAlbumListData() {
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationAlbumListData>(this) {
            @Override
            public void onSuccess(AssociationAlbumListData listData) {
                albumListInfo = listData.result;
                albumAdapter.notifyDataSetChanged();
                MyProgressDialog.dismiss();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getAssociationAlbumList(associationId, 0, 0).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void requesMemberListData() {
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationMemberListData>(this) {
            @Override
            public void onSuccess(AssociationMemberListData listData) {
                memberListInfo = listData.result;
                memberAdapter.notifyDataSetChanged();
                MyProgressDialog.dismiss();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).getAssociationMemberList(associationId, 0, 0).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

}
