package cn.talianshe.android.activity;

import android.os.Bundle;

import butterknife.ButterKnife;
import cn.talianshe.android.R;

/**
 * @author zcm
 * @ClassName: AboutUsActivity
 * @Description: 关于塔联社
 * @date 2017/12/7 19:57
 */
public class AboutUsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.about_us);
    }
}
