package cn.talianshe.android.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import cn.talianshe.android.R;
import cn.talianshe.android.app.TaliansheApplication;
import cn.talianshe.android.eventbus.StopSwipeLayoutRefreshEvent;

/**
 * @author zcm
 * @ClassName: BaseActivity
 * @Description: 所有Activity的父类,使用统一标题栏，可做一些初始化操作
 * @date 2017/11/3 17:33
 */
public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton btnLeft;
    TextView tvTitle;
    LinearLayout llTitleBar;
    LinearLayout llCenter;
    TextView btnRight;
    LinearLayout llContent;
    SwipeRefreshLayout swipeLayout;
    TextView tvEmpty;
    View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        TaliansheApplication.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);
        initViews();
        initListeners();
    }

    private void initViews() {
        llContent = (LinearLayout) findViewById(R.id.ll_content);
        llTitleBar = (LinearLayout) findViewById(R.id.ll_title_bar);
        llCenter = (LinearLayout) findViewById(R.id.ll_center);
        btnLeft = (ImageButton) findViewById(R.id.btn_left);
        btnRight = (TextView) findViewById(R.id.btn_right);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeLayout.setEnabled(false);
        swipeLayout.setColorSchemeResources(R.color.theme_color);
        tvEmpty = (TextView) findViewById(R.id.tv_empty_tip);
        progressBar = findViewById(R.id.ll_progress_bar);
    }

    public void hideTitleBar(){
        llTitleBar.setVisibility(View.GONE);
    }

    private void initListeners() {
        btnLeft.setOnClickListener(this);
    }

    public void setContentView(@LayoutRes int layoutResID) {
        View contentView = View.inflate(this, layoutResID, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llContent.addView(contentView, layoutParams);
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_left:
                // TODO: 2017/11/2 标题栏左边按钮被按
                onBackPressed();
                break;
        }
    }

    /**
     * 设置标题
     *
     * @param titleResID
     */
    public void setTitle(int titleResID) {
        setTitle(getResources().getString(titleResID));
    }

    public void setTitle(String title) {
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
    }

    /**
     * 设置标题栏中间布局
     *
     * @param layoutResID
     * @return
     */
    public View setTitleBarCenterView(Integer layoutResID) {
        View centerView = View.inflate(this, layoutResID, null);
        setTitleBarCenterView(centerView);
        return centerView;
    }

    public void setTitleBarCenterView(View view) {
        llCenter.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llCenter.setGravity(Gravity.CENTER);
        llCenter.addView(view, layoutParams);
    }
    private boolean shouldInterceptAfterInputHide = false;
    public void shouldInterceptAfterInputHide(){
        //需要拦截
        shouldInterceptAfterInputHide = true;
    }
    private Handler handler = new Handler();
    public void setSwipeRerefreshEnable(final SwipeRefreshLayout swipeLayout, final boolean enable){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setEnabled(enable);
            }
        },500);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if(cursorInvisibleEditText != null)
                cursorInvisibleEditText.setCursorVisible(true);
            if (isShouldHideInput(v, ev)) {
                boolean hideResult = hideInputMethod(this, v);
                if(shouldInterceptAfterInputHide && hideResult)
                    return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private EditText cursorInvisibleEditText;
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            v.getLocationOnScreen(leftTop);
            int left = leftTop[0], top = leftTop[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getRawX() > left && event.getRawX() < right
                    && event.getRawY() > top && event.getRawY() < bottom) {
                // 保留点击EditText的事件
                return false;
            } else {
                cursorInvisibleEditText = (EditText) v;
                cursorInvisibleEditText.setCursorVisible(false);
                return true;
            }
        }
        return false;
    }

    private Boolean hideInputMethod(Context context, View v) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TaliansheApplication.getInstance().removeActivity(this);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onStopSwipeLayoutRefresh(StopSwipeLayoutRefreshEvent event){
        if(swipeLayout.isRefreshing()){
            swipeLayout.setRefreshing(false);
        }
    }
}
