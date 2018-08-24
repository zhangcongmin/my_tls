package cn.talianshe.android.bean;


import java.util.HashMap;
import java.util.Map;

public enum QRCodeType {
//    PERSONAL("userId="), ASSOCIATION("associationId="), ACTIVITY("activityId="),ACTIVITY_SIGN("");
    PERSONAL("scanTyp=personal"), ASSOCIATION("scanTyp=association"), ACTIVITY("scanTyp=activity"),ACTIVITY_SIGN("scanTyp=activity_sign");
    private String qrCodeType;

    QRCodeType(String qrCodeType) {
        this.qrCodeType = qrCodeType;
    }
    private static final Map<String, QRCodeType> stringToEnum = new HashMap <>();
    private static final Map<QRCodeType, String> codePrefixMap = new HashMap <>();
    private static final Map<QRCodeType, String> codeSecondPrefixMap = new HashMap <>();

    static {
        codePrefixMap.put(PERSONAL,"userId=");
        codePrefixMap.put(ACTIVITY_SIGN,"userId=");
        codePrefixMap.put(ACTIVITY,"activityId=");
        codePrefixMap.put(ASSOCIATION,"associationId=");
        codeSecondPrefixMap.put(ACTIVITY_SIGN,"activityId=");
        codeSecondPrefixMap.put(PERSONAL,"identityType=");
        // Initialize map from constant name to enum constant
        for(QRCodeType type : values()) {
            stringToEnum.put(type.toString(), type);
        }
    }
    public static QRCodeType getEnumFromString(String string) {
        if (string != null) {
            try {
                return stringToEnum.get(string.trim());
            } catch (IllegalArgumentException ex) {
            }
        }
        return null;
    }

    public String getQrCodePrefix() {
        return codePrefixMap.get(this);
    }
    public String getSecondQrCodePrefix() {
        return codeSecondPrefixMap.get(this);
    }
    public String getQrCodeType() {
        return qrCodeType;
    }
    @Override
    public String toString() {
        return qrCodeType;
    }
}
