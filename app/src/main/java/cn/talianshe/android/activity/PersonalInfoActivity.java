package cn.talianshe.android.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
import cn.talianshe.android.bean.DepartMentMajorClassListData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.UploadData;
import cn.talianshe.android.bean.UserData;
import cn.talianshe.android.db.entity.UserInfo;
import cn.talianshe.android.db.util.UserEntityUtil;
import cn.talianshe.android.eventbus.FillPersonalInfoEvent;
import cn.talianshe.android.eventbus.OriginPlaceEvent;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.MultipartUtil;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.UploadLoadApiService;
import cn.talianshe.android.net.service.UserApiService;
import cn.talianshe.android.utils.DeleteFileUtil;
import cn.talianshe.android.utils.PhoneFormatCheckUtil;
import cn.talianshe.android.widget.MyActionSheetDialog;
import cn.talianshe.android.widget.MyToast;
import okhttp3.MediaType;
import okhttp3.MultipartBody;

/**
 * @author zcm
 * @ClassName: PersonalInfoActivity
 * @Description: 个人资料
 * @date 2017/12/7 21:32
 */
public class PersonalInfoActivity extends BaseActivity {

    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_mobile)
    TextView tvMobile;
    @BindView(R.id.et_nickname)
    EditText etNickname;
    @BindView(R.id.et_mobile)
    EditText etMobile;
    @BindView(R.id.et_identity)
    EditText etIdentity;
    @BindView(R.id.et_qq)
    EditText etQQ;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.tv_gender)
    TextView tvGender;
    @BindView(R.id.tv_origin_place)
    TextView tvOriginPlace;
    @BindView(R.id.tv_outlook)
    TextView tvOutlook;
    @BindView(R.id.tv_school)
    TextView tvSchool;
    @BindView(R.id.tv_department)
    TextView tvDepartment;
    @BindView(R.id.tv_major)
    TextView tvMajor;
    @BindView(R.id.tv_class)
    TextView tvClass;
    @BindView(R.id.ll_gender)
    LinearLayout llGender;
    @BindView(R.id.ll_student_school_infos)
    LinearLayout llStudentSchoolInfos;
    @BindView(R.id.ll_department)
    LinearLayout llDepartment;
    @BindView(R.id.ll_major)
    LinearLayout llMajor;
    @BindView(R.id.ll_class)
    LinearLayout llClass;
    @BindView(R.id.tv_person_id)
    TextView tvPersonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        ButterKnife.bind(this);
        initData();
    }

    private String[] outlooks;
    private UserInfo userInfo;

    private void initData() {
        btnRight.setText(R.string.save);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2017/12/7 保存个人信息
                if (TextUtils.isEmpty(etIdentity.getText().toString())) {
                    MyToast.show(R.string.identity_null_tip, PersonalInfoActivity.this);
                    return;
                }
                if (TextUtils.isEmpty(tvGender.getText().toString()) || getString(R.string.input_select_gender_tip).equals(tvGender.getText().toString())) {
                    MyToast.show(R.string.gender_null_tip, PersonalInfoActivity.this);
                    return;
                }
                if (!userInfo.isTeacher) {
                    if (!tvDepartment.isSelected()) {
                        MyToast.show(R.string.department_null_tip, PersonalInfoActivity.this);
                        return;
                    }
                    if (!tvMajor.isSelected()) {
                        MyToast.show(R.string.major_null_tip, PersonalInfoActivity.this);
                        return;
                    }
                    if (!tvClass.isSelected()) {
                        MyToast.show(R.string.class_null_tip, PersonalInfoActivity.this);
                        return;
                    }
                    if (!PhoneFormatCheckUtil.isChinaPhoneLegal(etMobile.getText().toString())) {
                        MyToast.show(getString(R.string.mobile_illegal), PersonalInfoActivity.this);
                        return;
                    }
                }
                saveUserInfo();
            }
        });
        setTitle(R.string.my_personal_info);
        outlooks = getResources().getStringArray(R.array.outlooks);
        userInfo = GlobalParams.USER_INFO;
        tvName.setText(userInfo.realname);
        if (!TextUtils.isEmpty(userInfo.avatar)) {
            RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
            options.error(R.mipmap.default_head);
            Glide.with(this)
                    .load(TLSUrl.BASE_URL + userInfo.avatar)
                    .apply(options)
                    .into(ivHead);
        }
        if (TextUtils.isEmpty(userInfo.sex)) {
            tvGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.more, 0);
            llGender.setClickable(true);
            llGender.setFocusable(true);
        } else {
            tvGender.setText("0".equals(userInfo.sex) ? R.string.female : R.string.male);
            tvGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            llGender.setClickable(false);
            llGender.setFocusable(false);
        }
        if (!TextUtils.isEmpty(userInfo.nickname)) {
            etNickname.setText(userInfo.nickname);
        }
        tvMobile.setText(userInfo.mobile);
        etMobile.setText(userInfo.mobile);
        etQQ.setText(userInfo.qq);
        etEmail.setText(userInfo.email);
        tvOriginPlace.setText(userInfo.originPlace);
        tvOutlook.setText(userInfo.politicalOutlook);
        tvSchool.setText(userInfo.school);
        tvPersonId.setText(userInfo.studentId);
        if (!TextUtils.isEmpty(userInfo.identity)) {
            etIdentity.setClickable(false);
            etIdentity.setFocusable(false);
            etIdentity.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            etIdentity.setText(userInfo.identity);
        }
        if (userInfo.isTeacher) {
            llStudentSchoolInfos.setVisibility(View.GONE);
        } else {
            tvDepartment.setSelected(false);
            tvMajor.setSelected(false);
            tvClass.setSelected(false);
            if (!TextUtils.isEmpty(userInfo.department)) {
                llDepartment.setClickable(false);
                llDepartment.setFocusable(false);
                tvDepartment.setSelected(true);
                tvDepartment.setText(userInfo.department);
                tvDepartment.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            if (!TextUtils.isEmpty(userInfo.major)) {
                llMajor.setClickable(false);
                llMajor.setFocusable(false);
                tvMajor.setSelected(true);
                tvMajor.setText(userInfo.major);
                tvMajor.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            if (!TextUtils.isEmpty(userInfo.grade)) {
                llClass.setClickable(false);
                llClass.setFocusable(false);
                tvClass.setSelected(true);
                tvClass.setText(userInfo.grade);
                tvClass.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }

    }

    private String departmentId;
    private String majorId;
    private String classId;

    private void saveUserInfo() {
        swipeLayout.setRefreshing(true);
        final UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        final HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData stringData) {
                MyToast.show(R.string.save_success, PersonalInfoActivity.this);
                //保存成功，更新用户信息，必填项置为不可更改
                etIdentity.setClickable(false);
                etIdentity.setFocusable(false);
                llGender.setClickable(false);
                llGender.setFocusable(false);
                llDepartment.setClickable(false);
                llDepartment.setFocusable(false);
                llMajor.setClickable(false);
                llMajor.setFocusable(false);
                llClass.setClickable(false);
                llClass.setFocusable(false);
                etIdentity.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                tvGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                tvDepartment.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                tvMajor.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                tvClass.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                getUserInfo();
            }
        };
        final String nickname = etNickname.getText().toString();
        final String sex = getString(R.string.male).equals(tvGender.getText().toString()) ? "1" : "0";
        final String identity = etIdentity.getText().toString();
        final String qq = etQQ.getText().toString();
        final String email = etEmail.getText().toString();
        final String originPlace = tvOriginPlace.getText().toString();
        final String outlook = tvOutlook.getText().toString();
        final String phone = etMobile.getText().toString();
        if (!TextUtils.isEmpty(clipImagePath)) {
//            builder = new MultipartBody.Builder().setType(MediaType.parse("multipart/form-data"));
//            MultipartUtil.addTextPart(builder, "nickname", "聪珉");
//            builder = MultipartUtil.filesToMultipartBodyBuilder(builder,"avatar", new String[]{clipImagePath}, MediaType.parse("image/jpg"));
            UploadLoadApiService uploadLoadApiService = RequestEngine.getInstance().getServer(UploadLoadApiService.class);
            final HttpSubscriber uploadSubscriber = new HttpSubscriber<UploadData>(this) {
                @Override
                public void onSuccess(UploadData uploadData) {
                    MultipartBody.Builder builder = new MultipartBody.Builder().setType(MediaType.parse("multipart/form-data"));
                    MultipartUtil.addTextPart(builder, "avatar", uploadData.result.id);
                    MultipartUtil.addTextPart(builder, "nickname", "加盐");

//                    userApiService.saveUserInfo(uploadData.result.id, "加盐").compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
                    userApiService.saveUserInfo(uploadData.result.id, nickname, identity, qq, email, originPlace, outlook, sex, departmentId, majorId, classId, phone).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

                }
            };
            MultipartBody.Builder builder = MultipartUtil.filesToMultipartBodyBuilder("file", new String[]{clipImagePath}, MediaType.parse("multipart/form-data"));
            uploadLoadApiService.uploadImage(builder.build()).compose(RxSchedulersHelper.io_main()).subscribe(uploadSubscriber);


        } else {
//            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MediaType.parse("multipart/form-data"));
//            MultipartUtil.addTextPart(builder, "nickname", "小聪珉");
//            userApiService.saveUserInfo(builder.build()).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
            userApiService.saveUserInfo(null, nickname, identity, qq, email, originPlace, outlook, sex, departmentId, majorId, classId, phone).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        }
    }

    private void getUserInfo() {
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<UserData>(this) {
            @Override
            public void onSuccess(UserData userData) {
                userInfo = userData.result;
                swipeLayout.setRefreshing(false);
                userInfo.token = GlobalParams.TOKEN;
                UserEntityUtil.saveOrUpdateUserInfo(PersonalInfoActivity.this, userInfo);
                GlobalParams.USER_INFO = userInfo;
            }
        };
        userApiService.getUserInfo().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    @OnClick({R.id.ll_head, R.id.ll_mobile, R.id.ll_gender, R.id.ll_origin_place, R.id.ll_political_outlook, R.id.ll_department, R.id.ll_major, R.id.ll_class})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_head:
                showPicDialog();
                break;
            case R.id.ll_mobile:
                //startActivity(BindMobileActivity.getChangeMobileIntent(this, userInfo.mobile));
                break;
            case R.id.ll_gender:
                showSelectGenderDialog();
                break;
            case R.id.ll_origin_place:
                startActivity(new Intent(this, ChooseOriginPlaceActivity.class));
                break;
            case R.id.ll_political_outlook:
                showSelectPoliticalOutlookDialog();
                break;
            case R.id.ll_department:
                // TODO: 2017/12/7 选择院系
                startActivity(ChooseMultiSchoolInfoActivity.getSchoolInfoIntent(this, ChooseMultiSchoolInfoActivity.TYPE_COLLEGE, null));
                break;
            case R.id.ll_major:
                // TODO: 2017/12/7 选择专业
                if (TextUtils.isEmpty(departmentId)) {
                    MyToast.show(R.string.choose_department_first_tip, this);
                    return;
                }
                startActivity(ChooseMultiSchoolInfoActivity.getSchoolInfoIntent(this, ChooseMultiSchoolInfoActivity.TYPE_MAJOR, departmentId));
                break;
            case R.id.ll_class:
                // TODO: 2017/12/7 选择班级
                if (TextUtils.isEmpty(majorId)) {
                    MyToast.show(R.string.choose_major_first_tip, this);
                    return;
                }
                startActivity(ChooseMultiSchoolInfoActivity.getSchoolInfoIntent(this, ChooseMultiSchoolInfoActivity.TYPE_CLASS, majorId));
                break;
        }
    }

    @Subscribe
    public void onReceiveDepartmentMajorClassInfo(DepartMentMajorClassListData.DepartmentMajorClassInfo event) {
        if (event.departmentId != null) {
            departmentId = event.departmentId;
            tvDepartment.setText(event.departmentName);
            tvDepartment.setSelected(true);
            //专业和班级置为默认值
            majorId = null;
            classId = null;
            tvMajor.setText(R.string.input_select_major_tip);
            tvClass.setText(R.string.input_select_class_tip);
            tvMajor.setSelected(false);
            tvClass.setSelected(false);
            tvDepartment.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tvMajor.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.more, 0);
            tvClass.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.more, 0);
        }
        if (event.majorId != null) {
            majorId = event.majorId;
            tvMajor.setText(event.majorName);
            tvMajor.setSelected(true);
            //班级置为默认值
            classId = null;
            tvClass.setText(R.string.input_select_class_tip);
            tvClass.setSelected(false);
            tvMajor.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tvClass.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.more, 0);
        }
        if (event.classId != null) {
            classId = event.classId;
            tvClass.setText(event.className);
            tvClass.setSelected(true);
            tvClass.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    @Subscribe
    public void onReceiveOriginPlaceEvent(OriginPlaceEvent event) {
        tvOriginPlace.setText(event.originPlace);
    }

    /**
     * 显示政治面貌选择对话框
     */
    private void showSelectPoliticalOutlookDialog() {
        MyActionSheetDialog dialog = new MyActionSheetDialog(this).builder().setCancelable(true)
                .setCanceledOnTouchOutside(true);
        for (String outlook : outlooks) {
            dialog.addSheetItem(new MyActionSheetDialog.SheetItem(outlook, MyActionSheetDialog.SheetItemColor.Black));
        }
        dialog.setSheetItemClickListener(new MyActionSheetDialog.SheetItemClickListener() {

            @Override
            public void onSheetItemClick(int position) {
                tvOutlook.setText(outlooks[position]);
            }
        });
        dialog.show();
    }

    /**
     * 显示性别选择对话框
     */
    private void showSelectGenderDialog() {
        List<MyActionSheetDialog.SheetItem> items = new ArrayList<>();
        items.clear();
        items.add(new MyActionSheetDialog.SheetItem(getString(R.string.male), MyActionSheetDialog.SheetItemColor.Black));
        items.add(new MyActionSheetDialog.SheetItem(getString(R.string.female), MyActionSheetDialog.SheetItemColor.Black));
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
                        tvGender.setText(R.string.male);
                        break;
                    case 1:
                        tvGender.setText(R.string.female);
                        break;
                    default:
                        break;
                }
            }
        });
        dialog.show();
    }

    //---------------------拍照、选择照片，裁剪-------------------
    public final static int RCODE_PICK_PICTURE = 700;
    public final static int RCODE_TAKE_PHOTO = 800;
    public final static int RCODE_CLIP_IMAGE = 900;

    /**
     * 被选中的图片
     */
    private ArrayList<String> mSelectedPhotos = new ArrayList<>();

    //选择图片
    private void showPicDialog() {
        List<MyActionSheetDialog.SheetItem> items = new ArrayList<>();
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
                        dispatchTakePictureIntent();
                        break;
                    case 1:
                        Intent intent = PhotoPickerActivity.getSinglePicIntent(PersonalInfoActivity.this);
                        startActivityForResult(intent, RCODE_PICK_PICTURE);
                        break;
                    default:
                        break;
                }
            }
        });
        dialog.show();
    }

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
            } else if (requestCode == RCODE_CLIP_IMAGE) {
                clipImageBack(data.getExtras());
            }
        }
    }

    private void selectPickBack(Bundle bundle) {

        if (bundle != null) {
            List<String> photos = bundle.getStringArrayList(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
            mSelectedPhotos.clear();
            mSelectedPhotos.addAll(photos);
            // TODO: 2017/11/22 开启裁剪的界面做裁剪
            startActivityForResult(ClipImageActivity.getIntent(this, mSelectedPhotos.get(0)), RCODE_CLIP_IMAGE);
        }
    }

    private void takePhotoBack() {
        //have add picture image
        mSelectedPhotos.clear();
        mSelectedPhotos.add(mCurrentPhotoPath);
        startActivityForResult(ClipImageActivity.getIntent(this, mSelectedPhotos.get(0)), RCODE_CLIP_IMAGE);
    }

    private String clipImagePath;

    private void clipImageBack(Bundle bundle) {
        if (clipImagePath != null) {
            //说明之前有裁剪的图片，删除掉
            DeleteFileUtil.deletePic(this,clipImagePath);
        }
        clipImagePath = bundle.getString(ClipImageActivity.KEY_CLIP_IMAGE);
        if (clipImagePath != null) {

            RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
            options.override(ivHead.getWidth());
            Glide.with(this).load(clipImagePath).apply(options).into(ivHead);
        }
    }
    //---------------------拍照、选择照片，裁剪-------------------


    @Override
    protected void onDestroy() {
        EventBus.getDefault().post(new FillPersonalInfoEvent());
        super.onDestroy();
    }
}
