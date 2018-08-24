package cn.talianshe.android.activity;

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

/**
 * @author zcm
 * @ClassName: OnGoingActivityListActivity
 * @Description: 进行中活动列表页
 * @date 2017/12/25 14:48
 */
public class OnGoingActivityListActivity extends BaseActivity {

    @BindView(R.id.x_refresh_view)
    MyXRefreshView xRefreshView;
    @BindView(R.id.rv_activity)
    RecyclerView rvActivity;
    private MyActivityListAdapter allActivityAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_going_activity_list);
        ButterKnife.bind(this);
        initData();
    }


    private void initData() {
        setTitle(R.string.on_going_activity);
        btnRight.setVisibility(View.GONE);
        btnRight.setBackgroundResource(R.mipmap.search_black);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TipDialogUtil.checkLogin(OnGoingActivityListActivity.this)){
                    startActivity(SearchActivityOrAssociationActivity.getSearchIntent(OnGoingActivityListActivity.this, SearchActivityOrAssociationActivity.TYPE_ACTIVITY));
                }
            }
        });
        swipeLayout.setEnabled(true);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getOnGoingActivityList(0, 20);
            }
        });
        xRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onLoadMore(boolean b) {
                getOnGoingActivityList(activityAllInfo.curPage + 1, activityAllInfo.pageSize);

            }
        });

        swipeLayout.setRefreshing(true);
        getOnGoingActivityList(0, 20);
    }

    private AssociationActivityListData.AssociationActivityListInfo activityAllInfo;

    private List<AssociationActivityListData.AssociationActivity> activityAllInfoList = new ArrayList<>();


    private void getOnGoingActivityList(int page, int pageSize) {
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationActivityListData>(this) {
            @Override
            public void onSuccess(AssociationActivityListData listData) {
                if (!swipeLayout.isRefreshing()) {
                    activityAllInfo.curPage = activityAllInfo.curPage + 1;
                    activityAllInfoList.addAll(listData.result.list);
                    xRefreshView.stopLoadMore();
                } else {
                    swipeLayout.setRefreshing(false);
                    activityAllInfo = listData.result;
                    activityAllInfo.curPage = 0;
                    activityAllInfoList.clear();
                    activityAllInfoList.addAll(listData.result.list);
                    if (activityAllInfoList.size() == 0) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        llContent.setVisibility(View.GONE);
                    } else {
                        llContent.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    }
                    allActivityAdapter = new MyActivityListAdapter();
                    rvActivity.setLayoutManager(new LinearLayoutManager(OnGoingActivityListActivity.this));
                    rvActivity.setAdapter(allActivityAdapter);
                    xRefreshView.setCustomFooterView(new XRefreshViewFooter(OnGoingActivityListActivity.this));

                }
                activityAllInfo.pageSize = 20;
                if ((activityAllInfo.curPage + 1) * activityAllInfo.pageSize >= activityAllInfo.totalCount) {
                    //说明没有加载更多,禁用
                    xRefreshView.setPullLoadEnable(false);
                } else {
                    xRefreshView.setPullLoadEnable(true);
                }
                allActivityAdapter.notifyDataSetChanged();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getOnGoingActivityList(page, pageSize, GlobalParams.SCHOOL_ID).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }


    public class MyActivityListAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity_list, parent, false);
            ActivityItemHolder holder = new ActivityItemHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ActivityItemHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return activityAllInfoList.size();
        }
    }

    public class ActivityItemHolder extends RecyclerView.ViewHolder {

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

        public ActivityItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            int width = getResources().getDisplayMetrics().widthPixels;
            ViewGroup.LayoutParams layoutParams = ivActivityLogo.getLayoutParams();
            if (layoutParams == null)
                layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.width = width;
            layoutParams.height = width * 2 / 5;
            ivActivityLogo.setLayoutParams(layoutParams);
        }

        public void bindData(int position) {
            final AssociationActivityListData.AssociationActivity info = activityAllInfoList.get(position);
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
                Glide.with(getApplicationContext()).load(TLSUrl.BASE_URL + info.activityLogo.imgPath).apply(optionsFirst).into(ivActivityLogo);
            } else {
                ivActivityLogo.setImageResource(R.mipmap.ic_img_thumbnail_large);
            }
            tvActivityManager.setVisibility(info.isManager ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(ActivityDetailActivity.getActivityDetailIntent(OnGoingActivityListActivity.this, info.id));
                }
            });
        }
    }
}
