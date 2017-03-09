package example.com.lookweather.util;

import java.util.concurrent.TimeUnit;

import example.com.lookweather.constant.Constants;
import example.com.lookweather.gson.WeatherApi;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by 黑月 on 2017/3/8.
 */

public class RetrofitSingleton {
    private static Retrofit sRetrofit = null;
    private static OkHttpClient sOkHttp = null;
    private static ApiInterface sApiService = null;

    private RetrofitSingleton() {
        init();
    }

    public static RetrofitSingleton getInstance() {
        return RetrofitHolder.instance;
    }

    private static class RetrofitHolder {
        private static final RetrofitSingleton instance = new RetrofitSingleton();
    }

    private void init() {
        initOkHttp();
        initRetrofit();
        sApiService = sRetrofit.create(ApiInterface.class);
    }

    private void initOkHttp() {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.addInterceptor(loggingInterceptor);

        //设置超时
        client.connectTimeout(15, TimeUnit.SECONDS);
        client.readTimeout(20, TimeUnit.SECONDS);
        client.writeTimeout(20, TimeUnit.SECONDS);
        //错误重连
        client.retryOnConnectionFailure(true);
        sOkHttp = client.build();

    }

    private void initRetrofit() {
        sRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.WEATHER_URL)
                .client(sOkHttp)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public ApiInterface getApiService() {
        return sApiService;
    }

    public Observable<WeatherApi.HeWeatherEntity> fetchWeather(String city) {
        return sApiService.weatherApi(city, Constants.WEATHER_KEY)
                .map(weatherApi -> weatherApi.getHeWeather().get(0))
                .compose(RxUtils.rxSchedulerHelper());
    }

    public Call<String> fetchPic() {
        return sApiService.picApi();
    }


}
