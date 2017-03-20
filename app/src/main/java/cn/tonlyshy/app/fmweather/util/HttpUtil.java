package cn.tonlyshy.app.fmweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by liaowm5 on 17/3/21.
 *
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String adress,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(adress).build();
        client.newCall(request).enqueue(callback);
    }
}
