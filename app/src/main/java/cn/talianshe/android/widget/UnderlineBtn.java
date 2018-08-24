package cn.talianshe.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.talianshe.android.R;
import cn.talianshe.android.utils.DensityUtils;

/**
 * @author zcm
 * @ClassName: UnderlineBtn
 * @Description: 带下划线的按钮
 * @date 2017/11/3 10:30
 */
public class UnderlineBtn extends RelativeLayout {
    /**
     * 对应属性
     */
    private float lineHeight, lineWeight, textSize;
    private int unCheckedColor, checkedColor,boldColor;
    private boolean isChecked;
    private Drawable drawableLeft;

    /**
     * 控件
     */
    private TextView textView;//文字
    private View view;//下划线
    private LayoutParams textViewParams, viewParams;

    public UnderlineBtn(Context context) {
        super(context);
    }

    public UnderlineBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UnderlineBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        /**
         * 获取自定义属性
         */
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.underlineBtn);
        lineHeight = array.getDimension(R.styleable.underlineBtn_lineHeight, DensityUtils.dipTopx(context,1));
        lineWeight = array.getDimension(R.styleable.underlineBtn_lineWight, LayoutParams.MATCH_PARENT);
        textSize = array.getDimensionPixelSize(R.styleable.underlineBtn_textSize, DensityUtils.spTopx(context,14));
        unCheckedColor = array.getColor(R.styleable.underlineBtn_unCheckedColor, Color.WHITE);
        checkedColor = array.getColor(R.styleable.underlineBtn_checkedColor, Color.BLUE);
        boldColor = array.getColor(R.styleable.underlineBtn_boldColor, 0);
        isChecked = array.getBoolean(R.styleable.underlineBtn_isChecked, false);
        drawableLeft = array.getDrawable(R.styleable.underlineBtn_drawableLeft);
        String text = array.getString(R.styleable.underlineBtn_text);
        array.recycle();//属性获取完之后及时回收

        //给控件赋值
        textView = new TextView(context);
//        textView.setId(StringUtil.generateViewId());
        view = new View(context);
        if (isChecked) {

            textView.setTextColor(boldColor == 0?checkedColor:boldColor);
            textView.setTypeface(boldColor == 0?Typeface.defaultFromStyle(Typeface.NORMAL):Typeface.defaultFromStyle(Typeface.BOLD));
            view.setBackgroundColor(checkedColor);
        } else {
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            textView.setTextColor(unCheckedColor);
            view.setBackgroundColor(Color.TRANSPARENT);
        }
        textView.setGravity(Gravity.CENTER);

        if(drawableLeft != null){
            textView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft,null,null,null);
            textView.setCompoundDrawablePadding(DensityUtils.dipTopx(getContext(),1));
        }
        textView.setText(text);
//        textView.setTextSize(textSize);
        textView.getPaint().setTextSize(textSize);
        //控件位置
        textViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(textView, textViewParams);

        viewParams = new LayoutParams((int) lineWeight, (int) lineHeight);
        viewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(view, viewParams);
    }

    public boolean isChecked(){
        return isChecked;
    }

    public void setTextDrawable(int resId){
        textView.setCompoundDrawablesWithIntrinsicBounds(resId,0,0,0);
        textView.setCompoundDrawablePadding(DensityUtils.dipTopx(getContext(),3));

    }
    public void setText(int strResId){
        setText(getResources().getString(strResId));
    }
    public void setText(String text){
        textView.setText(text);
        System.out.println("textview的id"+textView);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setChecked(boolean checked) {
        if (isChecked != checked) {
            isChecked = checked;
            /**
             * 保留改变后的显示，执行drawableStateChanged()中的变化
             * 不执行本方法：drawableStateChanged()中设置的改变将被复原
             */
            refreshDrawableState();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        //改变时的切换逻辑
        textView.setSelected(isChecked);
        if (isChecked) {
            textView.setTextColor(boldColor == 0?checkedColor:boldColor);
            textView.setTypeface(boldColor == 0?Typeface.defaultFromStyle(Typeface.NORMAL):Typeface.defaultFromStyle(Typeface.BOLD));
            view.setBackgroundColor(checkedColor);
        } else {
            textView.setTextColor(unCheckedColor);
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            view.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}

