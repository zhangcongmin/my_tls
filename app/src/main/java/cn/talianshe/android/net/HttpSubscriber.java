package cn.talianshe.android.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import cn.talianshe.android.bean.BaseBean;
import cn.talianshe.android.eventbus.StopSwipeLayoutRefreshEvent;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.MyToast;
import retrofit2.HttpException;
import rx.Subscriber;

public abstract class HttpSubscriber<T extends BaseBean> extends Subscriber<T> {

    protected Context context;

    public HttpSubscriber(Context context) {
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isNetWorking(context)) {
            onError("网络不可用");
            onFinish();
            if (!isUnsubscribed()) {
                unsubscribe();
            }
        }
    }

    @Override
    public void onCompleted() {
        onFinish();
        if (!isUnsubscribed()) {
            unsubscribe();
        }
    }

    /**
     * onCompleted和onError是互斥的，队列中调用了其中一个，就不应该再调用另一个。也是事件序列中的最后一个
     */

    @Override
    public void onError(Throwable e) {
        if (!isNetWorking(context)) {
            onError("网络不可用");
        } else if (e instanceof SocketTimeoutException) {
            onError("服务器响应超时");
        } else if (e instanceof ConnectException) {
            onError("服务器请求超时");
        } else if (e instanceof HttpException) {
            onError("服务器异常");
        } else {
            e.printStackTrace();
            onError("未知异常：" + e.getMessage());
//            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void onNext(T t) {
        if (t.isSuccess()) {
            t.castDataToObject();
            onSuccess(t);
        } else {
            if (t.code == -1) {
                //账号已失效
                GlobalParams.TOKEN = null;
                TipDialogUtil.showAccountUnavailableLoginDialog(context);
            } else {

                if (t.data != null) {
                    onError(t.data.toString());
                } else {
                    onError(t.msg);
                }
            }
        }
    }

    public abstract void onSuccess(T t);

    public void onError(String msg) {
        sendStopSLRefreshEvent();
        MyToast.show(msg, context);
    }

    public void onFinish() {
    }

    /**
     * 网络监测
     *
     * @param context
     * @return
     */
    public static boolean isNetWorking(Context context) {
        boolean flag = checkNet(context);
        if (!flag) {

            Toast.makeText(context, "当前设备网络异常，请检查后再重试！", Toast.LENGTH_SHORT).show();
        }
        return flag;
    }

    private static boolean checkNet(Context context) {

        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    /**
     * 发送停止swipelayout刷新事件
     */
    private void sendStopSLRefreshEvent() {
        EventBus.getDefault().post(new StopSwipeLayoutRefreshEvent());
    }
}
