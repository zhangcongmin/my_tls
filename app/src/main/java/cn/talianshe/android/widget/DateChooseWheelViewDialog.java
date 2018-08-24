package cn.talianshe.android.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andrew.datepicker.widget.OnWheelChangedListener;
import com.andrew.datepicker.widget.OnWheelScrollListener;
import com.andrew.datepicker.widget.WheelView;
import com.andrew.datepicker.widget.adapters.AbstractWheelTextAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cn.talianshe.android.R;

/**
 * @author zcm
 * @ClassName: DateChooseWheelViewDialog
 * @Description: 用于时间日期的选择
 * * 使用说明：1.showLongTerm()是否显示长期选项
 * 2.setTimePickerGone隐藏时间选择
 * 3.接口DateChooseInterface
 * @date 2017/11/24 18:47
 */
public class DateChooseWheelViewDialog extends Dialog implements View.OnClickListener {
    //控件
    private WheelView mYearWheelView;
    private WheelView mMonthWheelView;
    private WheelView mDateWheelView;
    private WheelView mHourWheelView;
    private WheelView mMinuteWheelView;
    private CalendarTextAdapter mDateAdapter;
    private CalendarTextAdapter mHourAdapter;
    private CalendarTextAdapter mMinuteAdapter;
    private CalendarTextAdapter mYearAdapter;
    private CalendarTextAdapter mMonthAdapter;
    private TextView mSureButton;
    private Dialog mDialog;
    private TextView mCloseDialog;

    //变量
    private ArrayList<String> array_date = new ArrayList<>();
    private ArrayList<String> array_hour = new ArrayList<>();
    private ArrayList<String> array_minute = new ArrayList<>();
    private ArrayList<String> array_year = new ArrayList<>();
    private ArrayList<String> array_month = new ArrayList<>();

    private int nowDateId = 0;
    private int nowHourId = 0;
    private int nowMinuteId = 0;
    private int nowYearId = 0;
    private int nowMonthId = 0;
    private String mYearStr;
    private String mDateStr;
    private String mMonthStr;
    private String mHourStr;
    private String mMinuteStr;
    private boolean mBlnBeLongTerm = false;//是否需要长期
    private boolean mBlnTimePickerGone = false;//时间选择是否显示


    //常量
    private final int MAX_TEXT_SIZE = 18;
    private final int MIN_TEXT_SIZE = 16;

    private Context mContext;
    private DateChooseInterface dateChooseInterface;

    public DateChooseWheelViewDialog(Context context, DateChooseInterface dateChooseInterface) {
        super(context);
        this.mContext = context;
        this.dateChooseInterface = dateChooseInterface;
        mDialog = new Dialog(context, R.style.ActionSheetDialogStyle);
        initView();
        initData();
    }

    public void resetDate() {
        Calendar nowCalendar = Calendar.getInstance();
        array_year.clear();
        int nowYear = nowCalendar.get(Calendar.YEAR);
        array_year.clear();
        for (int i = 0; i < 10; i++) {
            int year = nowYear + i;
            array_year.add(year + "年");
            if (nowYear == year) {
                nowYearId = array_year.size() - 1;
            }
        }
        nowMonthId = nowCalendar.get(Calendar.MONTH);
        nowDateId = nowCalendar.get(Calendar.DATE) - 1;
        int nowHour = nowCalendar.get(Calendar.HOUR_OF_DAY);
        array_hour.clear();
        for (int i = 0; i <= 23; i++) {
            array_hour.add(i + "时");
            if (nowHour == i) {
                nowHourId = array_hour.size() - 1;
            }
        }
        array_minute.clear();
        int nowMinite = nowCalendar.get(Calendar.MINUTE);
        for (int i = 0; i <= 59; i++) {
            array_minute.add((i<10?"0"+i:i+"")+ "分");
            if (nowMinite == i) {
                nowMinuteId = array_minute.size() - 1;
            }
        }
        mYearWheelView.setCurrentItem(nowYearId);
        mYearStr = array_year.get(nowYearId);

        mMonthWheelView.setCurrentItem(nowMonthId);
        mMonthStr = array_month.get(nowMonthId);
        setTextViewStyle(mMonthStr, mMonthAdapter);

        mDateWheelView.setCurrentItem(nowDateId);
        mDateStr = array_date.get(nowDateId);
        setTextViewStyle(mDateStr, mDateAdapter);

        mHourWheelView.setCurrentItem(nowHourId);
        mHourStr = array_hour.get(nowHourId) + "";
        setTextViewStyle(mHourStr, mHourAdapter);

        mMinuteWheelView.setCurrentItem(nowMinuteId);
        mMinuteStr = array_minute.get(nowMinuteId) + "";
        setTextViewStyle(mMinuteStr, mMinuteAdapter);

    }


