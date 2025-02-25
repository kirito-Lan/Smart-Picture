package com.jmu.kirito.smartpicture.annotation;

import com.jmu.kirito.smartpicture.constant.UserConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须要是角色
     */
    String mustRole() default "";
}
