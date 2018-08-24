package cn.talianshe.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.utils.DeleteFileUtil;
import cn.talianshe.android.widget.ScaleImageView;
import library.talianshe.android.photobrowser.PhotoViewActivity;
import library.talianshe.android.photobrowser.bean.PhotoBean;

/**
 * @author zcm
 * @ClassName: PhotoAdapter
 * @Description: 发布动态 图片适配器
 * @date 2017/11/24 14:44
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyViewHolder> {

    private static List<String> mDatas;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private int maxPicSize;
    private String lastPicPath;
    private boolean showCover;
    private Handler mHandler = new Handler();

    public PhotoAdapter(Context context, List<String> datas, int maxPicSize, String lastPicPath, boolean showCover) {
        this.mDatas = datas;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.maxPicSize = maxPicSize;
        this.lastPicPath = lastPicPath;
        this.showCover = showCover;
    }

    public PhotoAdapter(Context context, List<String> datas, int maxPicSize, String lastPicPath) {
        this(context, datas, maxPicSize, lastPicPath, false);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mLayoutInflater.inflate(R.layout.item_photo_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (position >= maxPicSize) {//图片已选完时，隐藏添加按钮
            holder.imageView.setVisibility(View.GONE);
        } else {
            holder.imageView.setVisibility(View.VISIBLE);
        }
        if (mDatas.get(position).contains(mContext.getResources().getString(R.string.glide_plus_icon_string))) {
            holder.ivDelete.setVisibility(View.GONE);
            holder.imageView.setOnClickListener(null);
        } else {
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ArrayList<PhotoBean> arrayList = new ArrayList<PhotoBean>();
                    for(int i = 0;i<mDatas.size();i++){
                        if(mDatas.get(position).contains(lastPicPath)){
                           continue;
                        }
                        PhotoBean photoBean = new PhotoBean();
                        photoBean.imgUrl = mDatas.get(i);
                        arrayList.add(photoBean);
                    }
                    PhotoViewActivity.startPhotoViewActivity(mContext, arrayList, v, position);

                }
            });
        }

        holder.ivCover.setVisibility(position == 0 && !mDatas.get(position).contains(mContext.getResources().getString(R.string.glide_plus_icon_string)) && showCover ? View.VISIBLE : View.GONE);
        holder.ivDelete.setTag(position);

        RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(mContext).load(mDatas.get(position)).apply(options).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_cover)
        ImageView ivCover;

        @BindView(R.id.sdv)
        ScaleImageView imageView;
        @BindView(R.id.iv_delete)
        ImageView ivDelete;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.iv_delete)
        public void onViewClicked(View view) {
            removeImage((Integer) view.getTag());
        }
    }

    private void removeImage(int position) {
        String removePath = mDatas.remove(position);
        if (!mDatas.contains(lastPicPath))
            mDatas.add(lastPicPath);
        notifyDataSetChanged();
        if(!TextUtils.isEmpty(removePath)){
            DeleteFileUtil.deletePic(mContext,removePath);
        }
    }

}
