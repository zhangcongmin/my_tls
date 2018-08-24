package cn.talianshe.android.widget.clipimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;

/**
 * @author zcm
 * @ClassName: ClipImageBorderView
 * @Description: 图片裁剪的相关自定义view
 * @date 2017/11/22 19:04
 */
public class ClipZoomImageView extends AppCompatImageView implements
        View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {

    private String TAG = ClipZoomImageView.class.getSimpleName();

    public float SCALE_MAX = 4.0f;
    public float SCALE_MID = 2.0f;

    /**
     * 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
     */
    private float initScale = 1.0f;

    /**
     * 用于存放矩阵的9个值
     */
    private final float[] matrixValues = new float[9];

    private boolean once = true;

    /**
     * 缩放的手势检测
     */
    private ScaleGestureDetector mScaleGestureDetector = null;
    private final Matrix mScaleMatrix = new Matrix();

    private int mTouchSlop;

    /**
     * 用于双击检测
     */
    private GestureDetector mGestureDetector;
    private boolean isAutoScale = false;

    public ClipZoomImageView(Context context) {
        this(context, null);
    }

    public ClipZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isAutoScale) {
                    return true;
                }

                float x = e.getX();
                float y = e.getY();

                if (getScale() < SCALE_MID) {
                    postDelayed(new AutoScaleRunnable(SCALE_MID, x, y), 16);
                    isAutoScale = true;
                } else if (getScale() >= SCALE_MID && getScale() < SCALE_MAX) {
                    postDelayed(new AutoScaleRunnable(SCALE_MAX, x, y), 16);
                    isAutoScale = true;
                } else {
                    postDelayed(new AutoScaleRunnable(initScale, x, y), 16);
                    isAutoScale = true;
                }

                return true;
            }
        });
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        super.setScaleType(ScaleType.MATRIX);
        this.setOnTouchListener(this);
    }



    /**
     * 计算两个手指间的距离
     */
    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        /** 使用勾股定理返回两点之间的距离 */
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private int lastPointerCount = 0;
    private boolean isCanDrag = true;
    private float mLastX = 0;
    private float mLastY = 0;

    // 第一个按下的点 滑动 的开始点  
    private PointF startPointF = new PointF();
    // 两点触控式时 两个点之间的初始距离  
    private float startDistace;
    // 两点触控时 的 两点之间中间点  
    private PointF midPointF = new PointF();
    private float currentScale;

    /** 记录是拖拉照片模式还是放大缩小照片模式 */
    private int mode = 0;// 初始状态  
    /** 拖拉照片模式 */
    private static final int MODE_DRAG = 1;
    /** 放大缩小照片模式 */
    private static final int MODE_ZOOM = 2;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        float x = 0, y = 0;
        // 拿到触摸点的个数
        final int pointerCount = event.getPointerCount();
        // 拿到多个触摸点的X与Y均值
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;
        /**
         * 每当触摸点发生变化时，重置mLastX, mLastY
         */
        if (pointerCount != lastPointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }
        lastPointerCount = pointerCount;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 手指压下屏幕
            case MotionEvent.ACTION_DOWN:
                mode = MODE_DRAG;
                mScaleMatrix.set(getImageMatrix());
                // 记录ImageView当前的移动位置
                startPointF.set(event.getX(), event.getY());
                break;
            // 手指在屏幕上移动，改事件会被不断触发
            case MotionEvent.ACTION_MOVE:
                // 拖拉图片
                if (mode == MODE_DRAG) {
                    float dx = event.getX() - mLastX; // 得到x轴的移动距离
                    float dy = event.getY() - mLastY; // 得到y轴的移动距离
                    // 在没有移动之前的位置上进行移动
                    mScaleMatrix.set(mScaleMatrix); // 在不使用拖动效果较好
                    // // mScaleMatrix的更新会有一个延时
                    // // 如果在image 中使用 想缩放那样的方法这回导致 拖动距离很大, 那是成比例的一直 拖动
                    mScaleMatrix.postTranslate(dx, dy);
                    checkBorder();
                    setImageMatrix(mScaleMatrix);
                    mLastX =x;
                    mLastY = y;
                }
                // 放大缩小图片
                else if (mode == MODE_ZOOM) {
                    float endDis = distance(event);// 结束距离
                    if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                        float scale = endDis / startDistace;// 得到缩放倍数=
                        // myImageView 处理缩放事件scale * currentScale得到的是相对于图片本身的缩放倍数
                        zoomSlowLy(scale * currentScale,
                                midPointF.x, midPointF.y);
                    }
                }
                break;
            // 手指离开屏幕
            case MotionEvent.ACTION_UP:
                // 当触点离开屏幕，但是屏幕上还有触点(手指)
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                lastPointerCount = 0;
                mode = 0;
                break;
            // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = MODE_ZOOM;
                /** 计算两个手指间的距离 */
                startDistace = distance(event);
                /** 计算两个手指间的中间点 */
                if (startDistace > 10f) { // 两个手指并拢在一起的时候像素大于10
                    midPointF = mid(event);
                    // 记录当前ImageView的缩放倍数
                    currentScale = getScale();
                }
                break;
        }
        return true;
    }
    /** 计算两个手指间的中间点 */
    private PointF mid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }
    /**
     * 缩放处理的方法 通过手势 触控 计算出来的缩放比例 在穿过来之前 和 之前的Scale想乘了得到了相对于图片本身的放大倍数 
     * 在手势缩放时使用该方法 
     *
     * @param scale
     *            相对于图片本身的放大倍数 
     *
     * @param x
     *            缩放 围绕点的坐标x 
     * @param y
     *            缩放 围绕点的坐标y 
     */
    public void zoomSlowLy(float scale, float x, float y) {
        Log.i("scale", "scale*currentScale -- " + scale);

        // case1  
        // 计算缩放比是否符合要求  
        if (scale > SCALE_MAX) {
            scale = SCALE_MAX;
        }
        if (scale < initScale) {
            scale = initScale;
        }
        // 放入 Matrix 中变换比例 不能使我们计算好的, Matrix 他自己会去计算  
        // 所以 为了 是 超出缩放范围的 也能正常的进行缩放 就把 之前 乘的getScale 在除回来  
        // 这样 当 newScale > SCALE_MAX 是 scale 就能等于 SCALE_MAX / getScale();  
        float purScale = getScale();
        scale = scale / purScale;
        Log.i("scale", "purScale -- " + purScale);
        Log.i("scale", "scale / purScale -- " + scale);

        mScaleMatrix.postScale(scale, scale, x, y);// matrix 矩阵变换 处理缩放
        checkBorder();
        // 把变换完成后的 matrix 在注入回 ImageView中
        this.setImageMatrix(mScaleMatrix);
        // TODO 处理图片 是的缩放后的图片能够正常显示在屏幕中央  
        // center(true, true);  
    }

    /**
     * 是否是推动行为
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isCanDrag(float dx, float dy) {
        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
    }

    private float getScale() {
        mScaleMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    /**
     * 水平方向与View的边距
     */
    private int mHorizontalPadding;

    /**
     * 垂直方向与View的边距
     */
    private int mVerticalPadding;

    @Override
    public void onGlobalLayout() {
        if (once) {
            Drawable d = getDrawable();
            if (d == null) {
                return;
            }

            // 垂直方向的边距
            mVerticalPadding = (getHeight() - (getWidth() - 2 * mHorizontalPadding)) / 2;

            int width = getWidth();
            int height = getHeight();
            // 拿到图片的宽和高
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();
            float scale = 1.0f;
            float wScale = 1.0f;
            float hScale = 1.0f;
            // 如果图片的宽或者高小于于屏幕，则缩放到屏幕的宽或者高
            if (dw < getWidth() - 2 * mHorizontalPadding) {
                wScale = (getWidth() * 1.0f - mHorizontalPadding * 2) / dw;
            }
            if (dh < getHeight() - 2 * mVerticalPadding) {
                hScale = (getHeight() * 1.0f - mVerticalPadding * 2) / dh;
            }
            scale = Math.max(wScale,hScale);

            // 如果宽和高都大于屏幕， 则让其按比例适合屏幕大小
            if (dw > getWidth() - 2 * mHorizontalPadding && dh > getHeight() - 2 * mVerticalPadding) {
                float scaleW = (getWidth() * 1.0f - mHorizontalPadding * 2) / dw;
                float scaleH = (getHeight() * 1.0f - mVerticalPadding * 2) / dh;
                scale = Math.max(scaleW, scaleH);
            }

            initScale = scale;
            // 图片移动至屏幕中心
            SCALE_MID = initScale * 2;
            SCALE_MAX = initScale * 4;
            mScaleMatrix.postTranslate((width - dw) / 2, (height - dh) / 2);
            mScaleMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
            setImageMatrix(mScaleMatrix);
            once = false;
        }
    }

    /**
     * 根据当前图片的matrix获取图片的范围
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rectF = new RectF();
        Drawable d = getDrawable();
        if (d != null) {
            rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    private class AutoScaleRunnable implements Runnable {

        static final float BIGGER = 1.07f;
        static final float SMALLER = 0.93f;
        private float mTargetScale;
        private float tmpScale;

        /**
         * 缩放的中心
         */
        private float x;
        private float y;

        /**
         * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
         *
         * @param targetScale
         * @param x
         * @param y
         */
        public AutoScaleRunnable(float targetScale, float x, float y) {
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            } else {
                tmpScale = SMALLER;
            }
        }

        @Override
        public void run() {
            // 进行缩放
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            checkBorder();
            setImageMatrix(mScaleMatrix);

            Log.e(TAG, "tmpScale : " + tmpScale);

            final float currentScale = getScale();
            //如果值在合法范围内，继续缩放
            if (((tmpScale > 1f) && (currentScale < mTargetScale))
                    || ((tmpScale < 1f) && (mTargetScale < currentScale))) {
                postDelayed(this, 16);
            } else {
                // 设置为目标的缩放比例
                final float deltaScale = mTargetScale / currentScale;
                mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
                checkBorder();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }
        }
    }

    /**
     * @return
     */
    public Bitmap clip() {
        //获取matrix里的位移和缩放值
        float[] matrixValues = new float[9];
        mScaleMatrix.getValues(matrixValues);
        float scaleX = matrixValues[Matrix.MSCALE_X];
        float scaleY = matrixValues[Matrix.MSCALE_Y];
        float translateX = matrixValues[Matrix.MTRANS_X];
        float translateY = matrixValues[Matrix.MTRANS_Y];
        System.out.println("scaleX:"+scaleX+",scaleY:"+scaleY+",translateX:"+translateX+",translateY"+translateY);
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return Bitmap.createBitmap(bitmap, mHorizontalPadding, mVerticalPadding,
                getWidth() - 2 * mHorizontalPadding, getWidth() - 2 * mHorizontalPadding);
    }

    /**
     * 边界检测
     */
    private void checkBorder() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        // 如果宽或高大于屏幕，则控制范围；这里的0.001是因为精度丢失会产生问题，但是误差一般很小。所以我们直接加了0.01
        if (rect.width() + 0.01 >= width - 2 * mHorizontalPadding) {
            if (rect.left > mHorizontalPadding) {
                deltaX = -rect.left + mHorizontalPadding;
            }
            if (rect.right < width - mHorizontalPadding) {
                deltaX = width - mHorizontalPadding - rect.right;
            }
        }
        if (rect.height() + 0.01 >= height - 2 * mVerticalPadding) {
            if (rect.top > mVerticalPadding) {
                deltaY = -rect.top + mVerticalPadding;
            }
            if (rect.bottom < height - mVerticalPadding) {
                deltaY = height - mVerticalPadding - rect.bottom;
            }
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 设置中间距与两边的边距
     *
     * @param mHorizontalPadding
     */
    public void setHorizontalPadding(int mHorizontalPadding) {
        this.mHorizontalPadding = mHorizontalPadding;
    }
}