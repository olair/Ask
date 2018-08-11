package com.olair.ask_test;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.olair.ask_test.gps.AskGpsStatus;
import com.olair.ask_test.gps.AskLocationListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "location---- ";

    LocationManager mLocationManager;

    LocationListener locationListener;

    TextView tvT;

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvT = findViewById(R.id.tv_location);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        tvT.setOnClickListener((view) -> {
            AskLocationListener askLocationListener = new AskLocationListener(10_000) {
                @Override
                public void onSuccess(Location location) {
                    mLocationManager.removeUpdates(getListener());
                    Log.i(TAG, "onSuccess: " + location);
                }

                @Override
                public void onError() {
                    mLocationManager.removeUpdates(getListener());
                    Log.i(TAG, "onError: " + System.currentTimeMillis());
                }
            };

            AskGpsStatus askGpsStatus = new AskGpsStatus(mLocationManager, 25F, 0.3F, 11_000) {
                @Override
                public void onSuccess(Float aFloat) {
                    mLocationManager.removeGpsStatusListener(getListener());
                    Log.i(TAG, "onSuccess: " + aFloat);
                }

                @Override
                public void onError() {
                    mLocationManager.removeGpsStatusListener(getListener());
                    Log.i(TAG, "onError: ");
                }
            };

            Log.i(TAG, "onCreate: " + System.currentTimeMillis());
            mLocationManager.addGpsStatusListener(askGpsStatus.buildListener());
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, askLocationListener.buildListener());
        });

    }

}
