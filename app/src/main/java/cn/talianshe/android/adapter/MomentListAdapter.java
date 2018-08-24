package cn.talianshe.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.talianshe.android.R;
import cn.talianshe.android.activity.ForwardMomentActivity;
import cn.talianshe.android.activity.MainActivity;
import cn.talianshe.android.activity.MemberInfoActivity;
import cn.talianshe.android.activity.MomentDetailActivity;
import cn.talianshe.android.bean.MomentListData;
import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.net.GlobalParams;
import cn.talianshe.android.net.HttpSubscriber;
import cn.talianshe.android.net.RequestEngine;
import cn.talianshe.android.net.RxSchedulersHelper;
import cn.talianshe.android.net.TLSUrl;
import cn.talianshe.android.net.service.SchoolApiService;
import cn.talianshe.android.utils.TimeUtil;
import cn.talianshe.android.utils.TipDialogUtil;
import cn.talianshe.android.widget.ExpandableTextView;
import cn.talianshe.android.widget.MyEditTextDialog;
import cn.talianshe.android.widget.MyForwardDialog;
import cn.talianshe.android.widget.MyProgressDialog;
import cn.talianshe.android.widget.MyToast;
import cn.talianshe.android.widget.ScaleImageView;
import library.talianshe.android.photobrowser.PhotoViewActivity;
import library.talianshe.android.photobrowser.bean.PhotoBean;

/**
 * @author zcm
 * @ClassName: MomentListAdapter
 * @Description: 动态列表适配器
 * @date 2017/11/15 16:00
 */
public class MomentListAdapter extends RecyclerView.Adapter {

    private List<MomentListData.MomentInfo> momentList;
    private Activity mActivity;

    public MomentListAdapter(Activity context, List<MomentListData.MomentInfo> list) {
        this.mActivity = context;
        this.momentList = list;
    }

