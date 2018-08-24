package cn.talianshe.android.net.service;

import cn.talianshe.android.bean.StringData;
import cn.talianshe.android.bean.UploadData;
import cn.talianshe.android.net.TLSUrl;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * @author zcm
 * @ClassName: ${CLASS}
 * @Description:
 * @date 2017/12/12 9:13
 */

public interface UploadLoadApiService {
    /**
     * 修改登录用户资料
     */
    @POST(TLSUrl.Upload.uploadImage)
//    @POST("http://cgh2619.tunnel.qydev.com/fileUpload/upload")
    Observable<UploadData> uploadImage(@Body MultipartBody multipartBody);
}
