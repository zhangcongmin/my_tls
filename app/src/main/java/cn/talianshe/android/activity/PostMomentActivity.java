package cn.talianshe.android.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.adapter.PhotoAdapter;
import cn.talianshe.android.app.TaliansheApplication;
import cn.talianshe.android.bean.AssociationActivityInfo;
import cn.talianshe.android.bean.AssociationMemberListData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.UploadData;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.MultipartUtil;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.net.service.UploadLoadApiService;
import cn.talianshe.android.utils.DeleteFileUtil;
import cn.talianshe.android.utils.PhotoCropUtil;
import cn.talianshe.android.utils.PromptManager;
import cn.talianshe.android.widget.MyActionSheetDialog;
import cn.talianshe.android.widget.MyCallBack;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.OnRecyclerItemClickListener;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import top.zibin.luban.OnCompressListener;

/**
 * @author zcm
 * @ClassName: PostMomentActivity
 * @Description: 发布动态
 * @date 2017/11/13 9:31
 */
public class PostMomentActivity extends BaseActivity {
    public static final String FILE_DIR_NAME = "com.kuyue.wechatpublishimagesdrag";//应用缓存地址
    public static final String FILE_IMG_NAME = "images";//放置图片缓存
    public static final int MAX_PICTURE_SIZE = 9;//可添加图片最大数
    private static final String EXTRA_PARCEL = "extra_parcel";
    @BindView(R.id.rcv_img)
    RecyclerView rcvImg;
    @BindView(R.id.tvDelete)
    TextView tvDelete;
    @BindView(R.id.ll_official)
    LinearLayout llOfficial;
    @BindView(R.id.ll_personal)
    LinearLayout llPersonal;
    @BindView(R.id.tv_association)
    TextView tvAssociation;
    @BindView(R.id.tv_activity)
    TextView tvActivity;
    @BindView(R.id.tv_visible_range)
    TextView tvVisibleRange;
    @BindView(R.id.et_content)
    EditText etContent;

    //    private ArrayList<String> dragImages;//压缩长宽后图片
    private Context mContext;
    private PhotoAdapter photoAdapter;
    private ItemTouchHelper itemTouchHelper;

    public static int TYPE_PERSONAL = 1;
    public static int TYPE_OFFICIAL = 2;
    private static String EXTRA_TYPE = "extra_type";
    /**
     * 根据发表动态类型显示和隐藏控件
     */
    private int type;

    public static Intent getPersonalMomentIntent(Context context) {
        Intent intent = new Intent(context, PostMomentActivity.class);
        intent.putExtra(EXTRA_TYPE, TYPE_PERSONAL);
        return intent;
    }

    public static Intent getOfficialMomentIntent(Context context, AssociationActivityInfo info) {
        Intent intent = new Intent(context, PostMomentActivity.class);
        intent.putExtra(EXTRA_TYPE, TYPE_OFFICIAL);
        intent.putExtra(EXTRA_PARCEL, Parcels.wrap(info));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_moment);
        ButterKnife.bind(this);
        mContext = this;
        initData();
    }

    private AssociationActivityInfo officialInfo;

    private String visibleRange = "0";
    private String visibleUserIds;

    private void initData() {
        setTitle(R.string.post_moment);
        btnRight.setText(R.string.post);
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etContent.getText().toString())) {
                    MyToast.show(R.string.post_moment_content_null_tip, PostMomentActivity.this);
                    return;
                }
                if (mSelectedPhotos.contains(ADD_PICTURE))
                    mSelectedPhotos.remove(ADD_PICTURE);
                MyProgressDialog.show(PostMomentActivity.this, false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mCompressPhotos.clear();
                        for (String imgPath : mSelectedPhotos) {
                            String compressImagePath = PhotoCropUtil.compressImage(PostMomentActivity.this, imgPath);
                            mCompressPhotos.add(compressImagePath);
                        }
                        if (type == TYPE_PERSONAL) {
                            //发布个人动态
                            postPersonalMoment();
                        } else {
                            //发布官方动态
                            postOfficialMoment();
                        }
                    }
                }).start();
            }
        });
        type = getIntent().getIntExtra(EXTRA_TYPE, TYPE_PERSONAL);
        llOfficial.setVisibility(type == TYPE_PERSONAL ? View.GONE : View.VISIBLE);
        llPersonal.setVisibility(type == TYPE_PERSONAL ? View.VISIBLE : View.GONE);
        officialInfo = type == TYPE_PERSONAL ? null : (AssociationActivityInfo) Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PARCEL));
        if (officialInfo != null) {
            tvAssociation.setText(officialInfo.associationName);
            tvActivity.setText(officialInfo.activityName);
        }
        //添加按钮图片资源
