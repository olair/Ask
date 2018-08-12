package com.olair.ask_test.gps;

import android.location.GpsStatus;

import com.olair.ask.AskTask;

public abstract class AskGpsStatusTentative extends AskTask<GpsStatus.Listener, Void> {

    private boolean isSuccess;

    protected AskGpsStatusTentative(int maxWaitMillis) {
        super(maxWaitMillis);
    }

    @Override
    protected GpsStatus.Listener buildListener0() {
        return event -> {
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                isSuccess = true;
                postExecute();
            }
        };
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public Void get() {
        return null;
    }

}
