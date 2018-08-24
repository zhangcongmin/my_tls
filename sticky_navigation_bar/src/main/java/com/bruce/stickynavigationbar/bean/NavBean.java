package com.bruce.stickynavigationbar.bean;

import android.support.annotation.IntDef;
import android.widget.BaseAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by qizhenghao on 16/11/7.
 *
 * 导航栏bean信息
 */
public class NavBean {

    public static int TYPE_CURRENT = -1;
    public static boolean IS_NEED_ATTACH = true;        //是否需要吸附

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_FIRST, TYPE_SECOND, TYPE_THIRD, TYPE_FOURTH})
    public @interface TYPE {
    }
    public static final int TYPE_FIRST = 0;
    public static final int TYPE_SECOND = 1;
    public static final int TYPE_THIRD = 2;
    public static final int TYPE_FOURTH = 3;


    public NavBean(@TYPE int type, BaseAdapter adapter) {
        this.type = type;
        this.adapter = adapter;
        this.firstVisibleItemUniversal = 0;
        this.topDistanceUniversal = 0;
    }

    public BaseAdapter adapter;
    public int count;                       //展示的数据量
    public int type = -1;                        //参考TYPE

    public boolean hasMore = false;         // 是否可以加载更多
    public boolean isRefresh = true;        // 是否是刷新
    public int pageNo = 1;
    public int pageSize = 10;

    private int firstVisibleItem;           //第一条可见的位置
    private int topDistance;                //距离顶部的位置
    public static int topDistanceUniversal;       //存储上一次的
    public static int firstVisibleItemUniversal;  //存储上一次的


    public int getFirstVisibleItem() {
        return firstVisibleItem;
    }

    public int getTopDistance() {
        return topDistance;
    }

    public void setTopDistance(int topDistance) {
        this.topDistance = topDistance;
        topDistanceUniversal = topDistance;
    }

    public void setFirstVisibleItem(int firstVisibleItem) {
        this.firstVisibleItem = firstVisibleItem;
        firstVisibleItemUniversal = firstVisibleItem;
    }
}
