package com.olair.ask;

import com.olair.ask.util.NoBuildListenerException;
import com.olair.ask.util.OlairExecutor;

public abstract class AskTask<Listener, Result> implements IAsk<Listener, Result>, Callback<Result> {

    /**
     * 异步回调
     */
    private Listener mListener;

    /**
     * 最长等待时间
     */
    private final int mMaxWaitMillis;

    /**
     * 该AskTask是否被取消
     */
    private boolean isCancel = false;

    /**
     * AskTask是否被提前执行
     */
    private boolean isAdvanceExecute = false;

    /**
     * 具体的执行逻辑
     */
    private Runnable executeBody = () -> {
        if (isCancel || isAdvanceExecute)
            return;
        isAdvanceExecute = true;
        if (!isSuccess()) {
            onError();
        } else {
            onSuccess(get());
        }
    };

    public AskTask(int maxWaitMillis) {
        this.mMaxWaitMillis = maxWaitMillis;
    }

    @Override
    public final Listener buildListener() {
        mListener = buildListener0();
        postDelay(executeBody, mMaxWaitMillis);
        return mListener;
    }

    @Override
    public Listener getListener() throws NoBuildListenerException {
        if (mListener == null) {
            throw new NoBuildListenerException();
        }
        return mListener;
    }

    @Override
    public void execute() {
        executeBody.run();
    }

    @Override
    public void postExecute() {
        post(executeBody);
    }

    @Override
    public void cancel() {
        isCancel = true;
    }

    @Override
    public boolean isCancel() {
        return isCancel;
    }

    /**
     * 子类必须实现该方法
     *
     * @return 异步回调方法
     */
    protected abstract Listener buildListener0();


    private void postDelay(Runnable r, int delayMillis) {
        OlairExecutor.postDelayed(r, delayMillis);
    }

    private void post(Runnable command) {
        OlairExecutor.post(command);
    }

}
