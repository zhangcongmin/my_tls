package cn.talianshe.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.SchoolListData;

/**
 * @author zcm
 * @ClassName: SchoolAdapter
 * @Description: 选择学校适配器
 * @date 2017/11/5 15:55
 */
public class SchoolAdapter extends BaseAdapter {

    public List<SchoolListData.School> list;
    //是否需要显示字母索引
    private boolean shouldShowLetter = true;

    public SchoolAdapter(ArrayList<SchoolListData.School> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            convertView = View.inflate(parent.getContext(), R.layout.item_school_list, null);
            holder = new ViewHolder(convertView);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        //绑定数据
        SchoolListData.School School = list.get(position);
        //显示首字母
        String firstLetter = School.pinyin.charAt(0)+"";

        if(position>0){
            //上一个条目的首字母
            String lastLetter = list.get(position - 1).pinyin.charAt(0)+"";
            //如果当前首字母和上一个相同，则隐藏首字母
            if(firstLetter.equalsIgnoreCase(lastLetter)){
                holder.letter.setVisibility(View.GONE);
            }else {
                //说明不相等，直接设置
                //由于是复用的，所以当需要显示的时候，要设置为可见
                holder.letter.setVisibility(shouldShowLetter?View.VISIBLE:View.GONE);
                holder.letter.setText(firstLetter);
            }
        }else {
            holder.letter.setVisibility(shouldShowLetter?View.VISIBLE:View.GONE);
            holder.letter.setText(firstLetter);
        }

        holder.name.setText(School.name);
        return convertView;
    }


    static class ViewHolder {
        @BindView(R.id.letter)
        TextView letter;
        @BindView(R.id.name)
        TextView name;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
            view.setTag(this);
        }
    }

    public void notifyDataSetChanged(boolean shouldShowLetter, List<SchoolListData.School> list){
        this.shouldShowLetter = shouldShowLetter;
        this.list = list;
        notifyDataSetChanged();
    }
}
