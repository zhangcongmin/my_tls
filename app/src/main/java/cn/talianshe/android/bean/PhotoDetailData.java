package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhotoDetailData extends BaseBean<PhotoDetailData.PhotoDetailInfo> {

    public class PhotoDetailInfo {
        public String activityId;
        public String activityName;
        public String nickname;
        public String realname;
        public String isname;
        public String isnickname;
        public long createtime;
        public int like;
        public int type;
        public boolean isOnGoing;
        public String activityLogo;
    }
}
