package cn.talianshe.android.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bruce.stickynavigationbar.bean.NavBean;
import com.bruce.stickynavigationbar.listener.NavListViewScrollListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jiang.android.indicatordialog.IndicatorBuilder;
import com.jiang.android.indicatordialog.IndicatorDialog;
import com.wc.widget.dialog.IosDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.ForwardLikeListData;
import cn.talianshe.android.bean.MomentDetailData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.ExpandableTextView;
import cn.talianshe.android.widget.MyCustomStickyNavHost;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.ScaleImageView;
import cn.talianshe.android.widget.UnderlineBtn;
import library.talianshe.android.photobrowser.PhotoViewActivity;
import library.talianshe.android.photobrowser.bean.PhotoBean;

/**
 * @author zcm
 * @ClassName: MomentDetailActivity
 * @Description: 动态详情页
 * @date 2017/12/6 14:39
 */
public class MomentDetailActivity extends BaseActivity {

    private static final int NAV_LENGTH = 3;
    private static final String EXTRA_MOM_ID = "extra_mom_id";
    @BindView(R.id.et_comment)
    EditText etComment;
    @BindView(R.id.tv_no_data)
    TextView tvNoData;
    @BindView(R.id.ll_comment)
    LinearLayout llComment;
    private int STICKY_POSITION_IN_HEADER;

    @BindView(R.id.list_view)
    ListView mListView;
    @BindView(R.id.sticky_nav_layout)
    MyCustomStickyNavHost stickyNavHostRoot;//根布局中的导航栏，表现为ListView上滑吸附在顶部
    MyCustomStickyNavHost stickyNavHostHead;//添加在ListView的headerView中的导航栏
    List<UnderlineBtn> stickyRootItems = new ArrayList<>();
    List<UnderlineBtn> stickyHeadItems = new ArrayList<>();


    private SparseArray<NavBean> mNavs;
    private NavListViewScrollListener scrollListener;//给ListView设置的滑动事件，里面处理了导航栏的显示与隐藏
    private String momentId;
    private SchoolApiService schoolApiService;

    public static Intent getMomDetailIntent(Context context, String momentId) {
        Intent intent = new Intent(context, MomentDetailActivity.class);
        intent.putExtra(EXTRA_MOM_ID, momentId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment_detail);
        ButterKnife.bind(this);
        momentId = getIntent().getStringExtra(EXTRA_MOM_ID);
        initView();

        initNavsView();//初始化导航栏view

        requestData();

//        initDefaultSelectedNav();//设置默认选择的导航tab
    }

