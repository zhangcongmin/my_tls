package cn.talianshe.android.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.util.List;

/**
 * @author zcm
 * @ClassName: NewsCenterTabVPAdapter
 * @Description: 首页顶部banner轮播图
 * @date 2017/11/9 11:51
 */
public class BannerVPAdapter extends PagerAdapter {
    private List<ImageView> imageViews;

    public BannerVPAdapter(List<ImageView> imageViews) {
        this.imageViews = imageViews;
    }


    @Override
    public int getCount() {
        return imageViews != null ? imageViews.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = imageViews.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
