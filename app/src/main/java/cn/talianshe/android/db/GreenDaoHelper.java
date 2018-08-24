package cn.talianshe.android.db;

import android.database.sqlite.SQLiteDatabase;

import cn.talianshe.android.app.TaliansheApplication;

public class GreenDaoHelper {

    private static DaoMaster.DevOpenHelper mHelper;
    private static SQLiteDatabase mDb;
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;

    /**
     * 设置greenDao
     */
    public static void initDatabase() {
        mHelper = new DaoMaster.DevOpenHelper(TaliansheApplication.getInstance(), "student_db", null);
        mDb = mHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(mDb);
        mDaoSession = mDaoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return mDaoSession;
    }
    public static SQLiteDatabase getDb() {
        return mDb;
    }
}