    private void initData() {
        Calendar nowCalendar = Calendar.getInstance();
        int nowYear = nowCalendar.get(Calendar.YEAR);
        initYear(nowYear);

        nowMonthId = nowCalendar.get(Calendar.MONTH);
        initMonth(nowMonthId);

        nowDateId = nowCalendar.get(Calendar.DATE) - 1;
        initDate(nowYear, nowMonthId, nowDateId);

        int nowHour = nowCalendar.get(Calendar.HOUR_OF_DAY);
        initHour(nowHour);

        int nowMinite = nowCalendar.get(Calendar.MINUTE);
        initMinute(nowMinite);
        initListener();
    }


    /**
     * 初始化滚动监听事件
     */
    private void initListener() {
        //年份*****************************
        mYearWheelView.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mYearAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mYearAdapter);
                mYearStr = array_year.get(wheel.getCurrentItem()) + "";
                //重新初始化日期，如果日期不存在说明超出，选到最后一天
                int nowYear = Integer.parseInt(mYearStr.substring(0, mYearStr.length() - 1));
                System.out.println("nowYear.." + nowYear);
                initDate(nowYear, nowMonthId, nowDateId);
            }
        });
        mYearWheelView.addScrollingListener(new OnWheelScrollListener() {

            @Override
            public void onScrollingStarted(WheelView wheel) {

            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mYearAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mYearAdapter);
            }
        });
        mMonthWheelView.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mMonthAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mMonthAdapter);
                mMonthStr = array_month.get(wheel.getCurrentItem()) + "";
//                nowMonthId = Integer.parseInt(mMonthStr.substring(0,mMonthStr.length()-1));
                nowMonthId = wheel.getCurrentItem();
                System.out.println("nowMonth.." + nowMonthId);
                int nowYear = Integer.parseInt(mYearStr.substring(0, mYearStr.length() - 1));
                System.out.println("nowYear.." + nowYear);
                initDate(nowYear, nowMonthId, nowDateId);
            }
        });

        mMonthWheelView.addScrollingListener(new OnWheelScrollListener() {

            @Override
            public void onScrollingStarted(WheelView wheel) {

            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mMonthAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mMonthAdapter);
            }
        });

        //日期********************
        mDateWheelView.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mDateAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mDateAdapter);
//                mDateCalendarTextView.setText(" " + array_date.get(wheel.getCurrentItem()));
                mDateStr = array_date.get(wheel.getCurrentItem());
