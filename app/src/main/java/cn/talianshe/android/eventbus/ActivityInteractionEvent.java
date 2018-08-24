package cn.talianshe.android.eventbus;

import java.util.List;


public class ActivityInteractionEvent {
    public InteractionType curInteractionType;
    public boolean isStartSignNotice;
    public boolean stopSign;
    public int countDownNum;
    public List<Boolean> voteNoticeList;
    public int voteNoteIndex;
    public boolean isSignTimeNotice;

    public enum InteractionType{
        REGIST_START_TIME,REGIST_END_TIME,START_SIGN,SIGN_TIME,START_TIME,VOTE,;
    }
}

