package cn.talianshe.android.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import cn.talianshe.android.R;

/**
 * @author zcm
 * @ClassName: MyProgressDialog
 * @Description: 圆形加载中弹框
 * @date 2017/12/14 11:32
 */
public class MyProgressDialog {


    private static Dialog dialog;
    private static TextView tvTip;

    public static void show(Activity context) {
        show(context, true);
    }

    public static void show(Context context, boolean tipVisible) {
        show(context, tipVisible, null);
    }

    public static void show(Context context, boolean tipVisible, String tip) {
        if (dialog != null && dialog.getContext() == context && dialog.isShowing()) {
            return;
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }

        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(
                R.layout.view_progress_dialog, null);
        tvTip = view.findViewById(R.id.tv_tip);
        // 设置Dialog最小宽度为屏幕宽度
        view.setMinimumWidth(display.getWidth());

        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.DialogTransparentStyle);
        dialog.setContentView(view);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setAttributes(lp);
        dialog.setCancelable(true);
        tvTip.setVisibility(tipVisible ? View.VISIBLE : View.GONE);
        if (!TextUtils.isEmpty(tip)) {
            tvTip.setText(tip);
        } else {
            tvTip.setText(R.string.loading);
        }
        dialog.show();
    }

    public static void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

}
