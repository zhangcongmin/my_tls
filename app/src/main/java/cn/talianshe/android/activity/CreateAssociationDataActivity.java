package cn.talianshe.android.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

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
import cn.talianshe.android.bean.CreateAssociationData;
import cn.talianshe.android.eventbus.CreateAssociationPageFinishEvent;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.utils.DeleteFileUtil;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.widget.MyEditTextDialog;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: CreateAssociationDataActivity
 * @Description: 创建社团
 * @date 2017/11/22 17:45
 */
public class CreateAssociationDataActivity extends BaseActivity implements View.OnClickListener {


    @BindView(R.id.iv_school_level)
    ImageView ivSchoolLevel;
    @BindView(R.id.tv_school_level)
    TextView tvSchoolLevel;
    @BindView(R.id.ll_school_level)
    LinearLayout llSchoolLevel;
    @BindView(R.id.iv_college_level)
    ImageView ivCollegeLevel;
    @BindView(R.id.tv_college_level)
    TextView tvCollegeLevel;
    @BindView(R.id.ll_college_level)
    LinearLayout llCollegeLevel;
    @BindView(R.id.ll_tags)
    LinearLayout llTags;
    @BindView(R.id.iv_association_logo)
    ImageView ivAssociationLogo;
    @BindView(R.id.ll_association_logo)
    LinearLayout llAssociationLogo;
    @BindView(R.id.ll_tags_content)
    LinearLayout llTagsContent;
    @BindView(R.id.tv_choose_tag)
    TextView tvChooseTag;
    @BindView(R.id.tv_association_desc)
    TextView tvAssociationDesc;
    @BindView(R.id.tv_association_function)
    TextView tvAssociationFunction;
    @BindView(R.id.tv_association_slogan)
    TextView tvAssociationSlogan;
    @BindView(R.id.tv_association_vision)
    TextView tvAssociationVision;
    @BindView(R.id.tv_association_plan)
    TextView tvAssociationPlan;
    @BindView(R.id.et_association_name)
    EditText etAssociationName;
    @BindView(R.id.et_association_number)
    EditText etAssociationNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_association_data);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.create_association);
        llSchoolLevel.setSelected(true);
        ivSchoolLevel.setVisibility(View.VISIBLE);
        llCollegeLevel.setSelected(false);
        ivCollegeLevel.setVisibility(View.INVISIBLE);
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

    public final static int RCODE_PICK_PICTURE = 700;
    public final static int RCODE_TAKE_PHOTO = 800;
    public final static int RCODE_CLIP_IMAGE = 900;
    public final static int RCODE_CHOOSE_TAGS = 1000;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RCODE_PICK_PICTURE) {
                selectPickBack(data.getExtras());
            } else if (requestCode == RCODE_TAKE_PHOTO) {
                takePhotoBack();
            } else if (requestCode == RCODE_CLIP_IMAGE) {
                clipImageBack(data.getExtras());
            } else if (requestCode == RCODE_CHOOSE_TAGS) {
                // TODO: 2017/11/23 选择标签回来

                selectLabelBack(data);

            }
        }
    }
    private ArrayList<String> labels = new ArrayList<>();
    private void selectLabelBack(Intent data) {
        ArrayList<String> labelResults = data.getStringArrayListExtra(ChooseAssociationTagActivity.EXTRA_TAGS);
        if(labelResults.size() == 0){
            tvChooseTag.setVisibility(labels.size() == 0?View.VISIBLE:View.GONE);
        }else{
            labels.clear();
            llTagsContent.removeAllViews();
            labels.addAll(labelResults);
            for(String label:labels){
                TextView tvLabel =new TextView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.leftMargin = DensityUtils.dipTopx(this,3);
                tvLabel.setText(label);
                tvLabel.setTextSize(12);
                tvLabel.setBackgroundResource(R.drawable.association_tag_bg);
                tvLabel.setTextColor(ContextCompat.getColor(this,R.color.theme_color));
                llTagsContent.addView(tvLabel);
            }
        }
        if(labels.size() == 0){
            tvChooseTag.setVisibility(View.VISIBLE);
        }else{
            tvChooseTag.setVisibility(View.GONE);


        }
    }

    private String clipImagePath;

    private void clipImageBack(Bundle bundle) {
        if (clipImagePath != null) {
            //说明之前有裁剪的图片，删除掉
            DeleteFileUtil.deletePic(this,clipImagePath);
        }
        clipImagePath = bundle.getString(ClipImageActivity.KEY_CLIP_IMAGE);
        if (clipImagePath != null) {

            RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(DensityUtils.dipTopx(this, 2)));
            options.override(ivAssociationLogo.getWidth());
            Glide.with(this).load(clipImagePath).apply(options).into(ivAssociationLogo);
        }
    }

    private List<String> mSelectedPhotos = new ArrayList();

    private void selectPickBack(Bundle bundle) {

        if (bundle != null) {
            List<String> photos = bundle.getStringArrayList(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
            mSelectedPhotos.clear();
            mSelectedPhotos.addAll(photos);
            // TODO: 2017/11/22 开启裁剪的界面做裁剪
            startActivityForResult(ClipImageActivity.getIntent(this, mSelectedPhotos.get(0)), RCODE_CLIP_IMAGE);
        }
    }

    String mCurrentPhotoPath;
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

    private void takePhotoBack() {
        //have add picture image
        mSelectedPhotos.clear();
        mSelectedPhotos.add(mCurrentPhotoPath);
        startActivityForResult(ClipImageActivity.getIntent(this, mSelectedPhotos.get(0)), RCODE_CLIP_IMAGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clipImagePath != null) {
            DeleteFileUtil.deletePic(this,clipImagePath);
        }
    }

    @OnClick({R.id.ll_school_level, R.id.ll_college_level, R.id.ll_association_logo, R.id.ll_tags, R.id.ll_association_slogan, R.id.ll_association_desc, R.id.ll_association_function, R.id.ll_association_vision, R.id.ll_association_plan, R.id.btn_next_step})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_school_level:
                if (llSchoolLevel.isSelected()) {
                    return;
                }
                llSchoolLevel.setSelected(true);
                ivSchoolLevel.setVisibility(View.VISIBLE);
                llCollegeLevel.setSelected(false);
                ivCollegeLevel.setVisibility(View.INVISIBLE);
                break;
            case R.id.ll_college_level:
                if (llCollegeLevel.isSelected()) {
                    return;
                }
                llCollegeLevel.setSelected(true);
                ivCollegeLevel.setVisibility(View.VISIBLE);
                llSchoolLevel.setSelected(false);
                ivSchoolLevel.setVisibility(View.INVISIBLE);
                break;
            case R.id.ll_association_logo:
                startActivityForResult(PhotoPickerActivity.getSinglePicIntent(this), RCODE_PICK_PICTURE);
                break;
            case R.id.ll_tags:
                Intent intent = new Intent(this, ChooseAssociationTagActivity.class);
                intent.putStringArrayListExtra(ChooseAssociationTagActivity.EXTRA_TAGS,labels);
                startActivityForResult(intent, RCODE_CHOOSE_TAGS);
                break;
            case R.id.ll_association_slogan:
//                MyProgressDialog.show(this);
                showEditDialog(view.getId(), "", "社团口号", "最多可输入200个字符", 4, 200);
                break;
            case R.id.ll_association_desc:
                showEditDialog(view.getId(), "", "社团简介", "最多可输入200个字符", 4, 200);
                break;
            case R.id.ll_association_function:
                showEditDialog(view.getId(), "", "社团职能", "最多可输入200个字符", 4, 200);
                break;
            case R.id.ll_association_vision:
                showEditDialog(view.getId(), "", "社团构想", "最多可输入200个字符", 4, 200);
                break;
            case R.id.ll_association_plan:
                showEditDialog(view.getId(), "", "社团规划", "最多可输入200个字符", 4, 200);
                break;
            case R.id.btn_next_step:
                checkData();
                break;
            default:
                break;
        }
    }

    private void checkData() {
        if(TextUtils.isEmpty(clipImagePath)){
            MyToast.show(R.string.association_logo_null_tip,this);
            return;
        }
        if(TextUtils.isEmpty(etAssociationName.getText().toString())){
            MyToast.show(R.string.input_association_name_tip,this);
            return;
        }
        if(TextUtils.isEmpty(tvAssociationSlogan.getText().toString())||getString(R.string.input_association_slogan_tip).equals(tvAssociationSlogan.getText().toString())){
            MyToast.show(R.string.association_slogan_null_tip,this);
            return;
        }
        if(labels.size() == 0){
            MyToast.show(R.string.input_association_tag_tip,this);
            return;
        }
        if(TextUtils.isEmpty(etAssociationNumber.getText().toString())){
            MyToast.show(R.string.association_num_null_tip,this);
            return;
        }


        if(TextUtils.isEmpty(tvAssociationDesc.getText().toString())||getString(R.string.input_association_desc_tip).equals(tvAssociationDesc.getText().toString())){
            MyToast.show(R.string.input_association_desc_tip,this);
            return;
        }
        if(TextUtils.isEmpty(tvAssociationFunction.getText().toString())||getString(R.string.input_association_function_tip).equals(tvAssociationFunction.getText().toString())){
            MyToast.show(R.string.input_association_function_tip,this);
            return;
        }
        if(TextUtils.isEmpty(tvAssociationVision.getText().toString())||getString(R.string.input_association_vision_tip).equals(tvAssociationVision.getText().toString())){
            MyToast.show(R.string.input_association_vision_tip,this);
            return;
        }
        if(TextUtils.isEmpty(tvAssociationPlan.getText().toString())||getString(R.string.input_association_plan_tip).equals(tvAssociationPlan.getText().toString())){
            MyToast.show(R.string.input_association_vision_tip,this);
            return;
        }

        CreateAssociationData data = new CreateAssociationData();
        String label = "";
        for(String s :labels){
            label+=s+",";
        }
        label = label.substring(0,label.length()-1);
        data.labels = label;
        data.logo = clipImagePath;
        data.level =
        data.name = etAssociationName.getText().toString();
        data.function = tvAssociationFunction.getText().toString();
        data.num = etAssociationNumber.getText().toString();
        data.level = llSchoolLevel.isSelected()?"1":"0";
        data.slogan = tvAssociationSlogan.getText().toString();
        data.desc = tvAssociationDesc.getText().toString();
        data.function = tvAssociationFunction.getText().toString();
        data.vision = tvAssociationVision.getText().toString();
        data.plan = tvAssociationPlan.getText().toString();
        startActivity(CreateAssociationTutorActivity.getSecondStepIntent(this,data));
    }

    private int curSelectedId;
    private MyEditTextDialog dialog;

    private void showEditDialog(int id, String defaultText, String info, String hint, int lines, int maxLength) {
        curSelectedId = id;
        dialog = new MyEditTextDialog(this, info, defaultText, hint, lines, maxLength).builder();
        dialog.setResultListener(new MyEditTextDialog.EditTextResultListener() {
            @Override
            public void onResult(String result) {
                switch (curSelectedId) {
                    case R.id.ll_association_desc:
                        tvAssociationDesc.setText(result);
                        break;
                    case R.id.ll_association_function:
                        tvAssociationFunction.setText(result);
                        break;
                    case R.id.ll_association_vision:
                        tvAssociationVision.setText(result);
                        break;
                    case R.id.ll_association_plan:
                        tvAssociationPlan.setText(result);
                        break;
                    case R.id.ll_association_slogan:
                        tvAssociationSlogan.setText(result);
                        break;
                    default:
                        break;
                }
            }
        });
        dialog.show();
    }

    @Subscribe
    public void onReceiveFinishEvent(CreateAssociationPageFinishEvent event){
        finish();
    }
}
