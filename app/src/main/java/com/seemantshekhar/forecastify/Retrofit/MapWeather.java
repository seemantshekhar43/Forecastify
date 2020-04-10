package com.seemantshekhar.forecastify.Retrofit;



import com.seemantshekhar.forecastify.Model.ForecastResponse;
import com.seemantshekhar.forecastify.Model.WeatherResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapWeather {
    @GET("weather")
    Observable<WeatherResponse> weatherByMapLangLat(@Query("lat") String lat,
                                                    @Query("lon") String lang,
                                                    @Query("appid")String appID,
                                                    @Query("units") String unit);

    @GET("weather")
    Observable<WeatherResponse> weatherByCity(@Query("q") String city,
                                                      @Query("appid")String appID,
                                                      @Query("units") String unit);
    @GET("forecast")
    Observable<ForecastResponse> forecastByCity(@Query("q") String city,
                                              @Query("appid")String appID,
                                              @Query("units") String unit);

    @GET("forecast")
    Observable<ForecastResponse> forecastByMapLangLat(@Query("lat") String lat,
                                                      @Query("lon") String lang,
                                                      @Query("appid")String appID,
                                                      @Query("units") String unit);

}
