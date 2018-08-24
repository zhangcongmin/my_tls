package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.ActivityPhotoListData;
import cn.talianshe.android.bean.PhotoDetailData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.utils.NameUtil;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.MyForwardDialog;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import library.talianshe.android.photobrowser.FileUtils;
import library.talianshe.android.photobrowser.HackyViewPager;
import library.talianshe.android.photobrowser.PhotoViewFragment;
import library.talianshe.android.photobrowser.PhotoViewPagerAdapter;
import library.talianshe.android.photobrowser.bean.PhotoBean;

/**
 * @author zcm
 * @ClassName: PhotoViewActivity
 * @Description: 图片查看器
 * @date 2017/11/10 16:27
 */
public class ActivityPhotoViewActivity extends BaseActivity implements View.OnClickListener {

    private static String INDEX = "index";
    private static String IS_UP = "is_up";
    private static String PHOTO_INFO = "photo_bean_list";
    @BindView(R.id.vp)
    HackyViewPager mVp;
    @BindView(R.id.tv_index)
    TextView mIndexTV;
    @BindView(R.id.tv_save)
    TextView mSaveTV;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_up_num)
    TextView tvUpNum;
    private List<PhotoBean> mPhotoBeanList;
    /**
     * 图片索引
     */
    private int index;
    private PhotoDetailData.PhotoDetailInfo photoDetailInfo;

    public static void startPhotoViewActivity(Context context, ActivityPhotoListData.ActivityPhotoInfo photoInfo, View view) {

        Intent intent = new Intent(context, ActivityPhotoViewActivity.class);
        intent.putExtra(PHOTO_INFO, Parcels.wrap(photoInfo));
//        intent.putStringArrayListExtra(PHOTO_INFO,mPhotoBeanList);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeScaleUpAnimation(view,
                        (int) view.getWidth() / 2, (int) view.getHeight() / 2,
                        0, 0);
        ActivityCompat.startActivity(context, intent, options.toBundle());
    }

    private ActivityPhotoListData.ActivityPhotoInfo photoInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view_activity);
        ButterKnife.bind(this);
        index = getIntent().getIntExtra(INDEX, 0);
        photoInfo = Parcels.unwrap(getIntent().getParcelableExtra(PHOTO_INFO));
        mPhotoBeanList = new ArrayList<>();
        PhotoBean photoBean = new PhotoBean();
        photoBean.imgUrl = TLSUrl.BASE_URL + photoInfo.path;
        mPhotoBeanList.add(photoBean);
        setTitle(R.string.wonderful_moment);
        initView();
        getPhotoDetail();
    }

    private void getPhotoDetail() {
        MyProgressDialog.show(this, false);
        HttpSubscriber httpSubscriber = new HttpSubscriber<PhotoDetailData>(this) {
            @Override
            public void onSuccess(PhotoDetailData data) {
                MyProgressDialog.dismiss();
                photoDetailInfo = data.result;
                tvTime.setText(TimeUtil.getDateHourMinuteSecondTime(photoDetailInfo.createtime));
                tvName.setText(NameUtil.getName(photoDetailInfo.realname, photoDetailInfo.nickname, photoDetailInfo.isname, photoDetailInfo.isnickname));
                tvUpNum.setText(photoDetailInfo.like + "");
                photoInfo.like = photoDetailInfo.like;
                tvUpNum.setCompoundDrawablesWithIntrinsicBounds(photoDetailInfo.type == 1 ? R.mipmap.up : R.mipmap.no_up, 0, 0, 0);
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).getPhotoDetail(photoInfo.imgid, null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }


    private void initView() {
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setBackgroundResource(R.mipmap.share);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //分享活动
                MyForwardDialog dialog = new MyForwardDialog(ActivityPhotoViewActivity.this,false).builder();
                dialog.setResultListener(new MyForwardDialog.ResultListener() {
                    @Override
                    public void onResult(MyForwardDialog.ForwardType type) {
                        UMShareListener umShareListener = new UMShareListener() {
                            @Override
                            public void onStart(SHARE_MEDIA share_media) {
                                System.out.println("开始分享");
                            }

                            @Override
                            public void onResult(SHARE_MEDIA share_media) {
                                MyToast.show(R.string.share_success, ActivityPhotoViewActivity.this);
                            }

                            @Override
                            public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                                System.out.println("分享失败");
                                MyToast.show(R.string.share_failed, ActivityPhotoViewActivity.this);
                                throwable.printStackTrace();
                            }

                            @Override
                            public void onCancel(SHARE_MEDIA share_media) {
                                System.out.println("分享取消");
//                                    MyToast.show(R.string.share_cancel,ActivityDetailActivity.this);

                            }
                        };
                        UMWeb web;
                        if (photoDetailInfo.isOnGoing) {
                            web = new UMWeb(TLSUrl.Forward.activityOngoingForwardUrl + photoDetailInfo.activityId);
                        } else {
                            web = new UMWeb(TLSUrl.Forward.activityUnliveForwardUrl +  photoDetailInfo.activityId);
                        }
                        UMImage umImage;
                        if (TextUtils.isEmpty(photoDetailInfo.activityLogo)) {
                            umImage = new UMImage(ActivityPhotoViewActivity.this, R.mipmap.tls_logo);
                        } else {
                            umImage = new UMImage(ActivityPhotoViewActivity.this, TLSUrl.BASE_URL + photoDetailInfo.activityLogo);
                        }
                        umImage.compressFormat = Bitmap.CompressFormat.PNG;
                        web.setThumb(umImage);  //缩略图
                        web.setTitle(getString(R.string.wonderful_moment_share_holder,photoDetailInfo.activityName));//标题
//                        web.setDescription(detailInfo.desc);//描述
                        if (type == MyForwardDialog.ForwardType.TYPE_QZONG) {
                            //QQ空间分享
                            new ShareAction(ActivityPhotoViewActivity.this)
                                    .setPlatform(SHARE_MEDIA.QZONE)//传入平台
                                    .withMedia(web)
                                    .setCallback(umShareListener)//回调监听器
                                    .share();
                        } else if (type == MyForwardDialog.ForwardType.TYPE_WECHAT_MOMENT) {
                            //微信朋友圈分享
                            new ShareAction(ActivityPhotoViewActivity.this)
                                    .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)//传入平台
                                    .withMedia(web)
                                    .setCallback(umShareListener)//回调监听器
                                    .share();
                        }
                    }
                });
                dialog.show();
            }
        });
        mSaveTV.setVisibility(View.GONE);
        tvUpNum.setText(photoInfo.like + "");
        List<Fragment> list = new ArrayList<>();
        for (int i = 0; i < mPhotoBeanList.size(); i++) {
            list.add(PhotoViewFragment.newInstance(mPhotoBeanList.get(i), false));
        }
        PhotoViewPagerAdapter vpAdapter = new PhotoViewPagerAdapter(getSupportFragmentManager(), list, null);
        mVp.setAdapter(vpAdapter);
        mVp.setCurrentItem(index);
        if (mPhotoBeanList.size() == 1) {
            mIndexTV.setVisibility(View.GONE);
        }
        updateStatus(index);
        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateStatus(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mSaveTV.setOnClickListener(this);
    }

    public void updateStatus(int position) {
        this.mIndexTV.setText(position + 1 + "/" + mPhotoBeanList.size());
        PhotoBean photoBean = mPhotoBeanList.get(position);
        if (!FileUtils.isExternalStorageEnable()) {
            mSaveTV.setVisibility(View.GONE);
        } else {
            String filePath = getSaveFileName(photoBean);

            if (new File(filePath).exists()) {
                mSaveTV.setText("已保存");
                mSaveTV.setEnabled(false);
            } else {
                mSaveTV.setText("保存");
                mSaveTV.setEnabled(true);
            }

        }

    }

    public String getSaveFileName(PhotoBean photoBean) {
        return FileUtils.getExternalStoragePath() + File.separator + "shuiniu"
                + File.separator + "img_" + photoBean.title + ".png";
    }

