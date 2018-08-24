package cn.talianshe.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.activity.ActivityDetailActivity;
import cn.talianshe.android.activity.ActivityListActivity;
import cn.talianshe.android.activity.AssociationDetailActivity;
import cn.talianshe.android.activity.AssociationListActivity;
import cn.talianshe.android.activity.OnGoingActivityListActivity;
import cn.talianshe.android.bean.AssociationActivityListData;
import cn.talianshe.android.bean.HotAssociationListData;
import cn.talianshe.android.bean.Marquee;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.widget.MarqueeTextView;

/**
 * @author zcm
 * @ClassName: HomeAdapter
 * @Description: 首页适配器
 * @date 2017/11/9 10:34
 */
public class HomeAdapter extends RecyclerViewBaseAdapter {

    private List<HotAssociationListData.HotAssociation> hotAssociationList;
    private AssociationActivityListData.AssociationActivityListInfo onGoingActivityListInfo;
    private AssociationActivityListData.AssociationActivityListInfo recommendActivityListInfo;
    private AsyncTask<Integer, Void, Bitmap> associationAsyncTask;

    public void setHotAssociationList(List<HotAssociationListData.HotAssociation> hotAssociationList) {
        this.hotAssociationList = hotAssociationList;
    }

    public void setOnGoingActivityListInfo(AssociationActivityListData.AssociationActivityListInfo onGoingActivityListInfo) {
            this.onGoingActivityListInfo = onGoingActivityListInfo;
    }

    public void setRecommendActivityListInfo(AssociationActivityListData.AssociationActivityListInfo recommendActivityListInfo) {
            this.recommendActivityListInfo = recommendActivityListInfo;
    }

    public HomeAdapter(Context mContext) {
        super(mContext);
    }

    @Override
    public int getTotalCount() {
        if (recommendActivityListInfo == null || recommendActivityListInfo.list == null) {
            return 3;
        } else {
            return 3 + recommendActivityListInfo.list.size();
        }
    }

    //顶部轮播图
    private static int TYPE_BANNER = 1;
    //热门活动跑马灯
    private static int TYPE_ACTIVITY_MARQUEE = 2;
    //热门社团
    private static int TYPE_ASSOCIATION_HOT = 3;
    //活动列表
    private static int TYPE_ACTIVITY_LIST = 4;

    @Override
    protected int getCustomeViewType(int position) {
        int viewType;
        if (position == 0) {
            viewType = TYPE_BANNER;
        } else if (position == 1) {
            viewType = TYPE_ACTIVITY_MARQUEE;
        } else if (position == 2) {
            viewType = TYPE_ASSOCIATION_HOT;
        } else {
            viewType = TYPE_ACTIVITY_LIST;
        }
        return viewType;
    }

