package com.jmu.kirito.smartpicture.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum PictureReviewStatusEnum {


    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝",2);


    private final String reviewStatus;
    private final int code;

    PictureReviewStatusEnum(String reviewStatus, int code) {
        this.reviewStatus = reviewStatus;
        this.code = code;
    }

    public static PictureReviewStatusEnum getEnumByCode(int code){
        if (ObjectUtil.isEmpty(code)){
            return null;
        }
        for (PictureReviewStatusEnum statusEnum : PictureReviewStatusEnum.values()) {
            if (code==statusEnum.code){
                return statusEnum;
            }
        }
        return null;
    }


}
