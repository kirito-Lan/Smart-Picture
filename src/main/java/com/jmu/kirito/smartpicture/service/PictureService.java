package com.jmu.kirito.smartpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jmu.kirito.smartpicture.model.dto.picture.PictureQueryRequest;
import com.jmu.kirito.smartpicture.model.dto.picture.PictureReviewRequest;
import com.jmu.kirito.smartpicture.model.dto.picture.PictureUploadByBatchRequest;
import com.jmu.kirito.smartpicture.model.dto.picture.PictureUploadRequest;
import com.jmu.kirito.smartpicture.model.entity.Picture;
import com.jmu.kirito.smartpicture.model.entity.User;
import com.jmu.kirito.smartpicture.model.vo.PictureVO;
import org.springframework.scheduling.annotation.Async;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tinho
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-02-11 10:17:26
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource          图片文件/URL
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片信息
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片封装类
     *
     * @param picture 图片信息
     * @param request 从session中获取User
     * @return Vo
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片封装类
     *
     * @param picturePage 图片分页信息
     * @param request     从session中获取User
     * @return Page</ Vo>
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片信息
     *
     * @param picture 图片
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     *
     * @param picture   图片
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest 图片批量上传请求
     * @param loginUser                  登录用户
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

    /**
     *  cos中删除图片
     * @param oldPicture 旧图片
     */
    @Async
    void clearPictureFile(Picture oldPicture);
}
