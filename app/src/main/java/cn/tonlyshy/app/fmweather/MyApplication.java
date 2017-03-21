package cn.tonlyshy.app.fmweather;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

/**
 * Created by liaowm5 on 17/3/21.
 */

public class MyApplication extends LitePalApplication {

    private static Context context;

    @Override
    public void onCreate() {
        context=getApplicationContext();
        super.onCreate();
    }

    public static Context getContext(){
        return context;
    }
}
