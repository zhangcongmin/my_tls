package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

import cn.talianshe.android.utils.JianPinUtil;
import cn.talianshe.android.utils.PinYinUtil;

public class ActivityAddressListData extends BaseListBean<ActivityAddressListData.ActivityAddressListInfo> {

    public class ActivityAddressListInfo extends BaseListData<ActivityAddressInfo> {
    }

    public class ActivityAddressInfo implements Comparable<ActivityAddressInfo>{
        public String id;
        public String name;//
        @SerializedName("adress")
        public String address;
        @SerializedName("groundImg")
        public List<AddressImg> addressImgs;
        public String scene;

        public String firstLetters;
        public String pinyin;
        public List<String> wordPinyinList;
        public void initPinyin() {
            wordPinyinList = new ArrayList<>();
            pinyin = PinYinUtil.getPinyin(name,wordPinyinList);
            this.firstLetters = JianPinUtil.getSimpleCharsOfStringByTrim(name);

        }

        @Override
        public int compareTo(ActivityAddressInfo another) {
            return pinyin.compareTo(another.pinyin);
        }

    }
    public class AddressImg{
        public String imgid;
        @SerializedName("path")
        public String imgPath;
    }
}
