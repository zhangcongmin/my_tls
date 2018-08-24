package cn.talianshe.android.bean;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.talianshe.android.utils.JianPinUtil;
import cn.talianshe.android.utils.PinYinUtil;

public class AssociationMemberListData extends BaseListBean<AssociationMemberListData.AssociationMemberListInfo> {
    public class AssociationMemberListInfo extends BaseListData<AssociationMember> {}

    public class AssociationMember implements Comparable<AssociationMember> {
        public String id;//成员id，学生表里的id
        public String realname;
        public String nickname;
        public String dutyName; //成员职务
        public String isname;//是否公开姓名，公开：1、不公开：0
        public String isnickname;//是否公开姓名，公开：1、不公开：0
        public String sex;//性别：1男、0女
        public String avatar;
        public String schoolName;
        public String departmentName;
        public String associationSum;//社团人数

        public String searchName;
        public List<String> wordPinyinList;
        public String pinyin;
        public String firstLetters;
        public boolean isSelected;
        public void initPinyin() {
            this.searchName = "1".equals(isnickname) ? ("1".equals(isname) ? nickname + "(" + realname + ")" : nickname) : realname;
            wordPinyinList = new ArrayList<>();
            this.pinyin = PinYinUtil.getPinyin(this.searchName, wordPinyinList);
            this.firstLetters = JianPinUtil.getSimpleCharsOfStringByTrim(this.searchName);

        }

        @Override
        public int compareTo(@NonNull AssociationMember another) {
            return pinyin.compareTo(another.pinyin);
        }

        @Override
        public String toString() {
            return "AssociationAlbum{" +
                    "realname='" + realname + '\'' +
                    ", nickname='" + nickname + '\'' +
                    '}';
        }
    }
}