//        new SaveBitMapTask().execute(mPhotoBeanList.get(mVp.getCurrentItem()));

    @OnClick(R.id.tv_up_num)
    public void onViewClicked() {
        if(!TipDialogUtil.checkLogin(this))
            return;
        MyProgressDialog.show(this, false);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData data) {
                MyProgressDialog.dismiss();
                photoDetailInfo.type =photoDetailInfo.type == 1?2:1;
                photoDetailInfo.like = photoDetailInfo.type == 1?photoDetailInfo.like+1:photoDetailInfo.like-1;
                tvUpNum.setText(photoDetailInfo.like + "");
                tvUpNum.setCompoundDrawablesWithIntrinsicBounds(photoDetailInfo.type == 1 ? R.mipmap.up : R.mipmap.no_up, 0, 0, 0);
                photoInfo.like = photoDetailInfo.like;
                EventBus.getDefault().post(photoInfo);

            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(ActivityApiService.class).photoLike(photoInfo.imgid, photoDetailInfo.type == 1?2:1,null).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    class SaveBitMapTask extends AsyncTask<PhotoBean, Void, File> {

        String TAG = "SaveBitMapTask";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSaveTV.setEnabled(false);
        }

        @Override
        protected File doInBackground(PhotoBean... params) {
            PhotoBean photoBean = params[0];
            try {
                return Glide.with(ActivityPhotoViewActivity.this)
                        .load(photoBean.imgUrl)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
            } catch (Exception ex) {
                Toast.makeText(ActivityPhotoViewActivity.this, R.string.store_failed, Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(File result) {
            if (result == null) {
                return;
            }
            //此path就是对应文件的缓存路径
            String path = result.getPath();
            //将缓存文件copy, 命名为图片格式文件
            copyFile(path);
        }
    }

    private File currentFile;

    public void copyFile(String oldPath) {
        // 首先保存图片
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();//注意小米手机必须这样获得public绝对路径
        String dirName = "tl_temp";
        File appDir = new File(file, dirName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        currentFile = new File(appDir, fileName);
        try {
            int byteRead;
            File oldFile = new File(oldPath);
            if (oldFile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(currentFile);
                byte[] buffer = new byte[1024];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
                Toast.makeText(ActivityPhotoViewActivity.this, R.string.store_success, Toast.LENGTH_SHORT).show();
                // 最后通知图库更新
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(new File(currentFile.getPath()))));
            }
        } catch (Exception e) {
            Toast.makeText(ActivityPhotoViewActivity.this, R.string.store_failed, Toast.LENGTH_SHORT).show();
            System.out.println("复制文件操作出错");
            e.printStackTrace();
        }

    }
//
//    @Override
//    public void onBackPressed() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            finishAfterTransition();
//        } else {
//            finish();
//        }
//    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.in_scale, R.anim.out_scale);
    }
}
