package cn.tonlyshy.app.fmweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by liaowm5 on 17/3/21.
 */

public class Province extends DataSupport {
    private String provinceName;
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }


}
