package cn.talianshe.android.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import cn.talianshe.android.db.entity.UserInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "user".
*/
public class UserInfoDao extends AbstractDao<UserInfo, Long> {

    public static final String TABLENAME = "user";

    /**
     * Properties of entity UserInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Token = new Property(0, String.class, "token", false, "TOKEN");
        public final static Property Id = new Property(1, Long.class, "id", true, "_id");
        public final static Property Sex = new Property(2, String.class, "sex", false, "SEX");
        public final static Property Mobile = new Property(3, String.class, "mobile", false, "MOBILE");
        public final static Property PoliticalOutlook = new Property(4, String.class, "politicalOutlook", false, "POLITICAL_OUTLOOK");
        public final static Property Nickname = new Property(5, String.class, "nickname", false, "NICKNAME");
        public final static Property Department = new Property(6, String.class, "department", false, "DEPARTMENT");
        public final static Property QqopenId = new Property(7, String.class, "qqopenId", false, "QQOPEN_ID");
        public final static Property WeixinopenId = new Property(8, String.class, "weixinopenId", false, "WEIXINOPEN_ID");
        public final static Property Avatar = new Property(9, String.class, "avatar", false, "AVATAR");
        public final static Property School = new Property(10, String.class, "school", false, "SCHOOL");
        public final static Property SchoolId = new Property(11, String.class, "schoolId", false, "SCHOOL_ID");
        public final static Property StudentId = new Property(12, String.class, "studentId", false, "STUDENT_ID");
        public final static Property TeacherId = new Property(13, String.class, "teacherId", false, "TEACHER_ID");
        public final static Property IsTeacher = new Property(14, boolean.class, "isTeacher", false, "IS_TEACHER");
        public final static Property OriginPlace = new Property(15, String.class, "originPlace", false, "ORIGIN_PLACE");
        public final static Property Email = new Property(16, String.class, "email", false, "EMAIL");
        public final static Property Grade = new Property(17, String.class, "grade", false, "GRADE");
        public final static Property Realname = new Property(18, String.class, "realname", false, "REALNAME");
        public final static Property Major = new Property(19, String.class, "major", false, "MAJOR");
        public final static Property Identity = new Property(20, String.class, "identity", false, "IDENTITY");
        public final static Property IsActivated = new Property(21, String.class, "isActivated", false, "IS_ACTIVATED");
        public final static Property Qq = new Property(22, String.class, "qq", false, "QQ");
    }


    public UserInfoDao(DaoConfig config) {
        super(config);
    }
    
    public UserInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"user\" (" + //
                "\"TOKEN\" TEXT," + // 0: token
                "\"_id\" INTEGER PRIMARY KEY ," + // 1: id
                "\"SEX\" TEXT," + // 2: sex
                "\"MOBILE\" TEXT," + // 3: mobile
                "\"POLITICAL_OUTLOOK\" TEXT," + // 4: politicalOutlook
                "\"NICKNAME\" TEXT," + // 5: nickname
                "\"DEPARTMENT\" TEXT," + // 6: department
                "\"QQOPEN_ID\" TEXT," + // 7: qqopenId
                "\"WEIXINOPEN_ID\" TEXT," + // 8: weixinopenId
                "\"AVATAR\" TEXT," + // 9: avatar
                "\"SCHOOL\" TEXT," + // 10: school
                "\"SCHOOL_ID\" TEXT," + // 11: schoolId
                "\"STUDENT_ID\" TEXT," + // 12: studentId
                "\"TEACHER_ID\" TEXT," + // 13: teacherId
                "\"IS_TEACHER\" INTEGER NOT NULL ," + // 14: isTeacher
                "\"ORIGIN_PLACE\" TEXT," + // 15: originPlace
                "\"EMAIL\" TEXT," + // 16: email
                "\"GRADE\" TEXT," + // 17: grade
                "\"REALNAME\" TEXT," + // 18: realname
                "\"MAJOR\" TEXT," + // 19: major
                "\"IDENTITY\" TEXT," + // 20: identity
                "\"IS_ACTIVATED\" TEXT," + // 21: isActivated
                "\"QQ\" TEXT);"); // 22: qq
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"user\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, UserInfo entity) {
        stmt.clearBindings();
 
        String token = entity.getToken();
        if (token != null) {
            stmt.bindString(1, token);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(2, id);
        }
 
        String sex = entity.getSex();
        if (sex != null) {
            stmt.bindString(3, sex);
        }
 
        String mobile = entity.getMobile();
        if (mobile != null) {
            stmt.bindString(4, mobile);
        }
 
        String politicalOutlook = entity.getPoliticalOutlook();
        if (politicalOutlook != null) {
            stmt.bindString(5, politicalOutlook);
        }
 
        String nickname = entity.getNickname();
        if (nickname != null) {
            stmt.bindString(6, nickname);
        }
 
        String department = entity.getDepartment();
        if (department != null) {
            stmt.bindString(7, department);
        }
 
        String qqopenId = entity.getQqopenId();
        if (qqopenId != null) {
            stmt.bindString(8, qqopenId);
        }
 
        String weixinopenId = entity.getWeixinopenId();
        if (weixinopenId != null) {
            stmt.bindString(9, weixinopenId);
        }
 
        String avatar = entity.getAvatar();
        if (avatar != null) {
            stmt.bindString(10, avatar);
        }
 
        String school = entity.getSchool();
        if (school != null) {
            stmt.bindString(11, school);
        }
 
        String schoolId = entity.getSchoolId();
        if (schoolId != null) {
            stmt.bindString(12, schoolId);
        }
 
        String studentId = entity.getStudentId();
        if (studentId != null) {
            stmt.bindString(13, studentId);
        }
 
        String teacherId = entity.getTeacherId();
        if (teacherId != null) {
            stmt.bindString(14, teacherId);
        }
        stmt.bindLong(15, entity.getIsTeacher() ? 1L: 0L);
 
        String originPlace = entity.getOriginPlace();
        if (originPlace != null) {
            stmt.bindString(16, originPlace);
        }
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(17, email);
        }
 
        String grade = entity.getGrade();
        if (grade != null) {
            stmt.bindString(18, grade);
        }
 
        String realname = entity.getRealname();
        if (realname != null) {
            stmt.bindString(19, realname);
        }
 
        String major = entity.getMajor();
        if (major != null) {
            stmt.bindString(20, major);
        }
 
        String identity = entity.getIdentity();
        if (identity != null) {
            stmt.bindString(21, identity);
        }
 
        String isActivated = entity.getIsActivated();
        if (isActivated != null) {
            stmt.bindString(22, isActivated);
        }
 
        String qq = entity.getQq();
        if (qq != null) {
            stmt.bindString(23, qq);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, UserInfo entity) {
        stmt.clearBindings();
 
        String token = entity.getToken();
        if (token != null) {
            stmt.bindString(1, token);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(2, id);
        }
 
        String sex = entity.getSex();
        if (sex != null) {
            stmt.bindString(3, sex);
        }
 
        String mobile = entity.getMobile();
        if (mobile != null) {
            stmt.bindString(4, mobile);
        }
 
        String politicalOutlook = entity.getPoliticalOutlook();
        if (politicalOutlook != null) {
            stmt.bindString(5, politicalOutlook);
        }
 
        String nickname = entity.getNickname();
        if (nickname != null) {
            stmt.bindString(6, nickname);
        }
 
        String department = entity.getDepartment();
        if (department != null) {
            stmt.bindString(7, department);
        }
 
        String qqopenId = entity.getQqopenId();
        if (qqopenId != null) {
            stmt.bindString(8, qqopenId);
        }
 
        String weixinopenId = entity.getWeixinopenId();
        if (weixinopenId != null) {
            stmt.bindString(9, weixinopenId);
        }
 
        String avatar = entity.getAvatar();
        if (avatar != null) {
            stmt.bindString(10, avatar);
        }
 
        String school = entity.getSchool();
        if (school != null) {
            stmt.bindString(11, school);
        }
 
        String schoolId = entity.getSchoolId();
        if (schoolId != null) {
            stmt.bindString(12, schoolId);
        }
 
        String studentId = entity.getStudentId();
        if (studentId != null) {
            stmt.bindString(13, studentId);
        }
 
        String teacherId = entity.getTeacherId();
        if (teacherId != null) {
            stmt.bindString(14, teacherId);
        }
        stmt.bindLong(15, entity.getIsTeacher() ? 1L: 0L);
 
        String originPlace = entity.getOriginPlace();
        if (originPlace != null) {
            stmt.bindString(16, originPlace);
        }
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(17, email);
        }
 
        String grade = entity.getGrade();
        if (grade != null) {
            stmt.bindString(18, grade);
        }
 
        String realname = entity.getRealname();
        if (realname != null) {
            stmt.bindString(19, realname);
        }
 
        String major = entity.getMajor();
        if (major != null) {
            stmt.bindString(20, major);
        }
 
        String identity = entity.getIdentity();
        if (identity != null) {
            stmt.bindString(21, identity);
        }
 
        String isActivated = entity.getIsActivated();
        if (isActivated != null) {
            stmt.bindString(22, isActivated);
        }
 
        String qq = entity.getQq();
        if (qq != null) {
            stmt.bindString(23, qq);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1);
    }    

    @Override
    public UserInfo readEntity(Cursor cursor, int offset) {
        UserInfo entity = new UserInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // token
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // id
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // sex
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // mobile
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // politicalOutlook
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // nickname
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // department
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // qqopenId
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // weixinopenId
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // avatar
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // school
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // schoolId
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // studentId
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // teacherId
            cursor.getShort(offset + 14) != 0, // isTeacher
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // originPlace
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // email
            cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17), // grade
            cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18), // realname
            cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19), // major
            cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20), // identity
            cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21), // isActivated
            cursor.isNull(offset + 22) ? null : cursor.getString(offset + 22) // qq
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, UserInfo entity, int offset) {
        entity.setToken(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setId(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setSex(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setMobile(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setPoliticalOutlook(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setNickname(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setDepartment(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setQqopenId(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setWeixinopenId(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setAvatar(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setSchool(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setSchoolId(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setStudentId(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setTeacherId(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setIsTeacher(cursor.getShort(offset + 14) != 0);
        entity.setOriginPlace(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setEmail(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setGrade(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
        entity.setRealname(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
        entity.setMajor(cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19));
        entity.setIdentity(cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20));
        entity.setIsActivated(cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21));
        entity.setQq(cursor.isNull(offset + 22) ? null : cursor.getString(offset + 22));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(UserInfo entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(UserInfo entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(UserInfo entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
