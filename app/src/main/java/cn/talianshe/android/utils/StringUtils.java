package cn.talianshe.android.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by EnzoChen on 2017/12/26.
 */

public class StringUtils {

    public static InputFilter[] getEmojiFilters() {
        InputFilter emojiFilter = new InputFilter() {
            Pattern emoji = Pattern.compile(
                    "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Matcher emojiMatcher = emoji.matcher(source);
                if (emojiMatcher.find()) {
                    return "";
                }
                return null;


            }
        };
        InputFilter[] emojiFilters = {emojiFilter};
        return emojiFilters;
    }


    public static String moneyFormat(Double money) {
        DecimalFormat decimalFormat = new DecimalFormat("###0.00");//格式化设置
        return decimalFormat.format(money);
    }
}
