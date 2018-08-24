package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.MessageDetailData;
import cn.talianshe.android.bean.NoticeBean;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: MessageCenterDetailActivity
 * @Description: 消息列表详情
 * @date 2017/12/31 20:33
 */
public class MessageCenterDetailActivity extends BaseActivity {

    private static final String EXTRA_NOTICE = "extra_notice";
    @BindView(R.id.tv_message_title)
    TextView tvMessageTitle;
    @BindView(R.id.iv_img)
    ImageView ivImg;
    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.tv_url)
    TextView tvUrl;
    @BindView(R.id.tv_time)
    TextView tvTime;

    private NoticeBean noticeBean;
    private String url;

    public static Intent getNoticeIntent(Context context, NoticeBean noticeBean) {
        Intent intent = new Intent(context, MessageCenterDetailActivity.class);
        intent.putExtra(EXTRA_NOTICE, Parcels.wrap(noticeBean));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center_detail);
        ButterKnife.bind(this);
        setTitle(R.string.message);
        initData();
    }

    private void initData() {
        noticeBean = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_NOTICE));
        getMessageDetail(noticeBean.id);
        tvUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noticeBean.url = url;
                startActivity(TlsWebViewActivity.getMessageIntent(MessageCenterDetailActivity.this, noticeBean));
            }
        });
    }

    private void getMessageDetail(String id) {
        MyProgressDialog.show(this);
        HttpSubscriber messageListSubscrober = new HttpSubscriber<MessageDetailData>(this) {
            @Override
            public void onSuccess(MessageDetailData detailData) {
                EventBus.getDefault().post(noticeBean);
                MyProgressDialog.dismiss();
                MessageDetailData.MessageDetailInfo info = detailData.result;
                tvMessageTitle.setVisibility(TextUtils.isEmpty(info.title) ? View.GONE : View.VISIBLE);
                tvMessageTitle.setText(info.title);
                if (info.imgList != null && info.imgList.size() > 0 && !TextUtils.isEmpty(info.imgList.get(0).imgPath)) {
                    RequestOptions options = new RequestOptions().error(R.mipmap.ic_img_thumbnail).placeholder(R.mipmap.ic_img_failure);
                    Glide.with(MessageCenterDetailActivity.this).load(TLSUrl.BASE_URL + info.imgList.get(0).imgPath).apply(options).thumbnail(0.1f).into(ivImg);
                    ivImg.setVisibility(View.VISIBLE);
                } else {
                    ivImg.setVisibility(View.GONE);
                }
                tvContent.setVisibility(TextUtils.isEmpty(info.content) ? View.GONE : View.VISIBLE);
                tvContent.setText(info.content);

                tvUrl.setVisibility(TextUtils.isEmpty(info.url) ? View.GONE : View.VISIBLE);
//                tvUrl.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
//                tvUrl.getPaint().setAntiAlias(true);
//                tvUrl.setText(Html.fromHtml("<u>"+url+"</u>"));
                if (!TextUtils.isEmpty(info.url)) {
                    url = info.url;
                    SpannableString content = new SpannableString(url);
                    content.setSpan(new MyUnderlineSpan(), 0, url.length(), 0);
                    tvUrl.setText(content);
                }

                tvTime.setVisibility(info.createTime == 0 ? View.GONE : View.VISIBLE);
                tvTime.setText(TimeUtil.getDateHourMinuteSecondTime(info.createTime));
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).getMessageDetail(id).compose(RxSchedulersHelper.io_main()).subscribe(messageListSubscrober);
    }

    private class MyUnderlineSpan extends ClickableSpan {

        @Override
        public void onClick(View widget) {
            noticeBean.url = url;
            startActivity(TlsWebViewActivity.getMessageIntent(MessageCenterDetailActivity.this, noticeBean));
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
//            ds.setUnderlineText(true);
        }
    }
}
