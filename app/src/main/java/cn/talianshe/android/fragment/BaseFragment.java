package cn.talianshe.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.talianshe.android.R;
import cn.talianshe.android.eventbus.StopSwipeLayoutRefreshEvent;

/**
 * @author zcm
 * @ClassName: BaseFragment
 * @Description: 基类fragment
 * @date 2017/11/8 18:51
 */
public abstract class BaseFragment extends Fragment {
    @BindView(R.id.btn_left)
    ImageButton btnLeft;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.ll_center)
    LinearLayout llCenter;
    @BindView(R.id.btn_right)
    TextView btnRight;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.tv_empty_tip)
    TextView tvEmpty;

    Unbinder unbinder;

    Activity mActivity;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        System.out.println("onAttach "+getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("oncreateview "+getActivity());
        View view = inflater.inflate(R.layout.fragment_base, container, false);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        View contentView = inflater.inflate(getContentViewResId(), null, false);
        ((ViewGroup) view.findViewById(R.id.ll_content)).addView(contentView, layoutParams);
        unbinder = ButterKnife.bind(this, view);
        swipeLayout.setEnabled(false);
        swipeLayout.setColorSchemeResources(R.color.theme_color);
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
        MobclickAgent.onResume(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
        MobclickAgent.onPause(getActivity());
    }

    public void requestData(){};

    public abstract int getContentViewResId();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    @OnClick({R.id.btn_left, R.id.btn_right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_left:
                break;
            case R.id.btn_right:
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
        View centerView = View.inflate(getActivity(), layoutResID, null);
        setTitleBarCenterView(centerView);
        return centerView;
    }

    public void setTitleBarCenterView(View view) {
        llCenter.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llCenter.addView(view, layoutParams);
        llCenter.setGravity(Gravity.CENTER);
    }


    @Subscribe
    public void onStopSwipeLayoutRefresh(StopSwipeLayoutRefreshEvent event){
        if(swipeLayout.isRefreshing()){
            swipeLayout.setRefreshing(false);
        }
    }
}
