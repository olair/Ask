package com.olair.ask.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

public class OlairExecutor {

    private static OlairExecutor INSTANCE = new OlairExecutor();

    public static void execute(Runnable command) {
        INSTANCE.mainThread.execute(command);
    }

    public static void postDelayed(Runnable command, int delayedMillis) {
        INSTANCE.mainThread.postDelayed(command, delayedMillis);
    }


    private final MainThreadExecutor mainThread;

    private OlairExecutor(MainThreadExecutor mainThread) {
        this.mainThread = mainThread;
    }

    private OlairExecutor() {
        this(new MainThreadExecutor());
    }

    private static class MainThreadExecutor {

        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }

        void postDelayed(Runnable command, int delayMillis) {
            mainThreadHandler.postDelayed(command, delayMillis);
        }

    }


}
