package com.jmu.kirito.smartpicture.manager.cache;

import java.util.concurrent.TimeUnit;

public interface CacheStrategy<T> {
    T get(String key);

    void set(String key, T value, long expireTime, TimeUnit unit);

    void truncateCache();
}

