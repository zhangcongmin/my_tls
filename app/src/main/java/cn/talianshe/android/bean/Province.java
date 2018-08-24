package cn.talianshe.android.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author zcm
 * @ClassName: Province
 * @Description: 省市
 * @date 2017/12/7 22:47
 */
public class Province {
    @SerializedName("name")
    public String provinceName;
    @SerializedName("sub")
    public List<City> cities;

    @SerializedName("level") //0：直辖市 1：省
    public int provinceType;

    public class City{
        @SerializedName("name") //0：直辖市 1：省
        public String cityName;
        @SerializedName("sub") //0：直辖市 1：省
        public List<Area> areas;

        @Override
        public String toString() {
            return "City{" +
                    "cityName='" + cityName + '\'' +
                    '}';
        }
    }

    public class Area{
        @SerializedName("name") //0：直辖市 1：省
        public String areaName;
    }
    @Override
    public String toString() {
        return "Province{" +
                "provinceName='" + provinceName + '\'' +
                ", cities=" + cities +
                ", provinceType=" + provinceType +
                '}';
    }
}
