package cn.talianshe.android.db.util;

import android.content.Context;

import org.greenrobot.greendao.query.QueryBuilder;

import cn.talianshe.android.activity.LoginActivity;
import cn.talianshe.android.db.DBManager;
import cn.talianshe.android.db.DaoMaster;
import cn.talianshe.android.db.UserInfoDao;
import cn.talianshe.android.db.entity.UserInfo;
import cn.talianshe.android.net.GlobalParams;

/**
 * @author zcm
 * @ClassName: UserEntityUtil
 * @Description: 操作user实体工具类
 * @date 2017/12/11 11:35
 */
public class UserEntityUtil {
    public static void saveOrUpdateUserInfo(Context context,UserInfo userInfo) {
        UserInfoDao userInfoDao = new DaoMaster(DBManager.getInstance(context).getWritableDatabase()).newSession().getUserInfoDao();
        QueryBuilder<UserInfo> queryBuilder = userInfoDao.queryBuilder();
        queryBuilder.where(UserInfoDao.Properties.Id.eq(String.valueOf(userInfo.id)));
        UserInfo dbUserInfo = queryBuilder.unique();
        if(dbUserInfo != null){
            userInfoDao.update(userInfo);
        }else{
            userInfoDao.insert(userInfo);
        }
    }
}
