package com.wxx.docker;


import com.wxx.docker.config.ReturnHandle;
import com.wxx.docker.config.VoidHandle;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@Data
@Slf4j
public class RedissonService {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 分布式锁实现
     *
     * @param lockName 锁名称
     * @param userId   业务ID
     * @param handle   业务处理
     */
    @Transactional(rollbackFor = Exception.class)
    public void lock(Long userId, String lockName, VoidHandle handle) {
        RLock rLock = getLock(lockName, userId);
        try {
            boolean isGetLock = rLock.tryLock(10, 10, TimeUnit.SECONDS);
            log.info("业务ID{}，获取锁成功", userId);
            while (isGetLock) {
                handle.execute();
                break;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    /**
     * 带返回值分布式锁实现
     *
     * @param lockName 锁名称
     * @param userId   业务ID
     * @param handle   业务处理
     * @param <T>      返回值
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T lock(Long userId, String lockName, Supplier<T> handle) {
        RLock rLock = getLock(lockName, userId);
        T execute = null;
        try {
            boolean isGetLock = rLock.tryLock(10, 10, TimeUnit.SECONDS);
            log.info("业务ID{}，获取锁成功", userId);
            while (isGetLock) {
                execute = handle.get();
                break;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
        return execute;
    }

    /**
     * 分布式锁实现
     *
     * @param lockName 锁名称
     * @param userId   业务ID
     * @param handle   业务处理
     */
    @Transactional(rollbackFor = Exception.class)
    public void tryLock(String lockName, Long userId, VoidHandle handle) {
        RLock rLock = getLock(lockName, userId);
        if (!rLock.tryLock()) {
            log.info("业务ID{}，获取锁失败，返回", userId);
            return;
        }

        try {
            log.info("业务ID{}，获取锁成功", userId);
            handle.execute();
        } finally {
            rLock.unlock();
        }
    }

    /**
     * 带返回值分布式锁实现
     *
     * @param lockName 锁名称
     * @param userId   业务ID
     * @param handle   业务处理
     * @param <T>      返回值
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T tryLock(String lockName, Long userId, ReturnHandle<T> handle) {
        RLock rLock = getLock(lockName, userId);
        if (!rLock.tryLock()) {
            log.info("业务ID{}，获取锁失败，返回null", userId);
            return null;
        }

        try {
            log.info("业务ID{}，获取锁成功", userId);
            return handle.execute();
        } finally {
            rLock.unlock();
        }
    }

    /**
     * 分布式锁实现
     *
     * @param lockName 锁名称
     * @param userId   业务ID
     * @param handle   业务处理
     */
    @Transactional(rollbackFor = Exception.class)
    public void tryLockException(String lockName, Long userId, VoidHandle handle) {
        RLock rLock = getLock(lockName, userId);
        if (!rLock.tryLock()) {
            log.info("业务ID{}，获取锁失败，抛异常处理", userId);
            throw new RuntimeException("处理中");
        }

        try {
            log.info("业务ID{}，获取锁成功", userId);
            handle.execute();
        } finally {
            rLock.unlock();
        }
    }

    /**
     * 带返回值分布式锁实现
     *
     * @param lockName 锁名称
     * @param userId   业务ID
     * @param handle   业务处理
     * @param <T>      返回值
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T tryLockException(String lockName, Long userId, ReturnHandle<T> handle) {
        RLock rLock = getLock(lockName, userId);
        if (!rLock.tryLock()) {
            log.info("业务ID{}，获取锁失败，抛异常处理", userId);
            throw new RuntimeException("处理中");
        }

        try {
            log.info("业务ID{}，获取锁成功", userId);
            return handle.execute();
        } finally {
            rLock.unlock();
        }
    }

    /**
     * 获取锁
     *
     * @param lockFlag
     * @param userId
     * @return
     */
    private RLock getLock(String lockFlag, Long userId) {
        log.info("获取分布式锁lockName:{},userId:{}", lockFlag, userId);
        if (Objects.isNull(lockFlag)) {
            throw new RuntimeException("分布式锁KEY为空");
        }
        if (Objects.isNull(userId)) {
            throw new RuntimeException("业务ID为空");
        }

        String lockKey = userId.toString() + "_" + lockFlag;
        return redissonClient.getFairLock(lockKey);
    }
}
