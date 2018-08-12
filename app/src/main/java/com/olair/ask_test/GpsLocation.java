package com.olair.ask_test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;

import com.olair.ask.Callback;
import com.olair.ask_test.gps.AskGpsStatus;
import com.olair.ask_test.gps.AskGpsStatusTentative;
import com.olair.ask_test.gps.AskLocation;

public class GpsLocation {

    private LocationManager locationManager;

    @SuppressLint("MissingPermission")
    public void getLocation(Context context) {

        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        AskLocation askLocation = new AskLocation(20_000) {
            @Override
            public void onSuccess(Location location) {
                locationManager.removeUpdates(getListener());
                // 定位成功处理
            }

            @Override
            public void onError() {
                // 20s内没有定位成功，终止定位
                locationManager.removeUpdates(getListener());
            }
        };

        // 1、开始定位，超时时间20s
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, askLocation.buildListener(), Looper.getMainLooper());

        // 2、2s搜星
        startSearchStar(new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // 搜星成功，开始持续监听卫星信号
                monitorGpsStatus(() -> {
                    // 停止定位
                    locationManager.removeUpdates(askLocation.getListener());
                }, 25, 0.2F, 20_000);
            }

            @Override
            public void onError() {
                // 停止定位
                locationManager.removeUpdates(askLocation.getListener());
            }
        });
    }

    /**
     * 在定位过程中监听卫星状态
     *
     * @param error 卫星变为不可定位时的处理
     */
    private void monitorGpsStatus(Runnable error, float validNR, float validRatio, int maxWaitMillis) {
        AskGpsStatus askGpsStatus = new AskGpsStatus(locationManager, validNR, validRatio, maxWaitMillis) {
            @Override
            public void onError() {
                locationManager.removeGpsStatusListener(getListener());
                error.run();
            }
        };
        GpsStatus.Listener monitorGpsStatus = askGpsStatus.buildListener();
    }

    /**
     * 在开始定位的时候观察两秒内是否能搜索到卫星
     *
     * @param callback 观察结果
     */
    @SuppressLint("MissingPermission")
    private void startSearchStar(Callback<Void> callback) {
        AskGpsStatusTentative askGpsStatusTentative = new AskGpsStatusTentative(2_000) {
            @Override
            public void onSuccess(Void aVoid) {
                // 2s监听成功，开始10s监听
                locationManager.removeGpsStatusListener(getListener());
                callback.onSuccess(aVoid);
            }

            @Override
            public void onError() {
                // 2s监听失败，停止定位
                locationManager.removeGpsStatusListener(getListener());
                callback.onError();
            }
        };
        GpsStatus.Listener tentativeGpsStatus = askGpsStatusTentative.buildListener();
        locationManager.addGpsStatusListener(tentativeGpsStatus);
    }

}
