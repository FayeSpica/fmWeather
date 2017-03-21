package cn.tonlyshy.app.fmweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liaowm5 on 17/3/21.
 */

public class AQI {
    @SerializedName("city")
    public AQICity aqiCity;

    public class AQICity{
        public String aqi;

        public String pm25;

        public String qlty; //共六个级别，分别：优，良，轻度污染，中度污染，重度污染，
    }
}
