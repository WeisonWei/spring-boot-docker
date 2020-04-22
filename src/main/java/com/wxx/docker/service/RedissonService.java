package com.wxx.docker.service;


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

    @Transactional(rollbackFor = Exception.class)
    public <T> T lock(Long userId, String lockFlag, Supplier<T> supplier) {
        RLock rLock = getLock(userId, lockFlag);
        T result = null;
        try {
            boolean isGetLock = rLock.tryLock(10, 10, TimeUnit.SECONDS);
            log.debug("业务ID{}，获取锁成功", userId);
            while (isGetLock) {
                result = supplier.get();
                break;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> T lockOnce(Long userId, String lockFlag, Supplier<T> supplier) {
        RLock rLock = getLock(userId, lockFlag);
        boolean isGetLock = false;
        T result = null;

        try {
            isGetLock = rLock.tryLock(10, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (isGetLock) {
            try {
                log.debug("userId{}，获取锁成功", userId);
                result = supplier.get();
            } finally {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }
        }
        return result;
    }

    /**
     * 锁格式: "userId_业务标示"
     *
     * @param userId
     * @param lockFlag
     * @return
     */
    private RLock getLock(Long userId, String lockFlag) {
        log.debug("获取分布式锁userId:{},lockFlag:{}", userId, lockFlag);

        if (Objects.isNull(userId)) {
            throw new RuntimeException("userId为空");
        }
        if (Objects.isNull(lockFlag)) {
            throw new RuntimeException("lockFlag为空");
        }
        String lockKey = userId.toString() + "_" + lockFlag;
        return redissonClient.getFairLock(lockKey);
    }
}
