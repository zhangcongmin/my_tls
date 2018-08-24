package cn.talianshe.android.widget;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.TextView;

import cn.talianshe.android.R;
/**
 * @author zcm
 * @ClassName: MyScoreDialog
 * @Description: 活动结束评价对话框
 * @date 2017/12/5 9:48
 */
public class MyScoreDialog {
    private Context context;
    private Dialog dialog;
    private EditText etScoreContent;
    private TextView tvScore;
    private TextView btnCommit;
    private CustomStarBar starBar;
    private Display display;
    private String[] scoreValues;

    public MyScoreDialog(Context context) {
        this(context,0,null);
        this.context = context;
    }
    private int starMark;
    private String content;
    public MyScoreDialog(Context context,int starMark,String content) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        this.starMark = starMark;
        this.content = content;
    }

    @SuppressWarnings("deprecation")
    public MyScoreDialog builder() {

        scoreValues=context.getResources().getStringArray(R.array.score_values);

        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(
                R.layout.view_dialog_activity_score, null);

        // 设置Dialog最小宽度为屏幕宽度
        view.setMinimumWidth(display.getWidth());

        // 获取自定义Dialog布局中的控件
        etScoreContent = view.findViewById(R.id.et_score_content);
        tvScore = view.findViewById(R.id.tv_score);
        btnCommit = view.findViewById(R.id.btn_commit);
        starBar = view.findViewById(R.id.starBar);

        if(starMark > 0){
            starBar.setStarEditable(false);
            etScoreContent.setFocusable(false);
            etScoreContent.setText(content);
            starBar.setStarMark(starMark);
            tvScore.setText(scoreValues[starMark-1]);
            btnCommit.setText(R.string.back);
        }else{
            starBar.setStarEditable(true);
            etScoreContent.setFocusable(true);
            btnCommit.setText(R.string.commit);

        }
        btnCommit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null && starMark <= 0){
                    if(starBar.getStarMark() == 0){
                        MyToast.show("请做出评价",v.getContext());
                        return;
                    }
                    listener.onResult(etScoreContent.getText().toString(), (int) starBar.getStarMark());
                }
                dialog.dismiss();
            }
        });

        starBar.setOnStarChangeListener(new CustomStarBar.OnStarChangeListener() {
            @Override
            public void onStarChange(double mark) {
                int index = (int) (mark - 1);
                tvScore.setText(scoreValues[index]);
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


    private EditTextResultListener listener;
    public void setResultListener(EditTextResultListener resultListener){
        this.listener = resultListener;
    }
    public interface EditTextResultListener {
        void onResult(String result,int star);
    }


    public void show() {
        dialog.show();
    }

}
