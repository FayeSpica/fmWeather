package cn.tonlyshy.app.fmweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liaowm5 on 17/3/21.
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public class Comfort{

        @SerializedName("txt")
        public String info;
    }
    public class CarWash{

        @SerializedName("txt")
        public String info;
    }
    public class Sport{

        @SerializedName("txt")
        public String info;
    }


}
