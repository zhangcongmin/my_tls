package cn.talianshe.android.utils;

import android.text.TextUtils;

public class PasswordCheckUtil {

    /**
     * 6-16位包含数字和字母的密码
     * @param str
     * @return
     */
    public static boolean checkPassword(String str){
        String reg = "(?!.*\\s)(?!^[\u4E00-\u9FA5]+$)(?!^[a-zA-Z]+$)(?!^[\\d]+$)(?!^[^\u4E00-\u9FA5a-zA-Z\\d]+$)^.{6,16}$";
        if(TextUtils.isEmpty(str))
            return false;
        return str.matches(reg);
    }
}