    private void requestData() {
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<MomentDetailData>(this) {
            @Override
            public void onSuccess(MomentDetailData data) {
//                MyProgressDialog.dismiss();
                fillHeadData(data.result);
//                commentList.clear();
//                for(MomentDetailData.Reviewer reviewer:data.result.reviewerList){
//                    ForwardLikeListData.ForwardCommentLikeInfo info =reviewer.castIntoForwardCommentLikeInfo();
//                    commentList.add(info);
//                }
                //继续请求获取转发人列表数据
                stickyRootItems.get(0).setText(data.result.forwardCount);
                stickyHeadItems.get(0).setText(data.result.forwardCount);
                stickyRootItems.get(1).setText(data.result.commentCount);
                stickyHeadItems.get(1).setText(data.result.commentCount);
                stickyRootItems.get(2).setText(data.result.likeCount);
                stickyHeadItems.get(2).setText(data.result.likeCount);
                initDefaultSelectedNav();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        schoolApiService = RequestEngine.getInstance().getServer(SchoolApiService.class);
        schoolApiService.getMomentDetail(momentId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void requestForwardCommentLikeData(@NavBean.TYPE final int type) {
        setEmptyTipState(type);
        if (type == NavBean.TYPE_FIRST) {
            if (forwardList.size() > 0) {
                return;
            }
        } else if (type == NavBean.TYPE_SECOND) {
            if (commentList.size() > 0) {
                return;
            }

        } else {
            if (likeList.size() > 0) {
                return;
            }
        }
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<ForwardLikeListData>(this) {
            @Override
            public void onSuccess(ForwardLikeListData listData) {
                MyProgressDialog.dismiss();
                if (type == NavBean.TYPE_FIRST) {
                    fillForwardData(listData.result);
                } else if (type == NavBean.TYPE_SECOND) {
                    fillCommentData(listData.result);
                } else {
                    fillLikeData(listData.result);
                }
                setEmptyTipState(type);
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        schoolApiService = RequestEngine.getInstance().getServer(SchoolApiService.class);
        if (type == NavBean.TYPE_FIRST) {
            schoolApiService.getForwardList(momentId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else if (type == NavBean.TYPE_SECOND) {
            schoolApiService.getCommentList(momentId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else {
            schoolApiService.getLikeList(momentId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        }
    }


    private void initDefaultSelectedNav() {
        onTabItemSelected(NavBean.TYPE_FIRST);//通过此方法可进行切换tab
    }

    private void initNavsView() {
        initNavsData();
        stickyNavHostRoot.setShowTopLine(false);


        NavBean[] sortedNavs = new NavBean[mNavs.size()];//指定导航栏的排列顺序
        sortedNavs[0] = mNavs.get(NavBean.TYPE_FIRST);
        sortedNavs[1] = mNavs.get(NavBean.TYPE_SECOND);
        sortedNavs[2] = mNavs.get(NavBean.TYPE_THIRD);

        scrollListener = new NavListViewScrollListener(stickyNavHostRoot, stickyNavHostHead);
        mListView.setOnScrollListener(scrollListener);//为listView设置滑动监听，内部处理了吸附view的显示与隐藏
    }

    protected void initNavsData() {
        mNavs = new SparseArray<>(NAV_LENGTH);
        mNavs.put(NavBean.TYPE_FIRST, new NavBean(NavBean.TYPE_FIRST, new MyActivityAdapter(NavBean.TYPE_FIRST)));
        mNavs.put(NavBean.TYPE_SECOND, new NavBean(NavBean.TYPE_SECOND, new MyActivityAdapter(NavBean.TYPE_SECOND)));
        mNavs.put(NavBean.TYPE_THIRD, new NavBean(NavBean.TYPE_THIRD, new MyActivityAdapter(NavBean.TYPE_THIRD)));
    }

    private void initView() {
        setTitle(R.string.moment_detail);
        //右侧分享按钮逻辑
        btnRight.setVisibility(View.GONE);
        btnRight.setBackgroundResource(R.mipmap.share_dot);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示分享弹框
                showShareDialog();
            }
        });

        stickyNavHostRoot.setVisibility(View.INVISIBLE);

        stickyRootItems.clear();
        stickyRootItems.add((UnderlineBtn) stickyNavHostRoot.findViewById(R.id.btn_share_num));
        stickyRootItems.add((UnderlineBtn) stickyNavHostRoot.findViewById(R.id.btn_comment_num));
        stickyRootItems.add((UnderlineBtn) stickyNavHostRoot.findViewById(R.id.btn_up_num));
        for (UnderlineBtn underlineBtn : stickyRootItems) {
            underlineBtn.setOnClickListener(stickyTabOnclickListener);
        }

        View headView = initHeadView();
        mListView.addHeaderView(headView);

        stickyNavHostHead = (MyCustomStickyNavHost) LayoutInflater.from(this).inflate(R.layout.item_moment_detail_sticky_tab, null);
        stickyNavHostHead.setVisibility(View.VISIBLE);

        stickyHeadItems.clear();
        stickyHeadItems.add((UnderlineBtn) stickyNavHostHead.findViewById(R.id.btn_share_num));
        stickyHeadItems.add((UnderlineBtn) stickyNavHostHead.findViewById(R.id.btn_comment_num));
        stickyHeadItems.add((UnderlineBtn) stickyNavHostHead.findViewById(R.id.btn_up_num));
        for (UnderlineBtn underlineBtn : stickyHeadItems) {
            underlineBtn.setOnClickListener(stickyTabOnclickListener);
        }

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
                }
            }
        });
    }

    private View.OnClickListener stickyTabOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_share_num:
                    System.out.println("分享按钮点击了");
                    onTabItemSelected(NavBean.TYPE_FIRST);
                    break;
                case R.id.btn_comment_num:
                    System.out.println("评论按钮点击了");
                    onTabItemSelected(NavBean.TYPE_SECOND);
                    break;
                case R.id.btn_up_num:
                    System.out.println("评论按钮点击了");
                    onTabItemSelected(NavBean.TYPE_THIRD);
                    break;
            }
        }
    };

    //----------------头布局相关----------------
    ImageView ivHead;
    TextView tvTitle;
    TextView tvForward;
    ExpandableTextView tvContent;
    TextView tvTime;
    ScaleImageView v1;
    ScaleImageView v2;
    ScaleImageView v3;
    ScaleImageView v4;
    ScaleImageView v5;
    ScaleImageView v6;
    ScaleImageView v7;
    ScaleImageView v8;
    ScaleImageView v9;

    private void fillHeadData(MomentDetailData.MomentDetailInfo info) {
        tvTitle.setText("1".equals(info.isnickname) ? ("1".equals(info.isname) ? info.realname + "(" + info.nickname + ")" : info.nickname) : info.realname);
        tvForward.setText(info.forwardContent);
        tvForward.setVisibility(TextUtils.isEmpty(info.forwardContent) ? View.GONE : View.VISIBLE);
        tvContent.setText(info.content);
        tvTime.setText(TimeUtil.getWeiboTime(info.createTime));
        setMomentImageViews(info);
        if (!TextUtils.isEmpty(info.avatar)) {
            RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
            options.override(this.getDrawable(R.mipmap.default_head).getIntrinsicWidth());
            options.placeholder(R.mipmap.default_head);
            options.error(R.mipmap.default_head);
            Glide.with(this).load(TLSUrl.BASE_URL + info.avatar).apply(options).into(ivHead);
        } else {
            ivHead.setImageResource(R.mipmap.default_head);
        }

    }

    ArrayList<PhotoBean> arrayList;

    private void setMomentImageViews(MomentDetailData.MomentDetailInfo momentInfo) {
        arrayList = new ArrayList<>();
        if (TextUtils.isEmpty(momentInfo.dynamicsImgs)) {
            //说明没有图片
        } else {
            String[] images = momentInfo.dynamicsImgs.split(",");
            for (int i = 0; i < images.length; i++) {
                PhotoBean photoBean = new PhotoBean();
                photoBean.imgUrl = TLSUrl.BASE_URL + images[i];
                arrayList.add(photoBean);
            }
        }
        v1.setVisibility(View.GONE);
        v2.setVisibility(View.GONE);
        v3.setVisibility(View.GONE);
        if (arrayList.size() <= 3) {
            v4.setVisibility(View.GONE);
            v5.setVisibility(View.GONE);
            v6.setVisibility(View.GONE);
            v7.setVisibility(View.GONE);
            v8.setVisibility(View.GONE);
            v9.setVisibility(View.GONE);

        } else if (arrayList.size() > 3 && arrayList.size() <= 6) {
            v4.setVisibility(View.INVISIBLE);
            v5.setVisibility(View.INVISIBLE);
            v6.setVisibility(View.INVISIBLE);
            v7.setVisibility(View.GONE);
            v8.setVisibility(View.GONE);
            v9.setVisibility(View.GONE);
        } else {
            v7.setVisibility(View.INVISIBLE);
            v8.setVisibility(View.INVISIBLE);
            v9.setVisibility(View.INVISIBLE);
        }
        RequestOptions options = new RequestOptions().placeholder(R.mipmap.ic_img_thumbnail).dontAnimate();
        switch (arrayList.size()) {
            case 9:
                v9.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(8).imgUrl)
                        .apply(options)
                        .into(v9);
            case 8:
                v8.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(7).imgUrl)
                        .apply(options)
                        .into(v8);
            case 7:
                v7.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(6).imgUrl)
                        .apply(options)
                        .into(v7);
            case 6:
                v6.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(5).imgUrl)
                        .apply(options)
                        .into(v6);
            case 5:
                v5.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(4).imgUrl)
                        .apply(options)
                        .into(v5);
            case 4:
                v4.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(3).imgUrl)
                        .apply(options)
                        .into(v4);
            case 3:
                v3.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(2).imgUrl)
                        .apply(options)
                        .into(v3);
            case 2:
                v2.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(1).imgUrl)
                        .apply(options)
                        .into(v2);
            case 1:
                v1.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(0).imgUrl)
                        .apply(options)
                        .into(v1);
                break;
        }
        v1.setOnClickListener(this);
        v2.setOnClickListener(this);
        v3.setOnClickListener(this);
        v4.setOnClickListener(this);
        v5.setOnClickListener(this);
        v6.setOnClickListener(this);
        v7.setOnClickListener(this);
        v8.setOnClickListener(this);
        v9.setOnClickListener(this);
    }

    //----------------头布局相关----------------

    private View initHeadView() {
        View view = LayoutInflater.from(this).inflate(R.layout.view_moment_detail_head, null);
        ivHead = view.findViewById(R.id.iv_head);
        tvTitle = view.findViewById(R.id.tv_title);
        tvForward = view.findViewById(R.id.tv_forward);
        tvContent = view.findViewById(R.id.tv_content);
        tvTime = view.findViewById(R.id.tv_time);
        v1 = view.findViewById(R.id.v1);
        v2 = view.findViewById(R.id.v2);
        v3 = view.findViewById(R.id.v3);
        v4 = view.findViewById(R.id.v4);
        v5 = view.findViewById(R.id.v5);
        v6 = view.findViewById(R.id.v6);
        v7 = view.findViewById(R.id.v7);
        v8 = view.findViewById(R.id.v8);
        v9 = view.findViewById(R.id.v9);
        return view;
    }


    public void onTabItemSelected(@NavBean.TYPE int type) {
        NavBean currNav = mNavs.get(type);
        if (currNav.type == NavBean.TYPE_CURRENT)//等于当前选中的tab，可以屏蔽掉
            return;
        requestForwardCommentLikeData(type);
        scrollListener.setNav(currNav);
        setItemChecked(currNav.type);
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
        for (int i = 0; i < stickyRootItems.size(); i++) {
            stickyRootItems.get(i).setChecked(i == position);
        }
        for (int i = 0; i < stickyHeadItems.size(); i++) {
            stickyHeadItems.get(i).setChecked(i == position);
        }
        llComment.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        etComment.setHint(getString(R.string.input_moment_detail_comment_tip, commentList.size()));
    }

    private void setEmptyTipState(@NavBean.TYPE int type) {

        if (type == NavBean.TYPE_FIRST) {
            tvNoData.setVisibility(forwardList.size() == 0 ? View.VISIBLE : View.GONE);
        } else if (type == NavBean.TYPE_SECOND) {
            tvNoData.setVisibility(commentList.size() == 0 ? View.VISIBLE : View.GONE);
            etComment.setHint(getString(R.string.input_moment_detail_comment_tip, commentList.size()));

        } else {
            tvNoData.setVisibility(likeList.size() == 0 ? View.VISIBLE : View.GONE);
        }
    }


    private List<ForwardLikeListData.ForwardCommentLikeInfo> forwardList = new ArrayList<>();
    private List<ForwardLikeListData.ForwardCommentLikeInfo> commentList = new ArrayList<>();
    private List<ForwardLikeListData.ForwardCommentLikeInfo> likeList = new ArrayList<>();

    /**
     * 填充转发列表数据
     *
     * @param info
     */
    private void fillForwardData(ForwardLikeListData.ForwardLikeListInfo info) {
        forwardList.clear();
        forwardList.addAll(info.list);
        mNavs.get(NavBean.TYPE_FIRST).adapter.notifyDataSetChanged();
        stickyRootItems.get(0).setText(forwardList.size() + "");
        stickyHeadItems.get(0).setText(forwardList.size() + "");
    }

    /**
     * 填充评论列表数据
     *
     * @param info
     */
    private void fillCommentData(ForwardLikeListData.ForwardLikeListInfo info) {
        commentList.clear();
        commentList.addAll(info.list);
        mNavs.get(NavBean.TYPE_SECOND).adapter.notifyDataSetChanged();
        stickyRootItems.get(1).setText(commentList.size() + "");
        stickyHeadItems.get(1).setText(commentList.size() + "");
    }

    /**
     * 填充点赞列表数据
     *
     * @param info
     */
    private void fillLikeData(ForwardLikeListData.ForwardLikeListInfo info) {
        likeList.clear();
        likeList.addAll(info.list);
        mNavs.get(NavBean.TYPE_THIRD).adapter.notifyDataSetChanged();
        stickyRootItems.get(2).setText(likeList.size() + "");
        stickyHeadItems.get(2).setText(likeList.size() + "");
    }

    @OnClick(R.id.tv_send_comment)
    public void onViewClicked() {
        if (TipDialogUtil.checkFillInfo(this)) {
            final String content = etComment.getText().toString();
            if (TextUtils.isEmpty(content)) {
                MyToast.show(R.string.comment_null_tip, this);
                return;
            }
            MyProgressDialog.show(this, false);
            final String type = "3";
            HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
                @Override
                public void onSuccess(StringData data) {
                    MyProgressDialog.dismiss();

                    ForwardLikeListData.ForwardCommentLikeInfo info = new ForwardLikeListData.ForwardCommentLikeInfo();
                    info.forwardAvatar = GlobalParams.USER_INFO.avatar;
                    info.commentId = GlobalParams.USER_INFO.id + "";
                    info.commentContent = content;
                    info.createtime = System.currentTimeMillis() + "";
                    etComment.setText("");
                    commentList.add(0, info);
                    mNavs.get(1).adapter.notifyDataSetChanged();
                    setEmptyTipState(NavBean.TYPE_SECOND);
                    stickyRootItems.get(1).setText(commentList.size() + "");
                    stickyHeadItems.get(1).setText(commentList.size() + "");
                }

                @Override
                public void onError(String msg) {
                    super.onError(msg);
                    MyProgressDialog.dismiss();
                }
            };
            RequestEngine.getInstance().getServer(SchoolApiService.class).operateMoment(momentId, content, type).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        }
    }

    private class MyActivityAdapter extends BaseAdapter {
        private int type;

        public MyActivityAdapter(@NavBean.TYPE int type) {
            this.type = type;
        }

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
            RecyclerView rvPhotos = new RecyclerView(parent.getContext());
            LinearLayoutManager layoutManager = new LinearLayoutManager(parent.getContext());
            rvPhotos.setLayoutManager(layoutManager);
            rvPhotos.setAdapter(new MySCUAdapter(type));
//            layoutManager.setSmoothScrollbarEnabled(true);
//            layoutManager.setAutoMeasureEnabled(true);
//
//            rvPhotos.setHasFixedSize(true);
//            rvPhotos.setNestedScrollingEnabled(false);
//            rvPhotos.setNestedScrollingEnabled(false);
            return rvPhotos;
        }

    }


    /**
     * @author zcm
     * @ClassName: MySCUAdapter
     * @Description: S：share分享 C：comment评论 U:up赞
     * @date 2017/12/7 9:26
     */
    private class MySCUAdapter extends RecyclerView.Adapter {

        private
        @NavBean.TYPE
        int type;

        public MySCUAdapter(@NavBean.TYPE int type) {
            this.type = type;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_moment_detail_list, parent, false);
            SCUViewHolder holder = new SCUViewHolder(itemView, type);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((SCUViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            if (type == NavBean.TYPE_FIRST) {
                return forwardList.size();
            } else if (type == NavBean.TYPE_SECOND) {
                return commentList.size();
            } else {
                return likeList.size();
            }
        }

    }

    public class SCUViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_head)
        ScaleImageView ivHead;

        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_content)
        TextView tvContent;
        @BindView(R.id.tv_up_title)
        TextView tvUpTitle;
        @BindView(R.id.ll_share_comment)
        LinearLayout llShareComment;
        @BindView(R.id.ll_up)
        LinearLayout llUp;
        private
        @NavBean.TYPE
        int type;

        SCUViewHolder(View view, int type) {
            super(view);
            this.type = type;
            ButterKnife.bind(this, view);
        }

        public void bindData(int position) {
            llShareComment.setVisibility(type == NavBean.TYPE_THIRD ? View.GONE : View.VISIBLE);
            llUp.setVisibility(type != NavBean.TYPE_THIRD ? View.GONE : View.VISIBLE);
            String imgUrl;
            String realname;
            String nickname;
            String time = null;
            String content = "";
            String id;
            if (type == NavBean.TYPE_FIRST) {
                imgUrl = forwardList.get(position).forwardAvatar;
                nickname = "1".equals(forwardList.get(position).isnickname) ? forwardList.get(position).nickname : "";
                realname = "1".equals(forwardList.get(position).isname) ? forwardList.get(position).realname : "";
                time = forwardList.get(position).createtime;
                content = forwardList.get(position).forwardContent;
                id = forwardList.get(position).forwardId;
            } else if (type == NavBean.TYPE_SECOND) {
                imgUrl = commentList.get(position).forwardAvatar;

                nickname = "1".equals(commentList.get(position).isnickname) ? commentList.get(position).nickname : "";
                realname = "1".equals(commentList.get(position).isname) ? commentList.get(position).realname : "";
                time = commentList.get(position).createtime;
                content = commentList.get(position).commentContent;
                id = commentList.get(position).commentId;
            } else {
                imgUrl = likeList.get(position).likeAvatar;
                nickname = "1".equals(likeList.get(position).isnickname) ? likeList.get(position).nickname : "";
                realname = "1".equals(likeList.get(position).isname) ? likeList.get(position).realname : "";
                id = likeList.get(position).likeId;
            }
            if (type != NavBean.TYPE_THIRD) {
                if ((GlobalParams.USER_INFO.id + "").equals(id)) {
                    tvTitle.setText(R.string.me);
                } else {
                    tvTitle.setText(TextUtils.isEmpty(realname) ? nickname : TextUtils.isEmpty(nickname) ? realname : realname + "(" + nickname + ")");
                }
                if (!TextUtils.isEmpty(time)) {
                    tvTime.setText(TimeUtil.getWeiboTime(Long.parseLong(time)));
                }
                tvContent.setText(type == NavBean.TYPE_FIRST ? (TextUtils.isEmpty(content) ? getString(R.string.forward) : content) : content);
            } else {
                if ((GlobalParams.USER_INFO.id + "").equals(id)) {
                    tvUpTitle.setText(R.string.me);
                } else {
                    tvUpTitle.setText(TextUtils.isEmpty(realname) ? nickname : TextUtils.isEmpty(nickname) ? realname : nickname + "(" + realname + ")");
                }
            }
            if (!TextUtils.isEmpty(imgUrl)) {
                RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
                options.override(getApplicationContext().getDrawable(R.mipmap.default_head).getIntrinsicWidth());
                options.placeholder(R.mipmap.default_head);
                options.error(R.mipmap.default_head);
                Glide.with(getApplicationContext()).load(TLSUrl.BASE_URL + imgUrl).apply(options).into(ivHead);
            } else {
                ivHead.setImageResource(R.mipmap.default_head);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    startActivity(new Intent(MomentDetailActivity.this, MemberInfoActivity.class));
                }
            });
        }
    }


    public class ActivityItemViewHolder {
        private final View view;
        @BindView(R.id.iv_activity_logo)
        ImageView ivActivityLogo;
        @BindView(R.id.rl_activity_logo)
        RelativeLayout rlActivityLogo;

        ActivityItemViewHolder(View view) {
            ButterKnife.bind(this, view);
            this.view = view;
        }

        public void bindData(int position) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MomentDetailActivity.this, ActivityDetailActivity.class));
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
                Intent noticeListIntent = new Intent(this, NoticeListActivity.class);
                startActivity(noticeListIntent);
                break;
            case R.id.tv_marquee:
                //打开单条公告详情
                break;
            case R.id.v1:
                PhotoViewActivity.startPhotoViewActivity(this, arrayList, view, 0);
                break;
            case R.id.v2:
                PhotoViewActivity.startPhotoViewActivity(this, arrayList, view, 1);
                break;
            case R.id.v3:
                PhotoViewActivity.startPhotoViewActivity(this, arrayList, view, 2);
                break;
            case R.id.v4:
                PhotoViewActivity.startPhotoViewActivity(this, arrayList, view, 3);
                break;
            case R.id.v5:
                PhotoViewActivity.startPhotoViewActivity(this, arrayList, view, 4);
                break;
            case R.id.v6:
                PhotoViewActivity.startPhotoViewActivity(this, arrayList, view, 5);
                break;
            case R.id.v7:
                PhotoViewActivity.startPhotoViewActivity(this, arrayList, view, 6);
                break;
            case R.id.v8:
                PhotoViewActivity.startPhotoViewActivity(this, arrayList, view, 7);
                break;
            case R.id.v9:
                PhotoViewActivity.startPhotoViewActivity(this, arrayList, view, 8);
                break;
            default:
                super.onClick(view);
                break;
        }
    }

    private int[] dialogResIds;
    private String[] dialogContents;

    private void showShareDialog() {
        TypedArray ar = getResources().obtainTypedArray(R.array.association_jion_icons);
        int len = ar.length();
        dialogResIds = new int[len];
        for (int i = 0; i < len; i++)
            dialogResIds[i] = ar.getResourceId(i, 0);
        ar.recycle();
        dialogContents = getResources().getStringArray(R.array.association_jion_strs);
        RecyclerView.Adapter adapter = new ShareAdapter();
        IndicatorDialog dialog = new IndicatorBuilder(this)
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
        dialog.setCanceledOnTouchOutside(true);
        dialog.show(btnRight);
    }

    private class ShareAdapter extends RecyclerView.Adapter {
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
            return dialogContents.length;
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
                    //设置点击事件
                    Dialog dialog = new IosDialog.Builder(v.getContext())
                            .setMessage("确定要报名吗").setMessageColor(ContextCompat.getColor(v.getContext(), R.color.dark_gray)).setMessageSize(15)
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
                                    //doSomething
                                    finish();
                                }
                            }).build();
                    dialog.show();

                }
            });
        }
    }
}
