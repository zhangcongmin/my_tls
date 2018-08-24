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

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.NoticeBean;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.widget.OnRecyclerItemClickListener;

/**
 * @author zcm
 * @ClassName: NoticeListActivity
 * @Description: 公告列表
 * @date 2017/11/30 9:50
 */
public class NoticeListActivity extends BaseActivity {

    private static final String EXTRA_NOTICE_LIST = "EXTRA_NOTICE_LIST";
    @BindView(R.id.rv_notice_list)
    RecyclerView rvNoticeList;
    private NoticeListAdapter noticeListAdapter;

    List<NoticeBean> noticeList = new ArrayList<>();

    public static Intent getNoticeListIntent(Context context, List<NoticeBean> noticeList) {
        Intent intent = new Intent(context, NoticeListActivity.class);
        intent.putExtra(EXTRA_NOTICE_LIST, Parcels.wrap(noticeList));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_list);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        setTitle(R.string.notice);
        List<NoticeBean> transList = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_NOTICE_LIST));
        if (transList != null) {
            noticeList.addAll(transList);
        }
        if (noticeList.size() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvNoticeList.setLayoutManager(new LinearLayoutManager(this));
            noticeListAdapter = new NoticeListAdapter();
            rvNoticeList.setAdapter(noticeListAdapter);
            rvNoticeList.addOnItemTouchListener(new OnRecyclerItemClickListener(rvNoticeList) {
                @Override
                public void onItemClick(RecyclerView.ViewHolder vh) {
                    //打开公告详情
                    startActivity(TlsWebViewActivity.getNoticeIntent(NoticeListActivity.this,noticeList.get(vh.getAdapterPosition())));
                }

                @Override
                public void onItemLongClick(RecyclerView.ViewHolder vh) {

                }
            });

        }
    }

    private class NoticeListAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice_list, parent, false);
            NoticeListViewHolder holder = new NoticeListViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((NoticeListViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return noticeList.size();
        }

    }

    public class NoticeListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_top_stick)
        TextView tvTopStick;
        @BindView(R.id.tv_marquee)
        TextView tvMarquee;
        @BindView(R.id.tv_time)
        TextView tvTime;

        NoticeListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(int position) {
            NoticeBean notice = noticeList.get(position);
            tvTopStick.setVisibility(notice.stick == 1 ? View.VISIBLE : View.INVISIBLE);
            tvMarquee.setText(notice.title);
            tvTime.setText(TimeUtil.getWeiboTime(notice.createtime));
        }
    }
}
