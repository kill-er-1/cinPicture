package com.cin.cinpicturebackend.model.dto.picture;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class PictureEditByBatchRequest implements Serializable {

  /**
   * 图片 id 列表
   */
  private List<Long> pictureIdList;

  /**
   * 空间 id
   */
  private Long spaceId;

  /**
   * 分类
   */
  private String category;

  /**
   * 标签
   */
  private List<String> tags;

  private String nameRule;

  private static final long serialVersionUID = 1L;
}