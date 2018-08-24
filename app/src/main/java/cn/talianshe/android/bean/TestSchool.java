package cn.talianshe.android.bean;

import java.util.ArrayList;
import java.util.List;

import cn.talianshe.android.utils.JianPinUtil;
import cn.talianshe.android.utils.PinYinUtil;

/**
 * @author zcm
 * @ClassName: TestSchool
 * @Description: 学校实体
 * @date 2017/11/5 13:28
 */
public class TestSchool implements Comparable<TestSchool>{
    /**
     * 学校id
     */
    public String id;
    public String name;
    public String pinyin;
    public String firstLetters;
    public List<String> wordPinyinList = new ArrayList<>();

    public TestSchool(String name) {
        this.name = name;
        this.pinyin = PinYinUtil.getPinyin(name,wordPinyinList);
        this.firstLetters = JianPinUtil.getSimpleCharsOfStringByTrim(name);
    }

    @Override
    public int compareTo(TestSchool another) {
        return pinyin.compareTo(another.pinyin);
    }
}
