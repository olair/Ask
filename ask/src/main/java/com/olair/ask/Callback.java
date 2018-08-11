package com.olair.ask;

public interface Callback<Result> {

    void onSuccess(Result result);

    void onError();
}
