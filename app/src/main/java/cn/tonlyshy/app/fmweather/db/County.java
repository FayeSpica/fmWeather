package cn.tonlyshy.app.fmweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by liaowm5 on 17/3/21.
 */

public class County extends DataSupport {
    private String countyName;
    private String weatherCode;
    private String cityName;

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
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
