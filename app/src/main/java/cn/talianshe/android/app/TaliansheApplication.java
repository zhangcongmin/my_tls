package cn.talianshe.android.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.SystemClock;

import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import cn.talianshe.android.BuildConfig;
import cn.talianshe.android.bean.LongData;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.UserApiService;

/**
 * Created by zcm on 2017/11/1.
 */

public class TaliansheApplication extends Application {
    private static TaliansheApplication mInstance;
    public static TaliansheApplication getInstance() {
        return mInstance;
    }
    {
        PlatformConfig.setWeixin("wx94c6ca197b07a6cf","a6fdd3af39e2bc6ee8cfbff786689433");
        PlatformConfig.setQQZone("1106446347","dMVJXGjr5J2XYHVb");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //记录application对象
        mInstance = this;
        AppConfig.config(this);
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "");
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_DUM_NORMAL);
        CrashReport.initCrashReport(getApplicationContext(), "3c61976777", BuildConfig.isDebug);
//        GreenDaoHelper.initDatabase();
    }
    private Context context;
    public Context getContext(){
        return context;
    }
    private List<Activity> mList = new LinkedList<Activity>();
    public void addActivity(Activity activity) {
        context = activity;
        mList.add(activity);
    }
    public void removeActivity(Activity activity) {
        mList.remove(activity);
    }
    public void exit() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
            mList = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
    public void finishAllActivity() {
        try {
            if(mList != null && mList.size() > 1){
                for(int i=0;i<mList.size()-1;i++){
                    if(i != mList.size()-1){
                        mList.get(i).finish();
                    }
                }
                Activity activity = mList.get(mList.size() - 1);
                mList.clear();
                mList.add(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTimeStamp(){
        HttpSubscriber httpSubscriber = new HttpSubscriber<LongData>(mInstance) {
            @Override
            public void onSuccess(LongData data) {
                GlobalParams.TIME_DIFF = data.result - System.currentTimeMillis();
            }

        };
        RequestEngine.getInstance().getServer(UserApiService.class).getTime().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }
}
