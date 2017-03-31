package cn.tonlyshy.app.fmweather;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.tonlyshy.app.fmweather.gson.Forecast;
import cn.tonlyshy.app.fmweather.gson.Weather;
import cn.tonlyshy.app.fmweather.service.AutoUpdateService;
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

    private TextView bingPicCopyRightTxv;

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        int themeId=prefs.getInt("theme",R.style.AppTheme);
        if(themeId!=R.style.AppTheme){
            this.setTheme(themeId);
        }else{
            this.setTheme(R.style.Dark);
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

    private SimpleTarget target = new SimpleTarget<GlideBitmapDrawable>() {
        @Override
        public void onResourceReady(GlideBitmapDrawable bitmap, GlideAnimation glideAnimation) {
            //图片加载完成
            nav_bing_pic = (ImageView) findViewById(R.id.nav_bing_pic);
            nav_bing_pic.setImageBitmap(bitmap.getBitmap());
            //Glide.with(WeatherActivity.this).load(bitmap.getBitmap()).into(nav_bing_pic);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.getBitmap().compress(Bitmap.CompressFormat.JPEG, 50, baos);
            String imageBase64 = new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
            editor.putString("PicBase64",imageBase64 );
            editor.commit();
        }
    };

    private void saveBingPic(){
        boolean isPermmited=false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            isPermmited=false;
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            saveBingPic();
        }
        else{
            isPermmited=true;
        }
        if(isPermmited){
            prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
            String temp = prefs.getString("PicBase64", "");
            if(!temp.equals("")) {
                ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
                //final Drawable  d=Drawable.createFromStream(bais, "");
                String fileName="";
                String descri="Bing";
                if(bingPicCopyRightTxv!=null){
                    fileName=bingPicCopyRightTxv.getText().toString().split(",")[0]==null? "":bingPicCopyRightTxv.getText().toString().split(",")[0];
                    if(bingPicCopyRightTxv.getText().toString().split(",").length>1) {
                        descri = bingPicCopyRightTxv.getText().toString().split(",")[1] == null ? "" : bingPicCopyRightTxv.getText().toString().split(",")[1];
                    }
                    Log.d("WeatherActivity", "saveBingPic: fileName="+fileName+"  descri="+descri);
                }
                MediaStore.Images.Media.insertImage(getContentResolver(), BitmapFactory.decodeStream(bais), fileName, descri);
                Toast.makeText(this,"保存成功~",Toast.LENGTH_SHORT).show();
            }else{
                loadBingPic();
                saveBingPic();
            }
        }else{
            Log.d("GG", "saveBingPic: no Permssion");
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
                        final String bingPicCopyRight=imagesObject.getString("copyright");
                        //final String bingPicCopyRightLink=imagesObject.getString("copyrightLink");
                        Log.d("loadBingPic", "loadBingPic onResponse: bingPicAddress="+bingPicAddress);
                        prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                        nav_bing_pic = (ImageView) findViewById(R.id.nav_bing_pic);

                        String temp = prefs.getString("PicBase64", "");

                        if(temp.equals("")||(nav_bing_pic != null&&nav_bing_pic.getDrawable()==null)||prefs.getString("bing_pic",null)==null||(!bingPicAddress.equals(prefs.getString("bing_pic",null)))) {
                            Log.d("Weather", "壁纸不存在: ");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    nav_bing_pic = (ImageView) findViewById(R.id.nav_bing_pic);
                                    bingPicCopyRightTxv = (TextView) findViewById(R.id.copy_right);
                                    if (nav_bing_pic != null) {
                                        Bitmap bing=null;
                                        Glide.with(WeatherActivity.this).load(bingPicAddress).into(target);
                                        //Bitmap bing=Glide.with(WeatherActivity.this).load(bingPicAddress);
                                        if (bingPicCopyRightTxv != null) {
                                            bingPicCopyRightTxv.setText(bingPicCopyRight);
                                        }
                                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
                                        editor.putString("bing_pic", bingPicAddress);
                                        editor.apply();
                                    }
                                }
                            });
                        }else{
                            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
                            final Drawable  d=Drawable.createFromStream(bais, "");
                            if (nav_bing_pic != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        nav_bing_pic.setImageDrawable(d);
                                    }
                                });
                            }
                            //Glide.with(WeatherActivity.this).load(d).into(nav_bing_pic);
                        }
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
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
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
            Toast.makeText(WeatherActivity.this,"暂无新增城市功能～",Toast.LENGTH_SHORT).show();
            }
        });
        navigationView=(NavigationView)findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_download);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //mDrawerLayout.closeDrawers();
                switch (item.getItemId()){
                    case R.id.nav_download:
                        saveBingPic();
                        break;
                    case R.id.nav_chooseCity:
                        DialogChooseFragment fragment=new DialogChooseFragment();
                        fragment.show(getFragmentManager(), "cityDialog");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_change_theme:
                        DialogChangeThemeFragment dialogChangeThemeFragment=new DialogChangeThemeFragment();
                        dialogChangeThemeFragment.show(getFragmentManager(),"themeDialog");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_about:
                        AboutFragment aboutFragment=new AboutFragment();
                        aboutFragment.show(getFragmentManager(),"aboutDialog");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_exit:
                        finish();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        nav_bing_pic=(ImageView)findViewById(R.id.nav_bing_pic);
        bingPicCopyRightTxv = (TextView) findViewById(R.id.copy_right);
        loadBingPic();
        boolean isReboot=getIntent().getBooleanExtra("themeRebootFlag",false);
        if(isReboot){
            drawerLayout.openDrawer(GravityCompat.START);
            navigationView.setCheckedItem(R.id.nav_change_theme);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);/*Simple Animation*/
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawerLayout.closeDrawers();
                            }
                        });
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout!=null&&drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers();
        }else if(getFragmentManager().findFragmentByTag("cityDialog")!=null){
            Fragment fragment=getFragmentManager().findFragmentByTag("cityDialog");
            fragment.onStop();
            drawerLayout.openDrawer(GravityCompat.START);
        }else if(getFragmentManager().findFragmentByTag("themeDialog")!=null){
            Fragment fragment=getFragmentManager().findFragmentByTag("themeDialog");
            fragment.onStop();
            drawerLayout.openDrawer(GravityCompat.START);
        }else if(getFragmentManager().findFragmentByTag("aboutDialog")!=null){
            Fragment fragment=getFragmentManager().findFragmentByTag("aboutDialog");
            fragment.onStop();
            drawerLayout.openDrawer(GravityCompat.START);
        }else {
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
