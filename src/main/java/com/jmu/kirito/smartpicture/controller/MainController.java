package com.jmu.kirito.smartpicture.controller;

import com.jmu.kirito.smartpicture.common.BaseResponse;
import com.jmu.kirito.smartpicture.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class MainController {

    /**
     * 健康检查
     * @return BaseResponse
     */
    @GetMapping("healthCheck")
    public BaseResponse<?> healthCheck() {
        return ResultUtils.success("ok");
    }


}
