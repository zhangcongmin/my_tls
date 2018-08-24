package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SignTypeData extends BaseBean<SignTypeData.SignTypeInfo> {

    public class SignTypeInfo {
        public String id;
        public String remark;
        public String signType;//签到方式：1、签到码，2、扫码签到，3、手势签到

    }
}