//                nowDateId = Integer.parseInt(mDateStr.substring(0,mDateStr.length()-1)) -1;
                nowDateId = wheel.getCurrentItem();
                System.out.println("nowDate.." + nowDateId);
            }
        });

        mDateWheelView.addScrollingListener(new OnWheelScrollListener() {

            @Override
            public void onScrollingStarted(WheelView wheel) {

            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mDateAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mDateAdapter);
            }
        });

        //小时***********************************
        mHourWheelView.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mHourAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mHourAdapter);
                mHourStr = array_hour.get(wheel.getCurrentItem()) + "";
            }
        });

        mHourWheelView.addScrollingListener(new OnWheelScrollListener() {

            @Override
            public void onScrollingStarted(WheelView wheel) {

            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mHourAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mHourAdapter);
            }
        });

        //分钟********************************************
        mMinuteWheelView.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mMinuteAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mMinuteAdapter);
                mMinuteStr = array_minute.get(wheel.getCurrentItem()) + "";
            }
        });

        mMinuteWheelView.addScrollingListener(new OnWheelScrollListener() {

            @Override
            public void onScrollingStarted(WheelView wheel) {

            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mMinuteAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mMinuteAdapter);
            }
        });
    }

    /**
     * 初始化分钟
     */
    private void initMinute(int nowMinite) {
        array_minute.clear();
        for (int i = 0; i <= 59; i++) {
            array_minute.add((i<10?"0"+i:i+"")+ "分");
            if (nowMinite == i) {
                nowMinuteId = array_minute.size() - 1;
            }
        }

        mMinuteAdapter = new CalendarTextAdapter(mContext, array_minute, nowMinuteId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mMinuteWheelView.setVisibleItems(5);
        mMinuteWheelView.setViewAdapter(mMinuteAdapter);
//        mMinuteWheelView.setCurrentItem(nowMinuteId);
//        mMinuteStr = array_minute.get(nowMinuteId) + "";
//        setTextViewStyle(mMinuteStr, mMinuteAdapter);

    }

    /**
     * 初始化时间
     */
    private void initHour(int nowHour) {
        array_hour.clear();
        for (int i = 0; i <= 23; i++) {
            array_hour.add(i + "时");
            if (nowHour == i) {
                nowHourId = array_hour.size() - 1;
            }
        }

        mHourAdapter = new CalendarTextAdapter(mContext, array_hour, nowHourId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mHourWheelView.setVisibleItems(5);
        mHourWheelView.setViewAdapter(mHourAdapter);
//        mHourWheelView.setCurrentItem(nowHourId);
//        mHourStr = array_hour.get(nowHourId) + "";
//        setTextViewStyle(mHourStr, mHourAdapter);
    }

    /**
     * 初始化年
     */
    private void initYear(int nowYear) {
        array_year.clear();
        for (int i = 0; i < 10; i++) {
            int year = nowYear + i;
            array_year.add(year + "年");
            if (nowYear == year) {
                nowYearId = array_year.size() - 1;
            }
        }
        mYearAdapter = new CalendarTextAdapter(mContext, array_year, nowYearId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mYearWheelView.setVisibleItems(5);
        mYearWheelView.setViewAdapter(mYearAdapter);
//        mYearWheelView.setCurrentItem(nowYearId);
//        mYearStr = array_year.get(nowYearId);
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_dialog_date_choose, null);
        mDialog.setContentView(view);
        Window dialogWindow = mDialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
        mYearWheelView = view.findViewById(R.id.year_wv);
        mMonthWheelView = view.findViewById(R.id.month_wv);
        mDateWheelView = view.findViewById(R.id.date_wv);
        mHourWheelView = view.findViewById(R.id.hour_wv);
        mMinuteWheelView = view.findViewById(R.id.minute_wv);
        mSureButton = view.findViewById(R.id.btn_confirm);
        mCloseDialog = view.findViewById(R.id.tv_cancel);

        mSureButton.setOnClickListener(this);
        mCloseDialog.setOnClickListener(this);
    }

    /**
     * 初始化日期
     */
    private void initMonth(int nowMonthId) {
        array_month.clear();
        array_month.add("1月");
        array_month.add("2月");
        array_month.add("3月");
        array_month.add("4月");
        array_month.add("5月");
        array_month.add("6月");
        array_month.add("7月");
        array_month.add("8月");
        array_month.add("9月");
        array_month.add("10月");
        array_month.add("11月");
        array_month.add("12月");
        mMonthAdapter = new CalendarTextAdapter(mContext, array_month, nowMonthId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mMonthWheelView.setVisibleItems(5);
        mMonthWheelView.setViewAdapter(mMonthAdapter);
//        mMonthWheelView.setCurrentItem(nowMonthId);
//
//        mMonthStr = array_month.get(nowMonthId);
//        setTextViewStyle(mMonthStr, mMonthAdapter);
    }

    /**
     * 初始化日期
     */
    private void initDate(int nowYear, int nowMonth, int nowDateId) {
        array_date.clear();
        setDate(nowYear, nowMonth + 1);
        if (nowDateId >= array_date.size()) {
            nowDateId = array_date.size() - 1;
        }
        mDateAdapter = new CalendarTextAdapter(mContext, array_date, nowDateId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mDateWheelView.setVisibleItems(5);
        mDateWheelView.setViewAdapter(mDateAdapter);
//        mDateWheelView.setCurrentItem(nowDateId);
//
//        mDateStr = array_date.get(nowDateId);
//        setTextViewStyle(mDateStr, mDateAdapter);
    }

    public void setTimePickerGone(boolean isGone) {
        mBlnTimePickerGone = isGone;
        if (isGone) {
            LinearLayout.LayoutParams yearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            yearParams.rightMargin = 22;

            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mYearWheelView.setLayoutParams(yearParams);
            mDateWheelView.setLayoutParams(dateParams);

            mHourWheelView.setVisibility(View.GONE);
            mMinuteWheelView.setVisibility(View.GONE);
        } else {
            mHourWheelView.setVisibility(View.VISIBLE);
            mMinuteWheelView.setVisibility(View.VISIBLE);
        }

    }


    /**
     * 将改年的所有日期写入数组
     *
     * @param year
     */
    private void setDate(int year, int month) {
        boolean isRun = isRunNian(year);
        Calendar nowCalendar = Calendar.getInstance();
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                for (int day = 1; day <= 31; day++) {
                    array_date.add(day + "日");
                }
                break;
            case 2:
                if (isRun) {
                    for (int day = 1; day <= 29; day++) {
                        array_date.add(day + "日");
                    }
                } else {
                    for (int day = 1; day <= 28; day++) {
                        array_date.add(day + "日");
                    }
                }
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                for (int day = 1; day <= 30; day++) {
                    array_date.add(day + "日");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 判断是否是闰年
     *
     * @param year
     * @return
     */
    private boolean isRunNian(int year) {
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置文字的大小
     *
     * @param curriteItemText
     * @param adapter
     */
    public void setTextViewStyle(String curriteItemText, CalendarTextAdapter adapter) {
        ArrayList<View> arrayList = adapter.getTestViews();
        int size = arrayList.size();
        String currentText;
        for (int i = 0; i < size; i++) {
            TextView textvew = (TextView) arrayList.get(i);
            currentText = textvew.getText().toString();
            if (curriteItemText.equals(currentText)) {
                textvew.setTextSize(MAX_TEXT_SIZE);
                textvew.setTextColor(mContext.getResources().getColor(R.color.text_10));
            } else {
                textvew.setTextSize(MIN_TEXT_SIZE);
                textvew.setTextColor(mContext.getResources().getColor(R.color.text_11));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm://确定选择按钮监听
                if (mBlnTimePickerGone) {
                    String time = strTimeToDateFormat(mYearStr, mDateStr);
                    long longTime = 0;
                    try {
                        longTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    dateChooseInterface.getDateTime(time, mBlnBeLongTerm, longTime);
                } else {
                    String time = strTimeToDateFormat(mYearStr, mMonthStr, mDateStr, mHourStr, mMinuteStr);
                    long longTime = 0;
                    try {
                        longTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    dateChooseInterface.getDateTime(time, mBlnBeLongTerm, longTime);
                }
                dismissDialog();
                break;
            case R.id.tv_cancel://关闭日期选择对话框
                dismissDialog();
                break;
            default:
                break;
        }
    }

    /**
     * 对话框消失
     */
    private void dismissDialog() {

        if (Looper.myLooper() != Looper.getMainLooper()) {

            return;
        }

        if (null == mDialog || !mDialog.isShowing() || null == mContext
                || ((Activity) mContext).isFinishing()) {

            return;
        }

        mDialog.dismiss();
        this.dismiss();
    }

    /**
     * 显示日期选择dialog
     */
    public void showDateChooseDialog() {
        if (null != mDialog) {
            resetDate();
            mDialog.show();
            return;
        }

        if (null == mDialog) {

            return;
        }
        resetDate();
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }

    /**
     * xx年xx月xx日xx时xx分转成yyyy-MM-dd HH:mm
     *
     * @param yearStr
     * @param dateStr
     * @param hourStr
     * @param minuteStr
     * @return
     */
    private String strTimeToDateFormat(String yearStr, String monthStr, String dateStr, String hourStr, String minuteStr) {

        return yearStr.replace("年", "-") + monthStr.replace("月", "-") + dateStr.replace("日", " ")
                + hourStr.replace("时", "") + ":" + minuteStr.replace("分", "");
    }

    private String strTimeToDateFormat(String yearStr, String dateStr) {

        return yearStr.replace("年", "-") + dateStr.replace("月", "-").replace("日", "");
    }

    /**
     * 滚轮的adapter
     */
    private class CalendarTextAdapter extends AbstractWheelTextAdapter {
        ArrayList<String> list;

        protected CalendarTextAdapter(Context context, ArrayList<String> list, int currentItem, int maxsize, int minsize) {
            super(context, R.layout.item_birth_year, R.id.tempValue, currentItem, maxsize, minsize);
            this.list = list;
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            View view = super.getItem(index, cachedView, parent);
            return view;
        }

        @Override
        public int getItemsCount() {
            return list.size();
        }

        @Override
        protected CharSequence getItemText(int index) {
            String str = list.get(index) + "";
            return str;
        }
    }

    /**
     * 回调选中的时间（默认时间格式"yyyy-MM-dd HH:mm:ss"）
     */
    public interface DateChooseInterface {
        void getDateTime(String time, boolean longTimeChecked, long longTime);
    }

}
