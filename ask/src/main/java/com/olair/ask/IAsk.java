package com.olair.ask;

import com.olair.ask.util.NoBuildListenerException;

public interface IAsk<Listener, Result> {

    /**
     * @return 用于构建异步回调对象
     */
    Listener buildListener();

    /**
     * @return 拿到 {@link #buildListener()}构建的异步回调
     * @throws NoBuildListenerException 如果没有构建则抛出异常
     */
    Listener getListener() throws NoBuildListenerException;

    /**
     * 在当前线程立即执行
     */
    void execute();

    /**
     * post到主线程执行
     */
    void postExecute();

    /**
     * @return 异步操作是否成功
     */
    boolean isSuccess();

    /**
     * @return 获取结果值
     */
    Result get();

    /**
     * 取消该异步请求回调
     */
    void cancel();

    /**
     * @return 是否被取消掉
     */
    boolean isCancel();

}
