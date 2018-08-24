package cn.talianshe.android.activity;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.wc.widget.dialog.IosDialog;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.talianshe.android.R;
import cn.talianshe.android.bean.MessageListData;
import cn.talianshe.android.bean.NoticeBean;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.ActivityApiService;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.utils.DensityUtils;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.ScaleImageView;

/**
 * @author zcm
 * @ClassName: MessageCenterActivity
 * @Description: 消息中心
 * @date 2017/12/5 11:23
 */
public class MessageCenterActivity extends BaseActivity {

    @BindView(R.id.swiperv_tutors)
    SwipeMenuRecyclerView swipervTutors;
    private List<MessageListData.MessageInfo> messageInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        btnRight.setText(R.string.clear);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageInfos.size() == 0){
                    MyToast.show(R.string.no_message_to_clear_tip,MessageCenterActivity.this);
                    return;
                }
                showConfirmClearDialog();
            }
        });
        setTitle(R.string.message_center);
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
        swipervTutors.setSwipeMenuCreator(swipeMenuCreator);
        swipervTutors.setLayoutManager(new LinearLayoutManager(this));
        requestData();
    }

    private void requestData() {
        MyProgressDialog.show(this);
        HttpSubscriber messageListSubscrober = new HttpSubscriber<MessageListData>(this) {
            @Override
            public void onSuccess(MessageListData listData) {
                MyProgressDialog.dismiss();
                messageInfos.addAll(listData.result.list);
                if (messageInfos.size() == 0) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    llContent.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    llContent.setVisibility(View.VISIBLE);
                    swipervTutors.setSwipeMenuItemClickListener(new SwipeMenuItemClickListener() {
                        @Override
                        public void onItemClick(SwipeMenuBridge menuBridge) {
                            //关掉侧边栏
                            //删除一条消息
                            menuBridge.closeMenu();
                            deleteMessage(menuBridge.getAdapterPosition());

                        }

                    });
                    swipervTutors.setAdapter(new MyMessageListAdapter());

                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).getMessageList(0, 0).compose(RxSchedulersHelper.io_main()).subscribe(messageListSubscrober);

    }


    private void showConfirmClearDialog() {
        Dialog clearDialog = new IosDialog.Builder(this)
                .setMessage(R.string.confirm_clear_msg).setMessageColor(ContextCompat.getColor(this, R.color.gray)).setMessageSize(15)
                .setNegativeButtonColor(ContextCompat.getColor(this, R.color.gray))
                .setNegativeButtonSize(16)
                .setNegativeButton(R.string.cancel, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButtonColor(ContextCompat.getColor(this, R.color.theme_color))
                .setPositiveButtonSize(16)
                .setPositiveButton(R.string.confirm, new IosDialog.OnClickListener() {
                    @Override
                    public void onClick(IosDialog dialog, View v) {
                        dialog.dismiss();
                        deleteMessage(null);
                    }
                }).build();
        clearDialog.show();
    }

    private void deleteMessage(final Integer removeIndex) {
        String removeId = removeIndex == null ? null : messageInfos.get(removeIndex).id;
        if(removeId == null){
            if(messageInfos.size() == 0){
                MyToast.show(R.string.no_message_to_clear_tip,this);
                return;
            }
        }
        MyProgressDialog.show(this, false);
        HttpSubscriber messageListSubscrober = new HttpSubscriber<StringData>(this) {
            @Override
            public void onSuccess(StringData listData) {
                MyProgressDialog.dismiss();
                MyToast.show(removeIndex == null ? R.string.clear_msg_success : R.string.delete_msg_success, MessageCenterActivity.this);
                if (removeIndex == null) {
                    tvEmpty.setVisibility(View.GONE);
                    llContent.setVisibility(View.VISIBLE);
                    messageInfos.clear();
                    swipervTutors.getAdapter().notifyDataSetChanged();
                } else {
//                    swipervTutors.smoothCloseMenu();
                    messageInfos.remove(removeIndex.intValue());
                    swipervTutors.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).emptyMessage(removeId).compose(RxSchedulersHelper.io_main()).subscribe(messageListSubscrober);
    }


    public class MyMessageListAdapter extends RecyclerView.Adapter {


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_messge_center_list, null);
            MessageListViewHolder holder = new MessageListViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MessageListViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return messageInfos.size();
        }

    }


    public class MessageListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_head)
        ScaleImageView ivHead;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_content)
        TextView tvContent;

        MessageListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            setListener();
        }

        private void setListener() {
        }


        public void bindData(final int position) {
            final MessageListData.MessageInfo info = messageInfos.get(position);
            ivHead.setTipVisible(info.reades == 1);
            if (!TextUtils.isEmpty(info.messageAvatar)) {
                RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
                options.placeholder(R.mipmap.ic_img_thumbnail);
                options.error(R.mipmap.ic_img_failure);
                options.diskCacheStrategy(DiskCacheStrategy.RESOURCE);
                Glide.with(MessageCenterActivity.this).load(TLSUrl.BASE_URL + info.messageAvatar).apply(options).into(ivHead);
            } else {
                ivHead.setImageResource(R.mipmap.system_avatar);
            }
            tvName.setText(info.title);
            tvContent.setText(info.content);
            tvTime.setText(TimeUtil.getWeiboTime(info.createTime));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NoticeBean noticeBean = new NoticeBean();
                    noticeBean.title = info.title;
                    noticeBean.content = info.htmlContent;
                    noticeBean.createtime = info.createTime;
                    noticeBean.id = info.id;
                    noticeBean.index = position;
                    startActivity(MessageCenterDetailActivity.getNoticeIntent(MessageCenterActivity.this, noticeBean));
                }
            });
        }
    }

    @Subscribe
    public void onReceiveReadNoticeBean(NoticeBean readBean){
        if(messageInfos != null && messageInfos.size() > readBean.index){
            messageInfos.get(readBean.index).reades =0;
            swipervTutors.getAdapter().notifyDataSetChanged();
        }
    }
}
