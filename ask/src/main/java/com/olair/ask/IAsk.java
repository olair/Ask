package com.olair.ask;

public interface IAsk<Listener, Result> {

    Listener getListener() throws NoBuildListenerException;

    Listener buildListener();

    void advanceTimeout();

    void onSuccess(Result result);

    void onError();

    void cancel();

    class NoBuildListenerException extends RuntimeException {
    }


}
