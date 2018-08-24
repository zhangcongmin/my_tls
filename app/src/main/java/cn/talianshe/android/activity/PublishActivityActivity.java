package cn.talianshe.android.activity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
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
import cn.talianshe.android.bean.ActivityTypeListData;
import cn.talianshe.android.bean.AssociationListData;
import cn.talianshe.android.bean.CreateActivityData;
import cn.talianshe.android.eventbus.CostSouceEvent;
import cn.talianshe.android.eventbus.PublishActivitySuccessEvent;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.net.service.AssociationApiService;
import cn.talianshe.android.utils.DeleteFileUtil;
import cn.talianshe.android.utils.PhotoCropUtil;
import cn.talianshe.android.utils.StringUtils;
import cn.talianshe.android.widget.MyActionSheetDialog;
import cn.talianshe.android.widget.MyCallBack;
import cn.talianshe.android.widget.MyEditTextDialog;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.OnRecyclerItemClickListener;
import cn.talianshe.android.widget.ScaleImageView;
import library.talianshe.android.photobrowser.PhotoViewActivity;
import library.talianshe.android.photobrowser.bean.PhotoBean;

/**
 * @author zcm
 * @ClassName: PublishActivityActivity
 * @Description: 发布活动
 * @date 2017/11/24 14:31
 */
public class PublishActivityActivity extends BaseActivity {
    public static final int MAX_PICTURE_SIZE = 9;//可添加图片最大数
    @BindView(R.id.rcv_img)
    RecyclerView rcvImg;
    @BindView(R.id.tvDelete)
    TextView tvDelete;
    @BindView(R.id.et_activity_name)
    EditText etActivityName;
    @BindView(R.id.iv_activity_logo)
    ScaleImageView ivActivityLogo;
    @BindView(R.id.iv_activity_logo_delete)
    ImageView ivActivityLogoDelete;
    @BindView(R.id.ll_official)
    LinearLayout llOfficial;
    @BindView(R.id.tv_banner_tip)
    TextView tvBannerTip;
    @BindView(R.id.tv_activity_association)
    TextView tvActivityAssociation;
    @BindView(R.id.tv_activity_type)
    TextView tvActivityType;
    @BindView(R.id.et_num)
    EditText etNum;
    @BindView(R.id.tv_cost_source)
    TextView tvCostSource;
    @BindView(R.id.tv_activity_desc)
    TextView tvActivityDesc;

