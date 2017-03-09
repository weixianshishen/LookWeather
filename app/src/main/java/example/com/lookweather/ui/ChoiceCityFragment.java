package example.com.lookweather.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import example.com.lookweather.R;
import example.com.lookweather.constant.Constants;
import example.com.lookweather.db.DbManager;
import example.com.lookweather.db.bean.City;
import example.com.lookweather.db.bean.County;
import example.com.lookweather.db.bean.Province;
import example.com.lookweather.db.dao.CityDao;
import example.com.lookweather.db.dao.CountyDao;
import example.com.lookweather.util.HttpUtil;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by 黑月 on 2017/3/2.
 */

public class ChoiceCityFragment extends Fragment {
    @BindView(R.id.btn_back)
    Button mBtnBack;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.lv_city)
    ListView mListView;

    private Unbinder mBind;
    private ArrayAdapter<String> mAdapter;
    private List<String> mDataList = new ArrayList<>();
    private ProgressDialog progressDialog;

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;
    /**
     * 数据库管理操作
     */
    private DbManager mDbManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_choice_city, container, false);
        mBind = ButterKnife.bind(this, contentView);
        mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(mAdapter);
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mDbManager == null) {
            mDbManager = new DbManager(getActivity());
        }
        queryProvinces();


        mListView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (currentLevel == LEVEL_PROVINCE) {
                selectedProvince = provinceList.get(i);
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList.get(i);
                queryCountries();
            } else if (currentLevel == LEVEL_COUNTY) {
                String weatherId = countyList.get(i).getWeatherId();
                if (getActivity() instanceof MainActivity) {
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();
                } else if (getActivity() instanceof WeatherActivity) {
                    WeatherActivity activity = (WeatherActivity) getActivity();
                    activity.mDrawerLayout.closeDrawers();
                    activity.mSwipeRefresh.setRefreshing(true);
                    //                        activity.requestWeather(weatherId);
                }
            }
        });
        mBtnBack.setOnClickListener(view -> {
            if (currentLevel == LEVEL_COUNTY) {
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                queryProvinces();
            }
        });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDbManager.close();
        mBind.unbind();
    }

    private void queryProvinces() {
        mTvTitle.setText("中国");
        mBtnBack.setVisibility(View.GONE);
        provinceList = mDbManager.query(Province.class);
        if (provinceList.size() > 0) {
            mDataList.clear();
            for (Province province : provinceList) {
                mDataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(Constants.BASE_URL, "province");

        }

    }


    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        mTvTitle.setText(selectedProvince.getProvinceName());
        mBtnBack.setVisibility(View.VISIBLE);
        cityList = mDbManager.query(City.class, CityDao.Properties.ProvinceId.eq(selectedProvince.getProvinceCode()));
        if (cityList.size() > 0) {
            mDataList.clear();
            for (City city : cityList) {
                mDataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            queryFromServer(Constants.BASE_URL + provinceCode, "city");
        }
    }

    private void queryCountries() {
        mTvTitle.setText(selectedCity.getCityName());
        mBtnBack.setVisibility(View.VISIBLE);
        countyList = mDbManager.query(County.class, CountyDao.Properties.CityId.eq(selectedCity.getCityCode()));
        if (countyList.size() > 0) {
            mDataList.clear();
            for (County county : countyList) {
                mDataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            queryFromServer(Constants.BASE_URL + provinceCode + "/" + cityCode, "country");
        }
    }



    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Toast.makeText(getActivity(), "加载失败...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Log.i("feng", "结果" + responseText);
                boolean isSuccess = false;
                if ("province".equals(type)) {
                    try {
                        JSONArray allProvinces = new JSONArray(responseText);
                        for (int i = 0; i < allProvinces.length(); i++) {
                            JSONObject provinceObject = allProvinces.getJSONObject(i);
                            Province province = new Province();
                            province.setProvinceName(provinceObject.getString("name"));
                            province.setProvinceCode(provinceObject.getInt("id"));
                            isSuccess = mDbManager.insert(province);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if ("city".equals(type)) {
                    try {
                        JSONArray allCities = new JSONArray(responseText);
                        for (int i = 0; i < allCities.length(); i++) {
                            JSONObject cityJson = allCities.getJSONObject(i);
                            City city = new City();
                            city.setCityName(cityJson.getString("name"));
                            city.setCityCode(cityJson.getInt("id"));
                            city.setProvinceId(selectedProvince.getProvinceCode());
                            isSuccess = mDbManager.insert(city);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if ("country".equals(type)) {
                    try {
                        JSONArray allCities = new JSONArray(responseText);
                        for (int i = 0; i < allCities.length(); i++) {
                            JSONObject countryJson = allCities.getJSONObject(i);
                            County county = new County();
                            county.setCountyName(countryJson.getString("name"));
                            county.setWeatherId(countryJson.getString("weather_id"));
                            county.setCityId(selectedCity.getCityCode());
                            isSuccess = mDbManager.insert(county);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (isSuccess) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("country".equals(type)) {
                                queryCountries();
                            }
                        }
                    });
                }

            }
        });
    }


    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
