package cn.talianshe.android.widget.clipimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;


/**
 * @author zcm
 * @ClassName: ClipImageBorderView
 * @Description: 图片裁剪的相关自定义view
 * @date 2017/11/22 19:04
 */
public class ClipImageLayout extends RelativeLayout {

    private ClipImageBorderView mClipImageBorderView;
    private ClipZoomImageView mClipZoomImageView;

    private int mHorizontalPadding = 20;

    public ClipImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mClipImageBorderView = new ClipImageBorderView(context);
        mClipZoomImageView = new ClipZoomImageView(context);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

//        mClipZoomImageView.setImageDrawable(getResources().getDrawable(R.mipmap.pic2));

        addView(mClipZoomImageView, lp);
        addView(mClipImageBorderView, lp);

        initPaddingValue();
    }

    /**
     * 对外公布的设置边距的方法，单位dp
     *
     * @param mHorizontalPadding
     */
    public void setHorizontalPadding(int mHorizontalPadding) {
        this.mHorizontalPadding = mHorizontalPadding;
        initPaddingValue();
    }

    /**
     * 计算padding的px
     */
    private void initPaddingValue() {
        // 计算padding的px
        mHorizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mHorizontalPadding, getResources().getDisplayMetrics());
        mClipZoomImageView.setHorizontalPadding(mHorizontalPadding);
        mClipImageBorderView.setHorizontalPadding(mHorizontalPadding);
    }

    /**
     * 截取图片
     *
     * @return
     */
    public Bitmap clip() {
        return mClipZoomImageView.clip();
    }

    public ImageView getImageView(){
        return mClipZoomImageView;
    }
}