    //    private ArrayList<String> dragImages;//压缩长宽后图片
    private Context mContext;
    private PhotoAdapter photoAdapter;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_activity);
        ButterKnife.bind(this);
        mContext = this;
        initData();
    }

    private void initData() {
        setTitle(R.string.publish_activity);
        mSelectedBannerPhotos.add(ADD_PICTURE);
        initRcv();
    }

    private void initRcv() {

        photoAdapter = new PhotoAdapter(mContext, mSelectedBannerPhotos, MAX_PICTURE_SIZE, ADD_PICTURE, false);
        rcvImg.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        rcvImg.setAdapter(photoAdapter);
//        MyCallBack myCallBack = new MyCallBack(photoAdapter, dragImages, mSelectedBannerPhotos);
        final MyCallBack myCallBack = new MyCallBack(photoAdapter, mSelectedBannerPhotos, ADD_PICTURE);
        itemTouchHelper = new ItemTouchHelper(myCallBack);
        itemTouchHelper.attachToRecyclerView(rcvImg);//绑定RecyclerView

        //事件监听
        rcvImg.addOnItemTouchListener(new OnRecyclerItemClickListener(rcvImg) {

            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                if (vh.getAdapterPosition() == mSelectedBannerPhotos.size() - 1 && mSelectedBannerPhotos.get(vh.getAdapterPosition()).contains(ADD_PICTURE)) {//打开相册
                    isChooseLogo = false;
                    showSelectBannerPicDialog();
                }
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {
                //如果item不是最后一个，即不是添加图片的哪一张
//                if (vh.getLayoutPosition() != dragImages.size() - 1) {
                if (!mSelectedBannerPhotos.get(vh.getLayoutPosition()).contains(ADD_PICTURE)) {
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
                if (!TextUtils.isEmpty(removePath)) {
                    DeleteFileUtil.deletePic(PublishActivityActivity.this,removePath);
                }
            }
        });
    }

    private List<MyActionSheetDialog.SheetItem> items = new ArrayList<>();

    //选择图片
    private void showSelectBannerPicDialog() {
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
     * 被选中的宣传图片
     */
    private ArrayList<String> mSelectedBannerPhotos = new ArrayList<>();
    private String mSelectedLogoPhoto;
    public static final String ADD_PICTURE = TaliansheApplication.getInstance().getResources().getString(R.string.glide_plus_icon_string) + TaliansheApplication.getInstance().getPackageName() + "/mipmap/" + R.mipmap.add_activity_photo;

    public void pickMultiPicture() {
//        ArrayList<String> sPhotos = new ArrayList();
//        sPhotos.addAll(mSelectedBannerPhotos);
//        sPhotos.remove(ADD_PICTURE);
//        Intent intent = PhotoPickerActivity.getIntent(this, sPhotos);
        Intent intent = PhotoPickerActivity.getSinglePicIntent(this);
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

    private int logoMinWidth = 320;
    private float logoScale = 1;//宽高比例
    private int bannerMinWidth = 480;
    private float bannerScale = 0.4f;

    private void selectPickBack(Bundle bundle) {

        if (bundle != null) {
            List<String> photos = bundle.getStringArrayList(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
            handleOriginImg(photos.get(0));
        }
    }

    private void takePhotoBack() {

        String originImgPath = mCurrentPhotoPath;

        handleOriginImg(originImgPath);
    }

    private void handleOriginImg(String originImgPath) {
        RectF photoRect = PhotoCropUtil.getPhotoRect(originImgPath);
        if (isChooseLogo) {
            if (photoRect.width() < logoMinWidth) {
                MyToast.show(R.string.logo_pic_too_small, this);
                return;
            } else {
                //图片符合要求
                String photoPath = PhotoCropUtil.centerCropPhoto(this, originImgPath, logoScale);
                // 最后通知图库更新
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(new File(photoPath))));
                if (!TextUtils.isEmpty(mSelectedLogoPhoto)) {
                    DeleteFileUtil.deletePic(PublishActivityActivity.this,mSelectedLogoPhoto);
                }
                mSelectedLogoPhoto = photoPath;
                RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(mContext).load(mSelectedLogoPhoto).apply(options).into(ivActivityLogo);
                ivActivityLogoDelete.setVisibility(View.VISIBLE);

            }
        } else {
            if (photoRect.width() < bannerMinWidth) {
                MyToast.show(R.string.banner_pic_too_small, this);
                return;
            } else {
                //图片符合要求
                String photoPath = PhotoCropUtil.centerCropPhoto(this, originImgPath, bannerScale);
                // 最后通知图库更新
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(new File(photoPath))));
                mSelectedBannerPhotos.remove(ADD_PICTURE);
                mSelectedBannerPhotos.add(photoPath);
                if (mSelectedBannerPhotos.size() != MAX_PICTURE_SIZE) {
                    mSelectedBannerPhotos.add(ADD_PICTURE);
                }
                photoAdapter.notifyDataSetChanged();
            }
        }
    }

    private boolean isChooseLogo = false;

    @OnClick({R.id.iv_activity_logo, R.id.iv_activity_logo_delete, R.id.ll_activity_association, R.id.ll_activity_type, R.id.ll_cost_source, R.id.ll_activity_desc, R.id.btn_next_step})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_activity_logo:
                if (TextUtils.isEmpty(mSelectedLogoPhoto)) {
                    //logo图片为空才能选择
                    isChooseLogo = true;
                    showSelectBannerPicDialog();
                } else {
                    ArrayList<PhotoBean> arrayList = new ArrayList<>();
                    PhotoBean photoBean = new PhotoBean();
                    photoBean.imgUrl = mSelectedLogoPhoto;
                    arrayList.add(photoBean);
                    PhotoViewActivity.startPhotoViewActivity(mContext, arrayList, view, 0);

                }
                break;
            case R.id.iv_activity_logo_delete:
                if (!TextUtils.isEmpty(mSelectedLogoPhoto)) {
                    ivActivityLogo.setImageResource(R.mipmap.add_picture);
                    DeleteFileUtil.deletePic(this,mSelectedLogoPhoto);
                    mSelectedLogoPhoto = null;
                }
                break;
            case R.id.ll_activity_association:
                getManageAssociationList();
                break;
            case R.id.ll_activity_type:
                getActivityTypeList();
                break;
            case R.id.ll_cost_source:
                startActivity(CostSourceActivity.getCostSourceIntent(this, costSouceEvent));
                break;
            case R.id.ll_activity_desc:
                showEditDialog("", getString(R.string.activity_desc), "最多可输入200个字符", 4, 200);
                break;
            case R.id.btn_next_step:
                checkData();
