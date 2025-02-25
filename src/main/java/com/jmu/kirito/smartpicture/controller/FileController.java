package com.jmu.kirito.smartpicture.controller;

import com.jmu.kirito.smartpicture.annotation.AuthCheck;
import com.jmu.kirito.smartpicture.common.BaseResponse;
import com.jmu.kirito.smartpicture.common.ResultUtils;
import com.jmu.kirito.smartpicture.constant.UserConstant;
import com.jmu.kirito.smartpicture.exception.BusinessException;
import com.jmu.kirito.smartpicture.exception.ErrCode;
import com.jmu.kirito.smartpicture.manager.CosManager;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    @Resource
    private COSClient cosClient;

    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> uploadFileTest(@RequestPart("file") MultipartFile file) {
        log.info("【FileController:uploadFileTest】上传文件");
        File files = null;
        try {
            //文件名称
            String fileName = file.getOriginalFilename();
            //文件目录
            String path = String.format("test/%s", fileName);
            files = File.createTempFile(path, null);
            file.transferTo(files);
            PutObjectResult putObjectResult = cosManager.putObject(path, files);
            return ResultUtils.success(putObjectResult.getETag()); //即使提前返回也会执行finally
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (files != null) {
                boolean delete = files.delete();
                if (!delete) {
                    log.error("文件删除失败");
                }
            }
        }
    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void downloadFileTest(String filepath, HttpServletResponse response) throws IOException {
        log.info("【FileController:testDownloadFile】下载文件");
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath {}", filepath, e);
            throw new BusinessException(ErrCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }


}
