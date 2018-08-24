package cn.talianshe.android.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshViewFooter;


public class MyXRefreshView extends XRefreshView implements AppBarLayout.OnOffsetChangedListener {
    public MyXRefreshView(Context context) {
        super(context);
    }

    public MyXRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPullRefreshEnable(false);
        setMoveHeadWhenDisablePullRefresh(false);
    }

    @Override
    public void moveView(float deltaY) {
//        if(mRefrshEnable){
//            super.moveView(deltaY);
//        }
        super.moveView(deltaY);
    }

    @Override
    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEventSupper(e);
    }

    private AppBarLayout appBarLayout;
    private int scrollRange;
    private boolean ignore = false;
    private boolean mRefrshEnable;
    private boolean tempRefreshEnable;
    public void setAppBarLayout(AppBarLayout appBarLayout){
        this.appBarLayout = appBarLayout;
        if(this.appBarLayout != null){
            appBarLayout.addOnOffsetChangedListener(this);
        }
    }
    //是否忽略AppBar高度进行刷新，如果忽略，任意高度都可以刷新，
    // 如果不忽略，则只有在AppBar完全收缩到最小高度才可以刷新
    public void openPullRefreshIgnoreAppBarLayout(){
        setPullRefreshEnable(true);
        ignore = false;
        tempRefreshEnable = true;
    }
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        Log.i("appbar", "-------->off:" + verticalOffset + "  ScrollRange:" + appBarLayout.getTotalScrollRange() + "  height:" + appBarLayout.getHeight());
        if(scrollRange < appBarLayout.getTotalScrollRange()){
            scrollRange = appBarLayout.getTotalScrollRange();
        }else{
            //如果不忽略高度，并且打开了刷新功能的情况下，当AppBarLayout收缩到最小高度才可以真正刷新
            if(!ignore && tempRefreshEnable){
                if(scrollRange+verticalOffset == 0){
                    //说明达到最小高度
                    setPullRefreshEnable(true);
                }else{
                    setPullRefreshEnable(false);
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(!ignore && tempRefreshEnable){
            if(mRefrshEnable){

            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setPullRefreshEnable(boolean enable) {
        super.setPullRefreshEnable(enable);
        mRefrshEnable = enable;
    }
}
