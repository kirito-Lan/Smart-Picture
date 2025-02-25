package com.jmu.kirito.smartpicture.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.jmu.kirito.smartpicture.config.CosClientConfig;
import com.jmu.kirito.smartpicture.exception.BusinessException;
import com.jmu.kirito.smartpicture.exception.ErrCode;
import com.jmu.kirito.smartpicture.exception.ThrowUtils;
import com.jmu.kirito.smartpicture.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    // ...

    /**
     * 上传图片
     *
     * @param multipartFile 文件
     * @param uploadPrefix  上传前缀
     * @return 上传结果
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPrefix) {
        log.info("【FileManager:uploadPicture】上传图片解析信息");
        log.info("【FileManager:uploadPicture】上传图片 uploadPrefix:【{}】", uploadPrefix);
        //图片校验
        validPicture(multipartFile);
        //图片上传处理文件名称
        String uuid = RandomUtil.randomString(16);
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        String formatDate = DateUtil.formatDate(new Date());
        String uploadPath = String.format("/%s/%s_%s.%s", uploadPrefix, formatDate, uuid, fileSuffix);
        File file = null;
        try {
            //创建零时文件
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            //上传文件
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            int pictureWidth = imageInfo.getWidth();
            int pictureHeight = imageInfo.getHeight();
            //返回结果
            UploadPictureResult result = new UploadPictureResult();
            result.setUrl(cosClientConfig.getHost() + uploadPath);
            result.setPicName(FileUtil.mainName(multipartFile.getOriginalFilename()));
            result.setPicSize(FileUtil.size(file));
            result.setPicWidth(pictureWidth);
            result.setPicHeight(pictureHeight);
            result.setPicScale(NumberUtil.round(pictureWidth * 1.0 / pictureHeight, 2, RoundingMode.HALF_UP).doubleValue());//宽高比
            result.setPicFormat(imageInfo.getFormat());
            return result;
            //获取图片信息
        } catch (Exception e) {
            log.error("上传图片失败", e);
            throw new BusinessException(ErrCode.SYSTEM_ERROR, "上传图片失败");
        } finally {
            this.deleteTempFile(file);
        }
    }

    /**
     * 删除临时文件
     *
     * @param file 文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        if (!delete) {
            log.error("文件删除失败 Path:{}", file.getAbsoluteFile());
        }
    }


    /**
     * 图片校验
     *
     * @param multipartFile 文件
     */
    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrCode.PARAMS_ERROR, "上传文件为空");
        long fileSize = multipartFile.getSize();
        final long ALLOW_SIZE = 1024 * 1024L;
        ThrowUtils.throwIf(5 * ALLOW_SIZE < fileSize, ErrCode.PARAMS_ERROR, "文件大侠超过5M限制");
        //检验文件后缀，判断文件是否允许上传
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> AllowSuffix = Arrays.asList("jpg", "jpeg", "png", "webp", "gif", "bmp");
        ThrowUtils.throwIf(!AllowSuffix.contains(fileSuffix), ErrCode.PARAMS_ERROR, "文件格式不支持");
    }
}

