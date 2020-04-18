package com.wxx.docker;


import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@Slf4j
public class Controller {

    private static volatile int NUMBER = 0;
    private static volatile int CORE_DATA_FLAG = 0;
    private static final Lock FAIR_LOCK = new ReentrantLock(true);
    private static final Lock NON_FAIR_LOCK = new ReentrantLock();

    @Resource
    RedissonClient redissonClient;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @PutMapping("/locks")
    public String setZero() {
        CORE_DATA_FLAG = 0;
        return "OK";
    }

    @GetMapping("/locks/n")
    public String noLock(@RequestParam String name) {
        NUMBER++;
        setThreadName(name + NUMBER);
        if (CORE_DATA_FLAG < 1) {
            CORE_DATA_FLAG++;
        }
        log.info(Thread.currentThread().getName() + "noLock:" + NUMBER);
        log.info("noLock:" + Thread.currentThread().getName() + "  num-->" + NUMBER + "  flag-->" + CORE_DATA_FLAG);
        return "OK";
    }

    @GetMapping("/locks/s")
    public synchronized String synchronizedLock(@RequestParam String name) {
        long begin = System.currentTimeMillis();
        NUMBER++;
        setThreadName(name + NUMBER);
        log.info(Thread.currentThread().getName() + "synchronized:" + NUMBER);
        long end = System.currentTimeMillis();
        return String.valueOf(NUMBER) + "cost:" + (end - begin) + "ms";
    }

    @GetMapping("/locks/f")
    public String rLockFairLock(@RequestParam String name) throws InterruptedException {
        long begin = System.currentTimeMillis();
        FAIR_LOCK.lock();
        try {
            NUMBER++;
            setThreadName(name + NUMBER);
            log.info(Thread.currentThread().getName() + "rLockFairLock:" + NUMBER);
        } finally {
            FAIR_LOCK.unlock();
        }
        long end = System.currentTimeMillis();
        return String.valueOf(NUMBER) + "cost:" + (end - begin) + "ms";
    }

    @GetMapping("/locks/uf")
    public String rLockUnFairLock(@RequestParam String name) throws InterruptedException {
        long begin = System.currentTimeMillis();
        NON_FAIR_LOCK.tryLock(2, TimeUnit.SECONDS);
        try {
            NUMBER++;
            setThreadName(name + NUMBER);
            TimeUnit.SECONDS.sleep(3);
        } finally {
            NON_FAIR_LOCK.unlock();
        }
        long end = System.currentTimeMillis();
        log.info("rLockUnFairLock:" + Thread.currentThread().getName() + "  num-->" + NUMBER + "  flag-->" + CORE_DATA_FLAG);
        return String.valueOf(NUMBER) + "cost:" + (end - begin) + "ms";
    }

    @GetMapping("/locks/rl1")
    public String redisRLock1(@RequestParam String name) throws InterruptedException {
        long begin = System.currentTimeMillis();
        int number = 0;
        RLock lock = redissonClient.getFairLock(name);
        boolean res = lock.tryLock(11, 11, TimeUnit.SECONDS);
        while (res) {
            try {
                NUMBER++;
                setThreadName(name + number);
                log.info(Thread.currentThread().getName() + "lock1:" + number);
                log.info(Thread.currentThread().getName() + "redisRLock1:" + number);
                TimeUnit.SECONDS.sleep(6);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
                res = false;
                break;
            }
        }
        long end = System.currentTimeMillis();
        log.info(number + "cost:" + (end - begin) + "ms");
        return number + "cost:" + (end - begin) + "ms";
        //return Thread.currentThread().getName() + "未获取锁";
    }

    @GetMapping("/locks/rl2")
    public String redisRLock2(@RequestParam String name) throws InterruptedException {
        long begin = System.currentTimeMillis();
        RLock lock = redissonClient.getFairLock("myLock");
        boolean res = lock.tryLock(11, 11, TimeUnit.SECONDS);
        while (res) {
            try {
                NUMBER++;
                setThreadName(name + NUMBER);
                log.info(Thread.currentThread().getName() + "redisRLock2:" + NUMBER);
                TimeUnit.SECONDS.sleep(3);
                return String.valueOf(NUMBER);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
                res = false;
                break;
            }
        }
        long end = System.currentTimeMillis();
        log.info(NUMBER + "cost:" + (end - begin) + "ms");
        return NUMBER + "cost:" + (end - begin) + "ms";
    }

    private void setThreadName(@RequestParam String name) {
        Thread.currentThread().setName(name);
    }
}
