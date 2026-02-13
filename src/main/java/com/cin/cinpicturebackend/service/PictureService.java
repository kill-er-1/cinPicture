package com.cin.cinpicturebackend.service;

import javax.servlet.http.HttpServletRequest;

import com.cin.cinpicturebackend.model.dto.picture.PictureReviewRequest;
import com.cin.cinpicturebackend.model.dto.picture.PictureUploadByBatchRequest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cin.cinpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.cin.cinpicturebackend.model.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.cin.cinpicturebackend.model.dto.picture.PictureEditByBatchRequest;
import com.cin.cinpicturebackend.model.dto.picture.PictureEditRequest;
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

  PictureVO uploadPicture(Object inputSource,
      PictureUploadRequest pictureUploadRequest,
      User loginUser);

  QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

  PictureVO getPictureVO(Picture picture, HttpServletRequest request);

  Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

  void validPicture(Picture picture);

  void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

  void fillReviewParams(Picture picture, User loginUser);

  Integer uploadPictureByBatch(
      PictureUploadByBatchRequest pictureUploadByBatchRequest,
      User loginUser);

  void checkPictureAuth(User loginUser, Picture picture);

  void deletePicture(long pictureId, User loginUser);

  void clearPictureFile(Picture oldPicture);

  void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

  CreateOutPaintingTaskResponse createPictureOutPaintingTask(
      CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

  void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

}
