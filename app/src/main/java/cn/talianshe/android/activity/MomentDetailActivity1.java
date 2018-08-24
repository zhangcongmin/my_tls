package cn.talianshe.android.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshViewFooter;
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
import cn.talianshe.android.widget.ExpandableTextView;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.MyXRefreshView;
import cn.talianshe.android.widget.ScaleImageView;
import cn.talianshe.android.widget.UnderlineBtn;
import library.talianshe.android.photobrowser.bean.PhotoBean;


/**
 * @author zcm
 * @ClassName: MomentDetailActivity
 * @Description: 动态详情页
 * @date TYPE_COMMENT0TYPE_SHARE7/TYPE_SHARETYPE_COMMENT/6 TYPE_SHARE4:TYPE_LIKE9
 */
public class MomentDetailActivity1 extends BaseActivity {

    private static final String EXTRA_MOM_ID = "extra_mom_id";
    @BindView(R.id.et_comment)
    TextView etComment;
    @BindView(R.id.tv_no_data)
    TextView tvNoData;
    @BindView(R.id.ll_comment)
    LinearLayout llComment;

    @BindView(R.id.rv_share_list)
    RecyclerView rvShareList;
    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_forward)
    TextView tvForward;
    @BindView(R.id.expandable_text)
    TextView expandableText;
    @BindView(R.id.expand_collapse)
    TextView expandCollapse;
    @BindView(R.id.tv_content)
    ExpandableTextView tvContent;
    @BindView(R.id.v1)
    ScaleImageView v1;
    @BindView(R.id.v2)
    ScaleImageView v2;
    @BindView(R.id.v3)
    ScaleImageView v3;
    @BindView(R.id.row1)
    TableRow row1;
    @BindView(R.id.v4)
    ScaleImageView v4;
    @BindView(R.id.v5)
    ScaleImageView v5;
    @BindView(R.id.v6)
    ScaleImageView v6;
    @BindView(R.id.row2)
    TableRow row2;
    @BindView(R.id.v7)
    ScaleImageView v7;
    @BindView(R.id.v8)
    ScaleImageView v8;
    @BindView(R.id.v9)
    ScaleImageView v9;
    @BindView(R.id.row3)
    TableRow row3;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.btn_share_num)
    UnderlineBtn btnShareNum;
    @BindView(R.id.btn_comment_num)
    UnderlineBtn btnCommentNum;
    @BindView(R.id.btn_up_num)
    UnderlineBtn btnUpNum;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.tv_send_comment)
    TextView tvSendComment;
