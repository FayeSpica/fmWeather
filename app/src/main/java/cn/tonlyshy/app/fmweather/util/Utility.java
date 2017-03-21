package cn.tonlyshy.app.fmweather.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import cn.tonlyshy.app.fmweather.MainActivity;
import cn.tonlyshy.app.fmweather.MyApplication;
import cn.tonlyshy.app.fmweather.db.City;
import cn.tonlyshy.app.fmweather.db.County;
import cn.tonlyshy.app.fmweather.db.Province;
import cn.tonlyshy.app.fmweather.gson.Weather;

/**
 * Created by liaowm5 on 17/3/21.
 * Analize JSON Object
 */

public class Utility {
    /*
    * Province Data
    * */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces=new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++) {
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setId(provinceObject.getInt("id"));
                    province.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return true;
    }

    /*
    * City Data
    * */
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities=new JSONArray(response);
                for(int i=0;i<allCities.length();i++) {
                    JSONObject cityObject=allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityId(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                    Log.i("handleCityResponse", "provinceId="+provinceId+"/"+city.getCityId()+"(name="+city.getCityName());
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return true;
    }

    /*
    * County Data
    * */
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties=new JSONArray(response);
                for(int i=0;i<allCounties.length();i++) {
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setCityId(cityId);
                    county.setWeatherCode(countyObject.getString("weather_id"));
                    county.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return true;
    }


    /*
    * JSON to Weather.class
    * */

    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather5");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

}
