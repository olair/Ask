package com.olair.ask;

import android.support.annotation.Nullable;

import com.olair.ask.util.OlairExecutor;

public abstract class Ask<Listener, Result> implements IAsk<Listener, Result> {

    private Listener mListener;

    /**
     * 最长等待时间
     */
    private final int maxWaitMillis;

    /**
     * 等待期间是否成功
     */
    private boolean isSuccess = false;

    /**
     * 是否被取消
     */
    private boolean isCancel = false;

    /**
     * 超时回调是否被提前执行
     */
    private boolean isAdvanceExecute = false;

    Ask(int maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    /**
     * 供外部调用，不可重写
     *
     * @return 由buildListener构造的Listener
     * @throws NoBuildListenerException 调用前必须调用buildListener
     */
    @Override
    public final Listener getListener() throws NoBuildListenerException {
        if (mListener == null) {
            throw new NoBuildListenerException();
        }
        return mListener;
    }

    @Override
    public final void advanceTimeout() {
        isAdvanceExecute = true;
        post(timeoutCallback);
    }

    /**
     * 供外部构造Listener
     *
     * @return 返回Listener对象
     */
    @Override
    public final Listener buildListener() {
        mListener = buildListener0();
        postDelay(() -> {
            if (isCancel || isAdvanceExecute)
                return;
            timeoutCallback.run();
        }, maxWaitMillis);
        return mListener;
    }

    /**
     * 超时以后执行的方法
     */
    private Runnable timeoutCallback = () -> {
        if (!isSuccess()) {
            onError();
        } else {
            success(getResult());
        }
    };

    /**
     * 子类中通知外部已经处理完成
     *
     * @param result 成功结果
     */
    protected final void success(Result result) {
        isSuccess = true;
        isAdvanceExecute = true;
        onSuccess(result);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public void cancel() {
        isCancel = true;
    }

    /**
     * 子类必须实现(不要在外部调用)
     *
     * @return 返回Listener
     */
    protected abstract Listener buildListener0();

    @Nullable
    protected abstract Result getResult();

    @Override
    public abstract void onSuccess(Result result);

    @Override
    public abstract void onError();

    private void post(Runnable r) {
        OlairExecutor.execute(r);
    }

    private void postDelay(Runnable r, int delayMillis) {
        OlairExecutor.postDelayed(r, delayMillis);
    }
}