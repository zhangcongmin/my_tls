package cn.talianshe.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bruce.stickynavigationbar.bean.NavBean;
import com.bruce.stickynavigationbar.view.IStickyNavHostObserver;

import cn.talianshe.android.R;


public class MyCustomStickyNavHost extends FrameLayout {

    private Paint linePaint;
    private int lineHeight;
    private boolean showTopLine = false;
    private int layoutResId;

    public MyCustomStickyNavHost(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(getResources().getColor(R.color.grey_listview_divider));

        lineHeight = getResources().getDimensionPixelOffset(R.dimen.sticky_nav_host_line_height);
    }

    public MyCustomStickyNavHost(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyCustomStickyNavHost(Context context) {
        this(context, null, 0);
    }



    /**
     * 设置是否显示顶部的描边
     * @param isShow
     */
    public void setShowTopLine(boolean isShow) {
        this.showTopLine = isShow;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showTopLine)
            canvas.drawRect(0, 0, getWidth(), lineHeight, linePaint);
        canvas.drawRect(0, getHeight() - lineHeight, getWidth(), getHeight(), linePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        setMeasuredDimension(widthMeasureSpec,measuredHeight);
    }
}
