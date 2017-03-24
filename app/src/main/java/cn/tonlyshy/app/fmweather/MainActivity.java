package cn.tonlyshy.app.fmweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        int themeId=prefs.getInt("theme",R.style.AppTheme);
        Log.d("GG", "onCreate: themeId="+themeId);
        if(themeId!=R.style.AppTheme){
            this.setTheme(themeId);
        }
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather",null)!=null){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
