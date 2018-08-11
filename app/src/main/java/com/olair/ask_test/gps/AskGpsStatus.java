package com.olair.ask_test.gps;

import android.annotation.SuppressLint;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.olair.ask.AskTask;

import java.util.Iterator;

import static android.location.GpsStatus.GPS_EVENT_SATELLITE_STATUS;

public abstract class AskGpsStatus extends AskTask<GpsStatus.Listener, Float> {

    private LocationManager locationManager;

    private float validNR;

    private float validRatio;

    private GpsStatus gpsStatus;

    private float validCount;

    private float totalCount;

    protected AskGpsStatus(LocationManager locationManager, float validNR, float validRatio, int maxWaitMillis) {
        super(maxWaitMillis);
        this.locationManager = locationManager;
        this.validNR = validNR;
        this.validRatio = validRatio;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected GpsStatus.Listener buildListener0() {
        return event -> {
            totalCount++;
            if (event == GPS_EVENT_SATELLITE_STATUS) {
                gpsStatus = locationManager.getGpsStatus(gpsStatus);
                Iterator<GpsSatellite> satelliteIterator = gpsStatus.getSatellites().iterator();
                Log.i("location----", "buildListener0: " + validCount(satelliteIterator, 25));
                if (validCount(satelliteIterator, validNR) >= 3) {
                    validCount++;
                }
            }
        };
    }

    @Override
    public boolean isSuccess() {
        return validCount / totalCount >= validRatio;
    }

    @Override
    public Float get() {
        return validCount / totalCount;
    }

    private int validCount(@NonNull Iterator<GpsSatellite> satelliteIterator, float validNR) {
        int count = 0;
        while (satelliteIterator.hasNext()) {
            GpsSatellite gpsSatellite = satelliteIterator.next();
            if (gpsSatellite.getSnr() > validNR) {
                count++;
            }
        }
        return count;
    }
}
