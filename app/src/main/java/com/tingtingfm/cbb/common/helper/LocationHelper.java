package com.tingtingfm.cbb.common.helper;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.tingtingfm.cbb.TTApplication;

/**
 * Created by lqsir on 2016/4/21.
 */
public class LocationHelper {
    private static volatile LocationHelper helper;

    public LocationClient mLocationClient;

    public LocationListener mLocationListener;

    private BDLocation mLocation;

    private LocationHelper() {
        mLocationClient = new LocationClient(TTApplication.getAppContext());
        mLocationListener = new LocationListener();
        mLocationClient.registerLocationListener(mLocationListener);
        setLocationOption();
    }

    public static LocationHelper getInstance() {
        if (helper == null) {
            synchronized (LocationHelper.class) {
                if (helper == null) {
                    helper = new LocationHelper();
                }
            }
        }

        return helper;
    }

    private void setLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setProdName("com.tingtingfm.cbb");
        option.setTimeOut(1000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("gcj02");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    //百度定位获取省信息
    public String getProvince() {
        return mLocation.getProvince();
    }

    //百度定位获取市信息
    public String getCity() {
        return mLocation.getCity();
    }

    //百度定位获取详细信息
    public String getDistrict() {
        return mLocation.getDistrict();
    }

    //百度定位获取区信息
    public String getAddrStr() {
        return mLocation.getAddrStr();
    }

    //获取BDLocation
    public BDLocation getBDLocation() {
        return mLocation;
    }

    //返回百度定位服务
    public LocationClient getLocationClient() {
        return mLocationClient;
    }

    /**
     * 实现实位回调监听
     */
    public class LocationListener implements BDLocationListener {


        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location != null) {
                mLocation = location;
                System.out.println("location.getAddrStr() = " + location.getAddrStr()
                + " location.getLatitude() = " + location.getLatitude()
                + "location.getLongitude() = " + location.getLongitude());
            }
            mLocationClient.stop();
        }
    }
}
