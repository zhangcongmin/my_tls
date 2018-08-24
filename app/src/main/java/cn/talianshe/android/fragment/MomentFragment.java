package cn.talianshe.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshViewFooter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.talianshe.android.R;
import cn.talianshe.android.activity.PostMomentActivity;
import cn.talianshe.android.adapter.MomentListAdapter;
import cn.talianshe.android.bean.MomentListData;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.MyXRefreshView;
import cn.talianshe.android.widget.pullloadmorerecyclerview.PullLoadMoreRecyclerView;
import library.talianshe.android.photobrowser.bean.PhotoBean;

/**
 * @author zcm
 * @ClassName: MomentFragment
 * @Description: 动态
 * @date 2017/11/9 19:20
 */
public class MomentFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.left_x_refresh_view)
    MyXRefreshView allXRefreshView;
    @BindView(R.id.right_x_refresh_view)
    MyXRefreshView personalXRefreshView;
    @BindView(R.id.left_loadmore_recyclerview)
    RecyclerView allRv;
    @BindView(R.id.right_loadmore_recyclerview)
    RecyclerView personalRv;
    private List<String> images;

    private TextView tvLeftTab;
    private TextView tvRightTab;
    private MomentListAdapter allMomAdapter;
    private MomentListAdapter personalMomAdapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initData();
        super.onViewCreated(view, savedInstanceState);
    }

    private void initData() {
        initTitleCenterView();
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setBackgroundResource(R.mipmap.camera);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PostMomentActivity.getPersonalMomentIntent(getActivity()));
            }
        });
        schoolApiService = RequestEngine.getInstance().getServer(SchoolApiService.class);

