package cn.talianshe.android.bean;


public class PrivacySettingData extends BaseBean<PrivacySettingData.PrivacySettingInfo> {
    /**
     * @author zcm
     * @ClassName: PrivacySettingInfo
     * @Description: 隐私设置信息
     * @date 2017/12/11 20:07
     */
    public class PrivacySettingInfo {
        public String isName; //是否公开姓名：:1：公开、0：不公开
        public String isNickname;//是否公开昵称：:1：公开、0：不公开
        public String isPhone;//是否公开电话：:1：公开、0：不公开

        public boolean isNamePublic() {
            return "1".equals(isName);
        }

        public boolean isNicknamePublic() {
            return "1".equals(isNickname);
        }

        public boolean isMobilePublic() {
            return "1".equals(isPhone);
        }
    }
}
