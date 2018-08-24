package cn.talianshe.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

import cn.talianshe.android.R;
import cn.talianshe.android.app.TaliansheApplication;
import cn.talianshe.android.db.DBManager;
import cn.talianshe.android.db.DaoMaster;
import cn.talianshe.android.db.UserInfoDao;
import cn.talianshe.android.db.entity.UserInfo;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.utils.DeviceIdUtil;

/**
 * @author zcm
 * @ClassName: SplashActivity
 * @Description: 闪屏页
 * @date 2017/11/1 11:01
 */
public class SplashActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getUserInfo();
    }

    private void getUserInfo() {
        UserInfoDao userInfoDao = new DaoMaster(DBManager.getInstance(this).getWritableDatabase()).newSession().getUserInfoDao();
        QueryBuilder<UserInfo> queryBuilder = userInfoDao.queryBuilder();
        queryBuilder.where(UserInfoDao.Properties.Token.isNotNull());
        UserInfo userInfo = queryBuilder.unique();
        if(userInfo != null ){
            GlobalParams.TOKEN = userInfo.token;
            GlobalParams.USER_INFO = userInfo;
            GlobalParams.SCHOOL_ID = userInfo.schoolId;

        }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    finish();
                }
            },2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
