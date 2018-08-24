package cn.talianshe.android.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.Province;
import cn.talianshe.android.eventbus.OriginPlaceEvent;
import cn.talianshe.android.widget.ScaleImageView;

/**
 * @author zcm
 * @ClassName: PrivacySettingActivity
 * @Description: 选择籍贯
 * @date 2017/12/7 19:53
 */
public class ChooseOriginPlaceActivity extends BaseActivity {

    @BindView(R.id.rv_provinces)
    RecyclerView rvProvinces;
    @BindView(R.id.rv_cities)
    RecyclerView rvCities;
    @BindView(R.id.tv_origin_place)
    TextView tvOriginPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_origin_place);
        ButterKnife.bind(this);
        initData();
    }

    private List<Province> provinceList;
    private void initData() {
        setTitle(R.string.choose_origin_place);
        btnRight.setText(R.string.confirm);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new OriginPlaceEvent(tvOriginPlace.getText().toString()));
                finish();
            }
        });
        try {
            InputStreamReader inputReader = null;
            inputReader = new InputStreamReader(getResources().getAssets().open("city.json"));
            BufferedReader bufReader = new BufferedReader(inputReader);
            provinceList = new Gson().fromJson(bufReader, new TypeToken<ArrayList<Province>>(){}.getType());
            rvProvinces.setLayoutManager(new LinearLayoutManager(this));
            rvCities.setLayoutManager(new LinearLayoutManager(this));

            myProvinceAdapter = new MyProvinceAdapter();
            rvProvinces.setAdapter(myProvinceAdapter);

            myCityAdapter = new MyCityAdapter();
            rvCities.setAdapter(myCityAdapter);

            tvOriginPlace.setText(provinceList.get(curProvinceIndex).provinceName+" "+provinceList.get(curProvinceIndex).cities.get(0).cityName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private MyProvinceAdapter myProvinceAdapter;
    private MyCityAdapter myCityAdapter;
    private int curProvinceIndex = 0;

    private class MyCityAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multi_right_list, parent, false);
            MyCityViewHolder viewHolder = new MyCityViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MyCityViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return provinceList.get(curProvinceIndex).cities.size();
        }
    }

    public class MyCityViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_head)
        ScaleImageView ivHead;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.cb_selected)
        CheckBox chSelected;

        public MyCityViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(final int postion) {
            chSelected.setVisibility(View.GONE);
            ivHead.setVisibility(View.GONE);
            tvName.setText(provinceList.get(curProvinceIndex).cities.get(postion).cityName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvOriginPlace.setText(provinceList.get(curProvinceIndex).provinceName+" "+provinceList.get(curProvinceIndex).cities.get(postion).cityName);
                }
            });
        }
    }

    private class MyProvinceAdapter extends RecyclerView.Adapter {


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multi_left_list, parent, false);
            MyProvinceViewHolder viewHolder = new MyProvinceViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MyProvinceViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return provinceList.size();
        }
    }

    public class MyProvinceViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_left)
        TextView tvProvince;

        public MyProvinceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(final int postion) {
            tvProvince.setText(provinceList.get(postion).provinceName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (postion != curProvinceIndex) {
                        curProvinceIndex = postion;
                        myCityAdapter.notifyDataSetChanged();
                        rvCities.smoothScrollToPosition(0);
                        myProvinceAdapter.notifyDataSetChanged();
                        tvOriginPlace.setText(provinceList.get(curProvinceIndex).provinceName+" "+provinceList.get(curProvinceIndex).cities.get(0).cityName);
                    }
                        LinearLayoutManager layoutManager = (LinearLayoutManager) rvProvinces.getLayoutManager();
                        int first = layoutManager.findFirstVisibleItemPosition();
                        int end = layoutManager.findLastVisibleItemPosition();
//                        int top = rvProvinces.getChildAt(first).getTop();
//                        rvProvinces.scrollBy(0,top);
                        if (curProvinceIndex <= first) {
                            rvProvinces.scrollToPosition(curProvinceIndex);
                        } else if (curProvinceIndex <= end) {
                            int top = rvProvinces.getChildAt(curProvinceIndex - first).getTop();
                            rvProvinces.scrollBy(0, top);
                        } else {
                            rvProvinces.scrollToPosition(curProvinceIndex);    //先让当前view滚动到列表内
                        }
                }
            });
            itemView.setSelected(curProvinceIndex == postion);

        }
    }
}
