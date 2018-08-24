package cn.talianshe.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.wc.widget.dialog.IosDialog;

import cn.talianshe.android.R;
import cn.talianshe.android.activity.LoginActivity;
import cn.talianshe.android.activity.MainActivity;
import cn.talianshe.android.activity.PersonalInfoActivity;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: TipDialogUtil
 * @Description: 登录弹框
 * @date 2017/12/18 10:33
 */
public class TipDialogUtil {

    public static boolean checkFillInfo(Context context) {
        if (GlobalParams.USER_INFO == null) {
            return true;
        } else if (GlobalParams.USER_INFO.needFillInfo()) {
            showFillInfoDialog(context);
            return false;
        } else {
            return true;
        }
    }

    private static Dialog fillInfoDialog;

    public static void showFillInfoDialog(final Context context) {
        fillInfoDialog = new IosDialog.Builder(context)
                .setMessage(R.string.fill_personal_info_tip).setMessageColor(ContextCompat.getColor(context, R.color.dark_gray)).setMessageSize(15)
                .setNegativeButtonColor(ContextCompat.getColor(context, R.color.gray))
                .setNegativeButtonSize(16)
                .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButtonColor(ContextCompat.getColor(context, R.color.theme_color))
                .setPositiveButtonSize(16)
                .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        dialog.dismiss();
                        // TODO: 2017/12/8 打开设置个人信息页
                        context.startActivity(new Intent(context, PersonalInfoActivity.class));
                    }
                }).build();
        fillInfoDialog.show();
    }


    public static boolean checkLogin(Context context) {
        if (TextUtils.isEmpty(GlobalParams.TOKEN)) {
            showLoginDialog(context);
            return false;
        } else {
            return true;
        }
    }

    private static Dialog loginDialog;

    public static void showAccountUnavailableLoginDialog(final Context context) {
        if (loginDialog != null && loginDialog.isShowing())
            return;
        loginDialog = new IosDialog.Builder(context)
                .setTitle(R.string.account_unavailable).setTitleColor(ContextCompat.getColor(context, R.color.dark_gray))
                .setMessage(R.string.go_login).setMessageColor(ContextCompat.getColor(context, R.color.gray)).setMessageSize(15)
                .setNegativeButtonColor(ContextCompat.getColor(context, R.color.gray))
                .setNegativeButtonSize(16)
                .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButtonColor(ContextCompat.getColor(context, R.color.theme_color))
                .setPositiveButtonSize(16)
                .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        Intent intent = new Intent(context, LoginActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                }).build();
        loginDialog.show();
    }
    public static void showLoginDialog(final Context context) {
        if (loginDialog != null && loginDialog.isShowing())
            return;
        loginDialog = new IosDialog.Builder(context)
                .setTitle(R.string.account_unlogin).setTitleColor(ContextCompat.getColor(context, R.color.dark_gray))
                .setMessage(R.string.go_login).setMessageColor(ContextCompat.getColor(context, R.color.gray)).setMessageSize(15)
                .setNegativeButtonColor(ContextCompat.getColor(context, R.color.gray))
                .setNegativeButtonSize(16)
                .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButtonColor(ContextCompat.getColor(context, R.color.theme_color))
                .setPositiveButtonSize(16)
                .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        Intent intent = new Intent(context, LoginActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                }).build();
        loginDialog.show();
    }

    /**
     * 测试环境选择
     * @param context
     */
    public static void showSelectUrlDialog(final Context context) {
        // 获取Dialog布局
        final Dialog dialog = new Dialog(context, R.style.ActionSheetDialogStyle);
        View view = LayoutInflater.from(context).inflate(
                R.layout.zz_debug_select_url_view, null);
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        // 设置Dialog最小宽度为屏幕宽度
        view.setMinimumWidth(display.getWidth());
        view.findViewById(R.id.setting_app_staging).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TLSUrl.BASE_URL = TLSUrl.BASE_DEV_URL;
                dialog.dismiss();
            }
        });
        final EditText etUrl = view.findViewById(R.id.et_url);
        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(etUrl.getText().toString())){
                    MyToast.show("ip地址不能为空",context);
                    return;
                }
                TLSUrl.BASE_URL = "http://"+etUrl.getText().toString()+":8885";
                dialog.dismiss();
            }
        });
        // 定义Dialog布局和参数
        dialog.setContentView(view);
        dialog.setCancelable(true);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setAttributes(lp);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialog.show();
    }
}
