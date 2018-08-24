package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.widget.clipimage.ClipImageLayout;

/**
 * @author zcm
 * @ClassName: ClipImageActivity
 * @Description: 裁剪图片
 * @date 2017/11/22 19:13
 */
public class ClipImageActivity extends BaseActivity implements View.OnClickListener {


    public static final String KEY_CLIP_IMAGE = "key_clip_image";
    @BindView(R.id.clip_image_layout)
    ClipImageLayout mClipImageLayout;

    private static String EXTRA_IMAGE_URL = "extra_image_url";
    private String extraImageUrl;

    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, ClipImageActivity.class);
        intent.putExtra(EXTRA_IMAGE_URL, url);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_image);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
//        setTitle(R.string.create_association);
        tvTitle.setVisibility(View.GONE);
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText(R.string.confirm);
        mClipImageLayout.setHorizontalPadding(DensityUtils.dipTopx(this, 20));
        extraImageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        Glide.with(this)
                .load(extraImageUrl)
                .into(mClipImageLayout.getImageView());
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进行裁剪
                onClip();
//                if (mClipTv.getText().equals("裁剪")) {
//                    onClip();
//                    mClipTv.setText("返回");
//                    mShowImgIv.setVisibility(View.VISIBLE);
//                    mClipImageLayout.setVisibility(View.GONE);
//                } else {
//                    mClipTv.setText("裁剪");
//                    mShowImgIv.setVisibility(View.GONE);
//                    mClipImageLayout.setVisibility(View.VISIBLE);
//                }
            }
        });

//        String headUrl = "http://com-dkhs-media-test.oss.aliyuncs.com/medias/2017/01/09/15/2014/temp_upload902408188.720x19999.jpg";
//        Glide.with(this)
//                .load(headUrl)
//                .apply(RequestOptions.bitmapTransform(new BlurTransformation(14)))
//                .into(ivDimHead);
//        RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop(this));
//        options.override(getResources().getDrawable(R.mipmap.personal_head_default).getIntrinsicWidth());
//        Glide.with(this)
//                .load(headUrl)
//                .apply(options)
//                .into(ivHead);
    }

    private String imagePath;

    private void onClip() {
        try {
            Bitmap bitmap = mClipImageLayout.clip();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(createImageFile()));
            Intent intent = new Intent();
            // 最后通知图库更新
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(new File(imagePath))));
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_PICTURES))));
            intent.putExtra(KEY_CLIP_IMAGE, imagePath);
            setResult(RESULT_OK,intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //create photo file to take photo
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), GlobalParams.TEMP_PIC_DIR);

        if (!storageDir.exists())
            storageDir.mkdir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imagePath = image.getAbsolutePath();
        return image;
    }

}
