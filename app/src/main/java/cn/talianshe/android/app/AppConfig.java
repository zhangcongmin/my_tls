package cn.talianshe.android.app;

import android.content.Context;
import android.content.pm.PackageInfo;


import cn.talianshe.android.BuildConfig;

/**
 * @author zcm
 * @ClassName: AppConfig
 * @Description: App配置 相关
 *
 * @date 2017/11/1 10:42
 */
public final class AppConfig {

    public static final boolean isDebug = BuildConfig.isDebug;

    public static final int VERSION_CURRENT = 4;//当前数据库版本号

    //是否强制替换本地数据库
    private static final boolean hasReplaceRawDB = false;

    /**
     * 版本名称
     */
    private static String mVersionName;
    /**
     * 版本号
     */
    private static int mVersionCode;

    private Context mContext;

    public static void config(Context ctx) {

        AppConfig appConfig = new AppConfig();
        appConfig.init(ctx);
    }

    public void init(Context context) {
        this.mContext = context;


        // 注册crashHandler，程序异常的日志管理工具
        if (isDebug) {
            CrashHandler.getInstance(context);
        }

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            mVersionName = info.versionName;
            mVersionCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getVersionName() {
        return mVersionName;
    }

    public static int getVersionCode() {
        return mVersionCode;
    }

}
