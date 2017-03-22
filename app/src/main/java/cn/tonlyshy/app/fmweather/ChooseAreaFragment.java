package cn.tonlyshy.app.fmweather;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;
import org.litepal.util.DBUtility;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.tonlyshy.app.fmweather.db.City;
import cn.tonlyshy.app.fmweather.db.County;
import cn.tonlyshy.app.fmweather.db.Province;
import cn.tonlyshy.app.fmweather.util.HttpUtil;
import cn.tonlyshy.app.fmweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by liaowm5 on 17/3/21.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> arrayAdapter;

    private List<String> dataList=new ArrayList<>();

    /*
    * List of Province
    * */
    private List<Province> provinceList;

    /*
    * List of City
    * */
    private List<City> cityList;

    /*
    * List of County
    * */
    private List<County> countyList;

    private Province selectedProvince;

    private City selectedCity;

    private int currentLevel;

    public ChooseAreaFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.btn_back);
        listView=(ListView) view.findViewById(R.id.list_view_city);
        arrayAdapter=new ArrayAdapter<String>(MyApplication.getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(arrayAdapter);
        listView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        cityList=new ArrayList<>();
        countyList=new ArrayList<>();
        provinceList=new ArrayList<>();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v=getView();
        //Back pressed Logic for fragment
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if(currentLevel==LEVEL_CITY){
                            queryProvinces();
                        }else if(currentLevel==LEVEL_COUNTY){
                            queryCities();
                        }else{
                            getActivity().onBackPressed();
                        }

                        return true;
                    }
                }
                return false;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId=countyList.get(position).getWeatherCode();
                    if(getActivity() instanceof MainActivity){
                        Intent intent=new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity=(WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.mWeatherId=weatherId;
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            String address="http://www.tonlyshy.cn:8080/static/weather/china.json";
            queryFromServer(address,"province");
            Log.i("query", "queryProvinces: "+address);
        }
    }
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList= DataSupport.where("provinceId = ? ",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            String address="http://www.tonlyshy.cn:8080/static/weather/"+selectedProvince.getId()+".json";
            queryFromServer(address,"city");
            Log.i("query", "queryCities: "+address);
        }
    }
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        Log.d("Choose", "queryCounties: selectedCity.getCityName()"+selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList= DataSupport.where("cityId = ?",String.valueOf(selectedCity.getCityId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county :countyList){
                dataList.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            String address="http://www.tonlyshy.cn:8080/static/weather/"+"conty_"+selectedCity.getCityId()+".json";
            Log.i("query", "queryCounties: "+address);
            queryFromServer(address,"county");
        }
    }



    private void queryFromServer(String adress,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(adress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(MyApplication.getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("MainActivity", "onResponse: type="+type);
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    Log.d("MainActivity", "onResponse: selectedProvince.getProvinceName()="+selectedProvince.getProvinceName());
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    Log.d("MainActivity", "onResponse: selectedCity.getCityName()="+selectedCity.getCityName());
                    result=Utility.handleCountyResponse(responseText,selectedCity.getCityId());
                }
                Log.d("MainActivity", "onResponse: result="+result);
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            switch (currentLevel){
                case LEVEL_CITY:
                    progressDialog.setMessage("正在获取...");
                    break;
                case LEVEL_PROVINCE:
                    progressDialog.setMessage("正在获取城市信息...");
                    break;
            }
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }

    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
            progressDialog=null;
        }
    }

}
