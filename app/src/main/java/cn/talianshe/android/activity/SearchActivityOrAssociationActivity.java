package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import cn.talianshe.android.R;
import cn.talianshe.android.bean.AssociationActivityListData;
import cn.talianshe.android.bean.AssociationLabelListData;
import cn.talianshe.android.bean.AssociationListData;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.net.service.AssociationApiService;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.widget.CustomStarBar;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.ScaleImageView;

/**
 * @author zcm
 * @ClassName: SearchActivityOrAssociationActivity
 * @Description: 搜索社团
 * @date 2017/11/19 16:02
 */
public class SearchActivityOrAssociationActivity extends BaseActivity {

    @BindView(R.id.ll_tags)
    LinearLayout llTags;
    @BindView(R.id.tv_search_null)
    TextView tvSeachNull;
    @BindView(R.id.fl_association_tags)
    TagFlowLayout flAssociationTags;
    @BindView(R.id.rv_search_result)
    RecyclerView rvSearchResult;

    private static final String EXTRA_SEARCH_TYPE = "extra_search_type";
    public static final int TYPE_ASSOCIATION = 1;
    public static final int TYPE_ACTIVITY = 2;
    private int searchType;
    private List<AssociationLabelListData.AssociationLabelInfo> labels;

    public static Intent getSearchIntent(Context context, int searchType) {
        Intent intent = new Intent(context, SearchActivityOrAssociationActivity.class);
        intent.putExtra(EXTRA_SEARCH_TYPE, searchType);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_activity_or_association);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        searchType = getIntent().getIntExtra(EXTRA_SEARCH_TYPE, TYPE_ASSOCIATION);
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText(R.string.search);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(TextUtils.isEmpty(etContent.getText().toString())){
                        MyToast.show(R.string.search_key_null_tip,SearchActivityOrAssociationActivity.this);
                        return;
                    }
                if(searchType == TYPE_ASSOCIATION){
                    // 开始进行社团搜索
                    searchAssociation();
                }else{
                    // TODO: 2017/12/16 搜索活动
                    searchActivity();
                }
            }
        });
        if (searchType == TYPE_ASSOCIATION) {
            //搜索社团，显示社团相关标签
            llTags.setVisibility(View.VISIBLE);
            getTagData();
            associationAdapter = new MyAssociationListAdapter();
            rvSearchResult.setLayoutManager(new LinearLayoutManager(this));
            rvSearchResult.setAdapter(associationAdapter);
        } else {
            //搜索活动,隐藏社团相关标签
            llTags.setVisibility(View.GONE);
            activityAdapter = new MyActivityListAdapter();
            rvSearchResult.setLayoutManager(new LinearLayoutManager(this));
            rvSearchResult.setAdapter(activityAdapter);
        }
        initTitleCenterView();
    }

    private MyAssociationListAdapter associationAdapter;
    private MyActivityListAdapter activityAdapter;
    private String selectLabel;
    private void getTagData() {
        MyProgressDialog.show(this,false);
        AssociationApiService associationApiService = RequestEngine.getInstance().getServer(AssociationApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationLabelListData>(this) {
            @Override
            public void onSuccess(AssociationLabelListData listData) {
                MyProgressDialog.dismiss();
                labels = listData.result.list;
                flAssociationTags.setMaxSelectCount(1);
                flAssociationTags.setAdapter(new TagAdapter<AssociationLabelListData.AssociationLabelInfo>(labels) {
                    @Override
                    public View getView(FlowLayout parent, int position, AssociationLabelListData.AssociationLabelInfo label) {
                        TextView tv = new TextView(SearchActivityOrAssociationActivity.this);
                        tv.setTextSize(14);
                        tv.setTextColor(getResources().getColor(R.color.association_tag_text_color));
                        tv.setBackgroundResource(R.drawable.association_tag_text_bg);
                        tv.setText(label.lableName);
                        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
                        layoutParams.rightMargin = DensityUtils.dipTopx(SearchActivityOrAssociationActivity.this, 8);
                        layoutParams.bottomMargin = DensityUtils.dipTopx(SearchActivityOrAssociationActivity.this, 10);
                        tv.setLayoutParams(layoutParams);
                        return tv;
                    }

                    @Override
                    public void onSelected(int position, View view) {
                        selectLabel = labels.get(position).lableName;
                        searchAssociation();
                        ((TextView) view).setTextColor(getResources().getColor(R.color.white));
                    }

                    @Override
                    public void unSelected(int position, View view) {
                        selectLabel = null;
                        ((TextView) view).setTextColor(getResources().getColor(R.color.gray));
                    }
                });
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        associationApiService.getAssociationLabelList().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }


    private EditText etContent;
    private void initTitleCenterView() {
        View titleView = setTitleBarCenterView(R.layout.view_title_center_search);
        titleView.findViewById(R.id.ib_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etContent.setText("");
            }
        });
        etContent = titleView.findViewById(R.id.et_content);
        etContent.setHint(searchType == TYPE_ASSOCIATION?R.string.input_search_association_key_tip:R.string.input_search_activity_key_tip);
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(tvSeachNull.getVisibility() == View.VISIBLE){
                    tvSeachNull.setVisibility(View.GONE);
                    rvSearchResult.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void searchAssociation(){
        MyProgressDialog.show(this,false);
        String key = etContent.getText().toString();
        AssociationApiService associationApiService = RequestEngine.getInstance().getServer(AssociationApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationListData>(this) {
            @Override
            public void onSuccess(AssociationListData listData) {
//                setSwipeRerefreshEnable(true);
                MyProgressDialog.dismiss();
                swipeLayout.setRefreshing(false);
                associationInfoList.clear();
                associationInfoList.addAll(listData.result.list);
                if(associationInfoList.size() == 0){
                    tvSeachNull.setVisibility(View.VISIBLE);
                    rvSearchResult.setVisibility(View.GONE);
                }else{
                    tvSeachNull.setVisibility(View.GONE);
                    rvSearchResult.setVisibility(View.VISIBLE);
                    associationAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String msg) {
                MyProgressDialog.dismiss();
                super.onError(msg);
//                setSwipeRerefreshEnable(true);
            }
        };
        associationApiService.getAssociationList(0, 0, GlobalParams.SCHOOL_ID, null, selectLabel, key).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void searchActivity(){
        MyProgressDialog.show(this,false);
        String key = etContent.getText().toString();
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationActivityListData>(this) {
            @Override
            public void onSuccess(AssociationActivityListData listData) {
//                setSwipeRerefreshEnable(true);
                MyProgressDialog.dismiss();
                swipeLayout.setRefreshing(false);
                activityInfoList.clear();
                activityInfoList.addAll(listData.result.list);
                if(activityInfoList.size() == 0){
                    tvSeachNull.setVisibility(View.VISIBLE);
                    rvSearchResult.setVisibility(View.GONE);
                }else{
                    tvSeachNull.setVisibility(View.GONE);
                    rvSearchResult.setVisibility(View.VISIBLE);
                    activityAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String msg) {
                MyProgressDialog.dismiss();
                super.onError(msg);
//                setSwipeRerefreshEnable(true);
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityList(0, 0, null, GlobalParams.SCHOOL_ID, key).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private List<AssociationListData.AssociationInfo> associationInfoList = new ArrayList<>();
    public class MyAssociationListAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_association_list, parent, false);
            SearchActivityOrAssociationActivity.AssociationItemHolder holder = new SearchActivityOrAssociationActivity.AssociationItemHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((SearchActivityOrAssociationActivity.AssociationItemHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return associationInfoList.size();
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

        public void bindData(int position) {
            final AssociationListData.AssociationInfo associationInfo = associationInfoList.get(position);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(AssociationDetailActivity.getAssociationDetailIntent(SearchActivityOrAssociationActivity.this,associationInfo.id));
                }
            });
            if (TextUtils.isEmpty(associationInfo.associationLogo)) {
                ivAssociationLogo.setImageResource(R.mipmap.association_default_logo);
            } else {

                RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(DensityUtils.dipTopx(SearchActivityOrAssociationActivity.this, 10)));
                options.override(DensityUtils.dipTopx(SearchActivityOrAssociationActivity.this, 52));
                options.error(R.mipmap.association_default_logo);
                Glide.with(getApplicationContext()).load(TLSUrl.BASE_URL+associationInfo.associationLogo).apply(options).thumbnail(0.1f).into(ivAssociationLogo);
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
            tvSchoolDepartment.setText(associationInfo.level == 1?associationInfo.schoolName:associationInfo.schoolName + associationInfo.departmentName);
            tagFlowLayout.setAdapter(new TagAdapter<AssociationListData.Label>(associationInfo.labels) {
                @Override
                public View getView(FlowLayout parent, int position, AssociationListData.Label s) {
                    TextView tv = new TextView(SearchActivityOrAssociationActivity.this);
                    tv.setTextSize(10);
                    tv.setTextColor(getResources().getColor(R.color.theme_color));
                    tv.setBackgroundResource(R.drawable.association_tag_bg);
                    tv.setText(s.lableName);
                    RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
                    layoutParams.rightMargin = DensityUtils.dipTopx(SearchActivityOrAssociationActivity.this, 5);
                    layoutParams.bottomMargin = DensityUtils.dipTopx(SearchActivityOrAssociationActivity.this, 5);
                    tv.setLayoutParams(layoutParams);
                    return tv;
                }
            });
        }
    }

    private List<AssociationActivityListData.AssociationActivity> activityInfoList = new ArrayList<>();

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
                return activityInfoList.size();
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
            final AssociationActivityListData.AssociationActivity info= activityInfoList.get(position);
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
                ivActivityLogo.setImageResource(R.mipmap.ic_img_thumbnail_large);
            }
            tvActivityManager.setVisibility(info.isManager ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(ActivityDetailActivity.getActivityDetailIntent(SearchActivityOrAssociationActivity.this, info.id));
                }
            });
        }
    }
}
