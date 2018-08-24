package library.talianshe.android.photobrowser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import library.talianshe.android.R;
import library.talianshe.android.photobrowser.bean.PhotoBean;

/**
 * @author zcm
 * @ClassName: PhotoViewActivity
 * @Description: 图片查看器
 * @date 2017/11/10 16:27
 */
public class PhotoViewActivity extends AppCompatActivity implements View.OnClickListener {

    private static String INDEX = "index";
    private static String IS_UP = "is_up";
    private static String PHOTO_BEAN_LIST = "photo_bean_list";
    private List<PhotoBean> mPhotoBeanList;
    /**
     * 图片索引
     */
    private int index;

    public static void startPhotoViewActivity(Context context, ArrayList<PhotoBean> mPhotoBeanList, View view, int defaultIndex) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(INDEX, defaultIndex);
        intent.putParcelableArrayListExtra(PHOTO_BEAN_LIST, mPhotoBeanList);
//        intent.putStringArrayListExtra(PHOTO_BEAN_LIST,mPhotoBeanList);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeScaleUpAnimation(view,
                        (int) view.getWidth() / 2, (int) view.getHeight() / 2,
                        0, 0);
        ActivityCompat.startActivity(context, intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);
        index = getIntent().getIntExtra(INDEX, 0);
        mPhotoBeanList = getIntent().getParcelableArrayListExtra(PHOTO_BEAN_LIST);
        initView();
    }

    private HackyViewPager mVp;
    private TextView mIndexTV;
    private TextView mSaveTV;

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initView() {
        mVp = (HackyViewPager) findViewById(R.id.vp);
        mIndexTV = (TextView) findViewById(R.id.tv_index);
        mSaveTV = (TextView) findViewById(R.id.tv_save);
        List<Fragment> list = new ArrayList<>();
        for (int i = 0; i < mPhotoBeanList.size(); i++) {
            list.add(PhotoViewFragment.newInstance(mPhotoBeanList.get(i)));
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

    @Override
    public void onClick(View v) {
        new SaveBitMapTask().execute(mPhotoBeanList.get(mVp.getCurrentItem()));
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
                return Glide.with(PhotoViewActivity.this)
                        .load(photoBean.imgUrl)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
            } catch (Exception ex) {
                Toast.makeText(PhotoViewActivity.this, R.string.store_failed, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PhotoViewActivity.this, R.string.store_success, Toast.LENGTH_SHORT).show();
                // 最后通知图库更新
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(new File(currentFile.getPath()))));
            }
        } catch (Exception e) {
            Toast.makeText(PhotoViewActivity.this, R.string.store_failed, Toast.LENGTH_SHORT).show();
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
