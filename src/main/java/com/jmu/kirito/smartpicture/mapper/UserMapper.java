package com.jmu.kirito.smartpicture.mapper;

import com.jmu.kirito.smartpicture.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mapstruct.Mapper;

/**
* @author tinho
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2025-02-01 15:38:47
* @Entity com.jmu.kirito.smartpicture.model.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




