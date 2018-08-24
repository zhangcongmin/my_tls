package cn.talianshe.android.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.AssociationMemberListData;
import cn.talianshe.android.bean.ContactAssociationListData;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.ScaleImageView;

/**
 * @author zcm
 * @ClassName: WhoCanSeeActivity
 * @Description: 查看范围
 * @date 2017/12/7 16:24
 */
public class WhoCanSeeActivity extends BaseActivity {

    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.rv_associations)
    RecyclerView rvAssociations;
    @BindView(R.id.cb_all)
    CheckBox cbAll;
    @BindView(R.id.ll_all_association)
    LinearLayout llAllAssociation;
    @BindView(R.id.rv_association_result)
    RecyclerView rvAssociationResult;
    @BindView(R.id.ll_all_content)
    LinearLayout llAllContent;
    @BindView(R.id.ll_all)
    LinearLayout llAll;
    @BindView(R.id.rv_search_result)
    RecyclerView rvSearchResult;
    @BindView(R.id.tv_search_null)
    TextView tvSearchNull;
    private MyAssociationAdapter myAssociationAdapter;
    private MyResultsAdapter myResultsAdapter;
    private MyResultsAdapter mySearchResultsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_can_see);
        ButterKnife.bind(this);
        initData();
    }

    private List<ContactAssociationListData.ContactAssociation> associationList = new ArrayList<>();
    private String curAssociationId;
    private Map<String, List<AssociationMemberListData.AssociationMember>> associationMemberMap = new HashMap<>();
    private Map<String, Boolean> allSelectMap = new HashMap<>();

    private int selectedAssociationIndex = -1;

    private List<String> selectIds = new ArrayList<>();

    private List<AssociationMemberListData.AssociationMember> searchResults = new ArrayList<>();
    private SchoolApiService schoolApiService;

    private void initData() {
        setTitle(R.string.who_can_see);
        btnRight.setText(R.string.complete);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2017/12/7 完成
                List<AssociationMemberListData.AssociationMember> memberList = new ArrayList<>();
                for (String in : associationMemberMap.keySet()) {
                    //map.keySet()返回的是所有key的值
                    List<AssociationMemberListData.AssociationMember> list = associationMemberMap.get(in);//得到每个key多对用value的值
                    for (AssociationMemberListData.AssociationMember member : list) {
                        if (member.isSelected && !selectIds.contains(member.id)) {
                            selectIds.add(member.id);
                            memberList.add(member);
                            System.out.println(member.toString());
                        }
                    }
                }
                EventBus.getDefault().post(memberList);
                finish();
            }
        });
        rvAssociations.setLayoutManager(new LinearLayoutManager(this));
        rvAssociationResult.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResult.setLayoutManager(new LinearLayoutManager(this));

        myAssociationAdapter = new MyAssociationAdapter();
        rvAssociations.setAdapter(myAssociationAdapter);

        myResultsAdapter = new MyResultsAdapter(false);
        rvAssociationResult.setAdapter(myResultsAdapter);

        rvSearchResult.setVisibility(View.GONE);
        mySearchResultsAdapter = new MyResultsAdapter(true);
        rvSearchResult.setAdapter(mySearchResultsAdapter);        //添加搜索
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    searchResults.clear();
                    llAll.setVisibility(View.VISIBLE);
                    rvSearchResult.setVisibility(View.GONE);
                    rvAssociations.setVisibility(View.VISIBLE);
                    rvAssociationResult.setVisibility(View.VISIBLE);
                    llAllAssociation.setVisibility(View.VISIBLE);
                } else {

                    searchResults = getSearchResult(s.toString(), associationMemberMap.get(curAssociationId));
                    if (searchResults.size() == 0) {
                        tvSearchNull.setVisibility(View.VISIBLE);
                        llAll.setVisibility(View.GONE);
                    } else {
                        tvSearchNull.setVisibility(View.GONE);
                        llAll.setVisibility(View.VISIBLE);
                        rvSearchResult.setVisibility(View.VISIBLE);
                        rvAssociations.setVisibility(View.GONE);
                        rvAssociationResult.setVisibility(View.GONE);
                        llAllAssociation.setVisibility(View.GONE);
                        mySearchResultsAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        getContactAssociationList();

    }


    private void getContactAssociationList() {
        if (TextUtils.isEmpty(GlobalParams.TOKEN))
            return;
        MyProgressDialog.show(this, false);
        HttpSubscriber associationListSubscrober = new HttpSubscriber<ContactAssociationListData>(this) {
            @Override
            public void onSuccess(ContactAssociationListData listData) {
                MyProgressDialog.dismiss();
                associationList.clear();
                if (listData.result.list != null && listData.result.list.size() > 0) {
                    associationList.addAll(listData.result.list);
                    selectedAssociationIndex = 0;
                    getContactAssociationMemberList(associationList.get(0).id);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    llContent.setVisibility(View.GONE);
                }
                myAssociationAdapter.notifyDataSetChanged();
            }
        };
        schoolApiService = RequestEngine.getInstance().getServer(SchoolApiService.class);
        schoolApiService.getContactAssociationList().compose(RxSchedulersHelper.io_main()).subscribe(associationListSubscrober);
    }

    private void getContactAssociationMemberList(final String associationId) {
        curAssociationId = associationId;
        if (associationMemberMap.get(associationId) != null) {
            myResultsAdapter.notifyDataSetChanged();
            resetAllCheckBox();
        } else {
            MyProgressDialog.show(this, false);
            HttpSubscriber associationListSubscrober = new HttpSubscriber<AssociationMemberListData>(this) {
                @Override
                public void onSuccess(AssociationMemberListData listData) {
                    MyProgressDialog.dismiss();
                    List<AssociationMemberListData.AssociationMember> memberList = listData.result.list;
                    associationMemberMap.put(associationId, memberList);
                    if (associationMemberMap.get(associationId) != null || associationMemberMap.get(associationId).size() > 0) {
                        allSelectMap.put(associationId, false);
                        for (AssociationMemberListData.AssociationMember member : memberList) {
                            member.initPinyin();
                        }
                    } else {
                        MyToast.show(R.string.no_data_tip, WhoCanSeeActivity.this);
                    }
                    myResultsAdapter.notifyDataSetChanged();
                    resetAllCheckBox();
                }
            };
            schoolApiService.getAssociationMemberList(associationId, 0, 0).compose(RxSchedulersHelper.io_main()).subscribe(associationListSubscrober);
        }
    }

    private void resetAllCheckBox() {
        cbAll.setOnCheckedChangeListener(null);
        cbAll.setChecked(false);
        if (allSelectMap.get(curAssociationId) != null) {
            cbAll.setChecked(allSelectMap.get(curAssociationId));
        }
        cbAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Boolean allState = allSelectMap.get(curAssociationId);
                allState = isChecked;
                allSelectMap.put(curAssociationId, allState);
                for (AssociationMemberListData.AssociationMember member : associationMemberMap.get(curAssociationId)) {
                    member.isSelected = allState;
                }
                myResultsAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 获取搜索结果
     *
     * @param s
     * @param contactAssociationMembers
     */
    private List<AssociationMemberListData.AssociationMember> getSearchResult(String s, List<AssociationMemberListData.AssociationMember> contactAssociationMembers) {
        List<AssociationMemberListData.AssociationMember> searchResults = new ArrayList<>();
        for (AssociationMemberListData.AssociationMember info : contactAssociationMembers) {

            if (!TextUtils.isEmpty(info.searchName) && info.searchName.contains(s)) {
                searchResults.add(info);
            } else {
                boolean flag = false;
                // 简拼匹配,如果输入在字符串长度大于6就不按首字母匹配了
                if (s.length() < 6) {
                    Pattern firstLetterMatcher = Pattern.compile("^" + s,
                            Pattern.CASE_INSENSITIVE);
                    flag = firstLetterMatcher.matcher(info.firstLetters).find();
                }
                if (!flag) {
                    // 如果简拼已经找到了，就不使用全拼了
                    // 全拼匹配
                    // 不区分大小写
//                    Pattern pattern2 = Pattern
//                            .compile(s, Pattern.CASE_INSENSITIVE);
//                    Matcher matcher2 = pattern2.matcher(info.pinyin);
//                    flag = matcher2.find();
                    for (String wordPinyin : info.wordPinyinList) {
                        if (wordPinyin.startsWith(s.toUpperCase())) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (flag)
                    searchResults.add(info);
            }
        }
        return searchResults;
    }

    private class MyResultsAdapter extends RecyclerView.Adapter {
        private boolean isSearchType;

        public MyResultsAdapter(boolean isSearchType) {
            this.isSearchType = isSearchType;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multi_right_list, parent, false);
            MyResultViewHolder viewHolder = new MyResultViewHolder(itemView, isSearchType);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MyResultViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            if (isSearchType) {
                return searchResults.size();
            } else {
                if (curAssociationId != null) {
                    List<AssociationMemberListData.AssociationMember> list = associationMemberMap.get(curAssociationId);
                    if (list != null) {
                        return list.size();
                    }
                }
                return 0;
            }
        }
    }

    public class MyResultViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.iv_head)
        ScaleImageView ivHead;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_job)
        TextView tvJob;
        @BindView(R.id.cb_selected)
        CheckBox cbSelected;

        private boolean isSearchType;

        public MyResultViewHolder(View itemView, boolean isSearchType) {
            super(itemView);
            this.isSearchType = isSearchType;
            ButterKnife.bind(this, itemView);
        }

        public void bindData(final int postion) {
            final AssociationMemberListData.AssociationMember info = isSearchType ? searchResults.get(postion) : associationMemberMap.get(curAssociationId).get(postion);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //打开成员详情页
                    startActivity(MemberInfoActivity.getMemberInfoIntent(WhoCanSeeActivity.this, MemberInfoActivity.TYPE_STUDENT, info.id));
                }
            });
            cbSelected.setOnCheckedChangeListener(null);
            cbSelected.setChecked(info.isSelected);
            cbSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    info.isSelected = isChecked;
                    if (!info.isSelected) {
                        if (cbAll.isChecked() && allSelectMap.get(curAssociationId) != null) {
                            allSelectMap.remove(curAssociationId);
                            allSelectMap.put(curAssociationId, false);
                            resetAllCheckBox();
                        }
                    }
                }
            });
            if (!TextUtils.isEmpty(info.avatar)) {
                RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
                options.override(getDrawable(R.mipmap.default_head).getIntrinsicWidth());
                options.placeholder(R.mipmap.default_head);
                options.error(R.mipmap.default_head);
                Glide.with(WhoCanSeeActivity.this).load(TLSUrl.BASE_URL + info.avatar).apply(options).into(ivHead);
            } else {
                ivHead.setImageResource(R.mipmap.default_head);
            }
