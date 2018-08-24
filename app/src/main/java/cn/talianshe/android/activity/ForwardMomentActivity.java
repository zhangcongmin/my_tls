package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.BaseBean;
import cn.talianshe.android.bean.MomentListData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: CostSourceActivity
 * @Description: 费用来源
 * @date 2017/11/24 15:52
 */
public class ForwardMomentActivity extends BaseActivity {

    private static final String EXTRA_PARCEL = "extra_parcel";
    @BindView(R.id.et_content)
    EditText etContent;
    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_content)
    TextView tvContent;

    public static Intent getForwardIntent(Context context, MomentListData.MomentInfo info){
        Intent intent = new Intent(context,ForwardMomentActivity.class);
        intent.putExtra(EXTRA_PARCEL, Parcels.wrap(info));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward_moment);
        ButterKnife.bind(this);
        initData();
    }
    private String momentId;
    private void initData() {
        setTitle(R.string.forward_moment);
        MomentListData.MomentInfo info = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PARCEL));
        tvName.setText(info.publisher);
        tvContent.setText(info.content);
        if (!TextUtils.isEmpty(info.avatar)) {
            RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
            options.override(this.getDrawable(R.mipmap.default_head).getIntrinsicWidth());
            options.placeholder(R.mipmap.default_head);
            options.error(R.mipmap.default_head);
            Glide.with(this).load(TLSUrl.BASE_URL + info.avatar).apply(options).into(ivHead);
        }
        momentId = info.id;
    }

    @OnClick(R.id.tv_commit)
    public void onViewClicked() {
        String forwardContent = etContent.getText().toString();

        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData strData) {
                MyProgressDialog.dismiss();
                MyToast.show(R.string.forward_success,ForwardMomentActivity.this);
                finish();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).forwardMoment(momentId,forwardContent).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }
}