    @Override
    protected RecyclerView.ViewHolder onCustomCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view;
        if (viewType == TYPE_BANNER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_adapter_banner, parent, false);
            int width = parent.getContext().getResources().getDisplayMetrics().widthPixels;
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = (int) (width * 4 * 1f / 9);
            view.setLayoutParams(layoutParams);
            holder = new BannerViewHolder(view);
        } else if (viewType == TYPE_ACTIVITY_MARQUEE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_adapter_activity_marquee, parent, false);
            holder = new ActivityMarqueeViewHolder(view);
        } else if (viewType == TYPE_ASSOCIATION_HOT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_adapter_association_hot, parent, false);
            holder = new AssociationHotViewHolder(view);
        } else if (viewType == TYPE_ACTIVITY_LIST) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_adapter_activity_list, parent, false);
            holder = new ActivityListViewHolder(view);
            ViewGroup.LayoutParams layoutParams = ((ActivityListViewHolder) holder).rlActivityLogo.getLayoutParams();
            int width = parent.getContext().getResources().getDisplayMetrics().widthPixels;
            layoutParams.width = width;
            layoutParams.height = (int) (width * 4 * 1f / 9);
            ((ActivityListViewHolder) holder).rlActivityLogo.setLayoutParams(layoutParams);
        }
        return holder;
    }

    @Override
    protected void onCustomBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_BANNER) {
            loadBannerData((BannerViewHolder) holder);
        } else if (getItemViewType(position) == TYPE_ACTIVITY_MARQUEE) {
            //活动跑马灯
            loadActivityMarqueeData((ActivityMarqueeViewHolder) holder);
        } else if (getItemViewType(position) == TYPE_ASSOCIATION_HOT) {
            //热门社团
            ((AssociationHotViewHolder) holder).bindData();
        } else {
            //推荐活动
            ((ActivityListViewHolder) holder).bindData(position - (getTotalCount() - recommendActivityListInfo.list.size()));

        }
    }

    /**
     * 加载跑马灯数据
     *
     * @param holder
     */
    private void loadActivityMarqueeData(ActivityMarqueeViewHolder holder) {
        if (onGoingActivityListInfo != null && onGoingActivityListInfo.list != null && onGoingActivityListInfo.list.size() > 0) {
            final List<AssociationActivityListData.AssociationActivity> list = onGoingActivityListInfo.list;
            List<Marquee> marquees = new ArrayList<>();
            for (AssociationActivityListData.AssociationActivity activityData : list) {
                Marquee marquee = new Marquee();
                marquee.setTitle(activityData.activityName);
                marquees.add(marquee);
            }
            holder.tvMarquee.setImage(false);
            holder.tvMarquee.startWithList(marquees);
            holder.tvMarquee.setOnItemClickListener(new MarqueeTextView.OnItemClickListener() {
                @Override
                public void onItemClick(int position, View textView) {
                    mContext.startActivity(ActivityDetailActivity.getActivityDetailIntent(mContext, list.get(position).id));
                }
            });
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext,OnGoingActivityListActivity.class));
            }
        });
    }

    private BannerViewHolder bannerViewHolder;
    private Handler autoLoadBannerHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int currentItem = bannerViewHolder.vpBanner.getCurrentItem();
            if (currentItem != bannerViewHolder.vpBanner.getAdapter().getCount() - 1) {
                bannerViewHolder.vpBanner.setCurrentItem(currentItem + 1);
            } else {
                bannerViewHolder.vpBanner.setCurrentItem(0);
            }
            this.sendEmptyMessageDelayed(0, 3000);
        }
    };

    /**
     * 加载banner轮播图数据
     */
    private void loadBannerData(final BannerViewHolder bannerViewHolder) {
        this.bannerViewHolder = bannerViewHolder;
        List<ImageView> imageViews = new ArrayList<>();

        ImageView imageView = new ImageView(bannerViewHolder.itemView.getContext());
        imageView.setImageResource(R.mipmap.banner3);
        imageViews.add(imageView);

        ImageView imageView2 = new ImageView(bannerViewHolder.itemView.getContext());
        imageView2.setImageResource(R.mipmap.banner1);
        imageViews.add(imageView2);

        ImageView imageView3 = new ImageView(bannerViewHolder.itemView.getContext());
        imageView3.setImageResource(R.mipmap.banner2);
        imageViews.add(imageView3);

        ImageView imageView4 = new ImageView(bannerViewHolder.itemView.getContext());
        imageView4.setImageResource(R.mipmap.banner3);
        imageViews.add(imageView4);

        ImageView imageView5 = new ImageView(bannerViewHolder.itemView.getContext());
        imageView5.setImageResource(R.mipmap.banner1);
        imageViews.add(imageView5);

        bannerViewHolder.vpBanner.setAdapter(new BannerVPAdapter(imageViews));
        bannerViewHolder.vpBanner.setCurrentItem(1);
        bannerViewHolder.vpBanner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //修正下标
                int pageIndex = 0;
                //正确数据的大小
                int size = 3;

                if (position == 0) {
                    pageIndex = size - 1;
                    //切换到最后一个页面
                    bannerViewHolder.vpBanner.setCurrentItem(size, false);
                } else if (position == size + 1) {
                    pageIndex = 0;
                    //切换到第一个页面
                    bannerViewHolder.vpBanner.setCurrentItem(1, false);
                } else {
                    pageIndex = position - 1;
                }

                //修改轮播图点的背景
                int childCount = bannerViewHolder.llPointContainer.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = bannerViewHolder.llPointContainer.getChildAt(i);
                    if (pageIndex == i) {
                        //选中的页面
                        ((ImageView) child).setImageResource(R.mipmap.banner_selected);
                    } else {
                        //未选中的页面
                        ((ImageView) child).setImageResource(R.mipmap.banner_unselected);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        bannerViewHolder.vpBanner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        autoLoadBannerHandler.removeCallbacksAndMessages(null);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        autoLoadBannerHandler.sendEmptyMessageDelayed(0, 3000);
                        break;
                }
                System.out.println("MotionEvent Action :" + event.getAction());
                return false;
            }
        });
        autoLoadBannerHandler.removeCallbacksAndMessages(null);
        autoLoadBannerHandler.sendEmptyMessageDelayed(0, 3000);
        initBannerPoint(bannerViewHolder);
    }

    //初始化点
    private void initBannerPoint(BannerViewHolder bannerViewHolder) {
        //清空容器里面的布局
        bannerViewHolder.llPointContainer.removeAllViews();
        for (int i = 0; i < 3; i++) {
            //小圆点
            ImageView view = new ImageView(bannerViewHolder.itemView.getContext());
            //设置背景颜色
            view.setImageResource(R.mipmap.banner_unselected);
            //布局参数
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //右边距
            params.rightMargin = DensityUtils.dipTopx(bannerViewHolder.itemView.getContext(), 8);
            //添加布局
            bannerViewHolder.llPointContainer.addView(view, params);
        }
        //让第一个点的背景为红色
        ((ImageView) bannerViewHolder.llPointContainer.getChildAt(0)).setImageResource(R.mipmap.banner_selected);
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {

        public BannerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @BindView(R.id.vp_banner)
        ViewPager vpBanner;
        @BindView(R.id.ll_point_container)
        LinearLayout llPointContainer;
    }

    public static class ActivityMarqueeViewHolder extends RecyclerView.ViewHolder {

        public ActivityMarqueeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @BindView(R.id.tv_marquee)
        MarqueeTextView tvMarquee;
    }

    public class AssociationHotViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ll_hot_association)
        LinearLayout llHotAssociation;
        @BindView(R.id.ll_activity_recommend)
        LinearLayout llActivityRecommend;

        public AssociationHotViewHolder(View itemView) {
            super(itemView);
            if (hotAssociationList == null)
                hotAssociationList = new ArrayList<>();
            ButterKnife.bind(this, itemView);
        }

        public void bindData() {
            llActivityRecommend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, ActivityListActivity.class));
                }
            });

            llHotAssociation.removeAllViews();
            for (int i = 0; i < hotAssociationList.size() + 1; i++) {
                     /*<TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:drawablePadding="8dp"
                    android:drawableTop="@mipmap/all_association"
                    android:text="微机社团"
                    android:textColor="@color/dark_gray"
                    android:textSize="12sp" />*/
                TextView textView = new TextView(mContext);
                textView.setTextSize(12);
                textView.setTextColor(ContextCompat.getColor(mContext, R.color.dark_gray));
                textView.setGravity(Gravity.CENTER);
                textView.setText(i == hotAssociationList.size() ? mContext.getString(R.string.association_all) : hotAssociationList.get(i).associationName);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (hotAssociationList.size() != 0) {
                    layoutParams.leftMargin = i == 0 ? 0 : DensityUtils.dipTopx(mContext, 30);
                }
                textView.setCompoundDrawablePadding(DensityUtils.dipTopx(mContext, 8));
                if (i == hotAssociationList.size()) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.all_association, 0, 0);
                }
                textView.setLayoutParams(layoutParams);
                llHotAssociation.addView(textView, layoutParams);
                textView.setTag(i);
                final int finalI = i;
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = (int) v.getTag();
                        if (index == hotAssociationList.size()) {
                            mContext.startActivity(new Intent(mContext, AssociationListActivity.class));
                        } else {
                            mContext.startActivity(AssociationDetailActivity.getAssociationDetailIntent(mContext, hotAssociationList.get(finalI).id));
                        }
                    }
                });
            }
            for (int i = 0; i < hotAssociationList.size(); i++) {
                final TextView tvAssociation = (TextView) llHotAssociation.getChildAt(i);
                tvAssociation.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_img_thumbnail_small, 0, 0);
                associationAsyncTask = new AsyncTask<Integer, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Integer... params) {
                        int index = params[0];
                        if(hotAssociationList.size()-1< index){
                            return null;
                        }
                        RequestOptions options = new RequestOptions();
                        options.placeholder(R.mipmap.ic_img_thumbnail).error(R.mipmap.ic_img_failure).centerCrop();
                        options.override(DensityUtils.dipTopx(mContext, 52));
                        FutureTarget<Bitmap> result = Glide.with(mContext).asBitmap().load(TLSUrl.BASE_URL + hotAssociationList.get(index).associationLogo).apply(options).submit();
                        try {
                            return result.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if (bitmap != null) {
                            tvAssociation.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(mContext.getResources(), bitmap), null, null);
                        } else {
                            tvAssociation.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.association_default_logo, 0, 0);

                        }
                    }
                };
                associationAsyncTask.execute(i);
            }

        }
    }

    public class ActivityListViewHolder extends RecyclerView.ViewHolder {

        public ActivityListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


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

        public void bindData(int position) {

            final AssociationActivityListData.AssociationActivity info = recommendActivityListInfo.list.get(position);
            long currentTime = GlobalParams.getCurrentTimeStamp();
            if (info.starttime <currentTime && info.endtime > currentTime) {
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
                Glide.with(mContext).load(TLSUrl.BASE_URL + info.activityLogo.imgPath).apply(optionsFirst).thumbnail(0.1f).into(ivActivityLogo);
            } else {
                ivActivityLogo.setImageResource(R.mipmap.ic_img_thumbnail_large);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(ActivityDetailActivity.getActivityDetailIntent(mContext, info.id));
                }
            });
        }
    }

}
