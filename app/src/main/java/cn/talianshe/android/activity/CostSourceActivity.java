package cn.talianshe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.eventbus.CostSouceEvent;
import cn.talianshe.android.utils.StringUtils;
import cn.talianshe.android.widget.MyToast;

/**
 * @author zcm
 * @ClassName: CostSourceActivity
 * @Description: 费用来源
 * @date 2017/11/24 15:52
 */
public class CostSourceActivity extends BaseActivity {

    private static final String EXTRA_PARCEL = "extra_parcel";
    @BindView(R.id.rb_self)
    RadioButton rbSelf;
    @BindView(R.id.rb_school)
    RadioButton rbSchool;
    @BindView(R.id.rb_business)
    RadioButton rbBusiness;
    @BindView(R.id.rb_multi)
    RadioButton rbMulti;
    @BindView(R.id.rg_source)
    RadioGroup rgSource;
    @BindView(R.id.et_cost_self)
    EditText etCostSelf;
    @BindView(R.id.ll_cost_self)
    LinearLayout llCostSelf;
    @BindView(R.id.et_cost_school)
    EditText etCostSchool;
    @BindView(R.id.ll_cost_school)
    LinearLayout llCostSchool;
    @BindView(R.id.et_cost_business)
    EditText etCostBusiness;
    @BindView(R.id.ll_cost_business)
    LinearLayout llCostBusiness;
    private CostSouceEvent event;

    public static Intent getCostSourceIntent(Context context, CostSouceEvent event) {
        Intent intent = new Intent(context, CostSourceActivity.class);
        if (event != null) {
            intent.putExtra(EXTRA_PARCEL, Parcels.wrap(event));
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_source);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.cost_source);
        event = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PARCEL));
        rgSource.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_self:
                        llCostSelf.setVisibility(View.VISIBLE);
                        llCostSchool.setVisibility(View.GONE);
                        llCostBusiness.setVisibility(View.GONE);
                        break;
                    case R.id.rb_school:
                        llCostSelf.setVisibility(View.GONE);
                        llCostSchool.setVisibility(View.VISIBLE);
                        llCostBusiness.setVisibility(View.GONE);
                        break;
                    case R.id.rb_business:
                        llCostSelf.setVisibility(View.GONE);
                        llCostSchool.setVisibility(View.GONE);
                        llCostBusiness.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_multi:
                        llCostSelf.setVisibility(View.VISIBLE);
                        llCostSchool.setVisibility(View.VISIBLE);
                        llCostBusiness.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        if (event != null) {
            switch (event.costSource) {
                case 1:
                    //自筹
                    rbSelf.setChecked(true);
                    etCostSelf.setText(StringUtils.moneyFormat(event.costSelf).toString());
                    break;
                case 2:
                    //学校拨款
                    rbSchool.setChecked(true);
                    etCostSchool.setText(StringUtils.moneyFormat(event.costSchool).toString());
                    break;
                case 3:
                    //商家赞助
                    rbBusiness.setChecked(true);
                    etCostBusiness.setText(StringUtils.moneyFormat(event.costBusiness).toString());
                    break;
                case 4:
                    //混合模式
                    etCostSelf.setText(event.costSelf == 0 ? null : StringUtils.moneyFormat(event.costSelf).toString());
                    etCostSchool.setText(event.costSchool == 0 ? null : StringUtils.moneyFormat(event.costSchool).toString());
                    etCostBusiness.setText(event.costBusiness == 0 ? null : StringUtils.moneyFormat(event.costBusiness).toString());
                    rbMulti.setChecked(true);
                    break;
            }
        }
        etCostSelf.addTextChangedListener(new MyTextWatcher(etCostSelf));
        etCostSchool.addTextChangedListener(new MyTextWatcher(etCostSchool));
        etCostBusiness.addTextChangedListener(new MyTextWatcher(etCostBusiness));
    }

    private class MyTextWatcher implements TextWatcher {
        private final EditText editText;

        public MyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String s = editable.toString();
            if (s.contains(".")) {
                if (s.length() - 1 - s.indexOf(".") > 2) {
                    s = s.substring(0, s.indexOf(".") + 3);
                    editText.setText(s);
                    editText.setSelection(s.length());
                }
            } else {
                if (s.length() > 8) {
                    s = s.substring(0, s.length() - 1);
                    editText.setText(s);
                    editText.setSelection(s.length());
                }
            }
            if (s.trim().substring(0).equals(".")) {
                s = "0" + s;
                editText.setText(s);
                editText.setSelection(2);
            }

            if (s.startsWith("0")
                    && s.trim().length() > 1) {
                if (!s.substring(1, 2).equals(".")) {
                    editText.setText(s.subSequence(0, 1));
                    editText.setSelection(1);
                    return;
                }
            }
        }
    }

    @OnClick(R.id.btn_confirm)
    public void onViewClicked() {
        CostSouceEvent event;
        if (rbSelf.isChecked()) {
            if (TextUtils.isEmpty(etCostSelf.getText().toString())) {
                MyToast.show(R.string.cost_null_tip, this);
                return;
            } else {
                event = new CostSouceEvent();
                event.costSource = 1;
                event.costSelf = Double.parseDouble(etCostSelf.getText().toString());
            }
        } else if (rbSchool.isChecked()) {
            if (TextUtils.isEmpty(etCostSchool.getText().toString())) {
                MyToast.show(R.string.cost_null_tip, this);
                return;
            } else {
                event = new CostSouceEvent();
                event.costSource = 2;
                event.costSchool = Double.parseDouble(etCostSchool.getText().toString());
            }
        } else if (rbBusiness.isChecked()) {
            if (TextUtils.isEmpty(etCostBusiness.getText().toString())) {
                MyToast.show(R.string.cost_null_tip, this);
                return;
            } else {
                event = new CostSouceEvent();
                event.costSource = 3;
                event.costBusiness = Double.parseDouble(etCostBusiness.getText().toString());
            }
        } else {
            if (TextUtils.isEmpty(etCostSelf.getText().toString()) && TextUtils.isEmpty(etCostSchool.getText().toString()) && TextUtils.isEmpty(etCostBusiness.getText().toString())) {
                MyToast.show(R.string.cost_null_tip, this);
                return;
            } else if (checkTwoNull()) {
                MyToast.show(R.string.cost_multi_error_tip, this);
                return;
            } else {
                event = new CostSouceEvent();
                event.costSource = 4;
                event.costSelf = TextUtils.isEmpty(etCostSelf.getText().toString()) ? 0 : Double.parseDouble(etCostSelf.getText().toString());
                event.costSchool = TextUtils.isEmpty(etCostSchool.getText().toString()) ? 0 : Double.parseDouble(etCostSchool.getText().toString());
                event.costBusiness = TextUtils.isEmpty(etCostBusiness.getText().toString()) ? 0 : Double.parseDouble(etCostBusiness.getText().toString());
            }
        }
        if (event != null) {
            EventBus.getDefault().post(event);
            finish();
        }
    }

    private boolean checkTwoNull() {
        boolean check1 = TextUtils.isEmpty(etCostSelf.getText().toString()) && TextUtils.isEmpty(etCostSchool.getText().toString());
        boolean check2 = TextUtils.isEmpty(etCostSelf.getText().toString()) && TextUtils.isEmpty(etCostBusiness.getText().toString());
        boolean check3 = TextUtils.isEmpty(etCostSchool.getText().toString()) && TextUtils.isEmpty(etCostBusiness.getText().toString());
        return check1 || check2 || check3;
    }
}
