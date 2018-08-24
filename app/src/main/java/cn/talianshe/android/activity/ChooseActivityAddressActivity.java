package cn.talianshe.android.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.ActivityAddressListData;
import cn.talianshe.android.bean.RegionListData;
import cn.talianshe.android.eventbus.AddressOrderTimeEvent;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.widget.LocationIconTextView;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.OnRecyclerItemClickListener;
import cn.talianshe.android.widget.ScaleImageView;

/**
 * @author zcm
 * @ClassName: ChooseActivityAddressActivity
 * @Description: 选择活动场地
 * @date 2017/11/25 10:34
 */
public class ChooseActivityAddressActivity extends BaseActivity {

    @BindView(R.id.rv_activity_address)
    RecyclerView rvActivityAddress;
    @BindView(R.id.tv_search_null)
    TextView tvSearchNull;
    private RecyclerView.Adapter activityAddressAdapter;
    private List<ActivityAddressListData.ActivityAddressInfo> searchSchools;
    private List<ActivityAddressListData.ActivityAddressInfo> searchResults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_activity_address);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        initTitleCenterView();
        rvActivityAddress.setLayoutManager(new LinearLayoutManager(this));
        activityAddressAdapter = new ActivityAddressAdapter();
        rvActivityAddress.setAdapter(activityAddressAdapter);
        rvActivityAddress.addOnItemTouchListener(new OnRecyclerItemClickListener(rvActivityAddress) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
//                startActivity(new Intent(ChooseActivityAddressActivity.this, ActivityAddressDetailActivity.class));
                startActivity(ActivityAddressDetailActivity.getAddressDetailIntent(ChooseActivityAddressActivity.this, isSearching ? searchResults.get(vh.getAdapterPosition()).id : addressListInfo.list.get(vh.getAdapterPosition()).id));
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {

            }
        });
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                regionInfo = null;
                getAddressList();
            }
        });
        swipeLayout.setRefreshing(true);
        getAddressList();
        getAddressRegionList();
    }

    private RegionListData.RegionInfo regionInfo;
    private ActivityAddressListData.ActivityAddressListInfo addressListInfo;

    //获取活动类型
    private void getAddressList() {
        HttpSubscriber httpSubscriber = new HttpSubscriber<ActivityAddressListData>(this) {

            @Override
            public void onSuccess(ActivityAddressListData listData) {
                MyProgressDialog.dismiss();
                swipeLayout.setRefreshing(false);
                addressListInfo = listData.result;
                if (addressListInfo == null || addressListInfo.list == null || addressListInfo.list.size() == 0) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    llContent.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    llContent.setVisibility(View.VISIBLE);
                    rvActivityAddress.getAdapter().notifyDataSetChanged();
                    for (ActivityAddressListData.ActivityAddressInfo info : addressListInfo.list) {
                        info.initPinyin();
                    }
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).addressList(0, 0, regionInfo == null ? null : regionInfo.id, GlobalParams.SCHOOL_ID).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private RegionListData.RegionListInfo regionListInfo;

    //获取活动类型
    private void getAddressRegionList() {
        HttpSubscriber httpSubscriber = new HttpSubscriber<RegionListData>(this) {

            @Override
            public void onSuccess(RegionListData listData) {
                MyProgressDialog.dismiss();
                swipeLayout.setRefreshing(false);
                regionListInfo = listData.result;
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getRegionList(GlobalParams.USER_INFO.schoolId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private class ActivityAddressAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_address_list, parent, false);
            ActivityAddressViewHolder holder = new ActivityAddressViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ActivityAddressViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            if (isSearching) {
                return searchResults == null ? 0 : searchResults.size();
            } else {
                if (addressListInfo == null || addressListInfo.list == null) {
                    return 0;
                } else {
                    return addressListInfo.list.size();
                }

            }
        }

    }

    public class ActivityAddressViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_address)
        ScaleImageView ivAddress;
        @BindView(R.id.tv_address_name)
        TextView tvAddressName;
        @BindView(R.id.tv_location)
        TextView tvLocation;
        @BindView(R.id.tv_address)
        LocationIconTextView tvAddress;


        ActivityAddressViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(int position) {
            ActivityAddressListData.ActivityAddressInfo info;
            if (isSearching) {
                info = searchResults.get(position);
            } else {
                info = addressListInfo.list.get(position);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 2017/12/17 打开详情
                }
            });
            tvLocation.setTextColor(getString(R.string.in_door).equals(info.scene) ? Color.parseColor("#FF7435") : Color.parseColor("#519FFF"));
            tvLocation.setText(getString(R.string.in_door).equals(info.scene) ? R.string.indoor : R.string.outdoor);
            tvLocation.setBackgroundResource(getString(R.string.in_door).equals(info.scene) ? R.drawable.area_indoor : R.drawable.area_outdoor);
            tvAddressName.setText(info.name);
            if (!TextUtils.isEmpty(info.address)) {
                tvAddress.setText(info.address);
            }
            if (info.addressImgs != null && info.addressImgs.size() > 0 && !TextUtils.isEmpty(info.addressImgs.get(0).imgPath)) {
                RequestOptions options = new RequestOptions();
                options.override(DensityUtils.dipTopx(ChooseActivityAddressActivity.this, 60));
                options.placeholder(R.mipmap.ic_img_thumbnail);
                options.error(R.mipmap.ic_img_thumbnail);
                Glide.with(ChooseActivityAddressActivity.this).load(TLSUrl.BASE_URL + info.addressImgs.get(0).imgPath).apply(options).into(ivAddress);
            } else {
                ivAddress.setImageResource(R.mipmap.ic_img_thumbnail);
            }
        }
    }


    private EditText etContent;
    private boolean isSearching;

    private void initTitleCenterView() {
        View titleView = setTitleBarCenterView(R.layout.view_title_center_search);
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText(R.string.activity_area);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出侧边栏选择区域
                if (regionListInfo == null || regionListInfo.list == null || regionListInfo.list.size() == 0) {
                    MyToast.show(R.string.no_region_tip, ChooseActivityAddressActivity.this);
                    return;
                }
                showChooseAreaDialog();
            }
        });
        titleView.findViewById(R.id.ib_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出侧边栏选择区域
                etContent.setText("");
            }
        });
        etContent = titleView.findViewById(R.id.et_content);
        etContent.setHint(R.string.input_search_activity_address_key_tip);
        etContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                isSearching = hasFocus;
            }
        });
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (addressListInfo == null || addressListInfo.list == null || addressListInfo.list.size() == 0) {
                    return;
                } else {
                    if (TextUtils.isEmpty(s)) {
                        isSearching = false;
                        rvActivityAddress.setVisibility(View.VISIBLE);
                        rvActivityAddress.getAdapter().notifyDataSetChanged();
                        tvSearchNull.setVisibility(View.GONE);
                    } else {
                        isSearching = true;
                        searchResults = getSearchResult(s.toString(), addressListInfo.list);
                        if (searchResults.size() == 0) {
                            tvSearchNull.setVisibility(View.VISIBLE);
                            rvActivityAddress.setVisibility(View.GONE);
                        } else {
                            rvActivityAddress.setVisibility(View.VISIBLE);
                            tvSearchNull.setVisibility(View.GONE);
                            rvActivityAddress.getAdapter().notifyDataSetChanged();
                        }
                    }

                }
            }
        });
    }

    /**
     * 获取搜索结果
     *
     * @param s
     * @param contactAssociationMembers
     */
    private List<ActivityAddressListData.ActivityAddressInfo> getSearchResult(String s, List<ActivityAddressListData.ActivityAddressInfo> contactAssociationMembers) {
        List<ActivityAddressListData.ActivityAddressInfo> searchResults = new ArrayList<>();
        for (ActivityAddressListData.ActivityAddressInfo info : contactAssociationMembers) {

            if (!TextUtils.isEmpty(info.name) && info.name.contains(s)) {
                searchResults.add(info);
            } else {
                boolean flag = false;
                // 简拼匹配,如果输入在字符串长度大于6就不按首字母匹配了
                if (s.length() < 6) {
                    Pattern firstLetterMatcher = Pattern.compile("^" + s,
                            Pattern.CASE_INSENSITIVE);
                    flag = firstLetterMatcher.matcher(info.firstLetters).find();
                }
                if (!flag) {
                    for (String wordPinyin : info.wordPinyinList) {
                        if (wordPinyin.startsWith(s.toUpperCase())) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (flag)
                    searchResults.add(info);
            }
        }
        return searchResults;
    }

    private Dialog dialog;

    private void showChooseAreaDialog() {
        if (dialog == null) {

            // 获取Dialog布局
            View view = LayoutInflater.from(this).inflate(
                    R.layout.view_choose_activity_area, null);
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            // 设置Dialog最小宽度为屏幕宽度
            view.setMinimumWidth(displayMetrics.widthPixels - DensityUtils.dipTopx(this, 39));
            view.setMinimumHeight(displayMetrics.heightPixels);

            // 获取自定义Dialog布局中的控件
            tagFlowLayout = view.findViewById(R.id.tfl_areas);
            // 定义Dialog布局和参数
            dialog = new Dialog(this, R.style.activity_area_dialog_style);
            dialog.setContentView(view);
            Window dialogWindow = dialog.getWindow();
            dialogWindow.setGravity(Gravity.END | Gravity.TOP);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.x = 0;
            lp.y = 0;
            dialogWindow.setAttributes(lp);
        }
        bindTagData();
        dialog.show();
    }


    private TagFlowLayout tagFlowLayout;

    public void bindTagData() {
        tagFlowLayout.setMaxSelectCount(1);
        TagAdapter<RegionListData.RegionInfo> adapter = new TagAdapter<RegionListData.RegionInfo>(regionListInfo.list) {
            @Override
            public View getView(FlowLayout parent, int position, RegionListData.RegionInfo info) {
                View view = LayoutInflater.from(ChooseActivityAddressActivity.this).inflate(R.layout.view_tfl_activity_area, null);
                TextView tvTag = view.findViewById(R.id.tv_tag);
                tvTag.setText(info.regionName);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = DensityUtils.dipTopx(ChooseActivityAddressActivity.this, 11);
                layoutParams.leftMargin = DensityUtils.dipTopx(ChooseActivityAddressActivity.this, 15);
                view.setLayoutParams(layoutParams);
                return view;
            }

            @Override
            public void onSelected(int position, View view) {
//                view.setBackgroundColor(Color.parseColor("#FF464B"));
                view.findViewById(R.id.iv_selected).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.tv_tag)).setTextColor(Color.parseColor("#FF464B"));
                MyProgressDialog.show(ChooseActivityAddressActivity.this, false);
                regionInfo = regionListInfo.list.get(position);
                dialog.dismiss();
                getAddressList();
                super.onSelected(position, view);
            }

            @Override
            public void unSelected(int position, View view) {
//                view.setBackgroundColor(Color.parseColor("#F0F2F5"));
                view.findViewById(R.id.iv_selected).setVisibility(View.INVISIBLE);
                ((TextView) view.findViewById(R.id.tv_tag)).setTextColor(ContextCompat.getColor(ChooseActivityAddressActivity.this, R.color.black));
                super.unSelected(position, view);
            }
        };
        tagFlowLayout.setAdapter(adapter);
        if (regionInfo != null) {
            int selectedIndex = regionListInfo.list.indexOf(regionInfo);
            adapter.setSelectedList(selectedIndex);
        }
//        adapter.setSelectedList(2);
    }

    @Subscribe
    public void onReceiveAddressOrderTimeEvent(AddressOrderTimeEvent event) {
        finish();
    }
}