    @Override
    public int getItemCount() {
        return momentList == null ? 0 : momentList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_moment_list, parent, false);
        MomentItemHolder holder = new MomentItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MomentItemHolder) holder).bindData(position);
    }


    public class MomentItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_head)
        ImageView ivHead;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_forward)
        TextView tvForward;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_comment_count)
        TextView tvCommentCount;
        @BindView(R.id.tv_forward_count)
        TextView tvForwardCount;
        @BindView(R.id.tv_give_like)
        TextView tvGiveLike;
        @BindView(R.id.expandable_text)
        TextView expandableText;
        @BindView(R.id.expand_collapse)
        TextView expandCollapse;
        @BindView(R.id.tv_content)
        ExpandableTextView tvContent;
        @BindView(R.id.v1)
        ScaleImageView v1;
        @BindView(R.id.v2)
        ScaleImageView v2;
        @BindView(R.id.v3)
        ScaleImageView v3;
        @BindView(R.id.row1)
        TableRow row1;
        @BindView(R.id.v4)
        ScaleImageView v4;
        @BindView(R.id.v5)
        ScaleImageView v5;
        @BindView(R.id.v6)
        ScaleImageView v6;
        @BindView(R.id.row2)
        TableRow row2;
        @BindView(R.id.v7)
        ScaleImageView v7;
        @BindView(R.id.v8)
        ScaleImageView v8;
        @BindView(R.id.v9)
        ScaleImageView v9;
        @BindView(R.id.row3)
        TableRow row3;


        public MomentItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(int position) {
            info = momentList.get(position);
            if ((GlobalParams.USER_INFO.id + "").equals(info.publisherId)) {
                tvTitle.setText(R.string.me);
            } else {
//                tvTitle.setText(TextUtils.isEmpty(realname) ? nickname : TextUtils.isEmpty(nickname) ? realname : realname + "(" + nickname + ")");
                tvTitle.setText(info.publisher);
            }
            tvForward.setText(info.forwardContent);
            tvForward.setVisibility(TextUtils.isEmpty(info.forwardContent) ? View.GONE : View.VISIBLE);
            tvContent.setText(info.content);
            tvForwardCount.setText(info.forwardCount + "");
            tvCommentCount.setText(info.reviewCount + "");
            tvGiveLike.setText(info.givelike + "");
            tvGiveLike.setCompoundDrawablesWithIntrinsicBounds("1".equals(info.isLike) ? R.mipmap.up : R.mipmap.no_up, 0, 0, 0);
            //2017/11/12 10:35:55
            tvTime.setText(info.createTime == null?null:TimeUtil.getWeiboTime(Long.parseLong(info.createTime)));
            setMomentImageViews(info);
            if (!TextUtils.isEmpty(info.avatar)) {
                RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop());
                options.override(mActivity.getDrawable(R.mipmap.default_head).getIntrinsicWidth());
                options.placeholder(R.mipmap.default_head);
                options.error(R.mipmap.default_head);
                options.diskCacheStrategy(DiskCacheStrategy.RESOURCE);
                Glide.with(mActivity).load(TLSUrl.BASE_URL + info.avatar).apply(options).into(ivHead);
            } else {
                ivHead.setImageResource(R.mipmap.default_head);
            }
            ivHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    mActivity.startActivity(MemberInfoActivity.getMemberInfoIntent(mActivity,MemberInfoActivity.TYPE_STUDENT,info.publisherId));
                }
            });
        }

        ArrayList<PhotoBean> arrayList;
        MomentListData.MomentInfo info;

        private void setMomentImageViews(MomentListData.MomentInfo momentInfo) {
            arrayList = new ArrayList<>();
            if (TextUtils.isEmpty(momentInfo.dynamicsImgs)) {
                //说明没有图片
            } else {
                String[] images = momentInfo.dynamicsImgs.split(",");
                for (int i = 0; i < images.length; i++) {
                    PhotoBean photoBean = new PhotoBean();
                    photoBean.imgUrl = TLSUrl.BASE_URL + images[i];
                    arrayList.add(photoBean);
                }
            }
            v1.setVisibility(View.GONE);
            v2.setVisibility(View.GONE);
            v3.setVisibility(View.GONE);
            if (arrayList.size() <= 3) {
                v4.setVisibility(View.GONE);
                v5.setVisibility(View.GONE);
                v6.setVisibility(View.GONE);
                v7.setVisibility(View.GONE);
                v8.setVisibility(View.GONE);
                v9.setVisibility(View.GONE);

            } else if (arrayList.size() > 3 && arrayList.size() <= 6) {
                v4.setVisibility(View.INVISIBLE);
                v5.setVisibility(View.INVISIBLE);
                v6.setVisibility(View.INVISIBLE);
                v7.setVisibility(View.GONE);
                v8.setVisibility(View.GONE);
                v9.setVisibility(View.GONE);
            } else {
                v7.setVisibility(View.INVISIBLE);
                v8.setVisibility(View.INVISIBLE);
                v9.setVisibility(View.INVISIBLE);
            }
            RequestOptions options = new RequestOptions().placeholder(R.mipmap.ic_img_thumbnail).dontAnimate();
            options.diskCacheStrategy(DiskCacheStrategy.RESOURCE);

            switch (arrayList.size()) {
                case 9:
                    v9.setVisibility(View.VISIBLE);
                    Glide.with(mActivity)
                            .load(arrayList.get(8).imgUrl)
                            .apply(options)
                            .thumbnail(0.1f)
                            .into(v9);
                case 8:
                    v8.setVisibility(View.VISIBLE);
                    Glide.with(mActivity)
                            .load(arrayList.get(7).imgUrl)
                            .apply(options)
                            .thumbnail(0.1f)
                            .into(v8);
                case 7:
                    v7.setVisibility(View.VISIBLE);
                    Glide.with(mActivity)
                            .load(arrayList.get(6).imgUrl)
                            .apply(options)
                            .thumbnail(0.1f)
                            .into(v7);
                case 6:
                    v6.setVisibility(View.VISIBLE);
                    Glide.with(mActivity)
                            .load(arrayList.get(5).imgUrl)
                            .apply(options)
                            .thumbnail(0.1f)
                            .into(v6);
                case 5:
                    v5.setVisibility(View.VISIBLE);
                    Glide.with(mActivity)
                            .load(arrayList.get(4).imgUrl)
                            .apply(options)
                            .thumbnail(0.1f)
                            .into(v5);
                case 4:
                    v4.setVisibility(View.VISIBLE);
                    Glide.with(mActivity)
                            .load(arrayList.get(3).imgUrl)
                            .apply(options)
                            .thumbnail(0.1f)
                            .into(v4);
                case 3:
                    v3.setVisibility(View.VISIBLE);
                    Glide.with(mActivity)
                            .load(arrayList.get(2).imgUrl)
                            .apply(options)
                            .thumbnail(0.1f)
                            .into(v3);
                case 2:
                    v2.setVisibility(View.VISIBLE);
                    Glide.with(mActivity)
                            .load(arrayList.get(1).imgUrl)
                            .apply(options)
                            .thumbnail(0.1f)
                            .into(v2);
                case 1:
                    v1.setVisibility(View.VISIBLE);
                    Glide.with(mActivity)
                            .load(arrayList.get(0).imgUrl)
                            .apply(options)
                            .thumbnail(0.1f)
                            .into(v1);
                    break;
            }
        }

        @OnClick({R.id.v1, R.id.v2, R.id.v3, R.id.v4, R.id.v5, R.id.v6, R.id.v7, R.id.v8, R.id.v9, R.id.tv_forward_count, R.id.tv_comment_count, R.id.tv_give_like, R.id.ll_item_moment})
        public void onClicked(View view) {
            switch (view.getId()) {
                case R.id.v1:
                    showPhotoView(0, view);
                    break;
                case R.id.v2:
                    showPhotoView(1, view);
                    break;
                case R.id.v3:
                    showPhotoView(2, view);
                    break;
                case R.id.v4:
                    showPhotoView(3, view);
                    break;
                case R.id.v5:
                    showPhotoView(4, view);
                    break;
                case R.id.v6:
                    showPhotoView(5, view);
                    break;
                case R.id.v7:
                    showPhotoView(6, view);
                    break;
                case R.id.v8:
                    showPhotoView(7, view);
                    break;
                case R.id.v9:
                    showPhotoView(8, view);
                    break;
                case R.id.tv_forward_count:
                    if (TipDialogUtil.checkFillInfo(mActivity)) {

                        MyForwardDialog dialog = new MyForwardDialog(mActivity).builder();
                        dialog.setResultListener(new MyForwardDialog.ResultListener() {
                            @Override
                            public void onResult(MyForwardDialog.ForwardType type) {
                                UMShareListener umShareListener = new UMShareListener() {
                                    @Override
                                    public void onStart(SHARE_MEDIA share_media) {
                                        System.out.println("开始分享");
                                    }

                                    @Override
                                    public void onResult(SHARE_MEDIA share_media) {
                                        MyToast.show(R.string.share_success, mActivity);
                                    }

                                    @Override
                                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                                        System.out.println("分享失败");
                                        MyToast.show(R.string.share_failed, mActivity);
                                        throwable.printStackTrace();
                                    }

                                    @Override
                                    public void onCancel(SHARE_MEDIA share_media) {
                                        System.out.println("分享取消");
//                                    MyToast.show(R.string.share_cancel,mActivity);

                                    }
                                };
                                UMWeb web = new UMWeb(TLSUrl.Forward.momentForwardUrl + info.id);
                                web.setTitle("塔联社动态分享");//标题
                                UMImage umImage = new UMImage(mActivity, R.mipmap.tls_logo);
                                umImage.compressFormat = Bitmap.CompressFormat.PNG;
                                web.setThumb(umImage);  //缩略图
                                web.setDescription("描述");//描述
                                if (type == MyForwardDialog.ForwardType.TYPE_QZONG) {
                                    //QQ空间分享
                                    new ShareAction(mActivity)
                                            .setPlatform(SHARE_MEDIA.QZONE)//传入平台
                                            .withText("hello")//分享内容
                                            .withMedia(web)
                                            .setCallback(umShareListener)//回调监听器
                                            .share();
                                } else if (type == MyForwardDialog.ForwardType.TYPE_WECHAT_MOMENT) {
                                    //微信朋友圈分享
                                    new ShareAction(mActivity)
                                            .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)//传入平台
                                            .withText("hello")//分享内容
                                            .withMedia(web)
                                            .setCallback(umShareListener)//回调监听器
                                            .share();
                                } else {
                                    //塔联社内部转发
                                    mActivity.startActivity(ForwardMomentActivity.getForwardIntent(mActivity, info));
                                }
                            }
                        });
                        dialog.show();
                    }
                    break;
                case R.id.tv_comment_count:
                    if (TipDialogUtil.checkFillInfo(mActivity)) {
                        // 评论
                        MyEditTextDialog.EditTextResultListener listener = new MyEditTextDialog.EditTextResultListener() {
                            @Override
                            public void onResult(String result) {
                                if (!TextUtils.isEmpty(result))
                                    operateMoment(mActivity, info, "3", result);
                            }
                        };
                        showCommentDialog(null, mActivity.getResources().getString(R.string.moment_comment), mActivity.getResources().getString(R.string.input_moment_comment_tip), 3, 300, listener);

                    }
                    break;
                case R.id.tv_give_like:
                    if (TipDialogUtil.checkFillInfo(mActivity)) {
                        //点赞
                        operateMoment(mActivity, info, "1", null);
                    }
                    break;
                case R.id.ll_item_moment:
                    mActivity.startActivity(MomentDetailActivity.getMomDetailIntent(mActivity, info.id));
                    break;
            }
        }

        /**
         * 显示图片详情
         *
         * @param showIndex 点击的图片索引
         */
        private void showPhotoView(int showIndex, View v) {
            PhotoViewActivity.startPhotoViewActivity(mActivity, arrayList, v, showIndex);
        }
    }

    private void operateMoment(final Context mContext, final MomentListData.MomentInfo info, String type, String review) {
        if (type == "2") {
            //转发
        } else if (type == "3") {
            //评论
        } else {
            type = "1".equals(info.isLike) ? "4" : "1";
        }
        MyProgressDialog.show(mContext, false);
        final String finalType = type;
        HttpSubscriber httpSubscriber = new HttpSubscriber<StringData>(mContext) {
            @Override
            public void onSuccess(StringData data) {
                MyProgressDialog.dismiss();
                if (finalType == "2") {
                    //转发
                    MyToast.show(R.string.forward_success, mContext);
                    info.forwardCount++;
                } else if (finalType == "3") {
                    //评论
                    MyToast.show(R.string.comment_success, mContext);
                    info.reviewCount++;
                } else {
                    //点赞
                    info.isLike = finalType;
                    MyToast.show("1".equals(finalType)?R.string.give_like_success:R.string.cancle_like_success, mContext);
                    info.givelike = "1".equals(finalType) ? info.givelike + 1 : info.givelike - 1;
                }
                notifyDataSetChanged();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                MyProgressDialog.dismiss();
            }
        };
        RequestEngine.getInstance().getServer(SchoolApiService.class).operateMoment(info.id, review, type).compose(RxSchedulersHelper.io_main()).subscribe(httpSubscriber);
    }

    private MyEditTextDialog dialog;

    private void showCommentDialog(String defaultText, String title, String hint, int lines, int maxLength, MyEditTextDialog.EditTextResultListener listener) {
        dialog = new MyEditTextDialog(mActivity, title, defaultText, hint, lines, maxLength).builder();
        dialog.setResultListener(listener);
        dialog.show();
    }
}