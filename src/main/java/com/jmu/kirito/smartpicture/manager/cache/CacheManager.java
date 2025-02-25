package com.jmu.kirito.smartpicture.manager.cache;

import com.jmu.kirito.smartpicture.constant.CacheConstant;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CacheManager {

    @Resource
    private List<CacheStrategy<String>> cacheStrategies;

    public String get(String key) {
        String value = null;
        for (int i = 0; i < cacheStrategies.size(); i++) {
            CacheStrategy<String> strategy = cacheStrategies.get(i);
            value = strategy.get(key);
            if (value != null) {
                // 更新之前的缓存策略
                if (i > 0) {
                    updatePreviousCaches(key, value, i);
                }
                return value;
            }
        }
        return null;
    }

    // 新增方法，更新之前的缓存策略
    private void updatePreviousCaches(String key, String value, int index) {
        for (int i = 0; i < index; i++) {
            CacheStrategy<String> strategy = cacheStrategies.get(i);
            strategy.set(key, value, CacheConstant.EXPIRE_TIME, CacheConstant.EXPIRE_SECONDS);
        }
    }


    public void set(String key, String value, long expireTime, TimeUnit unit) {
        for (CacheStrategy<String> strategy : cacheStrategies) {
            strategy.set(key, value, expireTime, unit);
        }
    }

    public void truncateCaches() {
        for (CacheStrategy<String> strategy : cacheStrategies) {
            strategy.truncateCache();
        }
    }
}
