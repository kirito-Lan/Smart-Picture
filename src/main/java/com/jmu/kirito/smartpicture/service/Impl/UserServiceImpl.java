package com.jmu.kirito.smartpicture.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jmu.kirito.smartpicture.common.BaseResponse;
import com.jmu.kirito.smartpicture.common.ResultUtils;
import com.jmu.kirito.smartpicture.constant.UserConstant;
import com.jmu.kirito.smartpicture.exception.BusinessException;
import com.jmu.kirito.smartpicture.exception.ErrCode;
import com.jmu.kirito.smartpicture.enums.UserRoleEnum;
import com.jmu.kirito.smartpicture.model.dto.user.UserQueryRequest;
import com.jmu.kirito.smartpicture.model.entity.User;
import com.jmu.kirito.smartpicture.model.vo.UserLoginVO;
import com.jmu.kirito.smartpicture.model.vo.UserVO;
import com.jmu.kirito.smartpicture.service.UserService;
import com.jmu.kirito.smartpicture.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tinho
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-02-01 15:38:47
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public BaseResponse<?> userRegister(String userAccount, String userPassword, String checkPassword) {
        log.info("用户注册请求参数:【{}】【{}】【{}】", userAccount, userPassword, checkPassword);
        //判空
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            return ResultUtils.error(ErrCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        //判断账号长度是否合法
        if (userAccount.length() < 6 || userAccount.length() > 20) {
            return ResultUtils.error(ErrCode.PARAMS_ERROR, "用户账户长度不合法");
        }
        //判断密码长度是否合法
        if (userPassword.length() < 6 || userPassword.length() > 20) {
            return ResultUtils.error(ErrCode.PARAMS_ERROR, "用户密码长度不合法");
        }
        //检验两次密码是否一致
        if (!userPassword.equals(checkPassword)) {
            return ResultUtils.error(ErrCode.PARAMS_ERROR, "两次密码不一致");
        }

        //判断账号是否已存在
        User user = this.lambdaQuery().eq(User::getUserAccount, userAccount).one();
        if (!ObjectUtils.isEmpty(user)) {
            return ResultUtils.error(ErrCode.PARAMS_ERROR, "用户账户已存在");
        }
        //插入用户
        User newUser = new User();
        newUser.setUserAccount(userAccount);
        newUser.setUserPassword(this.getEncryptPassword(userPassword));
        //默认角色为普通用户
        newUser.setUserRole(UserRoleEnum.USER.getRole());
        newUser.setUserName("无名氏");
        this.save(newUser);
        return ResultUtils.success(newUser.getId());
    }


    @Override
    public BaseResponse<?> userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        log.info("用户登录请求参数:【{}】【{}】", userAccount, userPassword);
        //判空
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        //判断数据库是否存在数据
        User user = this.lambdaQuery().eq(User::getUserAccount, userAccount).one();
        log.info("查询出user:【{}】", user);
        if (ObjectUtils.isEmpty(user)) {
            return ResultUtils.error(ErrCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //判断密码是否正确
        if (!this.getEncryptPassword(userPassword).equals(user.getUserPassword())) {
            log.info("用户不存在或密码错误");
            return ResultUtils.error(ErrCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //复制属性
        UserLoginVO userLoginVo = BeanUtil.copyProperties(user, UserLoginVO.class);
        //保存登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        return ResultUtils.success(userLoginVo);
    }

    @Override
    public BaseResponse<User> getLoginUser(HttpServletRequest request) {
        //获取登录用户
        var user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        //判断是否登录
        if (ObjectUtils.isEmpty(user) || ObjectUtils.isEmpty(user.getId())) {
            throw new BusinessException(ErrCode.NOT_LOGIN_ERROR);
        }
        //返回用户信息
        //不追求性能的情况下，可以从数据库查询出最新的
        User currentUser = this.lambdaQuery().eq(User::getId, user.getId()).one();
        if (ObjectUtils.isEmpty(currentUser)) {
            throw new BusinessException(ErrCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success(currentUser);
    }

    @Override
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        //首先判断是否登录
        var user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (ObjectUtils.isEmpty(user)) {
            throw new BusinessException(ErrCode.NOT_LOGIN_ERROR);
        }
        //清除登录状态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return ResultUtils.success(true);
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    //密码加密
    @Override
    public String getEncryptPassword(String password) {
        //加盐
        final String salt = "Kirito";
        password = password + salt;
        return DigestUtils.md5DigestAsHex(password.getBytes());

    }

    @Override
    public boolean isAdmin(User user) {
        //判断是否为管理员
        return user != null && UserRoleEnum.ADMIN.getRole().equals(user.getUserRole());
    }

}




