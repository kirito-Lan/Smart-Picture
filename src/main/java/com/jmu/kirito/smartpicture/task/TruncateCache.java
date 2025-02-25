package com.jmu.kirito.smartpicture.task;

import com.jmu.kirito.smartpicture.manager.cache.CacheManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class TruncateCache {

    private final CacheManager cacheManager;

    //每天凌晨三点清空缓存
    @Scheduled(cron = "0 0 3 * * ?")
    public void truncateCache() {
        log.info("进行清空缓存任务");
        cacheManager.truncateCaches();
    }
}
