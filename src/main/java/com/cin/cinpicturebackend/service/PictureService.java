package com.cin.cinpicturebackend.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cin.cinpicturebackend.model.dto.picture.PictureQueryRequest;
import com.cin.cinpicturebackend.model.dto.picture.PictureUploadRequest;
import com.cin.cinpicturebackend.model.entity.Picture;
import com.cin.cinpicturebackend.model.entity.User;
import com.cin.cinpicturebackend.model.vo.PictureVO;

/**
 * @author cin
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2026-01-23 16:28:06
 */
public interface PictureService extends IService<Picture> {

    PictureVO uploadPicture(MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            User loginUser);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

}
