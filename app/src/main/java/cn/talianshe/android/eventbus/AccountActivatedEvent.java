package cn.talianshe.android.eventbus;

public class AccountActivatedEvent {
    public String mobile;
    public String isActivated;

    public AccountActivatedEvent(String mobile, String isActivated) {
        this.mobile = mobile;
        this.isActivated = isActivated;
    }
}
