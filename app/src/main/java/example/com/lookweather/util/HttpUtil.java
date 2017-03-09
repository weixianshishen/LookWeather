package example.com.lookweather.util;

import com.google.gson.Gson;

import example.com.lookweather.gson.WeatherApi;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    public static void sendOkHttpRequest(String address, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static WeatherApi.HeWeatherEntity handleWeatherResponse(String response) {

        return new Gson().fromJson(response, WeatherApi.HeWeatherEntity.class);
    }

}
