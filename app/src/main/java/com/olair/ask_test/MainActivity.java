package com.olair.ask_test;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.olair.ask.Callback;
import com.olair.ask_test.gps.AskGpsStatus;
import com.olair.ask_test.gps.AskGpsStatusTentative;
import com.olair.ask_test.gps.AskLocation;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "location---- ";

    LocationManager mLocationManager;

    LocationListener locationListener;

    GpsStatus.Listener tentativeAskGpsStatusListener;

    GpsStatus.Listener gpsStatusListener;

    TextView tvT;

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvT = findViewById(R.id.tv_location);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }


}
