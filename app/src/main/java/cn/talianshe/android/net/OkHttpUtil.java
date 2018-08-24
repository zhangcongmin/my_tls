package cn.talianshe.android.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import cn.talianshe.android.app.TaliansheApplication;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpUtil {

    private static OkHttpClient mOkHttpClient;

    //设置缓存目录
    private static final File cacheDirectory = new File(TaliansheApplication.getInstance().getCacheDir().getAbsolutePath(), "httpCache");

    private static Cache cache = new Cache(cacheDirectory, 10 * 1024 * 1024);

//    //请求拦截
//    private static RequestInterceptor requestInterceptor = new RequestInterceptor();
//
//    //响应拦截
//    private static ResponseInterceptor responseInterceptor = new ResponseInterceptor();


    private static HttpRequestInterceptor requestInterceptor;
    public static OkHttpClient getOkHttpClient() {

        if (null == mOkHttpClient) {
            requestInterceptor = new HttpRequestInterceptor();
            mOkHttpClient = new OkHttpClient.Builder()
                    .cookieJar(CookieJar.NO_COOKIES)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(requestInterceptor)
                    .addNetworkInterceptor(new REWRITE_CACHE_CONTROL_INTERCEPTOR())
                    .addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                        @Override
                        public void log(String message) {
                            Log.i("tls_http",message);
                        }
                    }).setLevel(HttpLoggingInterceptor.Level.BODY))
                    .cache(cache)
                    .build();
        }
        return mOkHttpClient;
    }
    public static void setShouldAddToken(boolean shouldAddToken){
        if(requestInterceptor != null)
            requestInterceptor.shouldAddToken = shouldAddToken;
    }

    public static class REWRITE_CACHE_CONTROL_INTERCEPTOR implements Interceptor{

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if(!isNetWorking(TaliansheApplication.getInstance())){
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }
            Response originalResponse = chain.proceed(request);
            if(isNetWorking(TaliansheApplication.getInstance())){
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma")
                        .build();
            }else{
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
                        .removeHeader("Pragma")
                        .build();
            }

        }
    }
    /**
     * 网络监测
     *
     * @param context
     * @return
     */
    public static boolean isNetWorking(Context context) {
        boolean flag = checkNet(context);
        if (!flag) {

            Toast.makeText(context, "当前设备网络异常，请检查后再重试！", Toast.LENGTH_SHORT).show();
        }
        return flag;
    }

    private static boolean checkNet(Context context) {

        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}
