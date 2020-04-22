package com.wxx.docker.config;

public interface VoidHandle<T> {
    /**
     * 业务处理
     *
     * @return
     */
    T execute() throws InterruptedException;
}
