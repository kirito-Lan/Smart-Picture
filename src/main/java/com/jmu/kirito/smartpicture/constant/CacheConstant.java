package com.jmu.kirito.smartpicture.constant;

import cn.hutool.core.util.RandomUtil;

import java.util.concurrent.TimeUnit;

public interface CacheConstant {

    String CACHE_KEY_PREFIX = "smart:picture:";


    TimeUnit EXPIRE_MINUTES = TimeUnit.MINUTES;

    TimeUnit EXPIRE_SECONDS = TimeUnit.SECONDS;

    long EXPIRE_TIME = RandomUtil.randomLong(300, 600);

}
