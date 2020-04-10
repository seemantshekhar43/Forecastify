package com.seemantshekhar.forecastify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.seemantshekhar.forecastify.Model.ForecastResponse;
import com.seemantshekhar.forecastify.Model.WeatherResponse;
import com.seemantshekhar.forecastify.Retrofit.Client;
import com.seemantshekhar.forecastify.Retrofit.MapWeather;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import io.reactivex.Scheduler;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import static java.security.AccessController.getContext;

/**
 * I started developing this app on Wednesday 08.04.2020 at 18:34 IST
 * Its lockdown all around.
 *
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private CompositeDisposable compositeDisposable1;
    private CompositeDisposable compositeDisposable2;
    private MapWeather weatherService;
    private List<String> lstCities;

    private RecyclerView forecast_recycler;

    private TextView message, wind, humidity, sunrise, sunset, pressure, temp, realFeel;
    private ImageView cloudImg;
//    private ImageButton searchBtn;
    private MaterialSearchBar searchBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       compositeDisposable1  = new CompositeDisposable();
       compositeDisposable2  = new CompositeDisposable();
        Retrofit retrofit = Client.getInstance();
        weatherService = retrofit.create(MapWeather.class);



        temp = findViewById(R.id.temp);

        message = findViewById(R.id.message);
        wind = findViewById(R.id.wind);
        humidity = findViewById(R.id.humidity);
        sunrise = findViewById(R.id.sunrise);
        sunset = findViewById(R.id.sunset);
        pressure = findViewById(R.id.pressure);
        cloudImg =findViewById(R.id.cloud_img);
        realFeel = findViewById(R.id.real_feel);
        searchBar = findViewById(R.id.search_bar);


        forecast_recycler = findViewById(R.id.forecast_recycler);

        forecast_recycler.setHasFixedSize(true);
        forecast_recycler.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL,false ));

        searchBar.setEnabled(false);
        new LoadCities().execute();

        //Requesting Permission from user
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                if(report.areAllPermissionsGranted()){
                    buildLocationRequest();
                    buildLocationCallBack();




                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
               Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }).check();


    }

    private void buildLocationCallBack() {
        locationRequest =new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10.0f);
    }

    private void buildLocationRequest() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Global.current_location = locationResult.getLastLocation();
//                Toast.makeText(MainActivity.this, locationResult.getLastLocation().getLatitude()+"/"+
//                        locationResult.getLastLocation().getLongitude(), Toast.LENGTH_SHORT).show();
                getWeather();
                getForecast();
            }
        };
    }

    private void getWeather(){

        compositeDisposable1.add(weatherService.weatherByMapLangLat(String.valueOf(Global.current_location.getLatitude()),
                String.valueOf(Global.current_location.getLongitude()),
                Global.API_KEY,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResponse>(){
                    @Override
                    public void accept(WeatherResponse weatherResponse) throws Exception {

                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                                .append(weatherResponse.getWeather().get(0).getIcon())
                                .append(".png").toString()).into(cloudImg);
                        searchBar.setPlaceHolder(weatherResponse.getName());
                        message.setText(weatherResponse.getWeather().get(0).getDescription());
                        temp.setText(String.valueOf(weatherResponse.getMain().getTemp()));
                        realFeel.setText(new StringBuilder(String.valueOf(weatherResponse.getMain().getFeels_like()))
                                .append("°C").toString());
                        pressure.setText(new StringBuilder(String.valueOf(weatherResponse.getMain().getPressure()))
                                .append(" hpa").toString());
                        humidity.setText(new StringBuilder(String.valueOf(weatherResponse.getMain().getHumidity()))
                                .append("%").toString());
                        sunrise.setText(Global.getDate(weatherResponse.getSys().getSunrise()));
                        sunset.setText(Global.getDate(weatherResponse.getSys().getSunset()));
                        wind.setText(new StringBuilder(String.format("%.2f",weatherResponse.getWind().getSpeed() * 1.61)).append(" Km/h").toString());

                    }
                }, throwable -> {

                })
            );

    }

    private void getForecast(){
        compositeDisposable2.add(weatherService.forecastByMapLangLat(String.valueOf(Global.current_location.getLatitude()),
                String.valueOf(Global.current_location.getLongitude()),
                Global.API_KEY,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ForecastResponse>() {
                    @Override
                    public void accept(ForecastResponse forecastResponse) throws Exception {
                        displayForecast(forecastResponse);
                    }
                }, throwable -> {})
        );

    }

    private void displayForecast(ForecastResponse forecastResponse){
       //Log.d(TAG, "displayForecast: "+forecastResponse.getList().get(0).getwList().size());
        ForecastAdapter forecastAdapter =   new ForecastAdapter(MainActivity.this, forecastResponse);
        forecast_recycler.setAdapter(forecastAdapter);
    }

    private class LoadCities extends SimpleAsyncTask<List<String>> {

        @Override
        protected List<String> doInBackgroundSimple() {
            lstCities  = new ArrayList<>();
            try{
                StringBuilder stringBuilder = new StringBuilder();
                InputStream inputStream = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String read;
                while ((read = bufferedReader.readLine()) != null){
                    stringBuilder.append(read);
                    lstCities = new Gson().fromJson(stringBuilder.toString(), new TypeToken<List<String>>(){}.getType());

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return lstCities;
        }

        @Override
        protected void onSuccess(List<String> listCity) {
            super.onSuccess(listCity);

            searchBar.setEnabled(true);
            searchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggestion = new ArrayList<>();
                for(String search: listCity){
                    if(search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                        suggestion.add(search);
                }

                searchBar.setLastSuggestions(suggestion);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {

                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                    searchBar.setPlaceHolder("");
                    searchBar.closeSearch();
                    getWeatherInfo(text.toString());
                    getForecastByCity(text.toString());
                    searchBar.setLastSuggestions(listCity);

                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });
        }
    }

    private void getWeatherInfo(String city){

        compositeDisposable1.add(weatherService.weatherByCity(city,
                Global.API_KEY,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResponse>(){
                    @Override
                    public void accept(WeatherResponse weatherResponse) throws Exception {

                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                                .append(weatherResponse.getWeather().get(0).getIcon())
                                .append(".png").toString()).into(cloudImg);
                        searchBar.setPlaceHolder(weatherResponse.getName());
                        message.setText(weatherResponse.getWeather().get(0).getDescription());
                        temp.setText(String.valueOf(weatherResponse.getMain().getTemp()));
                        realFeel.setText(new StringBuilder(String.valueOf(weatherResponse.getMain().getFeels_like()))
                                .append("°C").toString());
                        pressure.setText(new StringBuilder(String.valueOf(weatherResponse.getMain().getPressure()))
                                .append(" hpa").toString());
                        humidity.setText(new StringBuilder(String.valueOf(weatherResponse.getMain().getHumidity()))
                                .append("%").toString());
                        sunrise.setText(Global.getDate(weatherResponse.getSys().getSunrise()));
                        sunset.setText(Global.getDate(weatherResponse.getSys().getSunset()));
                        wind.setText(new StringBuilder(String.format("%.2f",weatherResponse.getWind().getSpeed() * 1.61)).append(" Km/h").toString());

                    }
                }, throwable -> {

                })
        );
    }

    private void getForecastByCity(String city){
        compositeDisposable2.add(weatherService.forecastByCity(city,
                Global.API_KEY,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ForecastResponse>() {
                    @Override
                    public void accept(ForecastResponse forecastResponse) throws Exception {
                        displayForecast(forecastResponse);
                    }
                }, throwable -> {})
        );
    }

    @Override
    protected void onDestroy() {
        compositeDisposable1.clear();
        compositeDisposable2.clear();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        compositeDisposable1.clear();
        compositeDisposable2.clear();
        super.onStop();
    }
}
