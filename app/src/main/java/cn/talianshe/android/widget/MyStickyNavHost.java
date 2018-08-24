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
import android.widget.TextView;

import com.bruce.stickynavigationbar.bean.NavBean;
import com.bruce.stickynavigationbar.view.IStickyNavHostObserver;

import cn.talianshe.android.R;


public class MyStickyNavHost extends ViewGroup implements IStickyNavHostObserver {

    private TabItemClickListener mTabItemClickListener;

    private TabItem[] tabs;

    private Paint linePaint;
    private int lineHeight;
    private boolean showTopLine = true;
    private int measuredHeight;
    private int layoutResId;

    public MyStickyNavHost(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(getResources().getColor(R.color.grey_listview_divider));

        lineHeight = getResources().getDimensionPixelOffset(R.dimen.sticky_nav_host_line_height);
        measuredHeight = getResources().getDimensionPixelOffset(R.dimen.sticky_nav_host_height);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MyStickyNavHost);
        layoutResId = typedArray.getResourceId(R.styleable.MyStickyNavHost_layout_id, -1);
    }

    public MyStickyNavHost(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyStickyNavHost(Context context) {
        this(context, null, 0);
    }

    public void setTabItemClickListener(TabItemClickListener l) {
        mTabItemClickListener = l;
    }

    /**
     * 初始化导航栏数据
     * @param navs，会按照给定的数组顺序进行排列
     */
    public void initTabData(NavBean[] navs) {
        initTabs(navs.length);
        for (int i = 0; i < navs.length; i++) {
            tabs[i].type = navs[i].type;
            tabs[i].bindData();
        }
    }

    /**
     * 刷新某个nav的数据
     * @param nav
     */
    public void refreshTabData(NavBean nav) {
        for (TabItem tab : tabs) {
            if (tab.type == nav.type)
                tab.bindData();
        }
    }

    /**
     * 设置选中某个type
     * @param type
     */
    public void setSelectedType(@NavBean.TYPE int type) {
        for (TabItem tab : tabs) {
            if (tab.type == type)
                setSelectedItem(tab);
        }
    }

    /**
     * 设置是否显示顶部的描边
     * @param isShow
     */
    public void setShowTopLine(boolean isShow) {
        this.showTopLine = isShow;
    }

    private void initTabs(int TAB_COUNT) {
        tabs = new TabItem[TAB_COUNT];
        setBackgroundColor(Color.WHITE); //设置背景为透明
        // 初始化tabItem
        for (int i = 0; i < TAB_COUNT; i++) {
            tabs[i] = new TabItem(getContext());
            tabs[i].setPosition(i);
            initTabItem(tabs[i]);
            addView(tabs[i].getView());
        }
        if(mTabItemClickListener != null){
            mTabItemClickListener.onTabAddFinish(tabs);
        }
    }

    /**
     * 根据排列位置，切换显示的Tab
     * @param position
     */
    public void setSelectedPosition(int position) {
        setSelectedItem(tabs[position]);
    }
    private void setSelectedItem(TabItem tabItem) {
        cleanSelected();
        if (tabItem != null) {
            tabItem.setSelected();
        }
    }

    private void initTabItem(final TabItem item) {
        item.getView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTabItemClickListener != null)
                    mTabItemClickListener.onTabItemSelected(item.type);
                else
                    setSelectedPosition(item.position);
            }
        });
    }

    private void cleanSelected() {
        for (TabItem tab : tabs) {
            tab.setUnSelected();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (tabs != null) {
            final int nums = tabs.length;
            int tabWidth = Math.round(measuredWidth / nums);
            for (TabItem tab : tabs) {
                tab.getView().measure(MeasureSpec.makeMeasureSpec(tabWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY));
            }
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (tabs != null) {
            final int height = b - t;
            final int width = r - l;
            int tabWidth = width / tabs.length;
            for (int i = 0; i < tabs.length; ++i) {
                tabs[i].getView().layout(tabWidth * i, 0, tabWidth * (i + 1), height);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showTopLine)
            canvas.drawRect(0, 0, getWidth(), lineHeight, linePaint);
        canvas.drawRect(0, getHeight() - lineHeight, getWidth(), getHeight(), linePaint);
    }

    public class TabItem {
        protected Context context;
        protected int position;
        protected int type;
        protected ViewGroup itemLayout;

        public TabItem(Context context) {
            this.context = context;
            getView();
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public ViewGroup getView() {
            if (itemLayout == null) {
                itemLayout = (ViewGroup) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResId, null);
            }
            return itemLayout;
        }

        public void setSelected() {
            itemLayout.setSelected(true);
            itemLayout.requestLayout();
        }

        public void setUnSelected() {
            itemLayout.setSelected(false);
        }

        public void bindData() {
        }
    }

    public TabItem getTabItem(int position){
        if(position <= tabs.length){
            return tabs[position];
        }
        return null;
    }


    public interface TabItemClickListener {
        void onTabItemSelected(@NavBean.TYPE int type);
        void onTabAddFinish(TabItem[] items);
    }
}
