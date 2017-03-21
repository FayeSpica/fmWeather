package cn.tonlyshy.app.fmweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by liaowm5 on 17/3/21.
 */

public class County extends DataSupport {
    private String countryName;
    private String weatherCode;
    private String cityName;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }


    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }
}
