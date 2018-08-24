package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.CreateAssociationData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.TutorListData;
import cn.talianshe.android.bean.UploadData;
import cn.talianshe.android.eventbus.CreateAssociationPageFinishEvent;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.MultipartUtil;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.AssociationApiService;
import cn.talianshe.android.net.service.UploadLoadApiService;
import cn.talianshe.android.utils.DeleteFileUtil;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.OnRecyclerItemClickListener;
import cn.talianshe.android.widget.ScaleImageView;
import library.talianshe.android.photobrowser.FileUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;

/**
 * @author zcm
 * @ClassName: CreateAssociationDataActivity
 * @Description: 创建社团第二步导师选择
 * @date 2017/11/2317:45
 */
public class CreateAssociationTutorActivity extends BaseActivity implements View.OnClickListener {


    @BindView(R.id.tv_choose_tutor)
    TextView tvChooseTutor;
    @BindView(R.id.ll_choose_tutor)
    LinearLayout llChooseTutor;
    @BindView(R.id.swiperv_tutors)
    SwipeMenuRecyclerView swipervTutors;
    private static final String EXTRA_PARCEL = "extra_parcel";

    public static Intent getSecondStepIntent(Context context, CreateAssociationData data) {
        Intent intent = new Intent(context, CreateAssociationTutorActivity.class);
        intent.putExtra(EXTRA_PARCEL, Parcels.wrap(data));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_association_tutor);
        ButterKnife.bind(this);
        initData();
    }

    private CreateAssociationData data;

    private void initData() {
        setTitle(R.string.create_association);
        data = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PARCEL));
        System.out.println(data.toString());
        SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(swipeRightMenu.getContext())
                        .setBackground(R.drawable.swipe_menu_red_bg_selector)
                        .setText(R.string.delete)
                        .setTextColor(Color.WHITE)
                        .setTextSize(16)
                        .setWidth(DensityUtils.dipTopx(swipeRightMenu.getContext(), 64))
                        .setHeight(ViewGroup.LayoutParams.MATCH_PARENT); // 各种文字和图标属性设置。
                swipeRightMenu.addMenuItem(deleteItem); // 在Item左侧添加一个菜单。
            }
        };
        swipervTutors.setSwipeMenuItemClickListener(new SwipeMenuItemClickListener() {
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge) {
                //关掉侧边栏
                menuBridge.closeMenu();
                //侧滑菜单的点击事件
                //删除该条目
                int removePosition = menuBridge.getAdapterPosition();
                if (lastCheckedBox != null && selectedTutorIndex == removePosition) {
                    selectedTutorIndex = -1;
                    lastCheckedBox = null;
                }
                if (removePosition < selectedTutorIndex) {
                    selectedTutorIndex--;
                }
                System.out.println("menuBridge.getPosition()" + menuBridge.getPosition());
                tutors.remove(removePosition);
                if (tutors.size() > 0) {
                    if (tutors.size() == 1) {
                        selectedTutorIndex = 0;
                    }
                    tvChooseTutor.setText(getString(R.string.choose_tutor_holder, tutors.size()));
                } else {
                    tvChooseTutor.setText(R.string.association_choose_tutor_tip);
                }
                swipervTutors.getAdapter().notifyDataSetChanged();
            }
        });
        swipervTutors.setSwipeMenuCreator(swipeMenuCreator);
        swipervTutors.setLayoutManager(new LinearLayoutManager(this));
        swipervTutors.setAdapter(new MyTutorListAdapter());
    }

    private List<TutorListData.Tutor> tutors = new ArrayList<>();

    public class MyTutorListAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_create_association_tutor_list, null);
            TutorListViewHolder holder = new TutorListViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TutorListViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return tutors.size();
        }

    }

    private CheckBox lastCheckedBox;
    private int selectedTutorIndex = -1;

    public class TutorListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_head)
        ScaleImageView ivHead;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_job)
        TextView tvJob;
        @BindView(R.id.cb_set_leader)
        CheckBox cbSetLeader;

        TutorListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            setListener();
        }

        private void setListener() {
        }


        public void bindData(final int position) {
            cbSetLeader.setTag(position);
            cbSetLeader.setChecked(position == selectedTutorIndex);
            TutorListData.Tutor tutor = tutors.get(position);
            if (!TextUtils.isEmpty(tutor.avatar)) {
                RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
                options.override(itemView.getContext().getDrawable(R.mipmap.default_head).getIntrinsicWidth());
                options.error(R.mipmap.default_head);
                Glide.with(itemView.getContext()).load(TLSUrl.BASE_URL + tutor.avatar).apply(options).into(ivHead);
            } else {
                ivHead.setImageResource(R.mipmap.default_head);
            }
            tvName.setText(tutor.name);
            tvJob.setText(tutor.titleName + "/" + tutor.schoolName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedTutorIndex == position) {
                        return;
                    } else {
                        if (lastCheckedBox != null)
                            lastCheckedBox.setChecked(false);
                        lastCheckedBox = cbSetLeader;
                        selectedTutorIndex = (int) lastCheckedBox.getTag();
                        cbSetLeader.setChecked(true);
                    }
                }
            });
        }
    }

    public final static int RCODE_CHOOSE_TUTOR = 700;


    @OnClick({R.id.ll_choose_tutor, R.id.btn_next_step})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.ll_choose_tutor) {
            startActivityForResult(new Intent(this, ChooseTutorActivity.class), RCODE_CHOOSE_TUTOR);

        } else {
            //提交创建
            if (selectedTutorIndex < 0) {
                MyToast.show(R.string.leader_tutor_null_tip, this);
                return;
            }
            if (tutors != null && tutors.size() == 0) {
                MyToast.show(R.string.leader_tutor_null_tip, this);
                return;
            }
            createAssociation();
        }
    }

    private void createAssociation() {
        MyProgressDialog.show(this);
        HttpSubscriber uploadSubscriber = new HttpSubscriber<UploadData>(this) {

            @Override
            public void onSuccess(UploadData uploadData) {
                AssociationApiService associationApiService = RequestEngine.getInstance().getServer(AssociationApiService.class);
                HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(CreateAssociationTutorActivity.this) {
                    @Override
                    public void onSuccess(StringData strData) {
                        //对集合数据按照拼音进行排序
                        MyProgressDialog.dismiss();
                        EventBus.getDefault().post(new CreateAssociationPageFinishEvent());
                        startActivity(new Intent(CreateAssociationTutorActivity.this, CreateAssociationApplySuccessActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String msg) {
                        super.onError(msg);
                        MyProgressDialog.dismiss();
                    }
                };
                String tutorIds = "";
                Integer selectedIndex = 0;
                if (tutors.size() > 1) {
                    selectedIndex = (Integer) lastCheckedBox.getTag();

                    for (int i = 0; i < tutors.size(); i++) {
                        if (i != selectedIndex) {
                            tutorIds += tutors.get(i).id + ",";
                        }
                    }
                    tutorIds = tutorIds.substring(0, tutorIds.length() - 1);
                }
                associationApiService.createAssociation(uploadData.result.id, data.level, data.name, data.slogan, data.labels, data.desc, data.function, data.vision, data.plan, data.num, tutors.get(selectedIndex).id, tutors.get(selectedIndex).name, tutorIds).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

            }

            @Override
            public void onError(Throwable e) {
                MyProgressDialog.dismiss();
                super.onError(e);
            }

        };
        UploadLoadApiService uploadLoadApiService = RequestEngine.getInstance().getServer(UploadLoadApiService.class);
        MultipartBody.Builder builder = MultipartUtil.filesToMultipartBodyBuilder("file", new String[]{data.logo}, MediaType.parse("multipart/form-data"));
        uploadLoadApiService.uploadImage(builder.build()).compose(RxSchedulersHelper.io_main()).subscribe(uploadSubscriber);


    }

    @Subscribe
    public void onReceiveTutorsEvent(ArrayList<TutorListData.Tutor> selectedTutors) {
        if (selectedTutors != null && selectedTutors.size() > 0) {
//            tutors.clear();
//            tutors.addAll(selectedTutors);
            List<TutorListData.Tutor> allTutors = new ArrayList<>();
            allTutors.addAll(tutors);
            allTutors.addAll(selectedTutors);
            tutors.clear();
            tutors.addAll(new ArrayList<>(new LinkedHashSet<>(allTutors)));
            swipervTutors.getAdapter().notifyDataSetChanged();
            if (tutors.size() == 1) {
                selectedTutorIndex = 0;
            } else {
                selectedTutorIndex = -1;
            }
            tvChooseTutor.setText(getString(R.string.choose_tutor_holder, tutors.size()));
        }
    }
}
