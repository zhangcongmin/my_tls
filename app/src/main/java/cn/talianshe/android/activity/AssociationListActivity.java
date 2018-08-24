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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.AssociationListData;
import cn.talianshe.android.db.entity.UserInfo;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.AssociationApiService;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.CustomStarBar;
import cn.talianshe.android.widget.RecyclerViewItemClickListener;
import cn.talianshe.android.widget.ScaleImageView;
import cn.talianshe.android.widget.UnderlineBtn;

/**
 * @author zcm
 * @ClassName: AssociationListActivity
 * @Description: 社团列表页
 * @date 2017/11/23 14:25
 */
public class AssociationListActivity extends BaseActivity {
    private static final String EXTRA_IS_FOLLOW = "extra_is_follow";
    @BindView(R.id.rv_association)
    RecyclerView rvAssociation;
    @BindView(R.id.ll_mine)
    LinearLayout llMine;
    @BindView(R.id.btn_association_follow)
    UnderlineBtn btnAssociationFollow;
    @BindView(R.id.btn_association_jion)
    UnderlineBtn btnAssociationJion;
    private MyAssociationListAdapter associationAdapter;

    private boolean isFollow = false;

    public static Intent getFollowAssociationIntent(Context context) {
        Intent intent = new Intent(context, AssociationListActivity.class);
        intent.putExtra(EXTRA_IS_FOLLOW, true);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_association_list);
        ButterKnife.bind(this);
        initData();
    }

    private UserInfo userInfo;
    private List<AssociationListData.AssociationInfo> associationAllInfoList = new ArrayList<>();
    private List<AssociationListData.AssociationInfo> associationFollowInfoList = new ArrayList<>();
    private List<AssociationListData.AssociationInfo> associationJionInfoList = new ArrayList<>();

    private void initData() {
        isFollow = getIntent().getBooleanExtra(EXTRA_IS_FOLLOW, false);
        userInfo = GlobalParams.USER_INFO;
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setBackgroundResource(R.mipmap.search_black);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TipDialogUtil.checkLogin(AssociationListActivity.this)) {
                    startActivity(SearchActivityOrAssociationActivity.getSearchIntent(AssociationListActivity.this, SearchActivityOrAssociationActivity.TYPE_ASSOCIATION));
                }
            }
        });
        initTitleCenterView();
        rvAssociation.setLayoutManager(new LinearLayoutManager(this));
        associationAdapter = new MyAssociationListAdapter(associationAllInfoList);
        rvAssociation.setAdapter(associationAdapter);
        rvAssociation.addOnItemTouchListener(new RecyclerViewItemClickListener(rvAssociation) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                int position = vh.getAdapterPosition();
                String associationId = associationAdapter.associationInfoList.get(position).id;
                startActivity(AssociationDetailActivity.getAssociationDetailIntent(AssociationListActivity.this, associationId));
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {

            }
        });
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
        if (isFollow) {
            curTab = TAB_FOLLOW;
        } else {
            curTab = TAB_ALL;
        }
        requestData();
    }

    private int curTab;
    private static final int TAB_ALL = 1;
    private static final int TAB_FOLLOW = 2;
    private static final int TAB_JION = 3;
    private int allIndex = -2;
    private int followIndex = -2;
    private int jionIndex = -2;

    private void requestData() {
        swipeLayout.setRefreshing(true);
        setSwipeRerefreshEnable(swipeLayout, true);
        AssociationApiService associationApiService = RequestEngine.getInstance().getServer(AssociationApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationListData>(this) {
            @Override
            public void onSuccess(AssociationListData listData) {
//                setSwipeRerefreshEnable(true);
                swipeLayout.setRefreshing(false);
                switch (curTab) {
                    case TAB_ALL:
                        associationAllInfoList.clear();
                        associationAllInfoList.addAll(listData.result.list);
                        associationAdapter.associationInfoList = associationAllInfoList;
                        break;
                    case TAB_JION:
                        associationJionInfoList.clear();
                        associationJionInfoList.addAll(listData.result.list);
                        associationAdapter.associationInfoList = associationJionInfoList;
                        if (associationJionInfoList.size() == 0) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                        break;
                    case TAB_FOLLOW:
                        associationFollowInfoList.clear();
                        associationFollowInfoList.addAll(listData.result.list);
                        associationAdapter.associationInfoList = associationFollowInfoList;
                        if (associationFollowInfoList.size() == 0) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                        break;
                }
                if (associationAdapter.associationInfoList.size() == 0) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvAssociation.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvAssociation.setVisibility(View.VISIBLE);
                    associationAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
//                setSwipeRerefreshEnable(true);
            }
        };
        String type = null;
        switch (curTab) {
            case TAB_ALL:
                type = null;
                break;
            case TAB_JION:
                type = "1";
                break;
            case TAB_FOLLOW:
                type = "2";
                break;
        }
        associationApiService.getAssociationList(0, 0, GlobalParams.SCHOOL_ID, type, null, null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private TextView tvLeftTab;
    private TextView tvRightTab;

    private void initTitleCenterView() {
        View titleView = setTitleBarCenterView(R.layout.view_title_center_tab);
//        View titleView = View.inflate(mActivity, R.layout.view_title_center_tab, null);
        tvLeftTab = titleView.findViewById(R.id.tv_left_tab);
        tvLeftTab.setText(R.string.association_all);

        tvRightTab = titleView.findViewById(R.id.tv_rightt_tab);
        tvRightTab.setText(R.string.association_mine);
        tvLeftTab.setOnClickListener(this);
        tvRightTab.setOnClickListener(this);

        if (isFollow) {
            tvLeftTab.setSelected(false);
            tvRightTab.setSelected(true);
            llMine.setVisibility(View.VISIBLE);
        } else {
            tvLeftTab.setSelected(true);
            tvRightTab.setSelected(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_left_tab:
                //左边tab全部社团被点击
                if (tvLeftTab.isSelected()) {
                    return;
                }

                if (curTab == TAB_FOLLOW) {
                    followIndex = ((LinearLayoutManager) rvAssociation.getLayoutManager()).findFirstVisibleItemPosition();
                } else if (curTab == TAB_JION) {
                    jionIndex = ((LinearLayoutManager) rvAssociation.getLayoutManager()).findFirstVisibleItemPosition();
                }
                curTab = TAB_ALL;
                if (allIndex != -2) {
                    if (allIndex != -1) {
                        //说明有数据
                        tvEmpty.setVisibility(View.GONE);
                        rvAssociation.setVisibility(View.VISIBLE);
                        associationAdapter.associationInfoList = associationAllInfoList;
                        associationAdapter.notifyDataSetChanged();
                        RecyclerView.LayoutManager layoutManager = rvAssociation.getLayoutManager();
                        layoutManager.scrollToPosition(allIndex);
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvAssociation.setVisibility(View.GONE);
                    }

                } else {
                    //说明是第一次打开,请求数据
                    requestData();
                }
                llMine.setVisibility(View.GONE);
                tvLeftTab.setSelected(true);
                tvRightTab.setSelected(false);
                break;
            case R.id.tv_rightt_tab:
                //右边tab我的社团被点击
                if (!TipDialogUtil.checkLogin(this)) {
                    return;
                }
                if (tvRightTab.isSelected()) {
                    return;
                }
                if (curTab == TAB_ALL) {
                    allIndex = ((LinearLayoutManager) rvAssociation.getLayoutManager()).findFirstVisibleItemPosition();
                }
                if (btnAssociationFollow.isChecked()) {
                    curTab = TAB_FOLLOW;
                    if (followIndex != -2) {
                        if (followIndex != -1) {
                            tvEmpty.setVisibility(View.GONE);
                            rvAssociation.setVisibility(View.VISIBLE);
                            associationAdapter.associationInfoList = associationFollowInfoList;
                            associationAdapter.notifyDataSetChanged();
                            rvAssociation.scrollToPosition(followIndex);
                        } else {
                            rvAssociation.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                        }

                    } else {
                        //说明是第一次打开,请求数据
                        requestData();
                    }
                } else {
                    //说明我加入的社团是被选中的
                    curTab = TAB_JION;
                    if (jionIndex != -2) {
                        if (jionIndex != -1) {
                            tvEmpty.setVisibility(View.GONE);
                            rvAssociation.setVisibility(View.VISIBLE);
                            associationAdapter.associationInfoList = associationJionInfoList;
                            associationAdapter.notifyDataSetChanged();
                            rvAssociation.scrollToPosition(jionIndex);
                        } else {
                            rvAssociation.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    }
                }
                llMine.setVisibility(View.VISIBLE);
                tvRightTab.setSelected(true);
                tvLeftTab.setSelected(false);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @OnClick({R.id.btn_association_follow, R.id.btn_association_jion})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_association_follow:
                if (btnAssociationFollow.isChecked()) {
                    return;
                }
                curTab = TAB_FOLLOW;
                jionIndex = ((LinearLayoutManager) rvAssociation.getLayoutManager()).findFirstVisibleItemPosition();
                if (followIndex > -1) {
                    tvEmpty.setVisibility(View.GONE);
                    rvAssociation.setVisibility(View.VISIBLE);
                    associationAdapter.associationInfoList = associationFollowInfoList;
                    associationAdapter.notifyDataSetChanged();
                    rvAssociation.scrollToPosition(followIndex);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvAssociation.setVisibility(View.GONE);
                }
                btnAssociationFollow.setChecked(true);
                btnAssociationJion.setChecked(false);
                break;
            case R.id.btn_association_jion:
                if (btnAssociationJion.isChecked()) {
                    return;
                }
                curTab = TAB_JION;
                followIndex = ((LinearLayoutManager) rvAssociation.getLayoutManager()).findFirstVisibleItemPosition();

                if (jionIndex > -1) {
                    tvEmpty.setVisibility(View.GONE);
                    rvAssociation.setVisibility(View.VISIBLE);
                    associationAdapter.associationInfoList = associationJionInfoList;
                    associationAdapter.notifyDataSetChanged();
                    rvAssociation.scrollToPosition(jionIndex);
                } else {
                    if (jionIndex == -1) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvAssociation.setVisibility(View.GONE);
                    } else {
                        requestData();
                    }
                }
                btnAssociationFollow.setChecked(false);
                btnAssociationJion.setChecked(true);
                break;
        }
    }

    public class MyAssociationListAdapter extends RecyclerView.Adapter {

        public List<AssociationListData.AssociationInfo> associationInfoList;

        public MyAssociationListAdapter(List<AssociationListData.AssociationInfo> associationInfoList) {
            this.associationInfoList = associationInfoList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_association_list, parent, false);
            AssociationItemHolder holder = new AssociationItemHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((AssociationItemHolder) holder).bindData(position, associationInfoList.get(position));
        }

        @Override
        public int getItemCount() {
            return associationInfoList.size();
//            if (tvLeftTab.isSelected()) {
//                //说明选中的左边全部社团tab
//                return associationAllInfoList.size();
//            } else {
//                //说明选中的右边全部社团tab
//                if (btnAssociationFollow.isChecked()) {
//                    //说明选中的是关注的社团
//                    return associationFollowInfoList.size();
//                } else {
//                    //说明选中的是加入的社团
//                    return associationJionInfoList.size();
//                }
//            }
        }
    }


    public class AssociationItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tag_flow_layout)
        TagFlowLayout tagFlowLayout;
        @BindView(R.id.iv_association_logo)
        ScaleImageView ivAssociationLogo;
        @BindView(R.id.tv_association_name)
        TextView tvAssociationName;
        @BindView(R.id.tv_duty)
        TextView tvDuty;
        @BindView(R.id.starBar)
        CustomStarBar starBar;
        @BindView(R.id.tv_school_department)
        TextView tvSchoolDepartment;

        public AssociationItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(int position, AssociationListData.AssociationInfo associationInfo) {
            if (TextUtils.isEmpty(associationInfo.associationLogo)) {
                ivAssociationLogo.setImageResource(R.mipmap.association_default_logo);
            } else {

                RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(DensityUtils.dipTopx(AssociationListActivity.this, 10)));
                options.override(DensityUtils.dipTopx(AssociationListActivity.this, 52));
                options.error(R.mipmap.association_default_logo);
                Glide.with(getApplicationContext()).load(TLSUrl.BASE_URL + associationInfo.associationLogo).apply(options).thumbnail(0.1f).into(ivAssociationLogo);
            }
            tvAssociationName.setText(associationInfo.associationName);
            tvAssociationName.setCompoundDrawablesWithIntrinsicBounds(1 == associationInfo.level ? R.mipmap.school_level : R.mipmap.college_level, 0, 0, 0);
            starBar.setStarMark(associationInfo.score);
            if (TextUtils.isEmpty(associationInfo.dutyName)) {
                tvDuty.setVisibility(View.GONE);
            } else {
                tvDuty.setVisibility(View.VISIBLE);
                tvDuty.setText(associationInfo.dutyName);
            }
            tvSchoolDepartment.setText(associationInfo.level == 1 ? associationInfo.schoolName : associationInfo.schoolName + associationInfo.departmentName);
            tagFlowLayout.setAdapter(new TagAdapter<AssociationListData.Label>(associationInfo.labels) {
                @Override
                public View getView(FlowLayout parent, int position, AssociationListData.Label s) {
                    TextView tv = new TextView(AssociationListActivity.this);
                    tv.setTextSize(10);
                    tv.setTextColor(getResources().getColor(R.color.theme_color));
                    tv.setBackgroundResource(R.drawable.association_tag_bg);
                    tv.setText(s.lableName);
                    RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
                    layoutParams.rightMargin = DensityUtils.dipTopx(AssociationListActivity.this, 5);
                    layoutParams.bottomMargin = DensityUtils.dipTopx(AssociationListActivity.this, 5);
                    tv.setLayoutParams(layoutParams);
                    return tv;
                }
            });
        }
    }
}
