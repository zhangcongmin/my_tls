package cn.talianshe.android.net;

import retrofit2.Retrofit;

import static okhttp3.internal.Internal.instance;

public class RequestEngine {

    private static Retrofit mRetrofit;
    private static RequestEngine instance;

    private RequestEngine(boolean shouldAddToken,boolean shouldInit) {
        mRetrofit = RetrofitUtil.getRetrofit(shouldAddToken,shouldInit);
    }
    private RequestEngine(boolean shouldAddToken) {
        mRetrofit = RetrofitUtil.getRetrofit(shouldAddToken);
    }

    public static RequestEngine getInstance() {
        return getInstance(true);
    }

    public static RequestEngine getInstance(boolean shouldAddToken) {
        if (instance == null) {
            synchronized (RequestEngine.class) {
                if (null == instance) {
                    instance = new RequestEngine(shouldAddToken);
                }
            }
        }
        if (mRetrofit != null && !mRetrofit.baseUrl().toString().equals(TLSUrl.BASE_URL)) {
            synchronized (RequestEngine.class) {
                instance = new RequestEngine(shouldAddToken,true);
            }
        }
        OkHttpUtil.setShouldAddToken(shouldAddToken);
        return instance;
    }

    //返回一个泛型
    public <T> T getServer(Class<T> server) {
        return mRetrofit.create(server);
    }

}
