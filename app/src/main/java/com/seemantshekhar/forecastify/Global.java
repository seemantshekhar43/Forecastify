package com.seemantshekhar.forecastify;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Global {
    public static final String API_KEY = "4959852fea06e3893bbc40ecf08dcc10";
    public static Location current_location = null;

    static String getDate(int dt){
        Date date = new Date(dt *1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        return sdf.format(date);

    }

    static String getDay(int dt){
        Date date = new Date(dt *1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d");
        return sdf.format(date);

    }

}
