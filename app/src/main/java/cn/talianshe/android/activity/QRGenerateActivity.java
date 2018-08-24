package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.QRCodeType;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.utils.BGAQRCodeUtil;
import cn.talianshe.android.utils.Base64;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.utils.QRCodeEncoder;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.ScaleImageView;

/**
 * @author zcm
 * @ClassName: QRGenerateActivity
 * @Description: 生成二维码界面
 * @date 2017/12/5 17:39
 */
public class QRGenerateActivity extends BaseActivity {

    @BindView(R.id.iv_head)
    ScaleImageView ivHead;
    @BindView(R.id.iv_gender)
    ImageView ivGender;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_nick_name)
    TextView tvNickName;
    @BindView(R.id.iv_code)
    ImageView ivCode;

    private static final String QR_CODE_TYPE = "qr_code_type";
    private static final String EXTRA_GENERATE_ENTITY = "extra_generate_id";

    //二维码生成格式：base64加密的时间戳+","+"scanType=?,"+ userId=?或associationId=?或activityId=?
    public static Intent getQRGenerateIntent(Context context, QRCodeType qrCodeType, GenerateEntity generateEntity) {
        Intent intent = new Intent(context, QRGenerateActivity.class);
        intent.putExtra(QR_CODE_TYPE, qrCodeType);
        intent.putExtra(EXTRA_GENERATE_ENTITY, Parcels.wrap(generateEntity));
        return intent;
    }

    @Parcel
    public static class GenerateEntity {
        public String generateId;
        public String generateSecondStr;
        public String generateAvatar;
        public boolean isMale;
        public String name;
        public String nickName;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generate);
        ButterKnife.bind(this);
        initData();
    }

    private GenerateEntity generateEntity;
    private QRCodeType qrCodeType;

    private void initData() {
        qrCodeType = (QRCodeType) getIntent().getSerializableExtra(QR_CODE_TYPE);
        generateEntity = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_GENERATE_ENTITY));
        switch (qrCodeType) {
            case PERSONAL:
            case ACTIVITY_SIGN:
                setTitle(R.string.mine_qr_code);
                ivGender.setVisibility(TextUtils.isEmpty(GlobalParams.USER_INFO.sex) ? View.GONE : View.VISIBLE);
                ivGender.setImageResource("0".equals(GlobalParams.USER_INFO.sex) ? R.mipmap.female : R.mipmap.male);
                if (TextUtils.isEmpty(generateEntity.nickName)) {
                    tvNickName.setVisibility(View.GONE);
                } else {
                    tvNickName.setVisibility(View.VISIBLE);
                    tvNickName.setText(getString(R.string.nickname_placeholder, generateEntity.nickName));
                }
                break;
            case ASSOCIATION:
                setTitle(R.string.association_qr_code);
                ivGender.setVisibility(View.GONE);
                tvNickName.setVisibility(View.GONE);
                break;
            case ACTIVITY:
                setTitle(R.string.activity_qr_code);
                ivGender.setVisibility(View.GONE);
                tvNickName.setVisibility(View.GONE);
                break;
        }
        generateCode();
    }

    private Handler handler = new Handler();

    private void generateCode() {
        MyProgressDialog.show(this);
        AsyncTask<Void, Void, Bitmap> generateCodeTask = new MyAsyncTask();
        MyProgressDialog.show(this);
        generateCodeTask.execute();
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                String qrCode = Base64.getBase64(GlobalParams.getCurrentTimeStamp() + "") + "," + qrCodeType.getQrCodeType() + "," + qrCodeType.getQrCodePrefix() + generateEntity.generateId;
                if (qrCodeType == QRCodeType.ACTIVITY_SIGN) {
                    qrCode = qrCode + ",activityId=" + generateEntity.generateSecondStr;
                } else if (qrCodeType == QRCodeType.PERSONAL) {
                    qrCode = qrCode + ",identityType=" + generateEntity.generateSecondStr;
                }
                System.out.println("qrCode..." + qrCode);
                String avartarWithPrefix = generateEntity.generateAvatar.replace(TLSUrl.BASE_URL, "");
                if (TextUtils.isEmpty(avartarWithPrefix) || "null".equals(avartarWithPrefix)) {
                    Bitmap headBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.default_head);
                    return QRCodeEncoder.syncEncodeQRCode(qrCode, BGAQRCodeUtil.dp2px(QRGenerateActivity.this, 150), Color.BLACK, Color.WHITE, headBitmap);
                } else {
                    RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
                    options.override(DensityUtils.dipTopx(QRGenerateActivity.this, 48));
                    FutureTarget<Bitmap> result = Glide.with(QRGenerateActivity.this).asBitmap().load(generateEntity.generateAvatar).apply(options).submit();
                    Bitmap headBitmap = result.get();
                    return QRCodeEncoder.syncEncodeQRCode(qrCode, BGAQRCodeUtil.dp2px(QRGenerateActivity.this, 150), Color.BLACK, Color.WHITE, headBitmap);

                }
//                return QRCodeEncoder.syncEncodeQRCode(qrCode, BGAQRCodeUtil.dp2px(QRGenerateActivity.this, 150), Color.BLACK);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            MyProgressDialog.dismiss();
            RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop(QRGenerateActivity.this));
            options.placeholder(R.mipmap.default_head);
            options.error(R.mipmap.default_head);
            Glide.with(QRGenerateActivity.this)
                    .load(generateEntity.generateAvatar)
                    .apply(options)
                    .into(ivHead);
            tvName.setText(generateEntity.name);
            if (bitmap != null) {
                ivCode.setImageBitmap(bitmap);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AsyncTask<Void, Void, Bitmap> generateCodeTask = new MyAsyncTask();
                        generateCodeTask.execute();
                    }
                }, 29000);
            } else {
                MyToast.show("二维码生成失败", QRGenerateActivity.this);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}

