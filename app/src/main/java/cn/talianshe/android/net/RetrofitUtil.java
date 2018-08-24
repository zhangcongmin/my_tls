package cn.talianshe.android.net;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class RetrofitUtil {
    //服务路径
    private static Retrofit mRetrofit;
    private static OkHttpClient mOkHttpClient;

    //获取Retrofit对象
    protected static Retrofit getRetrofit(boolean shouldAddToken) {
        return getRetrofit(shouldAddToken, false);
    }

    protected static Retrofit getRetrofit(boolean shouldAddToken, boolean shouldInit) {
        if (shouldInit) {
            mOkHttpClient = OkHttpUtil.getOkHttpClient();
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(TLSUrl.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mOkHttpClient)
                    .build();
        } else {
            if (null == mRetrofit) {
                if (null == mOkHttpClient) {
                    mOkHttpClient = OkHttpUtil.getOkHttpClient();
                }
                mRetrofit = new Retrofit.Builder()
                        .baseUrl(TLSUrl.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .client(mOkHttpClient)
                        .build();
            }
        }
        OkHttpUtil.setShouldAddToken(shouldAddToken);
        return mRetrofit;
    }
}
