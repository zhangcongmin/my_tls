package cn.talianshe.android.utils;

import android.text.TextUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.List;

/**
 * @author zcm
 * @ClassName: PinYinUtil
 * @Description: 获取汉字的拼音
 * @date 2017/11/5 13:27
 */
public class PinYinUtil {
    /**
     * 获取汉字的拼音,本质是读取和解析xml，所以会消耗一定的内存，getPinyin方法不应该被频繁调用
     *
     * @param chinese
     * @return
     */
    public static String getPinyin(String chinese, List<String> wordPinyinList) {
        if (TextUtils.isEmpty(chinese)) return null;

        //转换汉语拼音的格式化对象
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);//大写字母
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);//不带声调

        StringBuilder builder = new StringBuilder();
        //由于pinyin4j只能对单个汉字获取拼音,只能讲字符串转为字符数组，一个一个获取，最后拼接一起
        char[] charArr = chinese.toCharArray();
        for (int i = 0; i < charArr.length; i++) {
            char c = charArr[i];

            //1.过滤掉空格
            if (Character.isWhitespace(c)) {
                //如果是空格，则继续下次遍历
                continue;
            }

            //2.判断是否是中文，此处采取比较简单的方法判断，
            //中文是占2个字节，一个字节的范围是-128~127，所以中文肯定大于127
            if (c > 127) {
                //说明可能是汉字
                try {
                    //由于多音字的存在，比如单：{dan , shan}
                    String[] pinyinArr = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArr != null && pinyinArr.length > 0) {
                        //此处虽有多音字存在，但是目前只能取第0个，如果要精确的判断汉字的读音，需要
                        //服务器提提供强大的数据库支持。
                        String cellPinyin = pinyinArr[0];
                        builder.append(cellPinyin);
                        wordPinyinList.add(cellPinyin);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                    badHanyuPinyinOutputFormatCombination.printStackTrace();
                    //如果抛异常，说明不是正确的汉字，那么忽略，不处理
                }

            } else {
                //说明肯定不是汉字，一般是英文字母，包括半角标点符号
                //对于这种情况，直接拼接
                builder.append(c);
                wordPinyinList.add(String.valueOf(c));
            }
        }

        return builder.toString();
    }

    /**
     * 获取汉字的拼音,本质是读取和解析xml，所以会消耗一定的内存，getPinyin方法不应该被频繁调用
     *
     * @param chinese
     * @return
     */
    public static String getPinyin(String chinese, List<String> wordPinyinList, String pinyin) {
        if (TextUtils.isEmpty(chinese)) return null;

        //转换汉语拼音的格式化对象
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);//大写字母
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);//不带声调

        StringBuilder builder = new StringBuilder();
        //由于pinyin4j只能对单个汉字获取拼音,只能讲字符串转为字符数组，一个一个获取，最后拼接一起
        char[] charArr = chinese.toCharArray();
        for (int i = 0; i < charArr.length; i++) {
            char c = charArr[i];

            //1.过滤掉空格
            if (Character.isWhitespace(c)) {
                //如果是空格，则继续下次遍历
                continue;
            }

            //2.判断是否是中文，此处采取比较简单的方法判断，
            //中文是占2个字节，一个字节的范围是-128~127，所以中文肯定大于127
            if (c > 127) {
                //说明可能是汉字
                try {
                    //由于多音字的存在，比如单：{dan , shan}
                    String[] pinyinArr = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArr != null && pinyinArr.length > 0) {
                        //此处虽有多音字存在，但是目前只能取第0个，如果要精确的判断汉字的读音，需要
                        //服务器提提供强大的数据库支持。
                        for (int j = 0; i < pinyinArr.length; i++) {

                            String cellPinyin = pinyinArr[j];
                            if (pinyin.startsWith(builder.toString() + cellPinyin)) {
                                builder.append(cellPinyin);

                                wordPinyinList.add(cellPinyin);
                            }
                        }
                    }
                } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                    badHanyuPinyinOutputFormatCombination.printStackTrace();
                    //如果抛异常，说明不是正确的汉字，那么忽略，不处理
                }

            } else {
                //说明肯定不是汉字，一般是英文字母，包括半角标点符号
                //对于这种情况，直接拼接
                builder.append(c);
                wordPinyinList.add(String.valueOf(c));
            }
        }

        return builder.toString();
    }
}
