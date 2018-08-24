package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.MessageDetailData;
import cn.talianshe.android.bean.MessageListData;
import cn.talianshe.android.bean.NoticeBean;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: TlsWebViewActivity
 * @Description: 网页专用页面
 * @date 2017/12/25 3:01
 */
public class TlsWebViewActivity extends BaseActivity {

    private static final String EXTRA_NOTICE = "extra_notice";
    private static final String EXTRA_VIEW_TYPE = "extra_view_type";
    private static final int TYPE_NOTICE = 1;//公告
    private static final int TYPE_MESSAGE = 2;//消息
    @BindView(R.id.web_view)
    WebView webView;
    private int viewType;
    private NoticeBean noticeBean;

    public static Intent getNoticeIntent(Context context, NoticeBean noticeBean) {
        Intent intent = new Intent(context, TlsWebViewActivity.class);
        intent.putExtra(EXTRA_NOTICE, Parcels.wrap(noticeBean));
        intent.putExtra(EXTRA_VIEW_TYPE, TYPE_NOTICE);
        return intent;
    }
    public static Intent getMessageIntent(Context context, NoticeBean noticeBean) {
        Intent intent = new Intent(context, TlsWebViewActivity.class);
        intent.putExtra(EXTRA_NOTICE, Parcels.wrap(noticeBean));
        intent.putExtra(EXTRA_VIEW_TYPE, TYPE_MESSAGE);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tls_web_view);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setWebChromeClient(new WebChromeClient());

        //7.在同种分辨率的情况下,屏幕密度不一样的情况下,自动适配页面:
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int scale = dm.densityDpi;
        if (scale == 240) { //
            webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (scale == 160) {
            webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        } else {
            webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
        }

        setTitle("");
        viewType = getIntent().getIntExtra(EXTRA_VIEW_TYPE, TYPE_MESSAGE);
            noticeBean = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_NOTICE));
            setTitle(noticeBean.title);
        if (viewType == TYPE_NOTICE) {
            if (TextUtils.isEmpty(noticeBean.content)) {
                getMessageDetail(noticeBean.id);
//                MyToast.show(R.string.notice_contnet_null_tip, this);
            } else {
                webView.loadDataWithBaseURL(null, noticeBean.content, "text/html", "UTF-8", null);
                EventBus.getDefault().post(noticeBean);
            }
        }else{
            webView.loadUrl(noticeBean.url);
        }
    }

    private void getMessageDetail(String id) {
        MyProgressDialog.show(this);
        HttpSubscriber messageListSubscrober = new HttpSubscriber<MessageDetailData>(this) {
            @Override
            public void onSuccess(MessageDetailData detailData) {
                MyProgressDialog.dismiss();
                MessageDetailData.MessageDetailInfo info = detailData.result;
                setTitle(info.title);
                if (TextUtils.isEmpty(info.content)) {
                    MyToast.show(R.string.notice_contnet_null_tip, TlsWebViewActivity.this);
                } else {
                    webView.loadDataWithBaseURL(null, info.content, "text/html", "UTF-8", null);
                    EventBus.getDefault().post(noticeBean);
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).getMessageDetail(id).compose(RxSchedulersHelper.io_main()).subscribe(messageListSubscrober);
    }
}
