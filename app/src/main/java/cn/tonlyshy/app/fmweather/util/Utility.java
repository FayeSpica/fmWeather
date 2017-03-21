package cn.tonlyshy.app.fmweather.util;

import android.text.TextUtils;
import android.webkit.WebView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.List;

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
                    List<Province> existProvince= DataSupport.where("provinceName = ?",provinceObject.getString("provinceZh")).find(Province.class);
                    if(existProvince.size()>0){
                        continue;
                    }
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("provinceZh"));
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
    public static boolean handleCityResponse(String response,String provinceName){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities=new JSONArray(response);
                for(int i=0;i<allCities.length();i++) {
                    JSONObject cityObject=allCities.getJSONObject(i);
                    List<City> existCity= DataSupport.where("cityName = ?",cityObject.getString("leaderZh")).find(City.class);
                    if(existCity.size()>0){
                        continue;
                    }
                    City city = new City();
                    city.setCityName(cityObject.getString("leaderZh"));
                    city.setProvinceName(cityObject.getString("provinceZh"));
                    city.save();
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
    public static boolean handleCountyResponse(String response,String cityName){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties=new JSONArray(response);
                for(int i=0;i<allCounties.length();i++) {
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    List<County> existCounty= DataSupport.where("countyName = ?",countyObject.getString("cityZh")).find(County.class);
                    if(existCounty.size()>0){
                        continue;
                    }
                    County county = new County();
                    county.setCountyName(countyObject.getString("cityZh"));
                    county.setCityName(countyObject.getString("leaderZh"));
                    county.setWeatherCode(countyObject.getString("id"));
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
