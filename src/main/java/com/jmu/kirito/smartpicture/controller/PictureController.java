package com.jmu.kirito.smartpicture.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jmu.kirito.smartpicture.annotation.AuthCheck;
import com.jmu.kirito.smartpicture.common.BaseResponse;
import com.jmu.kirito.smartpicture.common.DeleteRequest;
import com.jmu.kirito.smartpicture.common.ResultUtils;
import com.jmu.kirito.smartpicture.constant.CacheConstant;
import com.jmu.kirito.smartpicture.constant.UserConstant;
import com.jmu.kirito.smartpicture.enums.PictureReviewStatusEnum;
import com.jmu.kirito.smartpicture.exception.BusinessException;
import com.jmu.kirito.smartpicture.exception.ErrCode;
import com.jmu.kirito.smartpicture.exception.ThrowUtils;
import com.jmu.kirito.smartpicture.manager.cache.CacheManager;
import com.jmu.kirito.smartpicture.model.dto.picture.*;
import com.jmu.kirito.smartpicture.model.entity.Picture;
import com.jmu.kirito.smartpicture.model.entity.User;
import com.jmu.kirito.smartpicture.model.vo.PictureTagCategory;
import com.jmu.kirito.smartpicture.model.vo.PictureVO;
import com.jmu.kirito.smartpicture.service.PictureService;
import com.jmu.kirito.smartpicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RestController()
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private CacheManager cacheManager;

    private final ConcurrentMap<String, Object> keyLockMap = new ConcurrentHashMap<>();


    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart(value = "file",required = false) MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        //两个都空
        if (ObjectUtil.isEmpty(multipartFile) && StrUtil.isBlank(pictureUploadRequest.getFileUrl())) {
            throw new BusinessException(ErrCode.PARAMS_ERROR);
        }
        PictureVO pictureVO = null;
        User loginUser = userService.getLoginUser(request).getData();
        if (ObjectUtil.isEmpty(multipartFile) && ObjectUtil.isNotEmpty(pictureUploadRequest.getFileUrl())) {
            //文件为空，url地址不空
            pictureVO = pictureService.uploadPicture(pictureUploadRequest.getFileUrl(), pictureUploadRequest, loginUser);
        } else if (ObjectUtil.isNotEmpty(multipartFile) && ObjectUtil.isEmpty(pictureUploadRequest.getFileUrl())) {
            //文件不空，url地址空
            pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        }else {
            throw new BusinessException(ErrCode.PARAMS_ERROR);
        }
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //判空
        if (ObjectUtil.isEmpty(deleteRequest) || deleteRequest.getId() < 0) {
            throw new BusinessException(ErrCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request).getData();
        //查找图片是否存在
        Picture picture = pictureService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(picture), ErrCode.NOT_FOUND_ERROR);
        //只有本人或者管理员可以删除图片
        if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrCode.NO_AUTH_ERROR);
        }
        //删除图片
        boolean removed = pictureService.removeById(deleteRequest.getId());
        pictureService.clearPictureFile(picture);
        ThrowUtils.throwIf(!removed, ErrCode.OPERATION_ERROR, "删除图片失败");
        return ResultUtils.success(true);

    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        log.info("PictureController:updatePicture");
        log.info("入口参数：【{}】", pictureUpdateRequest);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureUpdateRequest), ErrCode.PARAMS_ERROR);
        //DTO转实体类
        Picture picture = BeanUtil.copyProperties(pictureUpdateRequest, Picture.class);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        User loginUser = userService.getLoginUser(request).getData();
        //查看图片是否存在
        boolean exists = pictureService.lambdaQuery().eq(Picture::getId, picture.getId()).exists();
        ThrowUtils.throwIf(!exists, ErrCode.NOT_FOUND_ERROR);
        //校验图片
        pictureService.validPicture(picture);
        //填充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        //操作数据库更新
        boolean updated = pictureService.updateById(picture);
        ThrowUtils.throwIf(!updated, ErrCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        log.info("PictureController:ListPictureVoByPage");
        log.info("入口参数：pictureQueryRequest：【{}】", pictureQueryRequest.toString());
        //校验参数
        if (ObjectUtil.isEmpty(pictureQueryRequest)) {
            throw new BusinessException(ErrCode.PARAMS_ERROR);
        }
        //限制每页最大数量
        ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > 20, ErrCode.FORBIDDEN_ERROR);
        //普通用户只能看到审核通过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getCode());
        //查询
        Page<Picture> picturePage = pictureService.page(
                new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize()),
                pictureService.getQueryWrapper(pictureQueryRequest));
        //转封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 从缓存中获取分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        log.info("PictureController:listPictureVOByPageWithCache");
        log.info("入口参数：pictureQueryRequest：【{}】", pictureQueryRequest);
        //校验参数
        if (ObjectUtil.isEmpty(pictureQueryRequest)) {
            throw new BusinessException(ErrCode.PARAMS_ERROR);
        }
        //限制每页最大数量
        ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > 20, ErrCode.FORBIDDEN_ERROR);
        //普通用户只能看到审核通过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getCode());
        //查询
        //构造key 从缓存中获取数据
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String md5DigestAsHex = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = CacheConstant.CACHE_KEY_PREFIX + md5DigestAsHex;
        String cacheValue = cacheManager.get(cacheKey);
        //缓存不空
        if (cacheValue != null) {
            //缓存转换bean不忽略null
            JSONObject jsonObject = JSONUtil.parseObj(cacheValue, JSONConfig.create().setIgnoreNullValue(false));
            Page<PictureVO> PageVoList = JSONUtil.toBean(jsonObject, Page.class);
            return ResultUtils.success(PageVoList);
        }
        // 缓存中没有数据，从数据库中获取将数据存入缓存
        Object lock = keyLockMap.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            Page<PictureVO> pictureVOPage;
            try {
                if ((cacheValue = cacheManager.get(cacheKey)) != null) {
                    //缓存转换bean不忽略null
                    JSONObject jsonObject = JSONUtil.parseObj(cacheValue, JSONConfig.create().setIgnoreNullValue(false));
                    Page<PictureVO> PageVoList = JSONUtil.toBean(jsonObject, Page.class);
                    return ResultUtils.success(PageVoList);
                }
                // 查询DB
                Page<Picture> picturePage = pictureService.page(
                        new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize()),
                        pictureService.getQueryWrapper(pictureQueryRequest));
                //转封装类
                pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
                //null值也存入缓存防止缓存穿透
                cacheManager.set(cacheKey, JSONUtil.toJsonStr(pictureVOPage), CacheConstant.EXPIRE_TIME, CacheConstant.EXPIRE_SECONDS);
            } finally {
                keyLockMap.remove(cacheKey);
            }
            return ResultUtils.success(pictureVOPage);
        }
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        //检验参数
        log.info("PictureController:editPicture");
        log.info("入口参数：pictureEditRequest【{}】", pictureEditRequest);
        if (pictureEditRequest == null || pictureEditRequest.getId() < 0) {
            throw new BusinessException(ErrCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request).getData();
        //Dto转实体类
        Picture picture = BeanUtil.copyProperties(pictureEditRequest, Picture.class);
        // 将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        //修改编辑时间
        picture.setEditTime(new Date());
        //填充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        // 数据校验
        pictureService.validPicture(picture);
        //查看图片是否存在
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureService.getById(picture.getId())), ErrCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取图片的分类和标签  (项目规模不大的时候可以直接写死，更具项目变化再进行修改)
     *
     * @return pictureTagCategory
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param request              从session中获取User
     * @return BaseResponse<Boolean>
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        log.info("图片审核请求：PictureController:editPicture");
        log.info("入口参数：pictureReviewRequest【{}】", pictureReviewRequest);
        pictureService.doPictureReview(pictureReviewRequest, userService.getLoginUser(request).getData());
        return ResultUtils.success(true);
    }

    /**
     * 通过 URL 上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request).getData();
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request).getData();
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }


}
