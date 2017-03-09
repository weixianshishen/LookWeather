package example.com.lookweather.util;

import java.util.List;

import example.com.lookweather.gson.CityApi;
import example.com.lookweather.gson.WeatherApi;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by 黑月 on 2017/3/8.
 */

public interface ApiInterface {
    @GET("weather")
    Observable<WeatherApi> weatherApi(@Query("cityid") String cityId, @Query("key") String key);

    @GET("bing_pic")
    Call<String> picApi();

    @GET("china/{province}/{city}")
    Observable<List<CityApi>> getCity(@Path("province") String province, @Path("city") String city);

}
