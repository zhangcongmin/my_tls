package cn.talianshe.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import cn.talianshe.android.R;
import cn.talianshe.android.utils.DensityUtils;

/**
 * @author zcm
 * @ClassName: ${CLASS}
 * @Description:
 * @date 2017/12/7 10:00
 */

public class MyThemeEditText extends AppCompatEditText {
    private Paint mPaint;

    public MyThemeEditText(Context context) {
        this(context,null);
    }

    public MyThemeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        System.out.println("getPaddingTop()"+getPaddingTop());
        int padding = dipTopx(getContext(), 0);
        setPadding(padding+getPaddingLeft(),padding+getPaddingTop(),padding+getPaddingRight(),padding+getPaddingBottom());
    }
    /**初始化Paint***/
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setStrokeWidth(dipTopx(getContext(),1)) ;
    }
    public static int dipTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale + 0.5f*(dpValue>=0?1:-1));
    }
    @Override
    protected void onDraw(Canvas canvas) {
//        mPaint.setColor(isFocused()? ContextCompat.getColor(getContext(), R.color.theme_color):ContextCompat.getColor(getContext(), R.color.light_gray));
//        int height = getRealTextViewHeight(this) > getHeight() ? getHeight(): getHeight();
//        canvas.drawLine(0, height, this.getWidth(),height, mPaint);
//        canvas.drawLine(0,height*7/8,0,height,mPaint);
//        canvas.drawLine(getWidth(),height*7/8,getWidth(),height,mPaint);
//        System.out.println("getRealTextViewHeight"+getRealTextViewHeight(this));
        super.onDraw(canvas);

    }
    private int getRealTextViewHeight(@NonNull EditText textView) {
        int textHeight = textView.getLayout().getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();

        return textHeight + padding;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float textSize = getTextSize();
        System.out.println("textSize"+textSize);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
