package com.wxx.docker.config;

public interface ReturnHandle<T> {
    /**
     * 业务处理
     *
     * @return
     */
    T execute();
}
