package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshViewFooter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.adapter.MomentListAdapter;
import cn.talianshe.android.bean.ActivityPhotoListData;
import cn.talianshe.android.bean.MomentListData;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.widget.MyXRefreshView;

/**
 * @author zcm
 * @ClassName: CostSourceActivity
 * @Description: 费用来源
 * @date 2017/11/24 15:52
 */
public class AlbumListActivity extends BaseActivity {

    private static final String EXTRA_ACTIVITY_ID = "extra_activity_id";
    @BindView(R.id.rv_photos)
    RecyclerView rvPhotos;
    @BindView(R.id.m_XRefreshView)
    MyXRefreshView mXRefreshView;

    private ActivityApiService activityApiService;
    private String activityId;
    private ActivityPhotoListData.ActivityPhotoListInfo photoListInfo;
    private PhotoGridAdapter mPhotoAdapter;

    public static Intent getAlbumIntent(Context context, String activityId) {
        Intent intent = new Intent(context, AlbumListActivity.class);
        intent.putExtra(EXTRA_ACTIVITY_ID, activityId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.activity_album);
        activityId = getIntent().getStringExtra(EXTRA_ACTIVITY_ID);
        activityApiService = RequestEngine.getInstance().getServer(ActivityApiService.class);
        swipeLayout.setEnabled(true);
        swipeLayout.setRefreshing(true);
        requestData();
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
        mXRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onLoadMore(boolean isSilence) {
                super.onLoadMore(isSilence);
                HttpSubscriber httpSubscriber = new AlbumListSubscriber(AlbumListActivity.this);
                activityApiService.getActivityPhotoList(activityId, photoListInfo.curPage + 1, photoListInfo.pageSize,null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
            }
        });

    }

    public class AlbumListSubscriber extends HttpSubscriber<ActivityPhotoListData> {
        public AlbumListSubscriber(Context context) {
            super(context);
        }

        @Override
        public void onSuccess(ActivityPhotoListData listData) {
            if (swipeLayout.isRefreshing()) {
                //说明是第一次加载
                swipeLayout.setRefreshing(false);
                photoListInfo = listData.result;
                photoListInfo.curPage = 0;
//                    allMomAdapter = new MomentListAdapter(getActivity(), arrayList);
                mPhotoAdapter = new PhotoGridAdapter();
                rvPhotos.setLayoutManager(new GridLayoutManager(AlbumListActivity.this, 3));
                rvPhotos.setAdapter(mPhotoAdapter);
                mXRefreshView.setCustomFooterView(new XRefreshViewFooter(AlbumListActivity.this));
            } else {
                //说明是加载更多
                photoListInfo.curPage = photoListInfo.curPage + 1;
                photoListInfo.list.addAll(listData.result.list);
                mXRefreshView.stopLoadMore();
            }
            photoListInfo.pageSize = 30;
            if ((photoListInfo.curPage + 1) * photoListInfo.pageSize >= photoListInfo.totalCount) {
                //说明没有加载更多,禁用
                mXRefreshView.setPullLoadEnable(false);
            } else {
                mXRefreshView.setPullLoadEnable(true);
            }
            rvPhotos.getAdapter().notifyDataSetChanged();
        }

        @Override
        public void onError(String msg) {
            super.onError(msg);
            swipeLayout.setRefreshing(false);
        }
    }

    ;

    private void requestData() {
        HttpSubscriber httpSubscriber = new AlbumListSubscriber(this);
        activityApiService.getActivityPhotoList(activityId, 0, 30,null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private ActivityPhotoListData.ActivityPhotoInfo selectedPhotoInfo;
    public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoViewHolder> {


        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_album_list, parent, false);
            PhotoViewHolder holder = new PhotoViewHolder(itemView);
            holder.ivPhoto.setScaleType(ImageView.ScaleType.CENTER);

            return holder;
        }


        @Override
        public void onBindViewHolder(final PhotoViewHolder holder, final int position) {
            final ActivityPhotoListData.ActivityPhotoInfo photoInfo = photoListInfo.list.get(position);
            photoInfo.index = position;
            RequestOptions options = new RequestOptions().centerCrop()
                    .placeholder(R.drawable.ic_photo_black_48dp)
                    .error(R.drawable.ic_broken_image_black_48dp);
            if (!TextUtils.isEmpty(photoInfo.path)) {

                Glide.with(AlbumListActivity.this)
                        .load(TLSUrl.BASE_URL + photoInfo.path)
                        .thumbnail(0.1f)
                        .apply(options)
                        .into(holder.ivPhoto);
                holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: 2017/12/20 打开图片预览界面
                    }
                });
            } else {
                holder.ivPhoto.setImageResource(R.drawable.ic_photo_black_48dp);
            }
            holder.tvUpNum.setText(photoInfo.like + "");
            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityPhotoViewActivity.startPhotoViewActivity(AlbumListActivity.this,photoInfo,v);
                }
            });
        }


        @Override
        public int getItemCount() {
            if (photoListInfo != null && photoListInfo.list != null) {
                return photoListInfo.list.size();
            }
            return 0;
        }
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private TextView tvUpNum;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            tvUpNum = itemView.findViewById(R.id.tv_up_num);
        }
    }
    @Subscribe
    public void onReceiveActivityPhotoUpEvent(ActivityPhotoListData.ActivityPhotoInfo photoInfo){

        ActivityPhotoListData.ActivityPhotoInfo activityPhotoInfo = photoListInfo.list.remove(photoInfo.index);
        activityPhotoInfo.like = photoInfo.like;
        photoListInfo.list.add(photoInfo.index,activityPhotoInfo);
        rvPhotos.getAdapter().notifyDataSetChanged();
    }
}
