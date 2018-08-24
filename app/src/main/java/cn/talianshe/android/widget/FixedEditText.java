package cn.talianshe.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author zcm
 * @ClassName: FixedEditText
 * @Description: 开头文字固定的edittext
 * @date 2017/12/6 13:25
 */
public class FixedEditText extends AppCompatEditText {
    private String fixedText;
    private View.OnClickListener mListener;

    public FixedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int orignLeft;
    public void setFixedText(String text) {
        fixedText = text;
        orignLeft = getPaddingLeft();
        int left = (int) getPaint().measureText(fixedText)+ getPaddingLeft();
        setPadding(left, getPaddingTop(), getPaddingBottom(), getPaddingRight());
        invalidate();
    }

    public void setDrawableClk(View.OnClickListener listener) {
        mListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!TextUtils.isEmpty(fixedText)) {
            canvas.drawText(fixedText, orignLeft, ((getMeasuredHeight()-getPaddingTop()-getPaddingBottom())/getMaxLines() - getTextSize()) / 2 + getTextSize()+getPaddingTop(), getPaint());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mListener != null && getCompoundDrawables()[2] != null) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int i = getMeasuredWidth() - getCompoundDrawables()[2].getIntrinsicWidth();
                    if (event.getX() > i) {
                        mListener.onClick(this);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:

                    break;
                default:
                    break;
            }

        }

        return super.onTouchEvent(event);
    }

}
