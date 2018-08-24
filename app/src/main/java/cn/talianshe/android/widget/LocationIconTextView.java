package cn.talianshe.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;

import cn.talianshe.android.R;

/**
 * @author zcm
 * @ClassName: LocationIconText
 * @Description: 第一个图标与换行文字对齐
 * @date 2017/11/25 11:27
 */
public class LocationIconTextView extends AppCompatTextView {
    public LocationIconTextView(Context context) {
        super(context);
    }

    public LocationIconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        VerticalImageSpan imgSpan = new VerticalImageSpan(ContextCompat.getDrawable(getContext(),R.mipmap.location_red),lineSpacing/2);
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        CharSequence locationStr = "location";
        spannable.append(locationStr);
        spannable.append(text);
        spannable.setSpan(imgSpan, 0, locationStr.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        super.setText(spannable, type);
    }
    private int lineSpacing;

    @Override
    public void setLineSpacing(float add, float mult) {
        lineSpacing = (int) add;
        super.setLineSpacing(add, mult);
    }

    /**
     * 垂直居中的ImageSpan
     *
     * @author KenChung
     */
    public class VerticalImageSpan extends ImageSpan {

        private float space;

        public VerticalImageSpan(Drawable drawable, float space) {
            super(drawable, ImageSpan.ALIGN_BASELINE);
            this.space = space;
        }
        public VerticalImageSpan(Context context, Bitmap bitmap, float space){
            super(context,bitmap, ImageSpan.ALIGN_BASELINE);
            this.space = space;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end,
                           Paint.FontMetricsInt fontMetricsInt) {
            this.getVerticalAlignment();
            Drawable drawable = getDrawable();
            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
            Rect rect = drawable.getBounds();
            if (fontMetricsInt != null) {
                Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
                int fontHeight = fmPaint.bottom - fmPaint.top;
                int drHeight = rect.bottom - rect.top;

                int top = drHeight / 2 - fontHeight / 4;
                int bottom = drHeight / 2 + fontHeight / 4;

                fontMetricsInt.ascent = -bottom;
                fontMetricsInt.top = -bottom;
                fontMetricsInt.bottom = top;
                fontMetricsInt.descent = top;
            }
            return rect.right;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {
            Drawable drawable = getDrawable();
            canvas.save();
            int transY = 0;
            transY = (int) (((bottom - top) - drawable.getBounds().bottom) / 2 + top - space);
            canvas.translate(x, transY);
            drawable.draw(canvas);
            canvas.restore();
        }
    }
}
