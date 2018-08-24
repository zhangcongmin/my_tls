package cn.talianshe.android.activity;

import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import cn.talianshe.android.R;

/**
 * @author zcm
 * @ClassName: CreateAssociationApplySuccessActivity
 * @Description: 创建社团第三步提交成功
 * @date 2017/11/23 20:07
 */
public class CreateAssociationApplySuccessActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_association_apply_success);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.create_association);
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText(R.string.confirm);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
