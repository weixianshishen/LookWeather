package example.com.lookweather.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.lookweather.R;
import example.com.lookweather.gson.WeatherApi;
import example.com.lookweather.service.AutoUpdateService;
import example.com.lookweather.util.HttpUtil;
import example.com.lookweather.util.RetrofitSingleton;
import example.com.lookweather.util.SPUtil;
import example.com.lookweather.util.SimpleSubscribe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.bing_pic_img)
    ImageView mBingPicImg;
    @BindView(R.id.nav_button)
    Button mNavButton;
    @BindView(R.id.title_city)
    TextView mTitleCity;
    @BindView(R.id.title_update_time)
    TextView mTitleUpdateTime;
    @BindView(R.id.degree_text)
    TextView mDegreeText;
    @BindView(R.id.weather_info_text)
    TextView mWeatherInfoText;
    @BindView(R.id.forecast_layout)
    LinearLayout mForecastLayout;
    @BindView(R.id.aqi_text)
    TextView mAqiText;
    @BindView(R.id.pm25_text)
    TextView mPm25Text;
    @BindView(R.id.comfort_text)
    TextView mComfortText;
    @BindView(R.id.car_wash_text)
    TextView mCarWashText;
    @BindView(R.id.sport_text)
    TextView mSportText;
    @BindView(R.id.weather_layout)
    ScrollView mWeatherLayout;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);

        String weatherString = SPUtil.getInstance(this).getStringValue("weather");
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            WeatherApi.HeWeatherEntity weatherEntity = HttpUtil.handleWeatherResponse(weatherString);
            showWeatherInfo(weatherEntity);
            mWeatherId = weatherEntity.getBasic().getId();
        } else {
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.INVISIBLE);
            fetchDataByNetWork(mWeatherId);
        }

        mSwipeRefresh.setOnRefreshListener(() -> fetchDataByNetWork(mWeatherId));
        mNavButton.setOnClickListener(view -> mDrawerLayout.openDrawer(GravityCompat.START));

        String bingPic = SPUtil.getInstance(this).getStringValue("bing_pic");
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(mBingPicImg);
        } else {
            loadBingPic();
        }

    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        RetrofitSingleton.getInstance().fetchPic().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String picUrl = response.body().toString();
                Log.i("feng", "nani"+picUrl);
                SPUtil.getInstance(WeatherActivity.this).putValue("bing_pic", picUrl);
                Glide.with(WeatherActivity.this).load(picUrl).into(mBingPicImg);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }


    private void fetchDataByNetWork(String weatherId) {
        RetrofitSingleton.getInstance().fetchWeather(weatherId)
                .subscribe(new SimpleSubscribe<WeatherApi.HeWeatherEntity>() {
                    @Override
                    public void onNext(WeatherApi.HeWeatherEntity heWeatherEntity) {
                        showWeatherInfo(heWeatherEntity);
                        String weather = new Gson().toJson(heWeatherEntity);
                        SPUtil.getInstance(WeatherActivity.this).putValue("weather", weather);
                    }

                    @Override
                    public void onComplete() {
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(WeatherApi.HeWeatherEntity weather) {
        String cityName = weather.getBasic().getCity();
        String updateTime = weather.getBasic().getUpdate().getLoc().split(" ")[1];
        String degree = weather.getNow().getTmp() + "℃";
        String weatherInfo = weather.getNow().getCond().getTxt();
        mTitleCity.setText(cityName);
        mTitleUpdateTime.setText(updateTime);
        mDegreeText.setText(degree);
        mWeatherInfoText.setText(weatherInfo);
        mForecastLayout.removeAllViews();
        for (WeatherApi.HeWeatherEntity.DailyForecastEntity forecast : weather.getDaily_forecast()) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, mForecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.getDate());
            infoText.setText(forecast.getCond().getTxt_d());
            maxText.setText(forecast.getTmp().getMax());
            minText.setText(forecast.getTmp().getMin());
            mForecastLayout.addView(view);
        }
        if (weather.getAqi() != null) {
            mAqiText.setText(weather.getAqi().getCity().getAqi());
            mPm25Text.setText(weather.getAqi().getCity().getPm25());
        }
        String comfort = "舒适度：" + weather.getSuggestion().getComf().getTxt();
        String carWash = "洗车指数：" + weather.getSuggestion().getCw().getTxt();
        String sport = "运行建议：" + weather.getSuggestion().getSport().getTxt();
        mComfortText.setText(comfort);
        mCarWashText.setText(carWash);
        mSportText.setText(sport);
        mWeatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
