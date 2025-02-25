package com.jmu.kirito.smartpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jmu.kirito.smartpicture.common.BaseResponse;
import com.jmu.kirito.smartpicture.model.dto.user.UserQueryRequest;
import com.jmu.kirito.smartpicture.model.entity.User;
import com.jmu.kirito.smartpicture.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author tinho
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-02-01 15:38:47
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    BaseResponse<?> userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request 请求
     * @return 用户登录信息
     */
    BaseResponse<?> userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    BaseResponse<User> getLoginUser(HttpServletRequest request);

    /**
     * 用户登出
     *
     * @param request 请求
     * @return 是否登出成功
     */
    BaseResponse<Boolean> userLogout(HttpServletRequest request);


    // 管理员使用
    /**
     * 获取脱敏后用户信息
     *
     * @return 脱敏后用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取所有用户列表
     *
     * @return 获取所有用户列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取Wrapper
     *
     * @param userQueryRequest 用户查询请求
     * @return 用户列表
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    //密码加密
    String getEncryptPassword(String password);

    /**
     * 是否为管理员
     * @param user 用户
     * @return 是否为管理员
     */
    boolean isAdmin(User user);

}
