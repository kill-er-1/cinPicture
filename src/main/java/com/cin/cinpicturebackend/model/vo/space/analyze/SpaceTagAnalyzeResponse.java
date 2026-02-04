package com.cin.cinpicturebackend.model.vo.space.analyze;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeResponse implements Serializable {

  /**
   * 标签名称
   */
  private String tag;

  /**
   * 使用次数
   */
  private Long count;

  private static final long serialVersionUID = 1L;
}