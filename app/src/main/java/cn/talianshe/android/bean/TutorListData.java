package cn.talianshe.android.bean;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cn.talianshe.android.utils.JianPinUtil;
import cn.talianshe.android.utils.PinYinUtil;

/**
 * @author zcm
 * @ClassName: AssociationListData
 * @Description: 社团列表数据
 * @date 2017/12/13 13:25
 */
public class TutorListData extends BaseListBean<TutorListData.TutorInfoList> {
    public class TutorInfoList extends BaseListData<Tutor>{}
    public class Tutor implements Comparable<Tutor>{
        public int id;
        public String realname;
        public String nickname;
        public String schoolName;
        public String departmentName;
        public String titleName;
        public String avatar;

        public String name;
        public String pinyin;
        public String firstLetters;
        public List<String> wordPinyinList;
        public boolean isSelected;
        public void initPinyin() {
            this.name = realname;
            wordPinyinList = new ArrayList<>();
            this.pinyin = PinYinUtil.getPinyin(name,wordPinyinList);
            this.firstLetters = JianPinUtil.getSimpleCharsOfStringByTrim(name);
        }
        @Override
        public int compareTo(@NonNull Tutor another) {
            return pinyin.compareTo(another.pinyin);
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Tutor))
                return false;
            Tutor other = (Tutor) obj;
            return this.id== other.id;
        }
    }
}
