package cn.talianshe.android.bean;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressDetailData extends BaseBean<AddressDetailData.AddressDetailInfo> {

    public class AddressDetailInfo {

        /**
         * id : 1
         * adress : 爱对方答复
         * lasttime : 1511344454000
         * name : 体育馆
         * scene : 室内
         * equipment : [{"equipmentQuantity":2,"equipmentName ":"麦克风"}]
         * fieldImg : []
         * firsttime : 1510480418000
         * facilities : []
         * galleryful : 50
         */

        public String id;
        @SerializedName("adress")
        public String address;
        public long lasttime;
        public String name;
        public String scene; //室内或室外
        @SerializedName("fristtime")
        public long firsttime;
        @SerializedName("galleryful")
        public String fullNum;
        @SerializedName("equipment")
        public List<EquipmentInfo> equipments;
        @SerializedName("fieldImg")
        public List<AddressImg> addressImgs;
        public List<Facility> facilities;

    }

    public class Facility {
        public int facilitiesCode;
        public String amount;
        @SerializedName("wide")
        public String width;
        @SerializedName("high")
        public String height;
        @SerializedName("long")
        public String length;
        @SerializedName("facilitiesName")
        public String facilityName;
    }

    public class AddressImg {
        @SerializedName("fieldImgid")
        public String imgId;
        @SerializedName("fieldImgPath")
        public String imgPath;
    }

    public class EquipmentInfo {
        /**
         * equipmentQuantity : 2
         * equipmentName  : 麦克风
         */

        public int equipmentQuantity;
        public String equipmentName;
    }

}
