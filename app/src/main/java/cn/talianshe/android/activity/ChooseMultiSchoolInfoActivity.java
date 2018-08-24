package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.DepartMentMajorClassListData;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.UserApiService;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: PrivacySettingActivity
 * @Description: 选择籍贯
 * @date 2017/12/7 19:53
 */
public class ChooseMultiSchoolInfoActivity extends BaseActivity {

    @BindView(R.id.rv_share_list)
    RecyclerView rvList;
    @BindView(R.id.tv_value)
    TextView tvValue;
    @BindView(R.id.tv_key)
    TextView tvKey;

    public static final int TYPE_COLLEGE = 1;
    public static final int TYPE_DEPARTMENT = 2;
    public static final int TYPE_MAJOR = 3;
    public static final int TYPE_CLASS = 4;
    private static String EXTRA_INFO_TYPE = "extar_info_type";
    private static String EXTRA_ID = "extra_id";
    private int curInfoType;
    private List<DepartMentMajorClassListData.DepartmentMajorClassInfo> collegeInfos;
    private DepartMentMajorClassListData.DepartmentMajorClassInfo selectedCollegeInfo;
    private String id;
    private List<DepartMentMajorClassListData.DepartmentMajorClassInfo> departmentMajorClassInfos;

    public static Intent getSchoolInfoIntent(Context context, int infoType, String id) {
        Intent intent = new Intent(context, ChooseMultiSchoolInfoActivity.class);
        intent.putExtra(EXTRA_INFO_TYPE, infoType);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_multi_school_info);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        curInfoType = getIntent().getIntExtra(EXTRA_INFO_TYPE, TYPE_COLLEGE);
        id = getIntent().getStringExtra(EXTRA_ID);
        switch (curInfoType) {
            case TYPE_COLLEGE:
                setTitle(R.string.choose_department);
                tvKey.setText(R.string.department);
                btnRight.setVisibility(View.GONE);
                break;
            case TYPE_MAJOR:
                setTitle(R.string.choose_major);
                tvKey.setText(R.string.major);
                break;
            case TYPE_CLASS:
                setTitle(R.string.choose_class);
                tvKey.setText(R.string.class_in_grade);
                break;
        }
        btnRight.setText(R.string.confirm);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curSelectIndex == -1) {
                    MyToast.show(R.string.pls_choose, ChooseMultiSchoolInfoActivity.this);
                    return;
                }
                DepartMentMajorClassListData.DepartmentMajorClassInfo event = new DepartMentMajorClassListData.DepartmentMajorClassInfo();
                DepartMentMajorClassListData.DepartmentMajorClassInfo curInfo = departmentMajorClassInfos.get(curSelectIndex);
                switch (curInfoType) {

                    case TYPE_DEPARTMENT:
                        event.departmentId = curInfo.departmentId;
                        event.departmentName = tvValue.getText().toString();
                    case TYPE_MAJOR:
                        event.majorId = curInfo.majorId;
                        event.majorName = tvValue.getText().toString();
                        break;
                    case TYPE_CLASS:
                        event.classId = curInfo.classId;
                        event.className = tvValue.getText().toString();
                        break;
                }
                EventBus.getDefault().post(event);
                finish();
            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(this));

        requestData();


    }

    private void requestData() {
        MyProgressDialog.show(this);
        HttpSubscriber httpSubscriber = new HttpSubscriber<DepartMentMajorClassListData>(this) {
            @Override
            public void onSuccess(DepartMentMajorClassListData listData) {
                MyProgressDialog.dismiss();
                if (curInfoType == TYPE_COLLEGE) {
                    collegeInfos = listData.result.list;
                    if (collegeInfos == null || collegeInfos.size() == 0) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        llContent.setVisibility(View.GONE);
                    } else {
                        rvList.setAdapter(new MySchoolInfoAdapter(collegeInfos));
                    }
                } else {

                    departmentMajorClassInfos = listData.result.list;
                    if (departmentMajorClassInfos == null || departmentMajorClassInfos.size() == 0) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        llContent.setVisibility(View.GONE);
                    } else {
                        mySchoolInfoAdapter = new MySchoolInfoAdapter(departmentMajorClassInfos);
                        rvList.setAdapter(mySchoolInfoAdapter);
                    }
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        UserApiService userApiService = RequestEngine.getInstance().getServer(UserApiService.class);
        switch (curInfoType) {
            case TYPE_COLLEGE:
                userApiService.getCollegeList().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
                break;
            case TYPE_DEPARTMENT:
                userApiService.getDepartmentList(selectedCollegeInfo.collegeId).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
                break;
            case TYPE_MAJOR:
                userApiService.getMajorList(id).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
                break;
            case TYPE_CLASS:
                userApiService.getClassList(id).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
                break;
        }
    }

    private MySchoolInfoAdapter mySchoolInfoAdapter;
    private int curSelectIndex = -1;

    private class MySchoolInfoAdapter extends RecyclerView.Adapter {

        List<DepartMentMajorClassListData.DepartmentMajorClassInfo> infos = new ArrayList<>();

        public MySchoolInfoAdapter(List<DepartMentMajorClassListData.DepartmentMajorClassInfo> infos) {
            this.infos.addAll(infos);
        }

        @Override

        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multi_school_info_list, parent, false);
            MySchoolInfoViewHolder viewHolder = new MySchoolInfoViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MySchoolInfoViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return infos.size();
        }
    }

    public class MySchoolInfoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_item)
        TextView tvItem;

        public MySchoolInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(final int postion) {
            DepartMentMajorClassListData.DepartmentMajorClassInfo info = curInfoType == TYPE_COLLEGE ? collegeInfos.get(postion) : departmentMajorClassInfos.get(postion);
            String curInfo = null;
            switch (curInfoType) {
                case TYPE_COLLEGE:
                    curInfo = info.collegeName;
                    break;
                case TYPE_DEPARTMENT:
//                    curInfo = selectedCollegeInfo.collegeName + " " + info.departmentName;
                    curInfo =  info.departmentName;
                    break;
                case TYPE_MAJOR:
                    curInfo = info.majorName;
                    break;
                case TYPE_CLASS:
                    curInfo = info.year + info.gradeName;
                    break;
            }
            tvItem.setText(curInfo);
            final String finalCurInfo = curInfo;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvValue.setText(curInfoType == TYPE_DEPARTMENT?selectedCollegeInfo.collegeName+" "+finalCurInfo:finalCurInfo);
                    if (curInfoType == TYPE_COLLEGE) {
                        curInfoType = TYPE_DEPARTMENT;
                        selectedCollegeInfo = collegeInfos.get(postion);
                        btnRight.setVisibility(View.VISIBLE);
                        requestData();
                    } else {
                        curSelectIndex = postion;
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (curInfoType == TYPE_DEPARTMENT) {
            resetCollegeInfo();
        } else {
            super.onBackPressed();
        }
    }

    private void resetCollegeInfo() {
        curInfoType = TYPE_COLLEGE;
        rvList.setAdapter(new MySchoolInfoAdapter(collegeInfos));
        tvValue.setText("");
        tvEmpty.setVisibility(View.GONE);
        llContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_left:
                if (curInfoType == TYPE_DEPARTMENT) {
                    resetCollegeInfo();
                } else {
                    finish();
                }

                break;
            default:
                super.onClick(view);
                break;
        }
    }
}
