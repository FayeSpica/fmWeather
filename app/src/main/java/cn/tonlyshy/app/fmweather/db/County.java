package cn.tonlyshy.app.fmweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by liaowm5 on 17/3/21.
 */

public class County extends DataSupport {
    private String countyName;
    private String weatherCode;
    private int cityId;
    private int countyId;

    public int getCountyId() {
        return countyId;
    }

    public void setCountyId(int countyId) {
        this.countyId = countyId;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }
}
