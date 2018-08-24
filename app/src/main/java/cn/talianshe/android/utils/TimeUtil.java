package cn.talianshe.android.utils;

import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.parceler.apache.commons.lang.time.FastDateFormat;
import org.parceler.transfuse.annotations.Data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.talianshe.android.net.GlobalParams;


/**
 * @author zcm
 * @ClassName: TimeUtil
 * @Description: 时间格式化工具
 * @date 2017/12/15 13:34
 */
public class TimeUtil {
    public static String getDateTime(Long date) {
        return getFormatTime(new Date(date), "yyyy年MM月dd日");
    }

    public static String getDateHourMinuteTime(Long date) {
        return getFormatTime(new Date(date), "yyyy年MM月dd日 HH:mm");
    }

    public static String getDateHourMinuteSecondTime(Long date) {
        return getFormatTime(new Date(date), "yyyy年MM月dd日 HH:mm:ss");
    }

    public static String getHourMinuteTime(Long date) {
        return getFormatTime(new Date(date), "HH:mm");
    }

    public static String getWeiboTime(Long date) {
        return getInterval(new Date(date));
    }

    //    public static String formatStringDate(String date){
//        return  getInterval(new Date(date));
//    }
    public static String getInterval(Date createAt) {
        // 定义最终返回的结果字符串。
        String interval = null;
        Date currentDate = new Date();
        long millisecond = currentDate.getTime() - createAt.getTime();

        long second = millisecond / 1000;

        if (second <= 0) {
            second = 0;
        }
        //*--------------微博体（标准）
        if (currentDate.getDay() == createAt.getDay()) {
            if (second == 0) {
                interval = "刚刚";
            } else if (second < 30) {
                interval = second + "秒以前";
            } else if (second >= 30 && second < 60) {
                interval = "半分钟前";
            } else if (second >= 60 && second < 60 * 60) {//大于1分钟 小于1小时
                long minute = second / 60;
                interval = minute + "分钟前";
            } else if (second >= 60 * 60 && second < 60 * 60 * 24) {//大于1小时 小于24小时
                long hour = (second / 60) / 60;
                if (hour <= 3) {
                    interval = hour + "小时前";
                } else {
                    interval = "今天" + getFormatTime(createAt, "HH:mm");
                }
            }
        } else {
//            if (currentDate.getDay() - createAt.getDay() == 1) {
//                //一天以前
//                interval = "昨天" + getFormatTime(createAt, "HH:mm");
//            } else if (createAt.getDay() > currentDate.getDay() - 7) {
//                long day = ((second / 60) / 60) / 24;
//                interval = day + "天前";
//            } else if (createAt.getYear() == currentDate.getYear()) {
//
//                interval = getFormatTime(createAt, "MM-dd HH:mm");
//            } else {
//                interval = getFormatTime(createAt, "yyyy/MM/dd HH:mm:ss");
//            }
            if (currentDate.getDay() - createAt.getDay() == 1) {
                //一天以前
                interval = "昨天" + getFormatTime(createAt, "HH:mm");
            } else {
                if (createAt.getYear() == currentDate.getYear()) {
                    interval = getFormatTime(createAt, "MM-dd HH:mm");
                } else {
                    interval = getFormatTime(createAt, "yyyy/MM/dd HH:mm:ss");
                }
            }
        }
        return interval;
    }

    public static String getFormatTime(Date date, String Sdf) {
        return (new SimpleDateFormat(Sdf)).format(date);
    }

    public static String getActivityTime(long starttime, long endtime) {
        String time;
        Date start = new Date(starttime);
        Date currentTime = new Date(GlobalParams.getCurrentTimeStamp());
        Date end = new Date(endtime);
        if (start.getDay() == end.getDay()) {
            //同一天
            if (currentTime.getYear() == start.getYear()) {
                time = getFormatTime(start, "MM/dd HH:mm") + getFormatTime(end, "-HH:mm");
            } else {
                time = getFormatTime(start, "yyyy/MM/dd HH:mm") + getFormatTime(end, "-HH:mm");
            }
        } else {
            //不同一天
            if (currentTime.getYear() == start.getYear()) {
                time = getFormatTime(start, "MM/dd HH:mm") + getFormatTime(end, "-MM/dd HH:mm");
            } else {
                time = getFormatTime(start, "yyyy/MM/dd HH:mm") + getFormatTime(end, "-MM/dd HH:mm");

            }

        }
        return time;
    }

    public static String getActivityAddressOrderTime(long starttime, long endtime) {
        String time;
        Date start = new Date(starttime);
        Date end = new Date(endtime);
        time = getFormatTime(start, "yyyy/MM/dd HH:mm") + getFormatTime(end, "-yyyy/MM/dd HH:mm");
        return time;
    }

    public static String getActivityLeftTime(long timeStamp, long startTime) {
        String leftTime;
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = startTime - timeStamp;
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        if (day == 0) {
            if (hour == 0) {
                return min + "分钟";
            } else {
                return hour + "小时" + min + "分钟";
            }
        } else {
            return day + "天" + hour + "小时" + min + "分钟";
        }
    }

    public static String getStringDate(long time) {
        return getFormatTime(new Date(time), "yyyy-MM-dd HH:mm:ss");
    }

    public static boolean checkOrderStartTime(long startTime, long availableStartTime) {
        Date startDate = new Date(startTime);
        Date availableDate = new Date(availableStartTime);
        if (startDate.getHours() < availableDate.getHours()) {
            return false;
        } else {
            if (startDate.getHours() == availableDate.getHours()) {
                if (startDate.getMinutes() < availableDate.getMinutes()) {
                    return false;
                }
            }

        }
        return true;
    }

    public static boolean checkOrderEndTime(long endTime, long availableEndTime) {
        Date endDate = new Date(endTime);
        Date availableDate = new Date(availableEndTime);
        if (endDate.getHours() > availableDate.getHours()) {
            return false;
        } else {
            if (endDate.getHours() == availableDate.getHours()) {
                if (endDate.getMinutes() > availableDate.getMinutes()) {
                    return false;
                }
            }
        }
        return true;
    }
}
