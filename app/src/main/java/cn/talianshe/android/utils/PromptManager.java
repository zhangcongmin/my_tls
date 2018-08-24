package cn.talianshe.android.utils;

import android.app.Dialog;
import android.os.Looper;
import android.widget.Toast;


import cn.talianshe.android.app.AppConfig;
import cn.talianshe.android.app.TaliansheApplication;

/**
 * 提示信息的管理
 */

public class PromptManager {
    private static Dialog dialog;
    private static Toast mToast;



    public static void showToast(String msg, int duration) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            if (mToast == null) {
                mToast = Toast.makeText(TaliansheApplication.getInstance(), msg, duration);
            } else {
                mToast.cancel();
                mToast = Toast.makeText(TaliansheApplication.getInstance(), msg, duration);
                // mToast.setText(msg);
                // mToast.setDuration(duration);
            }
            mToast.show();
        }
    }

    public static void showLToast(String msg) {
        showToast(msg, Toast.LENGTH_LONG);
    }

    public static void showShortToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public static void showShortToast(int msgResId) {
        showShortToast(TaliansheApplication.getInstance().getResources().getString(msgResId));
    }

    public static void showToast(int msgResId) {
        showLToast(TaliansheApplication.getInstance().getResources().getString(msgResId));
    }

    public static void showToast(String msg) {
        showLToast(msg);
    }

    // 当测试阶段时true
    private static final boolean isShow = AppConfig.isDebug;

    /**
     * 测试用 在正式投入市场：删
     *
     * @param msg
     */
    public static void showToastTest(String msg) {
        if (isShow) {
            showToast(msg);
        }
    }

}
