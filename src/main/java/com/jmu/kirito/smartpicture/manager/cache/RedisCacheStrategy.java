package com.jmu.kirito.smartpicture.manager.cache;

import com.jmu.kirito.smartpicture.constant.CacheConstant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
@Order(10)
public class RedisCacheStrategy implements CacheStrategy<String> {

    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void set(String key, String value, long expireTime, TimeUnit unit) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set(key, value, expireTime, unit);
    }

    @Override
    public void truncateCache() {
        log.info("清空Redis缓存");
        // 获取所有带有指定前缀的键
        String pattern = CacheConstant.CACHE_KEY_PREFIX + "*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }


}
