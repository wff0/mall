package com.wff.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/5/17 22:22
 */
@Configuration
public class MyRedisConfig {
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.21.128:6379");
        return Redisson.create(config);
    }
}
