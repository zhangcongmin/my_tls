package cn.talianshe.android.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import cn.talianshe.android.R;
import cn.talianshe.android.app.TaliansheApplication;

/**
 * @author zcm
 * @ClassName: MyToast
 * @Description: 自定义吐司
 * @date 2017/11/5 14:50
 */
public class MyToast {

    private static final int RM_TOAST = 1000;
    private static Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RM_TOAST:
                    hide();
                    break;
            }
        }
    };

    /**
     * 窗体管理者
     */
    private static WindowManager wm;
    private static WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
    private static View mView;
    private static TextView tv;

    public static void show(int msgResId, Context context) {
        String msg = context.getResources().getString(msgResId);
        show(msg, context);
    }

    /**
     * 显示自定义吐司
     *
     * @param message
     * @param context
     */
    public static void show(String message, Context context) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
//        context = context.getApplicationContext();
//        if (mView != null) {
//
//            toastHandler.removeMessages(RM_TOAST);
//            tv.setText(message);
////            wm.updateViewLayout(tv, mParams);
//            toastHandler.sendEmptyMessageDelayed(RM_TOAST, 3000);
//            System.out.println("走return了");
//            return;
//        } else {
//            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            mView = View.inflate(context, R.layout.view_mytoast_letter, null);
//            tv = mView.findViewById(R.id.tv_letter);
////            tv = new TextView(context);
//            tv.setText(message);
////            tv.setTextSize(DensityUtils.spTopx(context, 20));
//            // 原来TN所做的工作
//            WindowManager.LayoutParams params = mParams;
//            params.height = WindowManager.LayoutParams.MATCH_PARENT;
//            params.width = WindowManager.LayoutParams.MATCH_PARENT;
//            params.format = PixelFormat.TRANSLUCENT;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                params.type = WindowManager.LayoutParams.TYPE_PHONE;
//            } else {
//                params.type = WindowManager.LayoutParams.TYPE_TOAST;
//            }
//            params.gravity = Gravity.CENTER;
//            params.setTitle("Toast");
//            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//
//            wm.addView(mView, params);
//            Animation animation = new AlphaAnimation(0, 1);
//            animation.setDuration(500);
//            animation.setFillAfter(true);
//            mView.startAnimation(animation);
//            toastHandler.sendEmptyMessageDelayed(RM_TOAST, 3000);
//        }

    }

    /**
     * 更多自定义的形式，可以直接传入任一个自己定义好的view，自己设置wm的参数
     *
     * @param view
     * @param context
     * @param params  WindowManager.LayoutParams类型的参数， WindowManager.LayoutParams
     *                mParams = new WindowManager.LayoutParams(); params.height =
     *                WindowManager.LayoutParams.WRAP_CONTENT; params.width =
     *                WindowManager.LayoutParams.WRAP_CONTENT; params.format =
     *                PixelFormat.TRANSLUCENT; params.level =
     *                WindowManager.LayoutParams.TYPE_TOAST;
     *                params.setTitle("Toast"); params.flags =
     *                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
     *                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
     *                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
     */
    public static void show(View view, Context context,
                            WindowManager.LayoutParams params) {
        hide();
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        MyToast.mView = view;
        wm.addView(mView, params);


    }

    /**
     * 隐藏自定义吐司 这里一定要记得判空一下，因为平时没有打电话时，这两个量应该都是空的
     */
    public static void hide() {
        if (wm != null) {
            if (mView != null) {
                if (mView.getParent() != null)
                    wm.removeView(mView);
                mView = null;
            }
        }
    }

}