//                startActivity(PublishActivityTimeActivity.getTimeIntent(this,null));
                break;
        }
    }

    private CostSouceEvent costSouceEvent;

    @Subscribe
    public void onReceiveCostSourceEvent(CostSouceEvent event) {
        this.costSouceEvent = event;
        switch (event.costSource) {
            case 1:
                //自筹
                tvCostSource.setText(getString(R.string.cost_self_holder, StringUtils.moneyFormat(event.costSelf).toString()));
                break;
            case 2:
                //学校拨款
                tvCostSource.setText(getString(R.string.cost_school_holder, StringUtils.moneyFormat(event.costSchool).toString()));
                break;
            case 3:
                //商家赞助
                tvCostSource.setText(getString(R.string.cost_business_holder, StringUtils.moneyFormat(event.costBusiness).toString()));
                break;
            case 4:
                //混合模式
                tvCostSource.setText(R.string.cost_multi);
                double totalCost = event.costSelf + event.costBusiness + event.costSchool;

                tvCostSource.setText(getString(R.string.cost_multi_holder, StringUtils.moneyFormat(totalCost).toString()));
                break;
        }
    }

    private void checkData() {
        if (TextUtils.isEmpty(etActivityName.getText().toString())) {
            MyToast.show(R.string.activity_name_null_tip, this);
            return;
        }
        if (TextUtils.isEmpty(mSelectedLogoPhoto)) {
            MyToast.show(R.string.logo_null_tip, this);
            return;
        }
        if (mSelectedBannerPhotos.size() == 0 || (mSelectedBannerPhotos.size() == 1 && mSelectedBannerPhotos.get(0).equals(ADD_PICTURE))) {
            MyToast.show(R.string.banner_null_tip, this);
            return;
        }
        if (associationInfo == null) {
            MyToast.show(R.string.input_activity_association_tip, this);
            return;
        }
        if (activityTypeInfo == null) {
            MyToast.show(R.string.input_activity_type_tip, this);
            return;
        }
        if (TextUtils.isEmpty(etNum.getText().toString())) {
            MyToast.show(R.string.activity_num_null_tip, this);
            return;
        }
        if (costSouceEvent == null) {
            MyToast.show(R.string.input_cost_source_tip, this);
            return;
        }

        if (getString(R.string.input_activity_desc_tip).equals(tvActivityDesc.getText().toString())) {
            MyToast.show(R.string.input_activity_desc_tip, this);
            return;
        }
        CreateActivityData data = new CreateActivityData();
        data.mSelectedLogoPhoto = mSelectedLogoPhoto;
        mSelectedBannerPhotos.remove(ADD_PICTURE);
        data.mSelectedBannerPhotos = mSelectedBannerPhotos;
        data.associationInfo = associationInfo;
        data.activityTypeInfo = activityTypeInfo;
        data.num = etNum.getText().toString();
        data.costSouceEvent = costSouceEvent;
        data.activityDesc = tvActivityDesc.getText().toString();
        data.activityName = etActivityName.getText().toString();
        startActivity(PublishActivityTimeActivity.getTimeIntent(this, data));
    }


    private void showEditDialog(String defaultText, String info, String hint, int lines, int maxLength) {
        MyEditTextDialog dialog = new MyEditTextDialog(this, info, defaultText, hint, lines, maxLength).builder();
        dialog.setResultListener(new MyEditTextDialog.EditTextResultListener() {
            @Override
            public void onResult(String result) {
                if (!TextUtils.isEmpty(result)) {
                    tvActivityDesc.setText(result);
                }
            }
        });
        dialog.show();
    }

    private AssociationListData.AssociationInfoList associationListInfo;
    private ActivityTypeListData.ActivityTypeListInfo activityTypeListInfo;
    private AssociationListData.AssociationInfo associationInfo;
    private ActivityTypeListData.ActivityType activityTypeInfo;

    //获取管理的社团
    private void getManageAssociationList() {
        selectType = typeAssociation;
        if (associationListInfo == null || associationListInfo.list == null || associationListInfo.list.size() == 0) {
            MyProgressDialog.show(this, false);
            HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationListData>(this) {

                @Override
                public void onSuccess(AssociationListData listData) {
                    MyProgressDialog.dismiss();
                    associationListInfo = listData.result;
                    if (associationListInfo != null && associationListInfo.list != null && associationListInfo.list.size() > 0) {
                        showSelectAssociationOrActivityTypeDialog();
                    } else {
                        MyToast.show(R.string.no_association_tip, PublishActivityActivity.this);
                    }
                }

                @Override
                public void onError(String msg) {
                    super.onError(msg);
                    MyProgressDialog.dismiss();
                }
            };
            RequestEngine.getInstance().getServer(AssociationApiService.class).getManageAssociationList().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else {
            showSelectAssociationOrActivityTypeDialog();
        }
    }

    //获取活动类型
    private void getActivityTypeList() {
        selectType = typeActivity;
        if (activityTypeListInfo == null || activityTypeListInfo.list == null || activityTypeListInfo.list.size() == 0) {
            MyProgressDialog.show(this, false);
            HttpSubscriber httpSubscriber = new HttpSubscriber<ActivityTypeListData>(this) {

                @Override
                public void onSuccess(ActivityTypeListData listData) {
                    MyProgressDialog.dismiss();
                    activityTypeListInfo = listData.result;
                    if (activityTypeListInfo != null && activityTypeListInfo.list != null && activityTypeListInfo.list.size() > 0) {
                        showSelectAssociationOrActivityTypeDialog();
                    } else {
                        MyToast.show(R.string.no_activity_type_tip, PublishActivityActivity.this);
                    }
                }

                @Override
                public void onError(String msg) {
                    super.onError(msg);
                    MyProgressDialog.dismiss();
                }
            };
            RequestEngine.getInstance().getServer(ActivityApiService.class).getActivityTypeList().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else {
            showSelectAssociationOrActivityTypeDialog();
        }
    }

    private int selectType;
    private int typeAssociation = 1;
    private int typeActivity = 2;

    private Dialog selectAssociationOrActivityTypeDialog;

    private void showSelectAssociationOrActivityTypeDialog() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        View view = LayoutInflater.from(this).inflate(
                R.layout.view_select_association_or_activity_type_dialog, null);
        view.setMinimumWidth(display.getWidth());
        RecyclerView rvList = view.findViewById(R.id.rv_share_list);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(new MyAssociationAdapter());
        selectAssociationOrActivityTypeDialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        selectAssociationOrActivityTypeDialog.setContentView(view);
        Window dialogWindow = selectAssociationOrActivityTypeDialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
        selectAssociationOrActivityTypeDialog.show();
    }

    private class MyAssociationAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(
                    R.layout.item_select_association_or_activity_type_list_dialog, parent, false);
            MyItemViewHolder holder = new MyItemViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MyItemViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return selectType == typeAssociation ? associationListInfo.list.size() : activityTypeListInfo.list.size();
        }

    }

    public class MyItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_item_name)
        TextView tvItemName;

        MyItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(final int position) {
            if (selectType == typeAssociation) {
                AssociationListData.AssociationInfo info = associationListInfo.list.get(position);
                tvItemName.setText(info.associationName);

            } else {
                ActivityTypeListData.ActivityType info = activityTypeListInfo.list.get(position);
                tvItemName.setText(info.typeName);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectType == typeAssociation) {
                        associationInfo = associationListInfo.list.get(position);
                        tvActivityAssociation.setText(associationInfo.associationName);
                    } else {
                        activityTypeInfo = activityTypeListInfo.list.get(position);
                        tvActivityType.setText(activityTypeInfo.typeName);
                    }
                    selectAssociationOrActivityTypeDialog.dismiss();
                }
            });
        }
    }

    @Subscribe
    public void onReceivePublishActivitySuccessEvent(PublishActivitySuccessEvent event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSelectedBannerPhotos.remove(ADD_PICTURE);
        if (!TextUtils.isEmpty(mSelectedLogoPhoto)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DeleteFileUtil.delete(mSelectedLogoPhoto);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(new File(mSelectedLogoPhoto))));
                }
            }).start();
        }
        if (mSelectedBannerPhotos.size() >= 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (String bannerPhotoPath : mSelectedBannerPhotos) {
                        DeleteFileUtil.delete(bannerPhotoPath);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(new File(bannerPhotoPath))));
                    }
                }
            }).start();
        }
    }
}
