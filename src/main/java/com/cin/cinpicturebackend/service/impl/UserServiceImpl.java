package com.cin.cinpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cin.cinpicturebackend.exception.BusinessException;
import com.cin.cinpicturebackend.exception.ErrorCode;
import com.cin.cinpicturebackend.exception.ThrowUtils;
import com.cin.cinpicturebackend.model.dto.user.UserQueryRequest;
import com.cin.cinpicturebackend.model.entity.User;
import com.cin.cinpicturebackend.model.enums.UserRoleEnum;
import com.cin.cinpicturebackend.model.vo.LoginUserVO;
import com.cin.cinpicturebackend.model.vo.UserVO;
import com.cin.cinpicturebackend.service.UserService;
import com.cin.cinpicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.cin.cinpicturebackend.constant.UserConstant.USER_LOGIN_STATE;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
//ServiceImpl<M, T>：一个带泛型的基类
//M：Mapper 类型（数据访问层），这里是 UserMapper
//T：实体类型（表对应的 Java 类），这里是 User
//UserServiceImpl 自动具备很多方法（如 save、getById、list、removeById、updateById 等）
//以及 this.baseMapper 可直接用，类型就是 UserMapper（你代码里 this.baseMapper.selectCount(...) 就是这个）
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.参数校验 非空 长度 equal
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount,userPassword,checkPassword), ErrorCode.PARAMS_ERROR,"参数为空");

        ThrowUtils.throwIf(userAccount.length()<4, ErrorCode.PARAMS_ERROR,"用户账号过短");

        ThrowUtils.throwIf(userPassword.length()<8||checkPassword.length()<8, ErrorCode.PARAMS_ERROR,"用户密码过短");

        ThrowUtils.throwIf((!userPassword.equals(checkPassword)), ErrorCode.PARAMS_ERROR,"两次密码输入不一致");

        //2.是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }

        //3.加密
        String encryptPassword = getEncryptPassword(userPassword);

        //4.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
        }
        //回填id内存user
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount,userPassword), ErrorCode.PARAMS_ERROR,"参数为空");

        ThrowUtils.throwIf(userAccount.length()<4, ErrorCode.PARAMS_ERROR,"用户账号过短");

        ThrowUtils.throwIf(userPassword.length()<8, ErrorCode.PARAMS_ERROR,"用户密码过短");

        //2.加密
        String encryptPassword = getEncryptPassword(userPassword);

        //3.查询是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null){
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }

        //4.设置登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        //不会冲突，不同用户/不同浏览器/不同会话：各自有不同的 session（不同 `JSESSIONID`），即使 key 一样也互不影响

        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR,"未登录");
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public String getEncryptPassword(String password){
        final String SALT = "cinPictureBackend";
        return DigestUtils.md5DigestAsHex((SALT+password).getBytes());
    }

    @Override
    public LoginUserVO getLoginUserVO(User user){
        if (user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user){
        if (user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList){
        if (CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);//ObjUtil.isNotNull(id),查询执行的条件
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}




