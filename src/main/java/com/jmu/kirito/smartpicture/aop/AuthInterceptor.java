package com.jmu.kirito.smartpicture.aop;

import com.jmu.kirito.smartpicture.annotation.AuthCheck;
import com.jmu.kirito.smartpicture.exception.BusinessException;
import com.jmu.kirito.smartpicture.exception.ErrCode;
import com.jmu.kirito.smartpicture.enums.UserRoleEnum;
import com.jmu.kirito.smartpicture.model.entity.User;
import com.jmu.kirito.smartpicture.service.UserService;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 权限拦截器
 */
@Aspect
@Component
@AllArgsConstructor
public class AuthInterceptor {

    private final UserService userService;


    /**
     * 权限检查
     */
    @Around("@annotation(authCheck)")
    public Object doAuthCheck(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        //全局上下文获取request
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        //获取到servlet的request
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request).getData();
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByRole(mustRole);
        //不需要权限直接放行
        if (mustRoleEnum == null) {
            joinPoint.proceed();
        }
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByRole(loginUser.getUserRole());
        //当前用户权限为空
        if (userRoleEnum == null) {
            throw new BusinessException(ErrCode.NO_AUTH_ERROR);
        }
        //权限不足
        if (userRoleEnum.getLevel() < mustRoleEnum.getLevel()) {
            throw new BusinessException(ErrCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
