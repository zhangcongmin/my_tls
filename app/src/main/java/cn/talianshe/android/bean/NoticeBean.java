package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class NoticeBean {
    @SerializedName("autoid")
    public String id;
    public String title;
    public String type;
    public int stick;//是否置顶 置顶状态:1：置顶、0：不置顶
    public long createtime;
    public String content;
    public String url;
    public int index;//记录索引用于更新已读消息
}