//            tvName.setText(info.realname) ? info.nickname : TextUtils.isEmpty(info.nickname) ? info.realname : info.realname + "(" + info.nickname + ")");
            tvName.setText("1".equals(info.isnickname) ? ("1".equals(info.isname) ? info.nickname + "(" + info.realname + ")" : info.nickname) : info.realname);
            tvJob.setText(TextUtils.isEmpty(info.dutyName) ? getString(R.string.member) : info.dutyName);

        }
    }

    private class MyAssociationAdapter extends RecyclerView.Adapter {


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multi_left_list, parent, false);
            MyAssociationViewHolder viewHolder = new MyAssociationViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MyAssociationViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return associationList.size();
        }
    }

    public class MyAssociationViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_left)
        TextView tvAssociation;

        public MyAssociationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(final int postion) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (postion != selectedAssociationIndex) {
                        selectedAssociationIndex = postion;
                        myAssociationAdapter.notifyDataSetChanged();
                        String associationId = associationList.get(selectedAssociationIndex).id;
                        getContactAssociationMemberList(associationId);
                    }
                }
            });
            itemView.setSelected(postion == selectedAssociationIndex);
            tvAssociation.setSelected(postion == selectedAssociationIndex);
            tvAssociation.setText(associationList.get(postion).associationName);

        }
    }


}
