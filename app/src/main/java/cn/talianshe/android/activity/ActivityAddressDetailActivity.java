package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.adapter.BannerVPAdapter;
import cn.talianshe.android.bean.AddressDetailData;
import cn.talianshe.android.bean.AddressOrderListData;
import cn.talianshe.android.eventbus.AddressOrderTimeEvent;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.widget.LocationIconTextView;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.OnRecyclerItemClickListener;
import cn.talianshe.android.widget.ScrollViewPager;
import cn.talianshe.android.widget.UnderlineBtn;

/**
 * @author zcm
 * @ClassName: ActivityAddressDetailActivity
 * @Description: 场地详情
 * @date 2017/11/29 15:47
 */
public class ActivityAddressDetailActivity extends BaseActivity {

    private static final String EXTRA_ADDRESS_ID = "extra_address_id";
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    @BindView(R.id.btn_order)
    TextView btnOrder;
    private MyAddressDetailAdapter detailAdapter;

    private String addressId;

    public static Intent getAddressDetailIntent(Context context, String addresId) {
        Intent intent = new Intent(context, ActivityAddressDetailActivity.class);
        intent.putExtra(EXTRA_ADDRESS_ID, addresId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_address_detail);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.address_detail);
        addressId = getIntent().getStringExtra(EXTRA_ADDRESS_ID);
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        rvContent.addOnItemTouchListener(new OnRecyclerItemClickListener(rvContent) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                if (vh.getItemViewType() == MyAddressDetailAdapter.TYPE_ORDER_TIME) {
                    int adapterPosition = vh.getAdapterPosition();
                    System.out.println("adapterPosition" + adapterPosition);

                    startActivity(OrderTimeDetailActivity.getOrderDetailIntent(ActivityAddressDetailActivity.this, orderListInfo.list.get(adapterPosition - 1).id));
                }
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {

            }
        });
        requestData();
    }

    private AddressDetailData.AddressDetailInfo detailInfo;
    private AddressOrderListData.AddressOrderListInfo orderListInfo;

    private void requestData() {
        MyProgressDialog.show(this);
        //获取活动类型
        HttpSubscriber httpSubscriber = new HttpSubscriber<AddressDetailData>(this) {

            @Override
            public void onSuccess(AddressDetailData detailData) {
                MyProgressDialog.dismiss();
                swipeLayout.setRefreshing(false);
                detailInfo = detailData.result;
                if (detailInfo == null) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    llContent.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    llContent.setVisibility(View.VISIBLE);
                    detailAdapter = new MyAddressDetailAdapter();
                    rvContent.setAdapter(detailAdapter);
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).getAddressDetail(addressId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void requestOrderListData() {
        MyProgressDialog.show(this);
        //获取活动类型
        HttpSubscriber httpSubscriber = new HttpSubscriber<AddressOrderListData>(this) {

            @Override
            public void onSuccess(AddressOrderListData listData) {
                MyProgressDialog.dismiss();
                orderListInfo = listData.result;
                if (orderListInfo == null || orderListInfo.list == null || orderListInfo.list.size() == 0) {
                    MyToast.show(R.string.no_address_order, ActivityAddressDetailActivity.this);
                } else {
                    detailAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).getAddressOrderList(addressId, 0, 0).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    public class MyAddressDetailAdapter extends RecyclerView.Adapter {

        public boolean isLeftTabSelected = true;

        public static final int TYPE_HEAD = 0;
        public static final int TYPE_ADDRESS_INFO = 1;
        public static final int TYPE_ORDER_TIME = 2;

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEAD;
            } else {
                return isLeftTabSelected ? TYPE_ADDRESS_INFO : TYPE_ORDER_TIME;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == TYPE_HEAD) {

                View headView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_address_detail_head, parent, false);
                HeadViewHolder headViewHolder = new HeadViewHolder(headView);
                return headViewHolder;
            } else if (viewType == TYPE_ADDRESS_INFO) {
                View infoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_address_detail_info, parent, false);
                AddressInfoViewHolder infoHolder = new AddressInfoViewHolder(infoView);
                return infoHolder;
            } else {
                View timeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_address_detail_order_time, parent, false);
                OrderTimeViewHolder infoHolder = new OrderTimeViewHolder(timeView);
                return infoHolder;

            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            int viewType = getItemViewType(position);
            if (viewType == TYPE_HEAD) {

                ((HeadViewHolder) holder).bindData(position);
            } else if (viewType == TYPE_ADDRESS_INFO) {
                ((AddressInfoViewHolder) holder).bindData(position);
            } else {
                ((OrderTimeViewHolder) holder).bindData(position);

            }
        }

        @Override
        public int getItemCount() {
            if (isLeftTabSelected) {
                return 2;
            } else {
                if (orderListInfo == null || orderListInfo.list == null) {
                    return 1;
                } else {
                    return orderListInfo.list.size() + 1;
                }
            }

        }

        public class HeadViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.vp_imgs)
            ScrollViewPager vpImgs;
            @BindView(R.id.tv_index)
            TextView tvIndex;
            @BindView(R.id.tv_address_name)
            TextView tvAddressName;
            @BindView(R.id.tv_location)
            TextView tvLocation;
            @BindView(R.id.tv_address)
            LocationIconTextView tvAddress;
            @BindView(R.id.btn_address_info)
            UnderlineBtn btnAddressInfo;
            @BindView(R.id.btn_order_time)
            UnderlineBtn btnOrderTime;

            HeadViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            @OnClick({R.id.btn_address_info, R.id.btn_order_time})
            public void onViewClicked(View view) {
                switch (view.getId()) {
                    case R.id.btn_address_info:
                        if (!btnAddressInfo.isChecked()) {
                            btnAddressInfo.setChecked(true);
                            btnOrderTime.setChecked(false);
                            detailAdapter.isLeftTabSelected = true;
                            detailAdapter.notifyDataSetChanged();
                        }
                        break;
                    case R.id.btn_order_time:
                        if (!btnOrderTime.isChecked()) {
                            btnOrderTime.setChecked(true);
                            btnAddressInfo.setChecked(false);
                            detailAdapter.isLeftTabSelected = false;
                            detailAdapter.notifyDataSetChanged();
                            if (orderListInfo == null || orderListInfo.list == null || orderListInfo.list.size() == 0) {
                                requestOrderListData();
                            }
                        }
                        break;
                }
            }

            public void bindData(int position) {
                tvLocation.setTextColor(getString(R.string.in_door).equals(detailInfo.scene) ? Color.parseColor("#FF7435") : Color.parseColor("#519FFF"));
                tvLocation.setText(getString(R.string.in_door).equals(detailInfo.scene) ? R.string.indoor : R.string.outdoor);
                tvLocation.setBackgroundResource(getString(R.string.in_door).equals(detailInfo.scene) ? R.drawable.area_indoor : R.drawable.area_outdoor);

                tvAddressName.setText(detailInfo.name);
                if (!TextUtils.isEmpty(detailInfo.address)) {
                    tvAddress.setText(detailInfo.address);
                }

                if (detailInfo.addressImgs != null && detailInfo.addressImgs.size() > 0) {
                    loadBannerData(ActivityAddressDetailActivity.this, detailInfo.addressImgs);
                }

            }

            /**
             * 加载banner轮播图数据
             *
             * @param bannerList
             */
            public void loadBannerData(Context context, final List<AddressDetailData.AddressImg> bannerList) {
//            ArrayList<PhotoBean> arrayList = new ArrayList<>();
                List<ImageView> imageViews = new ArrayList<>();
                if (bannerList.size() >= 1) {
                    AddressDetailData.AddressImg firstBanner = bannerList.get(0);
                    ImageView imageViewFirst = new ImageView(context);
                    imageViewFirst.setLayoutParams(vpImgs.getLayoutParams());
                    imageViewFirst.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageViews.add(imageViewFirst);
                    RequestOptions optionsFirst = new RequestOptions();
                    optionsFirst.override(vpImgs.getLayoutParams().width, vpImgs.getLayoutParams().height);
                    optionsFirst.placeholder(R.mipmap.ic_img_thumbnail_large);
                    optionsFirst.error(R.mipmap.ic_img_failure_large);
                    Glide.with(context).load(TLSUrl.BASE_URL + firstBanner.imgPath).apply(optionsFirst).into(imageViewFirst);

                    for (AddressDetailData.AddressImg banner : bannerList) {
                        ImageView imageView = new ImageView(context);
                        imageView.setLayoutParams(vpImgs.getLayoutParams());
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageViews.add(imageView);
                        RequestOptions options = new RequestOptions();
                        options.override(vpImgs.getLayoutParams().width, vpImgs.getLayoutParams().height);
                        options.placeholder(R.mipmap.ic_img_thumbnail_large);
                        options.error(R.mipmap.ic_img_failure_large);
                        Glide.with(context).load(TLSUrl.BASE_URL + banner.imgPath).apply(options).into(imageView);

                    }
                    AddressDetailData.AddressImg lastBanner = bannerList.get(bannerList.size() - 1);
                    ImageView imageViewLast = new ImageView(context);
                    imageViewLast.setLayoutParams(vpImgs.getLayoutParams());
                    imageViewLast.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageViews.add(imageViewLast);
                    RequestOptions options = new RequestOptions();
                    options.override(vpImgs.getLayoutParams().width, vpImgs.getLayoutParams().height);
                    options.placeholder(R.mipmap.ic_img_thumbnail_large);
                    options.error(R.mipmap.ic_img_failure_large);
                    Glide.with(context).load(TLSUrl.BASE_URL + lastBanner.imgPath).apply(options).into(imageViewLast);
                } else {
                    ImageView imageView = new ImageView(context);
                    imageView.setLayoutParams(vpImgs.getLayoutParams());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageViews.add(imageView);
                    RequestOptions options = new RequestOptions();
                    options.override(vpImgs.getLayoutParams().width, vpImgs.getLayoutParams().height);
                    options.placeholder(R.mipmap.ic_img_thumbnail_large);
                    options.error(R.mipmap.ic_img_failure_large);
                    Glide.with(context).load(TLSUrl.BASE_URL + bannerList.get(0).imgPath).apply(options).into(imageView);
                }
                vpImgs.setAdapter(new BannerVPAdapter(imageViews));
                tvIndex.setText("1/" + bannerList.size());

                if (imageViews.size() > 1) {

                    vpImgs.setCurrentItem(1);
                    vpImgs.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                                    vpImgs.setCurrentItem(size, false);
                                } else if (position == size + 1) {
                                    //切换到第一个页面
                                    vpImgs.setCurrentItem(1, false);
                                }
                            }
                            tvIndex.setText(position + "/" + bannerList.size());
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {
                        }
                    });
                    autoLoadBannerHandler.removeCallbacksAndMessages(null);
                    autoLoadBannerHandler.sendEmptyMessageDelayed(0, 3000);
                }
            }

            public Handler autoLoadBannerHandler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    int currentItem = vpImgs.getCurrentItem();
                    if (currentItem != vpImgs.getAdapter().getCount() - 1) {
                        vpImgs.setCurrentItem(currentItem + 1);
                    } else {
                        vpImgs.setCurrentItem(0);
                    }
                    this.sendEmptyMessageDelayed(0, 3000);
                }
            };
        }


        public class OrderTimeViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.v_pd_top)
            View vPdTop;
            @BindView(R.id.tv_order_time)
            TextView tvOrderTime;
            @BindView(R.id.tv_order_state)
            TextView tvOrderState;
            @BindView(R.id.v_pd_bottom)
            View vPdBottom;

            OrderTimeViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bindData(int position) {
                if (position == 1) {
                    vPdBottom.setVisibility(View.GONE);
                    vPdTop.setVisibility(View.VISIBLE);
                } else if (position == detailAdapter.getItemCount() - 1) {
                    vPdTop.setVisibility(View.GONE);
                    vPdBottom.setVisibility(View.VISIBLE);
                } else {
                    vPdTop.setVisibility(View.GONE);
                    vPdBottom.setVisibility(View.GONE);
                }
                final AddressOrderListData.AddressOrderInfo info = orderListInfo.list.get(position - 1);
                tvOrderTime.setText(TimeUtil.getActivityAddressOrderTime(info.starttime, info.endtime));
                tvOrderState.setText("1".equals(info.state) ? R.string.ordered : R.string.un_order);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(OrderTimeDetailActivity.getOrderDetailIntent(ActivityAddressDetailActivity.this, info.id));

                    }
                });
            }
        }

        public class AddressInfoViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.tv_accommodate_num)
            TextView tvAccommodateNum;
            @BindView(R.id.tv_uesable_time)
            TextView tvUesableTime;
            @BindView(R.id.tv_mic)
            TextView tvMic;
            @BindView(R.id.tv_led_screen)
            TextView tvLedScreen;
            @BindView(R.id.tv_project)
            TextView tvProject;
            @BindView(R.id.tv_screen)
            TextView tvScreen;
            /*@BindView(R.id.tv_table_num)
            TextView tvTableNum;
            @BindView(R.id.tv_table_info)
            TextView tvTableInfo;
            @BindView(R.id.tv_chair_num)
            TextView tvChairNum;
            @BindView(R.id.tv_chair_info)
            TextView tvChairInfo;
            @BindView(R.id.tv_banner_num)
            TextView tvBannerNum;
            @BindView(R.id.tv_banner_info)
            TextView tvBannerInfo;
            @BindView(R.id.tv_vertical_banner_num)
            TextView tvVerticalBannerNum;
            @BindView(R.id.tv_vertical_banner_info)
            TextView tvVerticalBannerInfo;
            @BindView(R.id.tv_tent_num)
            TextView tvTentNum;
            @BindView(R.id.tv_tent_info)
            TextView tvTentInfo;
            @BindView(R.id.tv_display_rack_num)
            TextView tvDisplayRackNum;
            @BindView(R.id.tv_display_rack_info)
            TextView tvDisplayRackInfo;*/
            @BindView(R.id.tv_equipment)
            TextView tvEquipment;
            @BindView(R.id.ll_facility)
            LinearLayout llFacility;
            /*@BindView(R.id.ll_chair)
            LinearLayout llChair;
            @BindView(R.id.ll_banner)
            LinearLayout llBanner;
            @BindView(R.id.ll_vertical_banner)
            LinearLayout llVerticalBanner;
            @BindView(R.id.ll_tent)
            LinearLayout llTent;
            @BindView(R.id.ll_display_rack)
            LinearLayout llDisplayRack;*/

            AddressInfoViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bindData(int position) {
                tvAccommodateNum.setText(getString(R.string.accommodate_num, detailInfo.fullNum));
                tvUesableTime.setText(getString(R.string.useable_time, TimeUtil.getHourMinuteTime(detailInfo.firsttime), TimeUtil.getHourMinuteTime(detailInfo.lasttime)));
                tvMic.setText(getString(R.string.mic, "无"));
                tvLedScreen.setText(getString(R.string.led_screen, "无"));
                tvProject.setText(getString(R.string.input_project, "无"));
                tvScreen.setText(getString(R.string.input_screen, "无"));
                tvEquipment.setText(getString(R.string.equipment, "无"));
                if (detailInfo.equipments != null && detailInfo.equipments.size() != 0) {
                    for (AddressDetailData.EquipmentInfo equipment : detailInfo.equipments) {
                        if (tvMic.getText().toString().contains(equipment.equipmentName)) {
                            tvMic.setText(getString(R.string.mic, "有"));
                        }
                        if (tvLedScreen.getText().toString().contains(equipment.equipmentName)) {
                            tvLedScreen.setText(getString(R.string.led_screen, "有"));
                        }
                        if (tvProject.getText().toString().contains(equipment.equipmentName)) {
                            tvProject.setText(getString(R.string.input_project, "有"));
                        }
                        if (tvScreen.getText().toString().contains(equipment.equipmentName)) {
                            tvScreen.setText(getString(R.string.input_screen, "有"));
                        }
                        if (tvEquipment.getText().toString().contains(equipment.equipmentName)) {
                            tvEquipment.setText(getString(R.string.equipment, "有"));
                        }
                    }
                }

                llFacility.removeAllViews();
                if (detailInfo.facilities != null && detailInfo.facilities.size() > 0) {
                    for (AddressDetailData.Facility facility : detailInfo.facilities) {
                        View facilityView = LayoutInflater.from(ActivityAddressDetailActivity.this).inflate(R.layout.item_facility_view, null);
                        TextView tvFacilityNum = facilityView.findViewById(R.id.tv_facility_num);
                        TextView tvFacilityInfo = facilityView.findViewById(R.id.tv_facility_info);
                        tvFacilityNum.setText(facility.facilityName + "：" + facility.amount);
                        tvFacilityInfo.setText(facility.length + "*" + facility.width + "*" + facility.height);
                        llFacility.addView(facilityView);
                    }
                }

            }
        }

    }

    @OnClick(R.id.btn_order)
    public void onViewClicked() {
        startActivity(OrderTimeActivity.getOrderTimeIntent(this, addressId, detailInfo.name,detailInfo.firsttime,detailInfo.lasttime));
    }

    @Subscribe
    public void onReceiveAddressOrderTimeEvent(AddressOrderTimeEvent event) {
        finish();
    }
}
