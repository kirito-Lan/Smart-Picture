package com.jmu.kirito.smartpicture.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.jmu.kirito.smartpicture.exception.ErrCode;
import com.jmu.kirito.smartpicture.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrCode.PARAMS_ERROR, "上传文件为空");
        long fileSize = multipartFile.getSize();
        final long ALLOW_SIZE = 1024 * 1024L;
        ThrowUtils.throwIf(5 * ALLOW_SIZE < fileSize, ErrCode.PARAMS_ERROR, "文件大侠超过5M限制");
        //检验文件后缀，判断文件是否允许上传
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> AllowSuffix = Arrays.asList("jpg", "jpeg", "png", "webp", "gif", "bmp");
        ThrowUtils.throwIf(!AllowSuffix.contains(fileSuffix), ErrCode.PARAMS_ERROR, "文件格式不支持");
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