//        makeData();

        swipeLayout.setRefreshing(true);
        swipeLayout.setEnabled(true);
        requestData();
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                 requestData();
                
            }
        });

        allXRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onLoadMore(boolean isSilence) {
                super.onLoadMore(isSilence);
                httpSubscriber= new MomentSubscriber(mActivity);
                schoolApiService.getMomentList(allMomListInfo.curPage+1, allMomListInfo.pageSize, tvLeftTab.isSelected() ? "1" : "2").compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
            }
        });
        personalXRefreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onLoadMore(boolean isSilence) {
                httpSubscriber= new MomentSubscriber(mActivity);
                RequestEngine.getInstance().getServer(SchoolApiService.class).getMomentList(allMomListInfo.curPage+1, allMomListInfo.pageSize, tvLeftTab.isSelected() ? "1" : "2").compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
            }
        }); 

    }

    private MomentListData.MomentListInfo allMomListInfo;
    private MomentListData.MomentListInfo personalMomListInfo;

    private HttpSubscriber httpSubscriber;
    private SchoolApiService schoolApiService;

    @Override
    public void requestData() {
//        swipeLayout.setRefreshing(true);
        if(TextUtils.isEmpty(GlobalParams.TOKEN))
            return;
        httpSubscriber= new MomentSubscriber(mActivity);
        RequestEngine.getInstance().getServer(SchoolApiService.class).getMomentList(0, 20, tvLeftTab.isSelected() ? "1" : "2").compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    public class MomentSubscriber extends HttpSubscriber<MomentListData> {
        public MomentSubscriber(Context context) {
            super(context);
        }

        @Override
        public void onSuccess(MomentListData listData) {
            if (tvLeftTab.isSelected()) {
                if (swipeLayout.isRefreshing() || allMomListInfo == null) {
                    //说明是第一次加载
                    swipeLayout.setRefreshing(false);
                    allMomListInfo = listData.result;
                    allMomListInfo.curPage = 0;
//                    allMomAdapter = new MomentListAdapter(getActivity(), arrayList);
                    allMomAdapter = new MomentListAdapter(getActivity(), allMomListInfo.list);
                    allRv.setLayoutManager(new LinearLayoutManager(mActivity));
                    allRv.setAdapter(allMomAdapter);
                    allXRefreshView.setCustomFooterView(new XRefreshViewFooter(mActivity));
                } else {
                    //说明是加载更多
                    allMomListInfo.curPage = allMomListInfo.curPage + 1;
                    allMomListInfo.list.addAll(listData.result.list);
                    allXRefreshView.stopLoadMore();
                }
                allMomListInfo.pageSize = 20;
                if ((allMomListInfo.curPage+1)*allMomListInfo.pageSize >= allMomListInfo.totalCount) {
                    //说明没有加载更多,禁用
                    allXRefreshView.setPullLoadEnable(false);
                }else{
                    allXRefreshView.setPullLoadEnable(true);
                }
                allRv.getAdapter().notifyDataSetChanged();
            } else {
                if (swipeLayout.isRefreshing()) {
                    //说明是第一次加载
                    swipeLayout.setRefreshing(false);
                    personalMomListInfo = listData.result;
                    personalMomListInfo.curPage = 0;
//                    personalMomAdapter = new MomentListAdapter(getActivity(), arrayList);
                    personalMomAdapter = new MomentListAdapter(getActivity(), personalMomListInfo.list);
                    personalRv.setLayoutManager(new LinearLayoutManager(mActivity));
                    personalRv.setAdapter(personalMomAdapter);
                    personalXRefreshView.setCustomFooterView(new XRefreshViewFooter(mActivity));
                }else{
                    personalMomListInfo.curPage = personalMomListInfo.curPage + 1;
                    personalMomListInfo.list.addAll(listData.result.list);
                    personalXRefreshView.stopLoadMore();

                }
                personalMomListInfo.pageSize = 20;
                if ((personalMomListInfo.curPage+1)*personalMomListInfo.pageSize >= personalMomListInfo.totalCount) {
                    //说明没有加载更多,禁用
                    personalXRefreshView.setPullLoadEnable(false);
                }else{
                    personalXRefreshView.setPullLoadEnable(true);
                }
                personalRv.getAdapter().notifyDataSetChanged();
            }
        }

        @Override
        public void onError(String msg) {
            super.onError(msg);
            swipeLayout.setRefreshing(false);
        }
    };

    private void initTitleCenterView() {
        View titleView = setTitleBarCenterView(R.layout.view_title_center_tab);
//        View titleView = View.inflate(mActivity, R.layout.view_title_center_tab, null);
        tvLeftTab = titleView.findViewById(R.id.tv_left_tab);
        tvLeftTab.setText(R.string.all_moments);
        tvLeftTab.setSelected(true);

        tvRightTab = titleView.findViewById(R.id.tv_rightt_tab);
        tvRightTab.setText(R.string.my_moments);
        tvRightTab.setSelected(false);
        tvLeftTab.setOnClickListener(this);
        tvRightTab.setOnClickListener(this);

    }


    private void makeData() {
        String imgUrl1 = "http://com-dkhs-media-test.oss.aliyuncs.com/medias/2017/01/09/15/2014/temp_upload902408188.720x19999.jpg";
        String imgUrl2 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510318355663&di=06e7613cff881b468aeaa5a4b8ace0f9&imgtype=0&src=http%3A%2F%2Fimages.17173.com%2F2014%2F9yin%2F2014%2F03%2F29%2F20140329230429755.jpg";
        String imgUrl3 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510318355663&di=de0c78021b787eba016dbab0605ed907&imgtype=0&src=http%3A%2F%2Ffile21.mafengwo.net%2FM00%2F3F%2F13%2FwKgB3FDB7V2ATtoTAAwTZoPWCbQ46.jpeg";
        String imgUrl4 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510913228&di=0d652ff25b0f15a38499a493b7095527&imgtype=jpg&er=1&src=http%3A%2F%2Fe.hiphotos.baidu.com%2Fzhidao%2Fwh%253D450%252C600%2Fsign%3D57ad096bd60735fa91a546bdab612385%2F9825bc315c6034a8a26a13f1c913495409237632.jpg";
        String imgUrl5 = "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2180767450,2136654459&fm=27&gp=0.jpg";
        String imgUrl6 = "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=79478657,1483197780&fm=27&gp=0.jpg";
        String imgUrl7 = "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3523563477,3420922988&fm=27&gp=0.jpg";
        String imgUrl8 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510913301&di=c22d891faf56c2324036205dcdfa3697&imgtype=jpg&er=1&src=http%3A%2F%2Fimg.zcool.cn%2Fcommunity%2F018e2f5544bd8d0000019ae9800929.jpg%401280w_1l_2o_100sh.jpg";
        String imgUrl9 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510913345&di=93b4517d17bab27314bcd3068f5af2d2&imgtype=jpg&er=1&src=http%3A%2F%2Ffile20.mafengwo.net%2FM00%2FE0%2F28%2FwKgB3FHrYjiAeA8JAAJ9IrLgRyU92.jpeg";
        images = new ArrayList<>();
        images.add(imgUrl1);
        images.add(imgUrl2);
        images.add(imgUrl3);
        images.add(imgUrl4);
        images.add(imgUrl5);
        images.add(imgUrl6);
        images.add(imgUrl7);
        images.add(imgUrl8);
        images.add(imgUrl9);

        arrayList = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            PhotoBean photoBean = new PhotoBean();
            photoBean.imgUrl = images.get(i);
            arrayList.add(photoBean);
        }
    }

    private static ArrayList<PhotoBean> arrayList;

    @Override
    public int getContentViewResId() {
        return R.layout.fragment_moment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_left_tab:
                //左边tab全部动态被点击
                if (tvLeftTab.isSelected()) {
                    return;
                }
                personalXRefreshView.setVisibility(View.GONE);
                allXRefreshView.setVisibility(View.VISIBLE);
                tvLeftTab.setSelected(true);
                tvRightTab.setSelected(false);
                break;
            case R.id.tv_rightt_tab:
                if(TextUtils.isEmpty(GlobalParams.TOKEN)){
                    MyToast.show(R.string.unlogin,mActivity);
                    return;
                }
                //右边tab我的动态被点击
                if (tvRightTab.isSelected()) {
                    return;
                }
                personalXRefreshView.setVisibility(View.VISIBLE);
                allXRefreshView.setVisibility(View.GONE);
                tvRightTab.setSelected(true);
                tvLeftTab.setSelected(false);
                if(personalMomListInfo == null){
                    swipeLayout.setRefreshing(true);
                    requestData();
                }
                break;
            default:
                break;
        }
    }
}
