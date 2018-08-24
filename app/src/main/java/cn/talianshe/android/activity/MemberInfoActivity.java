package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.MemberStudentData;
import cn.talianshe.android.bean.MemberTutorData;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.ScaleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * @author zcm
 * @ClassName: MemberInfoActivity
 * @Description: 成员信息
 * @date 2017/11/22 14:27
 */
public class MemberInfoActivity extends BaseActivity implements View.OnClickListener {


    @BindView(R.id.iv_dim_head)
    ImageView ivDimHead;
    @BindView(R.id.iv_head)
    ScaleImageView ivHead;
    @BindView(R.id.iv_gender)
    ImageView ivGender;

    private static final String EXTRA_TYPE = "extra_type";
    private static final String EXTRA_ID = "extra_id";
    public static final int TYPE_STUDENT = 1;
    public static final int TYPE_TEACHER = 2;
    @BindView(R.id.ll_teacher)
    LinearLayout llTeacher;
    @BindView(R.id.ll_student)
    LinearLayout llStudent;
    @BindView(R.id.ll_teacher_school)
    LinearLayout llTeacherCchool;
    @BindView(R.id.tv_teacher_name)
    TextView tvTeacherName;
    @BindView(R.id.tv_teacher_job_id)
    TextView tvTeacherJobId;
    @BindView(R.id.tv_duty_name)
    TextView tvDutyName;
    @BindView(R.id.tv_teacher_school)
    TextView tvTeacherSchool;
    @BindView(R.id.tv_experience)
    TextView tvExperience;
    @BindView(R.id.tv_student_name)
    TextView tvStudentName;
    @BindView(R.id.tv_student_mobile)
    TextView tvStudentMobile;
    @BindView(R.id.tv_student_class)
    TextView tvStudentClass;
    @BindView(R.id.tv_student_school)
    TextView tvStudentSchool;
    private int curType;
    private String memberId;

    public static Intent getMemberInfoIntent(Context context, int memberType, String memberId) {
        Intent intent = new Intent(context, MemberInfoActivity.class);
        intent.putExtra(EXTRA_TYPE, memberType);
        intent.putExtra(EXTRA_ID, memberId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_info);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        curType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_STUDENT);
        memberId = getIntent().getStringExtra(EXTRA_ID);
        if (curType == TYPE_STUDENT) {
            setTitle(R.string.member_info);
            llStudent.setVisibility(View.VISIBLE);
            llTeacher.setVisibility(View.GONE);
            llTeacherCchool.setVisibility(View.VISIBLE);

        } else {
            setTitle(R.string.tutor_info);
            llTeacher.setVisibility(View.VISIBLE);
            llStudent.setVisibility(View.GONE);
            llTeacherCchool.setVisibility(View.GONE);
        }

        int width = getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams layoutParams = ivDimHead.getLayoutParams();
        if (layoutParams == null)
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, layoutParams.WRAP_CONTENT);
        layoutParams.width = width;
        layoutParams.height = width * 4 / 9;
        ivDimHead.setLayoutParams(layoutParams);

        requestData();
    }

    private MemberTutorData.TutorInfo tutorInfo;
    private MemberStudentData.StudentInfo studentInfo;

    private void requestData() {
        MyProgressDialog.show(this);
        if (curType == TYPE_TEACHER) {

            HttpSubscriber httpSubscriber = new HttpSubscriber<MemberTutorData>(this) {
                @Override
                public void onSuccess(MemberTutorData data) {
                    MyProgressDialog.dismiss();
                    tutorInfo = data.result;
                    if (!TextUtils.isEmpty(tutorInfo.avatar)) {
                        RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
                        options.override(getResources().getDrawable(R.mipmap.personal_head_default).getIntrinsicWidth());
                        options.placeholder(R.mipmap.personal_head_default);
                        options.error(R.mipmap.personal_head_default);
                        Glide.with(MemberInfoActivity.this)
                                .load(TLSUrl.BASE_URL + tutorInfo.avatar)
                                .apply(options)
                                .into(ivHead);
                    }
                    ivGender.setVisibility(TextUtils.isEmpty(tutorInfo.sex)?View.GONE:View.VISIBLE);
                    ivGender.setImageResource("0".equals(tutorInfo.sex) ? R.mipmap.female : R.mipmap.male);
                    tvTeacherName.setText(tutorInfo.realname);
                    tvDutyName.setText(tutorInfo.titleName);
                    tvTeacherJobId.setText(TextUtils.isEmpty(tutorInfo.teacherId) ? "****" : tutorInfo.teacherId);
                    tvExperience.setText(TextUtils.isEmpty(tutorInfo.workExperience) ? "暂无" : tutorInfo.workExperience);
                }

                @Override
                public void onError(String msg) {
                    MyProgressDialog.dismiss();
                    super.onError(msg);
                }
            };
            RequestEngine.getInstance().getServer(SchoolApiService.class).getTeacherDetail(memberId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
        } else {
            HttpSubscriber httpSubscriber = new HttpSubscriber<MemberStudentData>(this) {
                @Override
                public void onSuccess(MemberStudentData data) {
                    MyProgressDialog.dismiss();
                    studentInfo = data.result;
                    if (!TextUtils.isEmpty(studentInfo.avatar)) {
                        Glide.with(MemberInfoActivity.this)
                                .load(TLSUrl.BASE_URL + studentInfo.avatar)
                                .apply(RequestOptions.bitmapTransform(new BlurTransformation(14)))
                                .into(ivDimHead);
                        RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
                        options.override(getResources().getDrawable(R.mipmap.personal_head_default).getIntrinsicWidth());
                        options.placeholder(R.mipmap.personal_head_default);
                        options.error(R.mipmap.personal_head_default);
                        Glide.with(MemberInfoActivity.this)
                                .load(TLSUrl.BASE_URL + studentInfo.avatar)
                                .apply(options)
                                .into(ivHead);
                    }
                    ivGender.setVisibility(TextUtils.isEmpty(studentInfo.sex)?View.GONE:View.VISIBLE);
                    ivGender.setImageResource("0".equals(studentInfo.sex) ? R.mipmap.female : R.mipmap.male);
                    tvStudentName.setText(studentInfo.realname);
                    if (!TextUtils.isEmpty(studentInfo.mobile)) {
                        if ("1".equals(studentInfo.isMobile)) {
                            tvStudentMobile.setText(studentInfo.mobile);
                        } else {
                            if (studentInfo.mobile.length() < 8) {
                                tvStudentMobile.setText(studentInfo.mobile);
                            } else {
                                tvStudentMobile.setText(studentInfo.mobile.substring(0, 4) + "***" + studentInfo.mobile.substring(studentInfo.mobile.length() - 4, studentInfo.mobile.length()));
                            }
                        }
                    }
                    tvStudentClass.setText(studentInfo.grade + studentInfo.majors);
                    tvStudentSchool.setText(studentInfo.schoolName);
                }

                @Override
                public void onError(String msg) {
                    MyProgressDialog.dismiss();
                    super.onError(msg);
                }
            };
            RequestEngine.getInstance().getServer(SchoolApiService.class).getStudentDetail(memberId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

        }
    }

}
