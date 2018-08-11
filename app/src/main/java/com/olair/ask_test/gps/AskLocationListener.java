package com.olair.ask_test.gps;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.olair.ask.AskTask;

/**
 * 这个类将用于获取定位信息
 */
public abstract class AskLocationListener extends AskTask<LocationListener, Location> {

    private Location mLocation = null;

    protected AskLocationListener(int maxWaitMillis) {
        super(maxWaitMillis);
    }

    @Override
    protected LocationListener buildListener0() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;
                postExecute();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    @Override
    public boolean isSuccess() {
        return mLocation != null;
    }

    @Override
    public Location get() {
        return mLocation;
    }
}
