package com.cin.cinpicturebackend.service;

import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cin.cinpicturebackend.model.dto.space.SpaceAddRequest;
import com.cin.cinpicturebackend.model.dto.space.SpaceQueryRequest;
import com.cin.cinpicturebackend.model.entity.Space;
import com.cin.cinpicturebackend.model.entity.User;
import com.cin.cinpicturebackend.model.vo.SpaceVO;

/**
 * @author cin
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-01-30 14:23:44
 */
public interface SpaceService extends IService<Space> {

    void validSpace(Space space, boolean add);

    void fillSpaceBySpaceLevel(Space space);

    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    void checkSpaceAuth(User loginUser, Space space);
}
