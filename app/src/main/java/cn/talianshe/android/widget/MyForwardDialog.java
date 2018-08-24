package cn.talianshe.android.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import cn.talianshe.android.R;

/**
 * @author zcm
 * @ClassName: MyForwardDialog
 * @Description: 分享转发弹框
 * @date 2017/12/15 20:25
 */
public class MyForwardDialog implements OnClickListener {
    private Context context;
    private Dialog dialog;
    private Display display;
    private boolean showTlsForward;

    public MyForwardDialog(Context context) {
        this(context,true);
    }
    public MyForwardDialog(Context context,boolean showTlsForward) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        this.showTlsForward = showTlsForward;
    }

    @SuppressWarnings("deprecation")
    public MyForwardDialog builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(
                R.layout.view_forward_dialog, null);

        // 设置Dialog最小宽度为屏幕宽度
        view.setMinimumWidth(display.getWidth());
        view.findViewById(R.id.tv_qzone).setOnClickListener(this);
        view.findViewById(R.id.tv_wechat_moment).setOnClickListener(this);
        view.findViewById(R.id.tv_tls).setOnClickListener(this);
        view.findViewById(R.id.ll_tls).setVisibility(showTlsForward?View.VISIBLE:View.GONE);

        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.ActionSheetDialogStyle);
        dialog.setContentView(view);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);

        return this;
    }

    @Override
    public void onClick(View v) {
        ForwardType type = ForwardType.TYPE_WECHAT_MOMENT;
        switch (v.getId()) {
            case R.id.tv_qzone:
                type = ForwardType.TYPE_QZONG;
                break;
            case R.id.tv_wechat_moment:
                type = ForwardType.TYPE_WECHAT_MOMENT;
                break;
            case R.id.tv_tls:
                type = ForwardType.TYPE_TLS;
                break;
        }
        if(listener != null){
            listener.onResult(type);
        }
        dialog.dismiss();
    }

    public enum ForwardType {
        TYPE_QZONG, TYPE_WECHAT_MOMENT, TYPE_TLS
    }

    private ResultListener listener;

    public void setResultListener(ResultListener resultListener) {
        this.listener = resultListener;
    }

    public interface ResultListener {
        void onResult(ForwardType type);
    }


    public void show() {
        dialog.show();
    }

}
