package cn.tonlyshy.app.fmweather.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.tonlyshy.app.fmweather.db.City;
import cn.tonlyshy.app.fmweather.db.County;
import cn.tonlyshy.app.fmweather.db.Province;

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
                    province.setProvinceName(provinceObject.getString("provinceZh"));
                    province.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    * City Data
    * */
    public static boolean handleCityResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces=new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++) {
                    JSONObject cityObject=allProvinces.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("cityZh"));
                    city.setProvinceName("provinceZh");
                    city.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    * County Data
    * */
    public static boolean handleCountyResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces=new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++) {
                    JSONObject countyObject=allProvinces.getJSONObject(i);
                    County county = new County();
                    county.setCountryName(countyObject.getString("cityZh"));
                    county.setCityName(countyObject.getString("leaderZh"));
                    county.setWeatherCode(countyObject.getString("id"));
                    county.save();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
