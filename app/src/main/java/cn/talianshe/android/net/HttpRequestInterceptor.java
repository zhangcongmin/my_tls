package cn.talianshe.android.net;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class HttpRequestInterceptor implements Interceptor {
    public boolean shouldAddToken;

    public HttpRequestInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if(shouldAddToken && !TextUtils.isEmpty(GlobalParams.TOKEN))
            request = request.newBuilder().addHeader("token",GlobalParams.TOKEN).build();
        return chain.proceed(request);
    }
}
