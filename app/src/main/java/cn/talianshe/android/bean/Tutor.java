package cn.talianshe.android.bean;

import java.util.ArrayList;
import java.util.List;

import cn.talianshe.android.utils.JianPinUtil;
import cn.talianshe.android.utils.PinYinUtil;

/**
 * @author zcm
 * @ClassName: Tutor
 * @Description: 导师实体
 * @date 2017/11/23 19:30
 */
public class Tutor implements Comparable<Tutor>{
    /**
     * 导师id
     */
    public String id;
    public String name;
    public String pinyin;
    public String firstLetters;
    public List<String> wordPinyinList = new ArrayList<>();
    public boolean isSelected;

    public Tutor(String name) {
        this.name = name;
        this.pinyin = PinYinUtil.getPinyin(name,wordPinyinList);
        this.firstLetters = JianPinUtil.getSimpleCharsOfStringByTrim(name);
    }

    @Override
    public int compareTo(Tutor another) {
        return pinyin.compareTo(another.pinyin);
    }
}
