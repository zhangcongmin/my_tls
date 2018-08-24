package library.talianshe.android.photobrowser;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import library.talianshe.android.R;
import library.talianshe.android.photobrowser.bean.PhotoBean;
import uk.co.senab.photoviewi.PhotoView;
import uk.co.senab.photoviewi.PhotoViewAttacher;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoViewFragment extends Fragment {


    public PhotoBean mPhotoBean;
    public boolean showProgressBar;
    public ProgressBar mProgressBar;
    private ImageView mPreviewImage;

    public PhotoViewFragment() {
    }


    public static PhotoViewFragment newInstance(PhotoBean photoBean) {
        PhotoViewFragment fragment = new PhotoViewFragment();
        Bundle args = new Bundle();
        args.putParcelable("photoBean", photoBean);
        fragment.setArguments(args);
        return fragment;
    }
    public static PhotoViewFragment newInstance(PhotoBean photoBean,boolean showProgressBar) {
        PhotoViewFragment fragment = new PhotoViewFragment();
        Bundle args = new Bundle();
        args.putParcelable("photoBean", photoBean);
        args.putBoolean("showProgressBar", showProgressBar);
        fragment.setArguments(args);
        return fragment;
    }


    private PhotoView mPhotoView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPhotoBean = getArguments().getParcelable("photoBean");
        showProgressBar = getArguments().getBoolean("showProgressBar",true);
        View view = inflater.inflate(R.layout.fragment_photo_view, container, false);
        mPhotoView = view.findViewById(R.id.photoIm);
        mPreviewImage = view.findViewById(R.id.previewImage);
        mProgressBar = view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(showProgressBar?View.VISIBLE:View.GONE);
        mPhotoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                getActivity().finish();
            }
        });

        mPhotoView.setMinimumScale(0.6f);
        RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.mipmap.ic_img_failure_large).dontAnimate();
        new RequestListener<String>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<String> target, boolean isFirstResource) {
                mPhotoView.setImageResource(R.mipmap.ic_img_thumbnail_large);
                mProgressBar.setVisibility(View.GONE);
                mPreviewImage.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(String resource, Object model, Target<String> target, DataSource dataSource, boolean isFirstResource) {
                System.out.println("model" + model);
                if (model.equals(mPhotoBean.imgUrl)) {
                    mProgressBar.setVisibility(View.GONE);
                    mPreviewImage.setVisibility(View.GONE);
                }
                mProgressBar.setVisibility(View.GONE);
                mPreviewImage.setVisibility(View.GONE);
                mPhotoView.post(new Runnable() {
                    @Override
                    public void run() {

                        if (mPhotoView != null && mPhotoView.getDisplayRect() != null && mPhotoView.getDisplayRect().width() < mPhotoView.getWidth()) {
                            float scale = mPhotoView.getWidth() * 1.0f / mPhotoView.getDisplayRect().width();
                            mPhotoView.setMaximumScale(Math.max(scale, mPhotoView.getMaximumScale()));
                            mPhotoView.setScale(scale, mPhotoView.getWidth() / 2.0f, 0, false);
                        }
                    }
                });
                return false;
            }
        };
        Glide.with(container.getContext()).load(mPhotoBean.imgUrl).apply(options).listener(new RequestListener<Drawable>() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                mPhotoView.setImageResource(R.mipmap.ic_img_thumbnail_large);
                mProgressBar.setVisibility(View.GONE);
                mPreviewImage.setVisibility(View.GONE);
                return false;
            }
            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                System.out.println("model" + model);
                if (model.equals(mPhotoBean.imgUrl)) {
                    mProgressBar.setVisibility(View.GONE);
                    mPreviewImage.setVisibility(View.GONE);
                }
                mProgressBar.setVisibility(View.GONE);
                mPreviewImage.setVisibility(View.GONE);
                mPhotoView.post(new Runnable() {
                    @Override
                    public void run() {

                        if (mPhotoView != null && mPhotoView.getDisplayRect() != null && mPhotoView.getDisplayRect().width() < mPhotoView.getWidth()) {
                            float scale = mPhotoView.getWidth() * 1.0f / mPhotoView.getDisplayRect().width();
                            mPhotoView.setMaximumScale(Math.max(scale, mPhotoView.getMaximumScale()));
                            mPhotoView.setScale(scale, mPhotoView.getWidth() / 2.0f, 0, false);
                        }
                    }
                });
                return false;
            }
        }).into(mPhotoView);
        return view;
    }


}
