package com.wxx.docker;


import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class Controller {

    private static int num = 0;


    @Autowired
    RedissonClient redissonClient;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }


    @GetMapping("/lock1")
    public String test(@RequestParam String threadName) throws InterruptedException {
        RLock lock = redissonClient.getFairLock("myLock");
        boolean res = lock.tryLock(10, 20, TimeUnit.SECONDS);
        while (res) {
            try {
                int i = num + 1;
                setThreadName(threadName + i);
                log.info(Thread.currentThread().getName() + "lock1:" + i);
                TimeUnit.SECONDS.sleep(3);
                return String.valueOf(i);
            } finally {
                lock.unlock();
            }
        }
        return Thread.currentThread().getName() + "未获取锁";
    }

    @GetMapping("/lock2")
    public String test1(@RequestParam String name) throws InterruptedException {
        RLock lock = redissonClient.getFairLock("myLock");
        boolean res = lock.tryLock(10, 20, TimeUnit.SECONDS);
        while (res) {
            try {
                int i = num + 1;
                setThreadName(name + i);
                log.info(Thread.currentThread().getName() + "lock2:" + i);
                TimeUnit.SECONDS.sleep(3);
                return String.valueOf(i);
            } finally {
                lock.unlock();
            }
        }
        return Thread.currentThread().getName() + "未获取锁";
    }

    private void setThreadName(@RequestParam String name) {
        Thread.currentThread().setName(name);
    }
}
