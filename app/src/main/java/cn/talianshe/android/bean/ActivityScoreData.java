package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ActivityScoreData extends BaseBean<ActivityScoreData.ActivityScoreInfo> {

    public class ActivityScoreInfo {
        public double score;
        public String evaluate;
    }
}
