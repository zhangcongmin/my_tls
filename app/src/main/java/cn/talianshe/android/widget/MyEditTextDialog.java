package cn.talianshe.android.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.talianshe.android.R;

/**
 * @author zcm
 * @ClassName: MyEditTextDialog
 * @Description: 塔联社主题输入对话光dialog
 * @date 2017/11/23 10:26
 */
public class MyEditTextDialog{
    private Context context;
    private Dialog dialog;
    private EditText etContent;
    private TextView tvInfo;
    private TextView tvCancel;
    private TextView btnConfirm;
    private Display display;

    String info; //输入框上方信息
    String defaultText; //输入框默认文字
    String hint; //输入框提示文字
    int lines; //输入框显示行数
    int maxLength; //输入框最多输入字数行数
    int minLength; //输入框最多输入字数行数

    public MyEditTextDialog(Context context,String title,String defaultText,String hint,int lines,int maxLength) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        this.info = title;
        this.hint = hint;
        this.lines = lines;
        this.maxLength = maxLength;
        this.defaultText = defaultText;
    }

    @SuppressWarnings("deprecation")
    public MyEditTextDialog builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(
                R.layout.view_dialog_edit_text, null);

        // 设置Dialog最小宽度为屏幕宽度
        view.setMinimumWidth(display.getWidth());

        // 获取自定义Dialog布局中的控件
        etContent = view.findViewById(R.id.et_content);
        tvInfo = view.findViewById(R.id.tv_info);
        tvCancel = view.findViewById(R.id.tv_cancel);
        btnConfirm = view.findViewById(R.id.btn_confirm);

        etContent.setMaxLines(lines);
        etContent.setMinLines(lines);
        if(!TextUtils.isEmpty(defaultText)){
            etContent.setText(defaultText);
        }
        if(maxLength <=0){
            maxLength = 200;
        }
        InputFilter[] filters = {new InputFilter.LengthFilter(maxLength)};
        etContent.setFilters(filters);
        etContent.setHint(hint);
        if(!TextUtils.isEmpty(defaultText))
            etContent.setText(defaultText);
        tvInfo.setText(info);
        tvCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(listener != null)
                    listener.onResult(etContent.getText().toString());
            }
        });

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

    public void addTextWatcher(TextWatcher textWatcher){
        etContent.addTextChangedListener(textWatcher);
    }


    private EditTextResultListener listener;
    public void setResultListener(EditTextResultListener resultListener){
        this.listener = resultListener;
    }
    public interface EditTextResultListener {
        void onResult(String result);
    }


    public void show() {
        dialog.show();
    }

}
