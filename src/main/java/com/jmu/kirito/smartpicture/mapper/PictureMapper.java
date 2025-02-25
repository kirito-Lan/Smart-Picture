package com.jmu.kirito.smartpicture.mapper;

import com.jmu.kirito.smartpicture.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mapstruct.Mapper;

/**
* @author tinho
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2025-02-11 10:17:26
* @Entity com.jmu.kirito.smartpicture.model.entity.Picture
*/
@Mapper
public interface PictureMapper extends BaseMapper<Picture> {

}




