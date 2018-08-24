package cn.talianshe.android.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import cn.talianshe.android.R;

/**
 * @author zcm
 * @ClassName: RecyclerViewBaseAdapter
 * @Description: recyclerview适配器基础增加bottom
 * @date 2017/11/9 10:34
 */
public abstract class RecyclerViewBaseAdapter extends RecyclerView.Adapter {
    public RecyclerViewBaseAdapter(Context mContext) {
        this.mContext = mContext;
    }

    protected Context mContext;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        if (viewType == TYPE_BOTTOM) {
            ImageView view = new ImageView(parent.getContext());
            view.setImageResource(R.mipmap.bottom);
            holder = new BottomViewHolder(view);
        } else {
            holder = onCustomCreateViewHolder(parent, viewType);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position != getTotalCount()) {
            onCustomBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return getTotalCount() == 0 ? 0 : getTotalCount() + 1;
    }

    public abstract int getTotalCount();

    public static int TYPE_BOTTOM = 0;

    @Override
    public int getItemViewType(int position) {

        return position == getTotalCount() ? TYPE_BOTTOM : getCustomeViewType(position);
    }

    public class BottomViewHolder extends RecyclerView.ViewHolder {

        public BottomViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 获取自定义的viewtype(附：必须从1开始返回)
     *
     * @param position
     * @return
     */
    protected abstract int getCustomeViewType(int position);

    /**
     * 自定义的创建viewholder的方法,由子类实现
     *
     * @param parent
     * @param viewType
     * @return
     */
    protected abstract RecyclerView.ViewHolder onCustomCreateViewHolder(ViewGroup parent, int viewType);

    /**
     * 自定义的绑定holder方法,由子类实现
     *
     * @param holder
     * @param position
     */
    protected abstract void onCustomBindViewHolder(RecyclerView.ViewHolder holder, int position);

}
