/**
 * @Title: ScrollViewPager.java
 * @Package: com.naerju.views
 * @Description: TODO(用一句话描述该文件做什么)
 * @author: zhoujunzhou
 * @date: 2014-5-13 上午11:44:12
 * @version: V1.0
 */
package cn.talianshe.android.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
/**
 * @author zcm
 * @ClassName: ScrollViewPager
 * @Description: 可设置是否可以滑动的viewPager
 * @date 2017/11/8 19:23
 */
public class ScrollViewPager extends ViewPager {

    private boolean isCanScroll = true;

    public ScrollViewPager(Context context) {
        super(context);
    }

    public ScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void ScrollViewPager(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    public void setCanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (isCanScroll) {
            return super.onTouchEvent(arg0);
        } else {
            return false;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (isCanScroll) {
            return super.onInterceptTouchEvent(arg0);
        } else {
            return false;
        }

    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @Title
     * @Description TODO: (用一句话描述这个方法的功能)
     */
    @Override
    protected void onMeasure(int arg0, int arg1) {
        // TODO Auto-generated method stub
        super.onMeasure(arg0, arg1);

    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

}
