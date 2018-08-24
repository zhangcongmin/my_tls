package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BaseListData<T> {
    public List<T> list;
    @SerializedName("total")
    public int totalCount;  //总数
    @SerializedName("page")
    public int totalPage;   //总页数
    @SerializedName("cur")
    public int curPage;     //当前页数
    @SerializedName("size")
    public int pageSize;    //每页数量

    public BaseListData() {
        this.totalCount = 20;
    }
}
