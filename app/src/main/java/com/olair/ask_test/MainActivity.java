package com.olair.ask_test;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

        // 添加演示按钮
        Button demoButton = findViewById(R.id.btn_demo);
        if (demoButton != null) {
            demoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, HorizontalCenterScrollDemoActivity.class);
                    startActivity(intent);
                }
            });
        }
    }


}
