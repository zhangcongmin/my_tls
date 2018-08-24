package cn.talianshe.android.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.talianshe.android.bean.ActivityDetailData;
import cn.talianshe.android.bean.ActivityInteractionData;
import cn.talianshe.android.eventbus.ActivityInteractionEvent;
import cn.talianshe.android.net.GlobalParams;


public class ActivityInteractionService extends Service {

    private static final String EXTRA_ACTIVITY_INFO = "extra_activity_info";
    private static final String EXTRA_ACTIVITY_INTERACTION = "extra_activity_interaction";
    private ActivityInteractionData.ActivityInteractionInfo interactionInfo;
    private ActivityDetailData.ActivityDetailInfo activityInfo;

    public static Intent getActivityInteractionService(Context context, ActivityInteractionData.ActivityInteractionInfo interactionInfo, ActivityDetailData.ActivityDetailInfo activityInfo) {
        Intent intent = new Intent(context, ActivityInteractionService.class);
        intent.putExtra(EXTRA_ACTIVITY_INTERACTION, Parcels.wrap(interactionInfo));
        intent.putExtra(EXTRA_ACTIVITY_INFO, Parcels.wrap(activityInfo));
        return intent;
    }


    private boolean isStartSignNotice = false;
    private boolean isSignTimeNotice = false;
    private boolean isRegistStartTimeNotice = false;
    private boolean isRegistEndTimeNotice = false;
    private boolean isStartTimeNotice = false;
    private boolean isVoteNotice = false;
    private boolean isVoteResultNotice = false;
    private List<Boolean> voteNoticeList;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        activityInfo = Parcels.unwrap(intent.getParcelableExtra(EXTRA_ACTIVITY_INFO));
        interactionInfo = Parcels.unwrap(intent.getParcelableExtra(EXTRA_ACTIVITY_INTERACTION));
        isSignTimeNotice = false;
        isRegistStartTimeNotice = false;
        isRegistEndTimeNotice = false;
        isStartTimeNotice = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
            timerTask = null;
        }
        if (interactionInfo.sceneInteractivity.voteList != null && interactionInfo.sceneInteractivity.voteList.size() > 0) {
            voteNoticeList = new ArrayList<>();
            for (int i = 0; i < interactionInfo.sceneInteractivity.voteList.size(); i++) {
                voteNoticeList.add(false);
            }
        }
        timer = new Timer();
        timerTask = new MyTimerTask();
        //每秒钟执行一次
        timer.schedule(timerTask, 0, 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    private Timer timer;
    private TimerTask timerTask;

    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            long currentTime = GlobalParams.getCurrentTimeStamp();
            if (currentTime < activityInfo.endtime) {
                if (currentTime > (activityInfo.starttime - activityInfo.signtime * 60 * 1000)) {
                    //签到时间
                    //说明当前都是签到时间
                    if (!isSignTimeNotice) {
                        isSignTimeNotice = true;
                        ActivityInteractionEvent event = new ActivityInteractionEvent();
                        event.isSignTimeNotice = isSignTimeNotice;
                        event.curInteractionType = ActivityInteractionEvent.InteractionType.SIGN_TIME;
                        EventBus.getDefault().post(event);
                    }
                    if (currentTime > activityInfo.starttime) {
                        if (!isStartTimeNotice) {
                            isStartTimeNotice = true;
                            ActivityInteractionEvent event = new ActivityInteractionEvent();
                            event.isSignTimeNotice = isSignTimeNotice;
                            event.curInteractionType = ActivityInteractionEvent.InteractionType.START_TIME;
                            EventBus.getDefault().post(event);
                        }
                        //说明是互动进行时，判断投票配置
                        if (interactionInfo != null && interactionInfo.sceneInteractivity != null) {
                            //互动配置不为空，如果有投票
                            if (interactionInfo.sceneInteractivity.voteList != null && interactionInfo.sceneInteractivity.voteList.size() > 0) {
                                for (int i = 0; i < interactionInfo.sceneInteractivity.voteList.size(); i++) {

                                    ActivityInteractionData.VoteListBean voteListBean = interactionInfo.sceneInteractivity.voteList.get(i);
                                    if (voteListBean.beginDate > currentTime && voteListBean.endDate < currentTime && !voteNoticeList.get(i)) {
                                        //说明当前是投票时间,并且没有投过票
                                        voteNoticeList.remove(i);
                                        voteNoticeList.add(i, true);
                                        ActivityInteractionEvent event = new ActivityInteractionEvent();
                                        event.voteNoticeList = voteNoticeList;
                                        event.voteNoteIndex = i;
                                        event.curInteractionType = ActivityInteractionEvent.InteractionType.VOTE;
                                        EventBus.getDefault().post(event);
                                    }
                                }
                            }
                        }
                    }
                } else {

                    if (currentTime > activityInfo.registStarttime) {
                        if (!isRegistStartTimeNotice) {
                            isRegistStartTimeNotice = true;
                            ActivityInteractionEvent event = new ActivityInteractionEvent();
                            event.curInteractionType = ActivityInteractionEvent.InteractionType.REGIST_START_TIME;
                            EventBus.getDefault().post(event);
                        }
                        if (currentTime > activityInfo.registEndtime) {
                            if (!isRegistEndTimeNotice) {
                                isRegistEndTimeNotice = true;
                                ActivityInteractionEvent event = new ActivityInteractionEvent();
                                event.curInteractionType = ActivityInteractionEvent.InteractionType.REGIST_END_TIME;
                                EventBus.getDefault().post(event);
                            }
                        }
                    }

                }

            }

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
