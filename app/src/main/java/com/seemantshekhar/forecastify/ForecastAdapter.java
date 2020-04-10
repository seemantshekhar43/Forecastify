package com.seemantshekhar.forecastify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seemantshekhar.forecastify.Model.ForecastResponse;
import com.squareup.picasso.Picasso;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.MyViewHolder> {
    Context context;
    ForecastResponse forecastResponse;

    public ForecastAdapter(Context context, ForecastResponse forecastResponse) {
        this.context = context;
        this.forecastResponse = forecastResponse;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_forecast, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                    .append(forecastResponse.getList().get(position).getWeather().get(0).getIcon())
                .append(".png").toString()).into(holder.img);
        holder.msg.setText(forecastResponse.getList().get(position).getWeather().get(0).getDescription());
        holder.day.setText(Global.getDay(forecastResponse.getList().get(position).getDt()));
        holder.temp.setText(new StringBuilder(String.valueOf(forecastResponse.getList().get(position).getMain().getTemp()))
                    .append("°C").toString());
        holder.city.setText(forecastResponse.getCity().getName());
        holder.time.setText(Global.getDate(forecastResponse.getList().get(position).getDt()));


    }

    @Override
    public int getItemCount() {
        return forecastResponse.getList().size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView day, msg, temp, time, city;
        ImageView img;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            img = (ImageView)itemView.findViewById(R.id.forecast_img);
            day  = (TextView)itemView.findViewById(R.id.forecast_day);
            msg = (TextView) itemView.findViewById(R.id.forecast_msg);
            temp = (TextView) itemView.findViewById(R.id.forecast_temp);
            time = (TextView) itemView.findViewById(R.id.forecast_time);
            city = (TextView) itemView.findViewById(R.id.forecast_city);
        }
    }
}
