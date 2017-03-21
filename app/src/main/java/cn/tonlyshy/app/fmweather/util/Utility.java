package cn.tonlyshy.app.fmweather.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.List;

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
        return false;
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
                    List<City> existCity= DataSupport.where("cityName = ?",cityObject.getString("provinceZh")).find(City.class);
                    if(existCity.size()>0){
                        continue;
                    }
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
    public static boolean handleCountyResponse(String response,String cityName){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties=new JSONArray(response);
                for(int i=0;i<allCounties.length();i++) {
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    List<City> existCounty= DataSupport.where("cityName = ?",countyObject.getString("provinceZh")).find(City.class);
                    if(existCounty.size()>0){
                        continue;
                    }
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
