package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshViewFooter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.AssociationActivityListData;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.MyXRefreshView;
import cn.talianshe.android.widget.RecyclerViewItemClickListener;
import cn.talianshe.android.widget.UnderlineBtn;
import cn.talianshe.android.widget.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

/**
 * @author zcm
 * @ClassName: ActivityListActivity
 * @Description: 活动列表页
 * @date 2017/11/23 14:25
 */
public class ActivityListActivity extends BaseActivity {
    private static final String EXTRA_IS_FOLLOW = "extra_is_follow";

    @BindView(R.id.rv_all_activity)
    RecyclerView rvAllActivity;
    @BindView(R.id.ll_mine)
    LinearLayout llMine;
    @BindView(R.id.btn_activity_follow)
    UnderlineBtn btnActivityFollow;
    @BindView(R.id.btn_activity_jion)
    UnderlineBtn btnActivityJion;
    @BindView(R.id.rv_follow_activity)
    RecyclerView rvFollowActivity;
    @BindView(R.id.rv_jion_activity)
    RecyclerView rvJionActivity;
    @BindView(R.id.tv_no_data)
    TextView tvNoData;
    @BindView(R.id.ll_activities)
    RelativeLayout llActivities;
    @BindView(R.id.x_all_refresh_view)
    MyXRefreshView xAllRefreshView;
    @BindView(R.id.x_follow_refresh_view)
    MyXRefreshView xFollowRefreshView;
    @BindView(R.id.x_jion_refresh_view)
    MyXRefreshView xJionRefreshView;

    private boolean isFollow = false;
    private int curTab;
    private static final int TAB_ALL = 1;
    private static final int TAB_FOLLOW = 2;
    private static final int TAB_JION = 3;
    private MyActivityListAdapter allActivityAdapter;
    private MyActivityListAdapter followActivityAdapter;
    private MyActivityListAdapter jionActivityAdapter;

