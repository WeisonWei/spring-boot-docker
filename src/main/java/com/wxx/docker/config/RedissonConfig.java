package com.wxx.docker.config;


import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RedissonConfig {

    @Value("${spring.redis.url}")
    private String address;

    /*@Value("${spring.redis.password}")
    private String password;*/

    @Value("${spring.redis.database}")
    private String database;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        //config.useSingleServer().setAddress("redis://127.0.0.1:6379").setPassword("123456");
        config.useSingleServer().setAddress(address).setDatabase(Integer.valueOf(database));
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}