//    @BindView(R.id.m_share_XRefreshView)
//    MyXRefreshView mShareXRefreshView;
    @BindView(R.id.m_share_XRefreshView)
    MyXRefreshView mShareXRefreshView;
    @BindView(R.id.rv_comment_list)
    RecyclerView rvCommentList;
    @BindView(R.id.m_comment_XRefreshView)
    MyXRefreshView mCommentXRefreshView;
    @BindView(R.id.rv_like_list)
    RecyclerView rvLikeList;
    @BindView(R.id.m_like_XRefreshView)
    MyXRefreshView mLikeXRefreshView;


    private String momentId;
    private SchoolApiService schoolApiService;

    private int curType;
    private static int TYPE_SHARE;
    private static int TYPE_COMMENT;
    private static int TYPE_LIKE;

    public static Intent getMomDetailIntent(Context context, String momentId) {
        Intent intent = new Intent(context, MomentDetailActivity1.class);
        intent.putExtra(EXTRA_MOM_ID, momentId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment_detail1);
        ButterKnife.bind(this);
        momentId = getIntent().getStringExtra(EXTRA_MOM_ID);
        initView();

        requestData();

//        initDefaultSelectedNav();//设置默认选择的导航tab
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
        swipeLayout.setEnabled(true);
        initAppBarListener();

        btnCommentNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShareXRefreshView.setVisibility(View.GONE);
                mCommentXRefreshView.setVisibility(View.VISIBLE);
            }
        });
        btnShareNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShareXRefreshView.setVisibility(View.VISIBLE);
                mCommentXRefreshView.setVisibility(View.GONE);
            }
        });
        swipeLayout.setEnabled(false);
        mCommentXRefreshView.setPullRefreshEnable(true);
        mCommentXRefreshView.setPullLoadEnable(true);
        mCommentXRefreshView.openPullRefreshIgnoreAppBarLayout();
        mCommentXRefreshView.setCustomFooterView(new XRefreshViewFooter(this));
        mCommentXRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener(){
            @Override
            public void onRefresh(boolean isPullDown) {
                size+=10;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rvCommentList.getAdapter().notifyDataSetChanged();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                rvShareList.scrollToPosition(10);
                            }
                        },500);
                        mCommentXRefreshView.stopRefresh();
                    }
                },2000);
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                size+=10;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rvCommentList.getAdapter().notifyDataSetChanged();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                rvShareList.scrollToPosition(10);
                            }
                        },500);
                        mCommentXRefreshView.stopLoadMore();
                    }
                },2000);
            }
        });
    }

    private void initAppBarListener() {
        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                swipeLayout.setEnabled(verticalOffset >= 0);
            }
        });
    }

    private void requestData() {
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<MomentDetailData>(this) {
            @Override
            public void onSuccess(MomentDetailData data) {
                MyProgressDialog.dismiss();
                fillHeadData(data.result);
//                commentList.clear();
//                for(MomentDetailData.Reviewer reviewer:data.result.reviewerList){
//                    ForwardLikeListData.ForwardCommentLikeInfo info =reviewer.castIntoForwardCommentLikeInfo();
//                    commentList.add(info);
//                }
                //继续请求获取转发人列表数据
//                requestForwardCommentLikeData(0);
                rvShareList.setLayoutManager(new LinearLayoutManager(MomentDetailActivity1.this));
                rvShareList.setAdapter(new MySCUAdapter(TYPE_SHARE));
                rvCommentList.setLayoutManager(new LinearLayoutManager(MomentDetailActivity1.this));
                rvCommentList.setAdapter(new MySCUAdapter(TYPE_SHARE));
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


    private void requestForwardCommentLikeData(final int type) {
        if (type == TYPE_SHARE) {
            if (forwardList.size() > 0) {
                return;
            }
        } else if (type == TYPE_COMMENT) {
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
                if (type == TYPE_SHARE) {
                    fillForwardData(listData.result);
                } else if (type == TYPE_COMMENT) {
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
        if (type == TYPE_SHARE) {
            schoolApiService.getForwardList(momentId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else if (type == TYPE_COMMENT) {
            schoolApiService.getCommentList(momentId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else {
            schoolApiService.getLikeList(momentId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        }
    }


    //----------------头布局相关----------------

    private void fillHeadData(MomentDetailData.MomentDetailInfo info) {
        tvTitle.setText("TYPE_SHARE".equals(info.isnickname) ? ("TYPE_SHARE".equals(info.isname) ? info.realname + "(" + info.nickname + ")" : info.nickname) : info.realname);
        tvForward.setText(info.forwardContent);
        tvForward.setVisibility(TextUtils.isEmpty(info.forwardContent) ? View.GONE : View.VISIBLE);
        tvContent.setText(info.content);
//        tvTime.setText(TimeUtil.getWeiboTime(Long.parseLong(info.createTime)));
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
                        .load(arrayList.get(TYPE_LIKE).imgUrl)
                        .apply(options)
                        .into(v4);
            case 3:
                v3.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(TYPE_COMMENT).imgUrl)
                        .apply(options)
                        .into(v3);
            case 2:
                v2.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(arrayList.get(TYPE_SHARE).imgUrl)
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
    }


    private void setEmptyTipState(int type) {

        if (type == TYPE_SHARE) {
            tvNoData.setVisibility(forwardList.size() == 0 ? View.VISIBLE : View.GONE);
        } else if (type == TYPE_COMMENT) {
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

    }

    /**
     * 填充评论列表数据
     *
     * @param info
     */
    private void fillCommentData(ForwardLikeListData.ForwardLikeListInfo info) {
        commentList.clear();
        commentList.addAll(info.list);
    }

    /**
     * 填充点赞列表数据
     *
     * @param info
     */
    private void fillLikeData(ForwardLikeListData.ForwardLikeListInfo info) {
        likeList.clear();
        likeList.addAll(info.list);
    }

    @OnClick(R.id.tv_send_comment)
    public void onViewClicked() {
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
                info.commentAvatar = GlobalParams.USER_INFO.avatar;
                info.commentId = GlobalParams.USER_INFO.id + "";
                info.commentContent = content;
                info.createtime = System.currentTimeMillis() + "";
                commentList.add(0, info);
                setEmptyTipState(TYPE_COMMENT);
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).operateMoment(momentId, content, type).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

    }


    /**
     * @author zcm
     * @ClassName: MySCUAdapter
     * @Description: S：share分享 C：comment评论 U:up赞
     * @date TYPE_COMMENT0TYPE_SHARE7/TYPE_SHARETYPE_COMMENT/7 9:TYPE_COMMENT6
     */
    private class MySCUAdapter extends RecyclerView.Adapter {

        private
        int type;

        public MySCUAdapter(int type) {
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
//            if (type == TYPE_SHARE) {
//                return forwardList.size();
//            } else if (type == TYPE_COMMENT) {
//                return commentList.size();
//            } else {
//                return likeList.size();
//            }
            return size;
        }
    }
    private int size = 20;

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
        int type;

        SCUViewHolder(View view, int type) {
            super(view);
            this.type = type;
            ButterKnife.bind(this, view);
        }

        public void bindData(int position) {
            if(size == 20){
                tvContent.setText("内容"+position);
            }else{
                if(position > 9){
                    tvContent.setText("内容"+(position-10));
                }else{
                    tvContent.setText("新内容");
                }
            }
//            llShareComment.setVisibility(type == TYPE_LIKE ? View.GONE : View.VISIBLE);
//            llUp.setVisibility(type != TYPE_LIKE? View.GONE : View.VISIBLE);
//            String imgUrl;
//            String realname;
//            String nickname;
//            String time = null;
//            String content = "";
//            String id;
//            if (type == TYPE_SHARE) {
//                imgUrl = forwardList.get(position).forwardAvatar;
//                nickname = "TYPE_SHARE".equals(forwardList.get(position).isnickname) ? forwardList.get(position).nickname : "";
//                realname = "TYPE_SHARE".equals(forwardList.get(position).isname) ? forwardList.get(position).realname : "";
//                time = forwardList.get(position).createtime;
//                content = forwardList.get(position).forwardContent;
//                id = forwardList.get(position).forwardId;
//            } else if (type == TYPE_COMMENT) {
//                imgUrl = commentList.get(position).forwardAvatar;
//                nickname = "TYPE_SHARE".equals(commentList.get(position).isnickname) ? commentList.get(position).nickname : "";
//                realname = "TYPE_SHARE".equals(commentList.get(position).isname) ? commentList.get(position).realname : "";
//                time = commentList.get(position).createtime;
//                content = commentList.get(position).commentContent;
//                id = commentList.get(position).commentId;
//            } else {
//                imgUrl = likeList.get(position).forwardAvatar;
//                nickname = "TYPE_SHARE".equals(likeList.get(position).isnickname) ? likeList.get(position).nickname : "";
//                realname = "TYPE_SHARE".equals(likeList.get(position).isname) ? likeList.get(position).realname : "";
//                id = likeList.get(position).likeId;
//            }
//            if (type != TYPE_LIKE) {
//                if ((GlobalParams.USER_INFO.id + "").equals(id)) {
//                    tvTitle.setText(R.string.me);
//                } else {
//                    tvTitle.setText(TextUtils.isEmpty(realname) ? nickname : TextUtils.isEmpty(nickname) ? realname : realname + "(" + nickname + ")");
//                }
//                if (!TextUtils.isEmpty(time)) {
//                    tvTime.setText(TimeUtil.getWeiboTime(Long.parseLong(time)));
//                }
//                tvContent.setText(type ==TYPE_SHARE ? (TextUtils.isEmpty(content) ? getString(R.string.forward) : content) : content);
//            } else {
//                if ((GlobalParams.USER_INFO.id + "").equals(id)) {
//                    tvUpTitle.setText(R.string.me);
//                } else {
//                    tvUpTitle.setText(TextUtils.isEmpty(realname) ? nickname : TextUtils.isEmpty(nickname) ? realname : realname + "(" + nickname + ")");
//
//                }
//            }
//            if (!TextUtils.isEmpty(imgUrl)) {
//                RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
//                options.override(getApplicationContext().getDrawable(R.mipmap.default_head).getIntrinsicWidth());
//                options.placeholder(R.mipmap.default_head);
//                options.error(R.mipmap.default_head);
//                Glide.with(getApplicationContext()).load(TLSUrl.BASE_URL + imgUrl).apply(options).into(ivHead);
//            } else {
//                ivHead.setImageResource(R.mipmap.default_head);
//            }
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
                    startActivity(new Intent(MomentDetailActivity1.this, ActivityDetailActivity.class));
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                .height(-TYPE_SHARE)
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
            viewDivider.setVisibility(position == dialogContents.length - TYPE_SHARE ? View.GONE : View.VISIBLE);
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
