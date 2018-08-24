package cn.talianshe.android.eventbus;

import org.parceler.Parcel;

@Parcel
public class CostSouceEvent {
    public int costSource; //1自筹 2学校拨款 3商家赞助 4混合
    public double costSelf;
    public double costSchool;
    public double costBusiness;


    public String getCostSourceName() {
        String costSourceName = null;
        if (costSource == 1) {
            costSourceName = "自筹";
        } else if (costSource == 2) {
            costSourceName = "学校拨款";
        } else if (costSource == 3) {
            costSourceName = "商家赞助";
        } else {
            costSourceName = "混合方式";
        }

        return costSourceName;
    }
}
