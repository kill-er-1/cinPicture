package com.cin.cinpicturebackend.model.vo.space.analyze;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceSizeAnalyzeResponse implements Serializable {

  /**
   * 图片大小范围
   */
  private String sizeRange;

  /**
   * 图片数量
   */
  private Long count;

  private static final long serialVersionUID = 1L;
}
