package cn.talianshe.android.net;

import cn.talianshe.android.db.entity.UserInfo;

/**
 * @author zcm
 * @ClassName: GlobalParams
 * @Description: 网络层全局参数
 * @date 2017/11/29 9:57
 */
public class GlobalParams {
    public static final String TEMP_PIC_DIR = "temp_pics";
    public static String TOKEN;
    public static String MOBILE;
    public static String ACCOUNT;
    public static UserInfo USER_INFO;
    public static long TIME_STAMP = 0;
    public static long TIME_DIFF = 0;
    public static String SCHOOL_ID = null;
    public static long getCurrentTimeStamp(){
        return System.currentTimeMillis()+TIME_DIFF;
    }
}