//        dragImages = new ArrayList<>();
        mSelectedPhotos.add(ADD_PICTURE);
//        dragImages.addAll(mSelectedPhotos);
//        new Thread(new MyRunnable(dragImages, originImages, dragImages, myHandler, false)).start();//开启线程，在新线程中去压缩图片
//        photoAdapter.notifyDataSetChanged();
        initRcv();
    }

    //发布个人动态
    private void postPersonalMoment() {

        SchoolApiService schoolApiService = RequestEngine.getInstance().getServer(SchoolApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                for (String filePath : mCompressPhotos) {
                    DeleteFileUtil.deletePic(PostMomentActivity.this,filePath);
                }
                MyProgressDialog.dismiss();
                MyToast.show(R.string.post_moment_success, PostMomentActivity.this);
                finish();
            }
        };
        if (!TextUtils.isEmpty(imgIds)) {
            //说明图片都上传完毕
            schoolApiService.postPersonalMoment(etContent.getText().toString(), imgIds, visibleRange, visibleUserIds).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else {

            if (mCompressPhotos.size() > 0 && mCompressPhotos.get(0) != ADD_PICTURE) {
                //一张一张上传图片，完成后上传
                final UploadLoadApiService uploadLoadApiService = RequestEngine.getInstance().getServer(UploadLoadApiService.class);
                mUploadImgs.clear();
                imgIds = "";
                MultipartBody.Builder builder = MultipartUtil.filesToMultipartBodyBuilder("file", new String[]{mCompressPhotos.get(0)}, MediaType.parse("multipart/form-data"));
                HttpSubscriber uploadSubscriber = new RepeatSubscribe(this, uploadLoadApiService);
                uploadLoadApiService.uploadImage(builder.build()).compose(RxSchedulersHelper.io_main()).subscribe(uploadSubscriber);

            } else {
                schoolApiService.postPersonalMoment(etContent.getText().toString(), null, visibleRange, visibleUserIds).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
            }
        }
    }

    //发布官方动态
    private void postOfficialMoment() {
        SchoolApiService schoolApiService = RequestEngine.getInstance().getServer(SchoolApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                //发布成功，再次获取用户信息，未激活的话弹框激活
                for (String filePath : mCompressPhotos) {
                    DeleteFileUtil.deletePic(PostMomentActivity.this,filePath);
                }
                MyProgressDialog.dismiss();
                MyToast.show(R.string.post_moment_success, PostMomentActivity.this);
                finish();
            }
        };
        if (!TextUtils.isEmpty(imgIds)) {
            //说明图片都上传完毕
//            String coverImgId = mUploadImgs.get(0);
//            imgIds = mUploadImgs.size() == 1?"":imgIds.substring(coverImgId.length()+1);
            schoolApiService.postOfficialMoment(etContent.getText().toString(), imgIds, officialInfo.associationId, officialInfo.activityId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else {

            if (mCompressPhotos.size() > 0 && mCompressPhotos.get(0) != ADD_PICTURE) {
                //一张一张上传图片，完成后上传
                final UploadLoadApiService uploadLoadApiService = RequestEngine.getInstance().getServer(UploadLoadApiService.class);
                mUploadImgs.clear();
                imgIds = "";
                MultipartBody.Builder builder = MultipartUtil.filesToMultipartBodyBuilder("file", new String[]{mCompressPhotos.get(0)}, MediaType.parse("multipart/form-data"));
                HttpSubscriber uploadSubscriber = new RepeatSubscribe(this, uploadLoadApiService);
                uploadLoadApiService.uploadImage(builder.build()).compose(RxSchedulersHelper.io_main()).subscribe(uploadSubscriber);

            } else {
                schoolApiService.postPersonalMoment(etContent.getText().toString(), null, visibleRange, visibleUserIds).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
            }
        }
    }

    public class RepeatSubscribe extends HttpSubscriber<UploadData> {
        UploadLoadApiService uploadLoadApiService;

        public RepeatSubscribe(Context context, UploadLoadApiService uploadLoadApiService) {
            super(context);
            this.uploadLoadApiService = uploadLoadApiService;
        }

        @Override
        public void onSuccess(UploadData uploadData) {
            System.out.println("path:" + uploadData.result.path);
            mUploadImgs.add(uploadData.result.id);
            int totalImgCount = mCompressPhotos.contains(ADD_PICTURE) ? mCompressPhotos.size() - 1 : mCompressPhotos.size();
            if (mUploadImgs.size() == totalImgCount) {
                //说明全部上传成功
//                MyToast.show("上传成功", PostMomentActivity.this);

                for (String id : mUploadImgs) {
                    imgIds += id + ",";
                }
                imgIds = imgIds.substring(0, imgIds.length() - 1);
                if (type == TYPE_PERSONAL) {
                    postPersonalMoment();
                } else {
                    postOfficialMoment();
                }
            } else {
                MultipartBody.Builder builder = MultipartUtil.filesToMultipartBodyBuilder("file", new String[]{mCompressPhotos.get(mUploadImgs.size())}, MediaType.parse("multipart/form-data"));
                HttpSubscriber mSubscriber = new RepeatSubscribe(context, uploadLoadApiService);
                uploadLoadApiService.uploadImage(builder.build()).compose(RxSchedulersHelper.io_main()).subscribe(mSubscriber);
            }
        }

        @Override
        public void onError(Throwable e) {
            MyProgressDialog.dismiss();
            MyToast.show(R.string.post_failed_cause_upload_img, PostMomentActivity.this);
            super.onError(e);
        }

    }

    private void initRcv() {

        photoAdapter = new PhotoAdapter(mContext, mSelectedPhotos, MAX_PICTURE_SIZE, ADD_PICTURE);
        rcvImg.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        rcvImg.setAdapter(photoAdapter);
//        MyCallBack myCallBack = new MyCallBack(photoAdapter, dragImages, mSelectedPhotos);
        final MyCallBack myCallBack = new MyCallBack(photoAdapter, mSelectedPhotos, ADD_PICTURE);
        itemTouchHelper = new ItemTouchHelper(myCallBack);
        itemTouchHelper.attachToRecyclerView(rcvImg);//绑定RecyclerView

        //事件监听
        rcvImg.addOnItemTouchListener(new OnRecyclerItemClickListener(rcvImg) {

            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                if (vh.getAdapterPosition() == mSelectedPhotos.size() - 1 && mSelectedPhotos.get(vh.getAdapterPosition()).contains(ADD_PICTURE)) {//打开相册
                    showPicDialog();
                }
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {
                //如果item不是最后一个，即不是添加图片的哪一张
                if (!mSelectedPhotos.get(vh.getLayoutPosition()).contains(ADD_PICTURE)) {
                    tvDelete.setVisibility(View.VISIBLE);
                    int[] tvDeleteOutLocation = new int[2];
                    tvDelete.getLocationOnScreen(tvDeleteOutLocation);
//                    vh.itemView.bringToFront();
//                    llTextPicContent.bringToFront();
                    myCallBack.setBottomY(tvDeleteOutLocation[1]);
                    itemTouchHelper.startDrag(vh);
                }
            }
        });

        myCallBack.setDragListener(new MyCallBack.DragListener() {
            @Override
            public void deleteState(boolean delete) {
                if (delete) {
                    tvDelete.setBackgroundResource(R.color.holo_theme_dark);
                    tvDelete.setText(getResources().getString(R.string.post_delete_tv_s));
                } else {
                    tvDelete.setText(getResources().getString(R.string.post_delete_tv_d));
                    tvDelete.setBackgroundResource(R.color.holo_theme_light);
                }
            }

            @Override
            public void dragState(boolean start) {
                if (start) {
                    tvDelete.setVisibility(View.VISIBLE);
                } else {
                    tvDelete.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onDelete(String removePath) {
                System.out.println(removePath);
            }
        });
    }

    private List<MyActionSheetDialog.SheetItem> visibleRangeItems = new ArrayList<>();

    //选择图片
    private void showVisibleRangeDialog() {
        items.clear();
        items.add(new MyActionSheetDialog.SheetItem(getString(R.string.visible_all), MyActionSheetDialog.SheetItemColor.Black));
        items.add(new MyActionSheetDialog.SheetItem(getString(R.string.visible_all_association), MyActionSheetDialog.SheetItemColor.Black));
        items.add(new MyActionSheetDialog.SheetItem(getString(R.string.visible_part_association), MyActionSheetDialog.SheetItemColor.Black));
        MyActionSheetDialog dialog = new MyActionSheetDialog(this).builder().setCancelable(true)
                .setCanceledOnTouchOutside(true);
        for (int i = 0; i < items.size(); i++) {
            dialog.addSheetItem(items.get(i));
        }
        dialog.setSheetItemClickListener(new MyActionSheetDialog.SheetItemClickListener() {

            @Override
            public void onSheetItemClick(int position) {
                switch (position) {
                    case 0:
                        tvVisibleRange.setText(R.string.visible_all);
                        visibleRange = "0";
                        visibleUserIds = null;
                        break;
                    case 1:
                        tvVisibleRange.setText(R.string.visible_all_association);
                        visibleRange = "1";
                        visibleUserIds = null;
                        break;
                    case 2:
                        startActivity(new Intent(PostMomentActivity.this, WhoCanSeeActivity.class));
                        break;
                    default:
                        break;
                }
            }
        });
        dialog.show();
    }


    private List<MyActionSheetDialog.SheetItem> items = new ArrayList<>();

    //选择图片
    private void showPicDialog() {
        items.clear();
        items.add(new MyActionSheetDialog.SheetItem(getString(R.string.take_picture), MyActionSheetDialog.SheetItemColor.Black));
        items.add(new MyActionSheetDialog.SheetItem(getString(R.string.local_image), MyActionSheetDialog.SheetItemColor.Black));
        MyActionSheetDialog dialog = new MyActionSheetDialog(this).builder().setCancelable(true)
                .setCanceledOnTouchOutside(true);
        for (int i = 0; i < items.size(); i++) {
            dialog.addSheetItem(items.get(i));
        }
        dialog.setSheetItemClickListener(new MyActionSheetDialog.SheetItemClickListener() {

            @Override
            public void onSheetItemClick(int position) {
                switch (position) {
                    case 0:
                        //如果超过9张,返回
                        if (mSelectedPhotos.size() > 0 && !mSelectedPhotos.contains(ADD_PICTURE)) {
                            PromptManager.showToast(getString(R.string.max_photo_msg, MAX_PICTURE_SIZE));
                            return;
                        }
                        dispatchTakePictureIntent();
                        break;
                    case 1:
                        pickMultiPicture();
                        break;
                    default:
                        break;
                }
            }
        });
        dialog.show();
    }

    /**
     * 上传的图片id集合
     */
    private ArrayList<String> mUploadImgs = new ArrayList<>();
    /**
     * 上传的图片id集合
     */
    private String imgIds = "";
    /**
     * 被选中的图片
     */
    private ArrayList<String> mSelectedPhotos = new ArrayList<>();
    private ArrayList<String> mCompressPhotos = new ArrayList<>();
    public static final String ADD_PICTURE = TaliansheApplication.getInstance().getResources().getString(R.string.glide_plus_icon_string) + TaliansheApplication.getInstance().getPackageName() + "/mipmap/" + R.mipmap.add_picture;

    public void pickMultiPicture() {
        ArrayList<String> sPhotos = new ArrayList();
        sPhotos.addAll(mSelectedPhotos);
        sPhotos.remove(ADD_PICTURE);
        Intent intent = PhotoPickerActivity.getIntent(this, sPhotos);
        startActivityForResult(intent, RCODE_PICK_PICTURE);
    }

    public final static int RCODE_PICK_PICTURE = 700;
    public final static int RCODE_TAKE_PHOTO = 800;
    public final static int RCODE_PICK_FRIEND = 600;
    public final static int RCODE_PICK_STOCK = 500;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, photoFile.getAbsolutePath());
                uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            } else {
                uri = Uri.fromFile(photoFile);
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        uri);
                startActivityForResult(takePictureIntent, RCODE_TAKE_PHOTO);
            }
        }
    }

    String mCurrentPhotoPath;

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
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RCODE_PICK_PICTURE) {
                selectPickBack(data.getExtras());
            } else if (requestCode == RCODE_TAKE_PHOTO) {
                takePhotoBack();
            }
        }
    }

    private void selectPickBack(Bundle bundle) {

        if (bundle != null) {
            List<String> photos = bundle.getStringArrayList(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
            mSelectedPhotos.clear();
            mSelectedPhotos.addAll(photos);
            if (photos.size() < MAX_PICTURE_SIZE) {
                mSelectedPhotos.add(ADD_PICTURE);
            }
//            dragImages.clear();
//            dragImages.addAll(mSelectedPhotos);
            photoAdapter.notifyDataSetChanged();
//            uploadImage();
        }
    }

    private void takePhotoBack() {
        //have add picture image
        mSelectedPhotos.remove(ADD_PICTURE);
        mSelectedPhotos.add(mCurrentPhotoPath);
        if (mSelectedPhotos.size() < MAX_PICTURE_SIZE) {
            mSelectedPhotos.add(ADD_PICTURE);
        }
        photoAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.ll_visible_range)
    public void onViewClicked() {
        showVisibleRangeDialog();
    }

    @Subscribe
    public void onReceiveVisibleRangeList(List<AssociationMemberListData.AssociationMember> memberList) {
        visibleUserIds = "";
        for (AssociationMemberListData.AssociationMember member : memberList) {
            if (member.isSelected) {
                System.out.println(member.toString());
                visibleUserIds += member.id + ",";
            }
        }

        if (TextUtils.isEmpty(visibleUserIds)) {
            MyToast.show(R.string.no_visible_range_select, this);
            return;
        }
        visibleUserIds = visibleUserIds.substring(0, visibleUserIds.length() - 1);
        visibleRange = "2";
        tvVisibleRange.setText(R.string.visible_part_association);

    }
}
