package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import cn.talianshe.android.utils.JianPinUtil;
import cn.talianshe.android.utils.PinYinUtil;

public class AddressOrderListData extends BaseListBean<AddressOrderListData.AddressOrderListInfo> {

    public class AddressOrderListInfo extends BaseListData<AddressOrderInfo> {
    }

    public class AddressOrderInfo{
        public String id;
        @SerializedName("fieldId")
        public String addressId;//
        public long starttime;
        public long endtime;
        public String state;
    }
}
