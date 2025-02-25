package com.jmu.kirito.smartpicture.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jmu.kirito.smartpicture.model.entity.Space;

/**
* @author tinho
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-02-24 09:19:02
*/
public interface SpaceService extends IService<Space> {

    /**
     * 校验空间
     * @param space 空间
     * @param add 是否是新增
     */
    void validSpace(Space space, boolean add);

    /**
     * 根据空间级别，自动填充限额
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);
}
