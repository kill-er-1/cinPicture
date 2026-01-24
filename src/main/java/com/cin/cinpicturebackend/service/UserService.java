package com.cin.cinpicturebackend.service;

import com.cin.cinpicturebackend.model.dto.user.UserQueryRequest;
import com.cin.cinpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cin.cinpicturebackend.model.vo.LoginUserVO;
import com.cin.cinpicturebackend.model.vo.UserVO;

import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * @author cin
 * @description 针对表【user】的数据库操作Service
 * @createDate 2026-01-19 09:49:51
 */
public interface UserService extends IService<User> {

    // 接口里方法默认是 public abstract
    long userRegister(String userAccount, String userPassword, String checkPassword);

    // 写入/更新 `HttpSession`（例如 request.getSession().setAttribute(...)）来保存当前登录用户信息
    // 读取请求中的 Cookie、Header、IP 等信息（做风控、审计、限制等）
    // 与拦截器/鉴权体系对接（基于 session 的话尤其常见）
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    String getEncryptPassword(String password);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    boolean isAdmin(User user);
}
