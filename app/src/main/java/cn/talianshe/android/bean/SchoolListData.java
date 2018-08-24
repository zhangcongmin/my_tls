package cn.talianshe.android.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import cn.talianshe.android.utils.PinYinUtil;

/**
 * @author zcm
 * @ClassName: SchoolListData
 * @Description: 学校列表数据
 * @date 2017/12/11 15:45
 */
public class SchoolListData extends BaseListBean<SchoolListData.SchoolListInfo> {
    /**
     * @author zcm
     * @ClassName: DepartMentMajorClassListInfo
     * @Description: 学校列表信息
     * @date 2017/12/11 15:47
     */
    public class SchoolListInfo extends BaseListData<School> {
    }

    public class School implements Comparable<School> {

        /**
         * autoid : 1
         * code : fzdx
         * name : 福州大学
         * nameen : Fuzhou university
         * level : 1
         * address : 金榕北路17号2号楼4层中锐
         * province : 山西省
         * provinceId : 140000
         * city : 太原市
         * cityId : 140100
         * district : 小店区
         * districtId : 140105
         * compellation : 校长233
         * phonenumber : 0591-82131235
         * facsimile : 0591-82131235
         * postcode : 350002
         * fullspell:全拼大写
         * seq : 1
         * status : 1
         * operationuserid : 1
         * operationtime : 1512709328000
         * createtime : 1512974557000
         * memoryCode : 首字母拼音小写
         */

        @SerializedName("autoid")
        public String schoolId;
        public String code;
        public String name;
        public String nameen;
        public String type;
        public String address;
        public String province;
        public String provinceId;
        public String city;
        public String cityId;
        public String district;
        public String districtId;
        public String compellation;
        public String phonenumber;
        public String facsimile;
        public String postcode;
        public int seq;
        public int status;
        public int operationuserid;
        public long operationtime;
        public long createtime;
        @SerializedName("memoryCode")
        public String firstLetters;
        @SerializedName("fullspell")
        public String pinyin;
        public List<String> wordPinyinList;
        public boolean isFromHome;

        public void initPinyinList() {
            wordPinyinList = new ArrayList<>();
            if(TextUtils.isEmpty(pinyin)){
                pinyin = PinYinUtil.getPinyin(name,wordPinyinList);
            }else{

            PinYinUtil.getPinyin(name, wordPinyinList, pinyin);
            }
        }

        @Override
        public int compareTo(School another) {
            return pinyin.compareTo(another.pinyin);
        }
    }
}
