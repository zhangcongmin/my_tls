package cn.talianshe.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.AssociationLabelListData;
import cn.talianshe.android.bean.AssociationListData;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.service.AssociationApiService;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.widget.MyProgressDialog;

/**
 * @author zcm
 * @ClassName: ChooseAssociationTagActivity
 * @Description: 选择社团标签
 * @date 2017/11/23 14:57
 */
public class ChooseAssociationTagActivity extends BaseActivity {

    @BindView(R.id.fl_association_tags)
    TagFlowLayout flAssociationTags;
    private List<AssociationLabelListData.AssociationLabelInfo> labels;
    private List<AssociationLabelListData.AssociationLabelInfo> selectedLabels;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_association_tag);
        ButterKnife.bind(this);
        initData();
    }

    public static String EXTRA_TAGS = "extra_tags";
    private ArrayList<String> selectedTags = new ArrayList<>();

    private void initData() {
        setTitle(R.string.association_choose_tag);
        selectedTags.addAll(getIntent().getStringArrayListExtra(EXTRA_TAGS));
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText(R.string.confirm);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selectedTags = new ArrayList<>();
                for (int selectedPos : flAssociationTags.getSelectedList()) {
                    selectedTags.add(labels.get(selectedPos).lableName);
                }
                Intent intent = new Intent();
                intent.putStringArrayListExtra(EXTRA_TAGS, selectedTags);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        flAssociationTags.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
            @Override
            public void onSelected(Set<Integer> selectPosSet) {

            }
        });
        requestData();
    }

    private void requestData() {
        MyProgressDialog.show(this);
        AssociationApiService associationApiService = RequestEngine.getInstance().getServer(AssociationApiService.class);
        HttpSubscriber httpSubscriber = new HttpSubscriber<AssociationLabelListData>(this) {
            @Override
            public void onSuccess(AssociationLabelListData listData) {
                MyProgressDialog.dismiss();
                labels = listData.result.list;
                flAssociationTags.setMaxSelectCount(3);

                TagAdapter<AssociationLabelListData.AssociationLabelInfo> tagApater = new TagAdapter<AssociationLabelListData.AssociationLabelInfo>(labels) {
                    @Override
                    public View getView(FlowLayout parent, int position, AssociationLabelListData.AssociationLabelInfo label) {
                        TextView tv = new TextView(ChooseAssociationTagActivity.this);
                        tv.setTextSize(14);
                        if (selectedTags.contains(label.lableName)) {
                            //说明是之前选中的
                        }
                        tv.setTextColor(getResources().getColor(R.color.association_tag_text_color));
                        tv.setBackgroundResource(R.drawable.association_tag_text_bg);
                        tv.setText(label.lableName);
                        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
                        layoutParams.rightMargin = DensityUtils.dipTopx(ChooseAssociationTagActivity.this, 8);
                        layoutParams.bottomMargin = DensityUtils.dipTopx(ChooseAssociationTagActivity.this, 10);
                        tv.setLayoutParams(layoutParams);
                        return tv;
                    }

                    @Override
                    public void onSelected(int position, View view) {
                        ((TextView) view).setTextColor(getResources().getColor(R.color.white));
                    }

                    @Override
                    public void unSelected(int position, View view) {
                        ((TextView) view).setTextColor(getResources().getColor(R.color.gray));
                    }
                };
                flAssociationTags.setAdapter(tagApater);
                if (selectedTags.size() > 0) {
                    Set<Integer> set = new HashSet<>();
                    for (int i = 0; i < labels.size(); i++) {
                        AssociationLabelListData.AssociationLabelInfo info = labels.get(i);
                        if (selectedTags.contains(info.lableName)) {
                            set.add(i);
                        }
                    }
                    tagApater.setSelectedList(set);
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        associationApiService.getAssociationLabelList().compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }


}
