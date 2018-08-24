package cn.talianshe.android.bean;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.annotation.Property;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.talianshe.android.app.TaliansheApplication;

public class BaseBean<T> {
    public int code; //code：成功失败状态码， 0：失败     -1：未登录或者超时     1：成功
    @SerializedName("message")
    public String msg;
    @Property
    public Object data;
    public T result;

    public boolean isSuccess() {
        return code == 1;
    }

    @SuppressWarnings("unchecked")
    public void castDataToObject() {
        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            Gson gson = new GsonBuilder().
                    registerTypeAdapter(Double.class, new JsonSerializer<Double>() {

                        @Override
                        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                            if (src == src.longValue())
                                return new JsonPrimitive(src.longValue());
                            return new JsonPrimitive(src);
                        }
                    }).create();
            result = gson.fromJson(gson.toJson(data), type);
//        if (((Class<?>) type).getName().startsWith(TaliansheApplication.getInstance().getPackageName())||type != String.class) {
//        } else {
//            //说明要解析成自定义的javabean
//            result = (T) data ;
//        }
    }
}
