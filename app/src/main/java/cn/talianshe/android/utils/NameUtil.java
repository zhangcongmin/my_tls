package cn.talianshe.android.utils;


import android.text.TextUtils;

public class NameUtil {
    public static String getName(String realName,String nickName,String isName,String isNickname){
        if(realName == null)
            realName = "";

        return "1".equals(isNickname) ? ("1".equals(isName) ? nickName + "(" + realName + ")" : nickName) : realName;
    }
}