    public static Intent getFollowActivityIntent(Context context) {
        Intent intent = new Intent(context, ActivityListActivity.class);
        intent.putExtra(EXTRA_IS_FOLLOW, true);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_list);
        ButterKnife.bind(this);
        initData();
    }


    private void initData() {
        isFollow = getIntent().getBooleanExtra(EXTRA_IS_FOLLOW, false);
        if (isFollow) {
            curTab = TAB_FOLLOW;
        } else {
            curTab = TAB_ALL;
        }

        btnRight.setVisibility(View.VISIBLE);
        btnRight.setBackgroundResource(R.mipmap.search_black);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TipDialogUtil.checkLogin(ActivityListActivity.this)){
                    startActivity(SearchActivityOrAssociationActivity.getSearchIntent(ActivityListActivity.this, SearchActivityOrAssociationActivity.TYPE_ACTIVITY));
                }

            }
        });
        swipeLayout.setEnabled(true);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
        initTitleCenterView();
        xAllRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onLoadMore(boolean b) {
                HttpSubscriber httpSubscriber = new ActivityListSubscriber(ActivityListActivity.this, TAB_ALL);
                RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityList(activityAllInfo.curPage + 1, activityAllInfo.pageSize, null, GlobalParams.SCHOOL_ID, null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

            }
        });

        xFollowRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onLoadMore(boolean b) {
                HttpSubscriber httpSubscriber = new ActivityListSubscriber(ActivityListActivity.this, TAB_FOLLOW);
                RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityList(activityFollowInfo.curPage + 1, activityFollowInfo.pageSize, "1", GlobalParams.SCHOOL_ID, null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
            }
        });
        xJionRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onLoadMore(boolean b) {
                HttpSubscriber httpSubscriber = new ActivityListSubscriber(ActivityListActivity.this, TAB_JION);
                RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityList(activityJionInfo.curPage + 1, activityJionInfo.pageSize, "2", GlobalParams.SCHOOL_ID, null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
            }
        });

        swipeLayout.setRefreshing(true);
        requestData();
    }

    private AssociationActivityListData.AssociationActivityListInfo activityAllInfo;
    private AssociationActivityListData.AssociationActivityListInfo activityFollowInfo;
    private AssociationActivityListData.AssociationActivityListInfo activityJionInfo;

    private List<AssociationActivityListData.AssociationActivity> activityAllInfoList = new ArrayList<>();
    private List<AssociationActivityListData.AssociationActivity> activityFollowInfoList = new ArrayList<>();
    private List<AssociationActivityListData.AssociationActivity> activityJionInfoList = new ArrayList<>();


    private void requestData() {
        String type = null;
        switch (curTab) {
            case TAB_ALL:
                type = null;
                break;
            case TAB_JION:
                type = "2";
                break;
            case TAB_FOLLOW:
                type = "1";
                break;
        }
        HttpSubscriber httpSubscriber = new ActivityListSubscriber(this, curTab);
        RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityList(0, 20, type, GlobalParams.SCHOOL_ID, null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

    }


    public class ActivityListSubscriber extends HttpSubscriber<AssociationActivityListData> {
        private int tab;

        public ActivityListSubscriber(Context context, int tab) {
            super(context);
            this.tab = tab;
        }

        @Override
        public void onSuccess(AssociationActivityListData listData) {
            switch (tab) {
                case TAB_ALL:
                    if (!swipeLayout.isRefreshing()) {
                        activityAllInfo.curPage = activityAllInfo.curPage + 1;
                        activityAllInfoList.addAll(listData.result.list);
                        xAllRefreshView.stopLoadMore();
                    } else {
                        swipeLayout.setRefreshing(false);
                        activityAllInfo = listData.result;
                        activityAllInfo.curPage = 0;
                        activityAllInfoList.clear();
                        activityAllInfoList.addAll(listData.result.list);
                        if (activityAllInfoList.size() == 0 && tab == curTab) {
                            tvNoData.setVisibility(View.VISIBLE);
                        } else {
                            tvNoData.setVisibility(View.GONE);
                        }
                        allActivityAdapter = new MyActivityListAdapter(tab);
                        rvAllActivity.setLayoutManager(new LinearLayoutManager(ActivityListActivity.this));
                        rvAllActivity.setAdapter(allActivityAdapter);
                        xAllRefreshView.setCustomFooterView(new XRefreshViewFooter(ActivityListActivity.this));
                    }
                    activityAllInfo.pageSize = 20;
                    if ((activityAllInfo.curPage + 1) * activityAllInfo.pageSize >= activityAllInfo.totalCount) {
                        //说明没有加载更多,禁用
                        xAllRefreshView.setPullLoadEnable(false);
                    } else {
                        xAllRefreshView.setPullLoadEnable(true);
                    }
                    allActivityAdapter.notifyDataSetChanged();
                    break;
                case TAB_JION:
                    if (!swipeLayout.isRefreshing()) {
                        activityJionInfo.curPage = activityJionInfo.curPage + 1;
                        activityJionInfoList.addAll(listData.result.list);
                        xJionRefreshView.stopLoadMore();
                    } else {
                        swipeLayout.setRefreshing(false);
                        activityJionInfo = listData.result;
                        activityJionInfo.curPage = 0;
                        activityJionInfoList.clear();
                        activityJionInfoList.addAll(listData.result.list);
                        if (activityJionInfoList.size() == 0 && tab == curTab) {
                            tvNoData.setVisibility(View.VISIBLE);
                        } else {
                            tvNoData.setVisibility(View.GONE);
                        }
                        jionActivityAdapter = new MyActivityListAdapter(tab);
                        rvJionActivity.setLayoutManager(new LinearLayoutManager(ActivityListActivity.this));
                        rvJionActivity.setAdapter(jionActivityAdapter);
                        xJionRefreshView.setCustomFooterView(new XRefreshViewFooter(ActivityListActivity.this));
                    }
                    activityJionInfo.pageSize = 20;
                    if ((activityJionInfo.curPage + 1) * activityJionInfo.pageSize >= activityJionInfo.totalCount) {
                        //说明没有加载更多,禁用
                        xJionRefreshView.setPullLoadEnable(false);
                    } else {
                        xJionRefreshView.setPullLoadEnable(true);
                    }
                    jionActivityAdapter.notifyDataSetChanged();
                    break;
                case TAB_FOLLOW:
                    if (!swipeLayout.isRefreshing()) {
                        activityFollowInfo.curPage = activityFollowInfo.curPage + 1;
                        activityFollowInfoList.addAll(listData.result.list);
                        xFollowRefreshView.stopLoadMore();
                    } else {
                        swipeLayout.setRefreshing(false);
                        activityFollowInfo = listData.result;
                        activityFollowInfo.curPage = 0;
                        activityFollowInfoList.clear();
                        activityFollowInfoList.addAll(listData.result.list);
                        if (activityFollowInfoList.size() == 0 && tab == curTab) {
                            tvNoData.setVisibility(View.VISIBLE);
                        } else {
                            tvNoData.setVisibility(View.GONE);
                        }
                        followActivityAdapter = new MyActivityListAdapter(tab);
                        rvFollowActivity.setLayoutManager(new LinearLayoutManager(ActivityListActivity.this));
                        rvFollowActivity.setAdapter(followActivityAdapter);
                        xFollowRefreshView.setCustomFooterView(new XRefreshViewFooter(ActivityListActivity.this));
                    }
                    activityFollowInfo.pageSize = 20;
                    if ((activityFollowInfo.curPage + 1) * activityFollowInfo.pageSize >= activityFollowInfo.totalCount) {
                        //说明没有加载更多,禁用
                        xFollowRefreshView.setPullLoadEnable(false);
                    } else {
                        xFollowRefreshView.setPullLoadEnable(true);
                    }
                    followActivityAdapter.notifyDataSetChanged();
                    break;
            }
        }

        @Override
        public void onError(String msg) {
            super.onError(msg);
            swipeLayout.setRefreshing(false);
            if (xAllRefreshView.mPullLoading) {
                xAllRefreshView.stopLoadMore();
            }
            if (xFollowRefreshView.mPullLoading) {
                xFollowRefreshView.stopLoadMore();
            }
            if (xJionRefreshView.mPullLoading) {
                xJionRefreshView.stopLoadMore();
            }
        }
    }


    private TextView tvLeftTab;
    private TextView tvRightTab;

    private void initTitleCenterView() {
        View titleView = setTitleBarCenterView(R.layout.view_title_center_tab);
//        View titleView = View.inflate(mActivity, R.layout.view_title_center_tab, null);
        tvLeftTab = titleView.findViewById(R.id.tv_left_tab);
        tvLeftTab.setText(R.string.activity_all);

        tvRightTab = titleView.findViewById(R.id.tv_rightt_tab);
        tvRightTab.setText(R.string.activity_mine);
        tvLeftTab.setOnClickListener(this);
        tvRightTab.setOnClickListener(this);
        if (isFollow) {
            tvLeftTab.setSelected(false);
            tvRightTab.setSelected(true);
            llMine.setVisibility(View.VISIBLE);
            xFollowRefreshView.setVisibility(View.VISIBLE);
            xJionRefreshView.setVisibility(View.GONE);
        } else {
            tvLeftTab.setSelected(true);
            tvRightTab.setSelected(false);
            llMine.setVisibility(View.GONE);
            xFollowRefreshView.setVisibility(View.VISIBLE);
            xFollowRefreshView.setVisibility(View.GONE);
            xJionRefreshView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_left_tab:
                //左边tab全部活动被点击
                if (tvLeftTab.isSelected()) {
                    return;
                }
                llMine.setVisibility(View.GONE);
                curTab = TAB_ALL;
                if(activityAllInfo == null){
                    swipeLayout.setRefreshing(true);
                    requestData();
                }
                xAllRefreshView.setVisibility(View.VISIBLE);
                xFollowRefreshView.setVisibility(View.GONE);
                xJionRefreshView.setVisibility(View.GONE);
                tvLeftTab.setSelected(true);
                tvRightTab.setSelected(false);
                break;
            case R.id.tv_rightt_tab:
                //右边tab我的活动被点击
                if (!TipDialogUtil.checkLogin(this)) {
                    return;
                }
                if (tvRightTab.isSelected()) {
                    return;
                }
                if(tvLeftTab.isSelected()){
                    curTab = TAB_FOLLOW;
                    if(activityFollowInfo == null){
                        swipeLayout.setRefreshing(true);
                        requestData();
                    }
                }else {
                   curTab = TAB_JION;
                    if(activityJionInfo == null){
                        swipeLayout.setRefreshing(true);
                        requestData();
                    }
                }

                if(activityFollowInfo == null){

                }
                llMine.setVisibility(View.VISIBLE);

                xAllRefreshView.setVisibility(View.GONE);
                if (btnActivityFollow.isChecked()) {
                    xFollowRefreshView.setVisibility(View.VISIBLE);
                    xJionRefreshView.setVisibility(View.GONE);
                } else {
                    xFollowRefreshView.setVisibility(View.GONE);
                    xJionRefreshView.setVisibility(View.VISIBLE);
                }
                tvRightTab.setSelected(true);
                tvLeftTab.setSelected(false);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @OnClick({R.id.btn_activity_follow, R.id.btn_activity_jion})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_activity_follow:
                if (btnActivityFollow.isChecked()) {
                    return;
                }
                curTab = TAB_FOLLOW;
                btnActivityFollow.setChecked(true);
                btnActivityJion.setChecked(false);
                xAllRefreshView.setVisibility(View.GONE);
                xFollowRefreshView.setVisibility(View.VISIBLE);
                xJionRefreshView.setVisibility(View.GONE);
                break;
            case R.id.btn_activity_jion:
                if (btnActivityJion.isChecked()) {
                    return;
                }
                curTab = TAB_JION;
                if(activityJionInfo == null){
                    swipeLayout.setRefreshing(true);
                    requestData();
                }
                btnActivityFollow.setChecked(false);
                btnActivityJion.setChecked(true);
                xAllRefreshView.setVisibility(View.GONE);
                xFollowRefreshView.setVisibility(View.GONE);
                xJionRefreshView.setVisibility(View.VISIBLE);
                break;
        }
    }

    public class MyActivityListAdapter extends RecyclerView.Adapter {
        private int tab;

        public MyActivityListAdapter(int tab) {
            this.tab = tab;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity_list, parent, false);
            ActivityItemHolder holder = new ActivityItemHolder(view, tab);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ActivityItemHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            if (tab == TAB_ALL) {
                return activityAllInfoList.size();
            } else if (tab == TAB_FOLLOW) {
                return activityFollowInfoList.size();
            } else {
                return activityJionInfoList.size();
            }
        }
    }

    public class ActivityItemHolder extends RecyclerView.ViewHolder {
        private int tab;

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
        @BindView(R.id.tv_activity_manager)
        TextView tvActivityManager;

        public ActivityItemHolder(View view, int tab) {
            super(view);
            ButterKnife.bind(this, view);
            this.tab = tab;
            int width = getResources().getDisplayMetrics().widthPixels;
            ViewGroup.LayoutParams layoutParams = ivActivityLogo.getLayoutParams();
            if (layoutParams == null)
                layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.width = width;
            layoutParams.height = width * 2 / 5;
            ivActivityLogo.setLayoutParams(layoutParams);
        }

        public void bindData(int position) {
            final AssociationActivityListData.AssociationActivity info;
            if (tab == TAB_ALL) {
                info = activityAllInfoList.get(position);
            } else if (tab == TAB_FOLLOW) {
                info = activityFollowInfoList.get(position);
            } else {
                info = activityJionInfoList.get(position);
            }
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
            if (info.activityLogo != null && !TextUtils.isEmpty(info.activityLogo.imgPath)) {
                RequestOptions optionsFirst = new RequestOptions();
                optionsFirst.override(ivActivityLogo.getLayoutParams().width, ivActivityLogo.getLayoutParams().height);
                optionsFirst.placeholder(R.mipmap.ic_img_thumbnail_large);
                optionsFirst.error(R.mipmap.ic_img_failure_large);
                Glide.with(getApplicationContext()).load(TLSUrl.BASE_URL + info.activityLogo.imgPath).apply(optionsFirst).thumbnail(0.1f).into(ivActivityLogo);
            } else {
                ivActivityLogo.setImageResource(R.mipmap.ic_img_thumbnail);
            }
            tvActivityManager.setVisibility(info.isManager ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(ActivityDetailActivity.getActivityDetailIntent(ActivityListActivity.this, info.id));
                }
            });
        }
    }
}
