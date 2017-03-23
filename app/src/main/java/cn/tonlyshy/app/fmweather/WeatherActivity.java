package cn.tonlyshy.app.fmweather;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.transition.Transition;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.tonlyshy.app.fmweather.db.County;
import cn.tonlyshy.app.fmweather.gson.Forecast;
import cn.tonlyshy.app.fmweather.gson.Weather;
import cn.tonlyshy.app.fmweather.service.AutoUpdateService;
import cn.tonlyshy.app.fmweather.util.DialogChooseFragment;
import cn.tonlyshy.app.fmweather.util.HttpUtil;
import cn.tonlyshy.app.fmweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by liaowm5 on 17/3/21.
 */

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView weatherCondImageView;

    public SwipeRefreshLayout swipeRefreshLayout;

    public String mWeatherId;

    public DrawerLayout drawerLayout;

    private FloatingActionButton fab;

    private ImageView bingPicImg;

    private ImageView nav_bing_pic;

    private NavigationView navigationView;

    private SharedPreferences prefs;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        prefs= PreferenceManager.getDefaultSharedPreferences(this);
        int themeId=prefs.getInt("theme",R.style.AppTheme);
        if(themeId!=R.style.AppTheme){
            this.setTheme(themeId);
        }
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView =getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initializeUI();
        String weatherString = prefs.getString("weather",null);
        if(weatherString!=null){
            Weather weather= Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            mWeatherId=getIntent().getStringExtra("weather_id");
            if(weatherLayout!=null) {
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            }else{
                Toast.makeText(WeatherActivity.this,"未知错误",Toast.LENGTH_SHORT).show();
            }
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        bingPicImg=(ImageView)findViewById(R.id.bing_pic);
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
    }

    public void loadBingPic() {
        String requestBingPic="http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                if(!TextUtils.isEmpty(bingPic)){
                    try{
                        JSONObject bingObject=new JSONObject(bingPic);
                        Log.d("JSONObject(bingPic);", "onResponse: bingPic"+bingPic);
                        JSONArray bingJsonArray=bingObject.getJSONArray("images");
                        String images=bingJsonArray.getJSONObject(0).toString();
                        JSONObject imagesObject=new JSONObject(images);
                        final String bingPicAddress="http://cn.bing.com"+imagesObject.getString("url");
                        Log.d("loadBingPic", "loadBingPic onResponse: bingPicAddress="+bingPicAddress);
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("bing_pic",bingPicAddress);
                        editor.apply();
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                //View v=navigationView.getHeaderView(R.layout.nav_header);
                                nav_bing_pic=(ImageView)navigationView.findViewById(R.id.nav_bing_pic);
                                if(nav_bing_pic!=null) {
                                    Glide.with(WeatherActivity.this).load(bingPicAddress).into(nav_bing_pic);
                                }
                            }
                        });
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void requestWeather(final String weatherId) {
        String weatherUrl="https://free-api.heweather.com/v5/weather?city="+weatherId+"&key=046c70da943642818f30e427075a3ef5";
        Log.d("WeatherActivity","weatherId="+weatherId);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"链接故障,获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }
    @TargetApi(21)
    private void showWeatherInfo(Weather weather) {
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"˚C";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        String imgCondName="weathericon"+String.valueOf(weather.now.more.code);
        int imgCondId = getResources().getIdentifier(imgCondName, "drawable", "cn.tonlyshy.app.fmweather");
        weatherCondImageView.setImageResource(imgCondId);

        if(weather!=null&&"ok".equals(weather.status)){
            Intent intent=new Intent(this, AutoUpdateService.class);
            startService(intent);
        }

        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);

            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            ImageView condView=(ImageView) view.findViewById(R.id.weather_cond_imageView);
            String imgname="weathericon"+String.valueOf(forecast.more.code_d);
            int imgid = getResources().getIdentifier(imgname, "drawable", "cn.tonlyshy.app.fmweather");
            condView.setImageResource(imgid);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null) {
            aqiText.setText(weather.aqi.aqiCity.aqi);
            pm25Text.setText(weather.aqi.aqiCity.pm25);
        }
        String comfort="舒适度:"+weather.suggestion.comfort.info;
        String carWash="洗车指数:"+weather.suggestion.carWash.info;
        String sport="运动建议:"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

    }

    private void initializeUI() {
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout) findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swip_refresh);
        //swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        fab=(FloatingActionButton)findViewById(R.id.fab);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        weatherCondImageView=(ImageView)findViewById(R.id.big_cond_imageCardView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //drawerLayout.openDrawer(GravityCompat.START);
                int themeId=prefs.getInt("theme",R.style.AppTheme);
                if(themeId!=R.style.AppTheme){
                    setTheme(themeId);
                    themeId=R.style.AppTheme;
                }else{
                    setTheme(R.style.Red);
                    themeId=R.style.Red;
                }
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putInt("theme",themeId);
                editor.apply();
                //recreate();
                Intent intent=new Intent(WeatherActivity.this,WeatherActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION|IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }
        });
        navigationView=(NavigationView)findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_download);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //mDrawerLayout.closeDrawers();
                switch (item.getItemId()){
                    case R.id.nav_chooseCity:
                        DialogChooseFragment fragment=new DialogChooseFragment();
                        fragment.show(getFragmentManager(), "loginDialog");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_change_theme:
                        int themeId=prefs.getInt("theme",R.style.AppTheme);
                        if(themeId!=R.style.AppTheme){
                            setTheme(themeId);
                            themeId=R.style.AppTheme;
                        }else{
                            setTheme(R.style.Red);
                            themeId=R.style.Red;
                        }
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putInt("theme",themeId);
                        editor.apply();
                        //recreate();
                        Intent intent=new Intent(WeatherActivity.this,WeatherActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION|IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        finish();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        loadBingPic();
        nav_bing_pic=(ImageView)findViewById(R.id.nav_bing_pic);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout!=null&&drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers();
        }else{
            super.onBackPressed();
        }
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.txv_fmweather:
                Intent intent=new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/SteiensGate/fmWeather"));
                startActivity(intent);
                break;
            case R.id.txv_api:
                Intent intent2=new Intent(Intent.ACTION_VIEW);
                intent2.setData(Uri.parse("http://www.heweather.com/"));
                startActivity(intent2);
                break;
        }
    }
}
