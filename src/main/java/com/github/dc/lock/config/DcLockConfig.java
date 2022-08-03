package com.github.dc.lock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * <p>
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/8/3 8:33
 */
@Configuration
public class DcLockConfig {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        if (StringUtils.hasText(redisProperties.getUrl())) {
            config.useSingleServer().setAddress(redisProperties.getUrl());
        } else {
            config.useSingleServer().setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort());
        }
        config.useSingleServer().setPassword(redisProperties.getPassword());
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
