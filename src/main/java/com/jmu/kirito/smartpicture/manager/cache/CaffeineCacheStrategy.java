package com.jmu.kirito.smartpicture.manager.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.jmu.kirito.smartpicture.constant.CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(5)
public class CaffeineCacheStrategy implements CacheStrategy<String> {

    private final Cache<String, String> cache;

    public CaffeineCacheStrategy() {
        this.cache = Caffeine.newBuilder()
                .initialCapacity(1024)
                .maximumSize(10000L)
                .expireAfterWrite(5L, TimeUnit.MINUTES)
                .removalListener((String key, String value, RemovalCause cause) -> {
                    // 处理删除事件
                    log.info("Caffeine缓存删除：【key: {}, value: {}, cause: {}】", key, value, cause);
                })
                .build();
    }

    @Override
    public String get(String key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void set(String key, String value, long expireTime, TimeUnit unit) {
        cache.put(key, value);
        //使用策略覆盖存在的key的默认过期时间 但是有二级缓存可以不设置？
        cache.policy().expireVariably().ifPresent(policy -> policy.put(key, value, expireTime, unit));
    }

    @Override
    public void truncateCache() {
        log.info("清空Caffeine缓存");
        cache.asMap().keySet()
                .stream()
                .filter(key -> key.startsWith(CacheConstant.CACHE_KEY_PREFIX))
                .forEach(cache::invalidate);
    }
}

