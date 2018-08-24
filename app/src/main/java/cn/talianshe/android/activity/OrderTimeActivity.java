package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.AddressOrderDetailData;
import cn.talianshe.android.bean.BooleanData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.eventbus.AddressOrderTimeEvent;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.widget.DateChooseWheelViewDialog;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: OrderTimeActivity
 * @Description: 预约时间
 * @date 2017/11/29 19:10
 */
public class OrderTimeActivity extends BaseActivity {

    @BindView(R.id.tv_start_time)
    TextView tvStartTime;
    @BindView(R.id.tv_end_time)
    TextView tvEndTime;


    private static final String EXTRA_ADDRESS_ID = "extra_address_id";
    private static final String EXTRA_ADDRESS_NAME = "extra_address_name";
    private static final String EXTRA_AVAILABLE_START_TIME = "extra_available_start_time";
    private static final String EXTRA_AVAILABLE_END_TIME = "extra_available_end_time";

    private String addressId;
    private String addressName;
    private long availableStartTime;
    private long availableEndTime;

    public static Intent getOrderTimeIntent(Context context, String addresId, String addressName,long availableStartTime,long availableEndTime) {
        Intent intent = new Intent(context, OrderTimeActivity.class);
        intent.putExtra(EXTRA_ADDRESS_ID, addresId);
        intent.putExtra(EXTRA_ADDRESS_NAME, addressName);
        intent.putExtra(EXTRA_AVAILABLE_START_TIME, availableStartTime);
        intent.putExtra(EXTRA_AVAILABLE_END_TIME, availableEndTime);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_time);
        ButterKnife.bind(this);
        initData();
        addressId = getIntent().getStringExtra(EXTRA_ADDRESS_ID);
        addressName = getIntent().getStringExtra(EXTRA_ADDRESS_NAME);
        availableStartTime = getIntent().getLongExtra(EXTRA_AVAILABLE_START_TIME,0);
        availableEndTime = getIntent().getLongExtra(EXTRA_AVAILABLE_END_TIME,0);
    }

    private void initData() {
        setTitle(R.string.order_time);
    }

    private DateChooseWheelViewDialog datePicker;
    private AddressOrderTimeEvent addressOrderTimeEvent = new AddressOrderTimeEvent();
    private int curTimeId;
    @OnClick({R.id.ll_start_time, R.id.ll_end_time, R.id.btn_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_start_time:
            case R.id.ll_end_time:
                curTimeId = view.getId();
                if (datePicker == null) {
                    datePicker = new DateChooseWheelViewDialog(this, new DateChooseWheelViewDialog.DateChooseInterface() {
                        @Override
                        public void getDateTime(String time, boolean longTimeChecked, long longTime) {
                            System.out.println(time);
                            if(curTimeId == R.id.ll_start_time){
                                if(addressOrderTimeEvent.endTime != 0 && addressOrderTimeEvent.endTime < longTime){
                                    MyToast.show(R.string.starttime_less_endtime,OrderTimeActivity.this);
                                    return;
                                }
                                addressOrderTimeEvent.startTime = longTime;
                                tvStartTime.setText(time);
                            }else{

                                if(addressOrderTimeEvent.startTime != 0 && addressOrderTimeEvent.startTime > longTime){
                                    MyToast.show(R.string.starttime_more_endtime,OrderTimeActivity.this);
                                    return;
                                }
                                addressOrderTimeEvent.endTime = longTime;
                                tvEndTime.setText(time);
                            }
                        }
                    });
                }
                datePicker.showDateChooseDialog();
                break;
            case R.id.btn_confirm:
                if(addressOrderTimeEvent.endTime != 0 && addressOrderTimeEvent.startTime != 0){
                    if(!TimeUtil.checkOrderStartTime(addressOrderTimeEvent.startTime,availableStartTime)){
                        MyToast.show(getString(R.string.order_start_time_holder,TimeUtil.getHourMinuteTime(availableStartTime)),this);
                        return;
                    }
                    if(!TimeUtil.checkOrderEndTime(addressOrderTimeEvent.endTime,availableEndTime)){
                        MyToast.show(getString(R.string.order_end_time_holder,TimeUtil.getHourMinuteTime(availableEndTime)),this);
                        return;
                    }
                    checkOrderTime(addressOrderTimeEvent);
                }else{
                    MyToast.show(R.string.activity_order_time,this);
                }
                break;
        }
    }

    private void checkOrderTime(final AddressOrderTimeEvent addressOrderTimeEvent) {
        MyProgressDialog.show(this,false);
        //获取活动类型
        final HttpSubscriber httpSubscriber = new HttpSubscriber<BooleanData>(this) {

            @Override
            public void onSuccess(BooleanData booleanData) {
                MyProgressDialog.dismiss();
                swipeLayout.setRefreshing(false);
                if(booleanData.result){
                    addressOrderTimeEvent.addressId = addressId;
                    addressOrderTimeEvent.addressName = addressName;
                    EventBus.getDefault().post(addressOrderTimeEvent);
                    finish();
                }else{
                    MyToast.show(R.string.order_time_not_available_tip,OrderTimeActivity.this);
                }

            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).checkAddressOrder(addressId, TimeUtil.getStringDate(addressOrderTimeEvent.startTime),
                TimeUtil.getStringDate(addressOrderTimeEvent.endTime)).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }
}
