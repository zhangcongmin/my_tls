package cn.talianshe.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.adapter.ChooseTutorListAdapter;
import cn.talianshe.android.bean.TutorListData;
import cn.talianshe.android.bean.TutorListData.Tutor;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.OnRecyclerItemClickListener;
import cn.talianshe.android.widget.QuickIndexBar;

/**
 * @author zcm
 * @ClassName: ChooseTutorActivity
 * @Description: 选择导师
 * @date 2017/11/23 19:20
 */
public class ChooseTutorActivity extends BaseActivity implements View.OnClickListener {


    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.lv_tutors)
    RecyclerView lvTutors;
    @BindView(R.id.quick_index_bar)
    QuickIndexBar quickIndexBar;
    @BindView(R.id.tv_search_null)
    TextView tvSearchNull;
    private ChooseTutorListAdapter tutorAdapter;

    ArrayList<Tutor> tutors = new ArrayList<>();
    ArrayList<Tutor> searchTutors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_tutor);
        ButterKnife.bind(this);
        initDatas();
    }

    private void initDatas() {
        setTitle(R.string.association_choose_tutor);
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText(R.string.commit);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(tutorAdapter.choosedList);
                finish();
            }
        });
        setListeners();
        //获取数据
        requestData();


    }

    private void setListeners() {
        quickIndexBar.setOnLetterChangeListener(new QuickIndexBar.OnLetterChangeListener() {
            @Override
            public void onLetterChange(String letter) {
                //根据letter找到列表中首字母和letter相同的条目，然后置顶
                for (int i = 0; i < tutors.size(); i++) {
                    String firstWord = tutors.get(i).pinyin.charAt(0) + "";
                    if (firstWord.equalsIgnoreCase(letter)) {
                        //说明找到了和letter同样字母的条目
//                        lvTutors.setSelection(i);
                        lvTutors.scrollToPosition(i);
                        //找到立即中断
                        break;
                    }
                }
                //显示出字母
//                showLetter(letter);
                MyToast.show(letter, ChooseTutorActivity.this);
                System.out.println(letter);
            }
        });
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
                    tutorAdapter.notifyDataSetChanged(true, tutors);
                    lvTutors.setVisibility(View.VISIBLE);
                    tvSearchNull.setVisibility(View.GONE);
                    quickIndexBar.setVisibility(View.VISIBLE);
                } else {

                    searchTutors = getSearchResult(s.toString());
                    if (searchTutors.size() == 0) {
                        tvSearchNull.setVisibility(View.VISIBLE);
                        lvTutors.setVisibility(View.GONE);
                        quickIndexBar.setVisibility(View.GONE);
                    } else {
                        tvSearchNull.setVisibility(View.GONE);
                        lvTutors.setVisibility(View.VISIBLE);
                        quickIndexBar.setVisibility(View.GONE);
                        tutorAdapter.notifyDataSetChanged(false, searchTutors);
                    }
                }
            }
        });
    }

    /**
     * 获取搜索结果
     *
     * @param s
     */
    private ArrayList<Tutor> getSearchResult(String s) {
        searchTutors.clear();
        for (Tutor tutor : tutors) {
            if (tutor.name.contains(s)) {
                searchTutors.add(tutor);
            } else {
                boolean flag = false;
                // 简拼匹配,如果输入在字符串长度大于6就不按首字母匹配了
                if (s.length() < 6) {
                    Pattern firstLetterMatcher = Pattern.compile("^" + s,
                            Pattern.CASE_INSENSITIVE);
                    flag = firstLetterMatcher.matcher(tutor.firstLetters).find();
                }
                if (!flag) {
                    // 如果简拼已经找到了，就不使用全拼了
                    // 全拼匹配
                    // 不区分大小写
//                    Pattern pattern2 = Pattern
//                            .compile(s, Pattern.CASE_INSENSITIVE);
//                    Matcher matcher2 = pattern2.matcher(school.pinyin);
//                    flag = matcher2.find();
                    for (String wordPinyin : tutor.wordPinyinList) {
                        if (wordPinyin.startsWith(s.toUpperCase())) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (flag)
                    searchTutors.add(tutor);
            }
        }
        return searchTutors;
    }

    private void requestData() {
        MyProgressDialog.show(this);
        SchoolApiService schoolApiService = RequestEngine.getInstance().getServer(SchoolApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<TutorListData>(this) {
            @Override
            public void onSuccess(TutorListData listData) {
                //对集合数据按照拼音进行排序
                tutors.clear();
                searchTutors.clear();
                tutors.addAll(listData.result.list);
                for (Tutor tutor : tutors) {
                    tutor.initPinyin();
                }
                Collections.sort(tutors);
                MyProgressDialog.dismiss();
                tutorAdapter = new ChooseTutorListAdapter(ChooseTutorActivity.this, tutors);
                lvTutors.setLayoutManager(new LinearLayoutManager(ChooseTutorActivity.this));
                lvTutors.setAdapter(tutorAdapter);
//                lvTutors.addOnItemTouchListener(new OnRecyclerItemClickListener(lvTutors) {
//                    @Override
//                    public void onItemClick(RecyclerView.ViewHolder vh) {
//                        Intent intent = new Intent(ChooseTutorActivity.this, MemberInfoActivity.class);
//                        startActivity(intent);
//                    }
//
//                    @Override
//                    public void onItemLongClick(RecyclerView.ViewHolder vh) {
//
//                    }
//                });
//                lvTutors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        Intent intent = new Intent(ChooseTutorActivity.this, MemberInfoActivity.class);
//                        startActivity(intent);
//                    }
//                });
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        schoolApiService.getTeacherList(0,0).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);

    }
}
