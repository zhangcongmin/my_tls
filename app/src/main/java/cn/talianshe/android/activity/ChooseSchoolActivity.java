package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.adapter.SchoolAdapter;
import cn.talianshe.android.bean.SchoolListData;
import cn.talianshe.android.bean.SchoolListData.School;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.QuickIndexBar;

/**
 * @author zcm
 * @ClassName: ChooseSchoolActivity
 * @Description: 选择学校
 * @date 2017/11/5 13:55
 */
public class ChooseSchoolActivity extends BaseActivity implements View.OnClickListener {

    private static final String EXTRA_IS_FROM_HOME = "extra_is_from_home";
    private boolean isFromHome;
    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.lv_school)
    ListView lvSchool;
    @BindView(R.id.quick_index_bar)
    QuickIndexBar quickIndexBar;
    @BindView(R.id.tv_search_null)
    TextView tvSearchNull;
    private SchoolAdapter schoolAdapter;
    ArrayList<School> schools = new ArrayList<>();
    ArrayList<School> searchSchools = new ArrayList<>();

    public static Intent getChooseSchoolIntent(Context context,boolean isFromHome){
        Intent intent = new Intent(context,ChooseSchoolActivity.class);
        intent.putExtra(EXTRA_IS_FROM_HOME,isFromHome);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_school);
        ButterKnife.bind(this);
        initDatas();
        setListeners();
    }

    private void initDatas() {
        setTitle("选择学校");
        isFromHome = getIntent().getBooleanExtra(EXTRA_IS_FROM_HOME,false);
        getSchoolList();
        lvSchool.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    School school = schoolAdapter.list.get(position);
                    school.isFromHome = isFromHome;
                    EventBus.getDefault().post(school);
                finish();
            }
        });
    }

    private void getSchoolList() {
        SchoolApiService userApiService = RequestEngine.getInstance(false).getServer(SchoolApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<SchoolListData>(this) {
            @Override
            public void onSuccess(SchoolListData listData) {
                //登录成功，再次获取用户信息，未激活的话弹框激活
                swipeLayout.setRefreshing(false);
                List<SchoolListData.School> schoolList = listData.result.list;
                for (School school : schoolList) {
                    school.initPinyinList();
                }
                schools.clear();
                schools.addAll(schoolList);
                //对集合数据按照拼音进行排序
                Collections.sort(schools);
                schoolAdapter = new SchoolAdapter(schools);
                lvSchool.setAdapter(schoolAdapter);
            }
        };
        swipeLayout.setRefreshing(true);
        userApiService.getSchoolList().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private void setListeners() {
        quickIndexBar.setOnLetterChangeListener(new QuickIndexBar.OnLetterChangeListener() {
            @Override
            public void onLetterChange(String letter) {
                //根据letter找到列表中首字母和letter相同的条目，然后置顶
                for (int i = 0; i < schools.size(); i++) {
                    String firstWord = schools.get(i).pinyin.charAt(0) + "";
                    if (firstWord.equalsIgnoreCase(letter)) {
                        //说明找到了和letter同样字母的条目
                        lvSchool.setSelection(i);
                        //找到立即中断
                        break;
                    }
                }
                //显示出字母
//                showLetter(letter);
                MyToast.show(letter, ChooseSchoolActivity.this);
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
                    schoolAdapter.notifyDataSetChanged(true, schools);
                    lvSchool.setVisibility(View.VISIBLE);
                    tvSearchNull.setVisibility(View.GONE);
                    quickIndexBar.setVisibility(View.VISIBLE);
                } else {

                    searchSchools = getSearchResult(s.toString());
                    if (searchSchools.size() == 0) {
                        tvSearchNull.setVisibility(View.VISIBLE);
                        lvSchool.setVisibility(View.GONE);
                        quickIndexBar.setVisibility(View.GONE);
                    } else {
                        lvSchool.setVisibility(View.VISIBLE);
                        tvSearchNull.setVisibility(View.GONE);
                        quickIndexBar.setVisibility(View.GONE);
                        schoolAdapter.notifyDataSetChanged(false, searchSchools);
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
    private ArrayList<School> getSearchResult(String s) {
        searchSchools.clear();
        for (School school : schools) {
            if (school.name.contains(s)) {
                searchSchools.add(school);
            } else {
                boolean flag = false;
                // 简拼匹配,如果输入在字符串长度大于6就不按首字母匹配了
                if (s.length() < 6) {
                    Pattern firstLetterMatcher = Pattern.compile("^" + s,
                            Pattern.CASE_INSENSITIVE);
                    flag = firstLetterMatcher.matcher(school.firstLetters).find();
                }
                if (!flag) {
                    // 如果简拼已经找到了，就不使用全拼了
                    // 全拼匹配
                    // 不区分大小写
//                    Pattern pattern2 = Pattern
//                            .compile(s, Pattern.CASE_INSENSITIVE);
//                    Matcher matcher2 = pattern2.matcher(school.pinyin);
//                    flag = matcher2.find();
                    for (String wordPinyin : school.wordPinyinList) {
                        if (wordPinyin.startsWith(s.toUpperCase())) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (flag)
                    searchSchools.add(school);
            }
        }
        return searchSchools;
    }
}
