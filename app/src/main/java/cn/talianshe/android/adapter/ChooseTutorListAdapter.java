package cn.talianshe.android.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.activity.ChooseTutorActivity;
import cn.talianshe.android.activity.MemberInfoActivity;
import cn.talianshe.android.bean.TutorListData.Tutor;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.widget.ScaleImageView;

/**
 * @author zcm
 * @ClassName: ChooseTutorListAdapter
 * @Description: 导师选择列表适配器
 * @date 2017/11/23 19:32
 */
public class ChooseTutorListAdapter extends RecyclerView.Adapter {


    private ChooseTutorActivity activity;
    private List<Tutor> list;
    //是否需要显示字母索引
    private boolean shouldShowLetter = true;

    public ArrayList<Tutor> choosedList = new ArrayList<>();

    public ChooseTutorListAdapter(ChooseTutorActivity activity, ArrayList<Tutor> list) {
        this.activity = activity;
        this.list = list;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_choose_tutor_list, null);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder) holder).bindData(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.letter)
        TextView letter;
        @BindView(R.id.cb_selected)
        CheckBox cbSelected;
        @BindView(R.id.iv_head)
        ScaleImageView ivHead;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_job)
        TextView tvJob;

        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(final int position) {

            //绑定数据
            final Tutor tutor = list.get(position);
            //显示首字母
            String firstLetter = tutor.pinyin.charAt(0) + "";

            if (position > 0) {
                //上一个条目的首字母
                String lastLetter = list.get(position - 1).pinyin.charAt(0) + "";
                //如果当前首字母和上一个相同，则隐藏首字母
                if (firstLetter.equalsIgnoreCase(lastLetter)) {
                    letter.setVisibility(View.GONE);
                } else {
                    //说明不相等，直接设置
                    //由于是复用的，所以当需要显示的时候，要设置为可见
                    letter.setVisibility(shouldShowLetter ? View.VISIBLE : View.GONE);
                    letter.setText(firstLetter);
                }
            } else {
                letter.setVisibility(shouldShowLetter ? View.VISIBLE : View.GONE);
                letter.setText(firstLetter);
            }

            tvName.setText(tutor.name);
            tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.startActivity(MemberInfoActivity.getMemberInfoIntent(activity, MemberInfoActivity.TYPE_TEACHER, list.get(position).id + ""));
                }
            });
            tvJob.setText(tutor.titleName + "/" + tutor.schoolName);
            if (!TextUtils.isEmpty(tutor.avatar)) {
                RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
                options.override(activity.getDrawable(R.mipmap.default_head).getIntrinsicWidth());
                options.placeholder(R.mipmap.default_head);
                options.error(R.mipmap.default_head);
                Glide.with(activity).load(TLSUrl.BASE_URL + tutor.avatar).apply(options).into(ivHead);
            } else {
                ivHead.setImageResource(R.mipmap.default_head);
            }
            cbSelected.setTag(position);
            cbSelected.setChecked(tutor.isSelected);
            cbSelected.setClickable(false);
            cbSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        tutor.isSelected = true;
                        choosedList.add(tutor);
                    } else {
                        tutor.isSelected = false;
                        choosedList.remove(tutor);
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cbSelected.isChecked()) {
                        tutor.isSelected = false;
                        choosedList.remove(tutor);
                    } else {
                        tutor.isSelected = true;
                        choosedList.add(tutor);
                    }
                    cbSelected.setChecked(!cbSelected.isChecked());
                }
            });

            tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = MemberInfoActivity.getMemberInfoIntent(activity, MemberInfoActivity.TYPE_TEACHER, tutor.id + "");
                    activity.startActivity(mIntent);
                }
            });
        }
    }

    public void notifyDataSetChanged(boolean shouldShowLetter, List<Tutor> list) {
        this.shouldShowLetter = shouldShowLetter;
        this.list = list;
        notifyDataSetChanged();
    }
}
