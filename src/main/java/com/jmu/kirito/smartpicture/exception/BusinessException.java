package com.jmu.kirito.smartpicture.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrCode ErrCode) {
        super(ErrCode.getMessage());
        this.code = ErrCode.getCode();
    }

    public BusinessException(ErrCode ErrCode, String message) {
        super(message);
        this.code = ErrCode.getCode();
    }

}