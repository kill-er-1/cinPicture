package com.cin.cinpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cin.cinpicturebackend.exception.BusinessException;
import com.cin.cinpicturebackend.exception.ErrorCode;
import com.cin.cinpicturebackend.exception.ThrowUtils;
import com.cin.cinpicturebackend.model.entity.Picture;
import com.cin.cinpicturebackend.model.entity.Space;
import com.cin.cinpicturebackend.model.entity.User;
import com.cin.cinpicturebackend.model.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.cin.cinpicturebackend.model.vo.space.analyze.SpaceSizeAnalyzeResponse;
import com.cin.cinpicturebackend.model.vo.space.analyze.SpaceTagAnalyzeResponse;
import com.cin.cinpicturebackend.model.vo.space.analyze.SpaceUsageAnalyzeResponse;
import com.cin.cinpicturebackend.model.vo.space.analyze.SpaceUserAnalyzeResponse;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceRankAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceSizeAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceTagAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceUserAnalyzeRequest;
import com.cin.cinpicturebackend.service.PictureService;
import com.cin.cinpicturebackend.service.SpaceAnalyzeService;
import com.cin.cinpicturebackend.service.SpaceService;
import com.cin.cinpicturebackend.service.UserService;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {

  @Resource
  private UserService userService;
  @Resource
  private SpaceService spaceService;
  @Resource
  private PictureService pictureService;

  @Override
  public void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {

    if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()) {
      ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "只有管理员才能分析公共空间或全空间");
    } else {
      Long spaceId = spaceAnalyzeRequest.getSpaceId();
      ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
      Space space = spaceService.getById(spaceId);
      ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "空间不存在");
      spaceService.checkSpaceAuth(loginUser, space);
    }
  }

  @Override
  public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
      User loginUser) {
    // 1.校验请求体，避免非空
    ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

    // 2.检查isqueryAll isQueryPublic权限
    if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {

      ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "只有管理员才能分析公共空间或全空间");

      QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
      pictureQueryWrapper.select("picSize");
      // 2.1如果是querypubulic 构造查询条件 spaceId是空 查询的是picSize总和
      if (!spaceUsageAnalyzeRequest.isQueryAll()) {
        pictureQueryWrapper.isNull("spaceId");
      }
      // 2.2流式编程处理 需要校验返回类型
      List<Object> pictureObjs = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper);
      long totalSize = 0L;
      totalSize = pictureObjs.stream()
          .mapToLong(pictureObj -> pictureObj instanceof Long ? (Long) pictureObj : 0L).sum();
      long count = pictureObjs.size();
      // 3.返回结果封装
      SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
      spaceUsageAnalyzeResponse.setUsedSize(totalSize);
      spaceUsageAnalyzeResponse.setUsedCount(count);
      // 公共图库无上限 无比例
      spaceUsageAnalyzeResponse.setMaxCount(null);
      spaceUsageAnalyzeResponse.setMaxSize(null);
      spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
      spaceUsageAnalyzeResponse.setCountUsageRatio(null);
      return spaceUsageAnalyzeResponse;
    } else {

      // 4.如果是查询spaceID
      // 5.校验权限 参数
      Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
      ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
      Space space = spaceService.getById(spaceId);
      ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
      spaceService.checkSpaceAuth(loginUser, space);
      // 6.构造查询条件
      QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
      pictureQueryWrapper.eq("spaceId", spaceId);
      pictureQueryWrapper.select("picSize");
      List<Object> pictureObjs = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper);
      long usedSize = 0L;
      usedSize = pictureObjs.stream().mapToLong(pictureObj -> pictureObj instanceof Long ? (Long) pictureObj : 0L)
          .sum();
      long usedCount = pictureObjs.size();
      // 7.查询返回结果
      SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
      spaceUsageAnalyzeResponse.setUsedSize(usedSize);
      spaceUsageAnalyzeResponse.setUsedCount(usedCount);
      spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
      spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
      // 8.计算比例
      double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2)
          .doubleValue();
      double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2)
          .doubleValue();
      spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeUsageRatio);
      spaceUsageAnalyzeResponse.setCountUsageRatio(countUsageRatio);
      return spaceUsageAnalyzeResponse;
    }
  }

  @Override
  public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(
      SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
    ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

    // 检查权限
    checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);

    // 构造查询条件
    QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
    // 根据分析范围补充查询条件
    fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);

    // 使用 MyBatis-Plus 分组查询
    queryWrapper.select("category AS category",
        "COUNT(*) AS count",
        "SUM(picSize) AS totalSize")
        .groupBy("category");

    // 查询并转换结果
    return pictureService.getBaseMapper().selectMaps(queryWrapper)
        .stream()
        .map(result -> {
          String category = result.get("category") != null ? result.get("category").toString() : "未分类";
          Long count = ((Number) result.get("count")).longValue();
          Long totalSize = ((Number) result.get("totalSize")).longValue();
          return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
        })
        .collect(Collectors.toList());
  }

  @Override
  public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,
      User loginUser) {
    ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

    // 检查权限
    checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);

    // 构造查询条件
    QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
    fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);

    // 查询所有符合条件的标签
    queryWrapper.select("tags");
    List<String> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper)
        .stream()
        .filter(ObjUtil::isNotNull)
        .map(Object::toString)
        .collect(Collectors.toList());

    // 合并所有标签并统计使用次数
    Map<String, Long> tagCountMap = tagsJsonList.stream()
        .flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())
        .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

    // 转换为响应对象，按使用次数降序排序
    return tagCountMap.entrySet().stream()
        .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())) // 降序排列
        .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
      User loginUser) {
    ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

    // 检查权限
    checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);

    // 构造查询条件
    QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
    fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);

    // 查询所有符合条件的图片大小
    queryWrapper.select("picSize");
    List<Long> picSizes = pictureService.getBaseMapper().selectObjs(queryWrapper)
        .stream()
        .map(size -> ((Number) size).longValue())
        .collect(Collectors.toList());

    // 定义分段范围，注意使用有序 Map
    Map<String, Long> sizeRanges = new LinkedHashMap<>();
    sizeRanges.put("<100KB", picSizes.stream().filter(size -> size < 100 * 1024).count());
    sizeRanges.put("100KB-500KB", picSizes.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
    sizeRanges.put("500KB-1MB", picSizes.stream().filter(size -> size >= 500 * 1024 && size < 1 * 1024 * 1024).count());
    sizeRanges.put(">1MB", picSizes.stream().filter(size -> size >= 1 * 1024 * 1024).count());

    // 转换为响应对象
    return sizeRanges.entrySet().stream()
        .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest,
      User loginUser) {
    ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    // 检查权限
    checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);

    // 构造查询条件
    QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
    Long userId = spaceUserAnalyzeRequest.getUserId();
    queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
    fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);

    // 分析维度：每日、每周、每月
    String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
    switch (timeDimension) {
      case "day":
        queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') AS period", "COUNT(*) AS count");
        break;
      case "week":
        queryWrapper.select("YEARWEEK(createTime) AS period", "COUNT(*) AS count");
        break;
      case "month":
        queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') AS period", "COUNT(*) AS count");
        break;
      default:
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
    }

    // 分组和排序
    queryWrapper.groupBy("period").orderByAsc("period");

    // 查询结果并转换
    List<Map<String, Object>> queryResult = pictureService.getBaseMapper().selectMaps(queryWrapper);
    return queryResult.stream()
        .map(result -> {
          String period = result.get("period").toString();
          Long count = ((Number) result.get("count")).longValue();
          return new SpaceUserAnalyzeResponse(period, count);
        })
        .collect(Collectors.toList());
  }

  @Override
  public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
    ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

    // 仅管理员可查看空间排行
    ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权查看空间排行");

    // 构造查询条件
    QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
    queryWrapper.select("id", "spaceName", "userId", "totalSize")
        .orderByDesc("totalSize")
        .last("LIMIT " + spaceRankAnalyzeRequest.getTopN()); // 取前 N 名

    // 查询结果
    return spaceService.list(queryWrapper);
  }

  private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
    // 全空间分析
    boolean queryAll = spaceAnalyzeRequest.isQueryAll();
    if (queryAll) {
      return;
    }
    // 公共图库
    boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
    if (queryPublic) {
      queryWrapper.isNull("spaceId");
      return;
    }
    // 分析特定空间
    Long spaceId = spaceAnalyzeRequest.getSpaceId();
    if (spaceId != null) {
      queryWrapper.eq("spaceId", spaceId);
      return;
    }
    throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
  }
}
