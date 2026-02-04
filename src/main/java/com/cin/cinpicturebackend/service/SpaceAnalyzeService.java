package com.cin.cinpicturebackend.service;

import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceUserAnalyzeRequest;
import com.cin.cinpicturebackend.model.entity.Space;
import com.cin.cinpicturebackend.model.entity.User;

import java.util.List;

import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceRankAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceSizeAnalyzeRequest;
import com.cin.cinpicturebackend.model.dto.space.analyze.SpaceTagAnalyzeRequest;
import com.cin.cinpicturebackend.model.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.cin.cinpicturebackend.model.vo.space.analyze.SpaceSizeAnalyzeResponse;
import com.cin.cinpicturebackend.model.vo.space.analyze.SpaceTagAnalyzeResponse;
import com.cin.cinpicturebackend.model.vo.space.analyze.SpaceUsageAnalyzeResponse;
import com.cin.cinpicturebackend.model.vo.space.analyze.SpaceUserAnalyzeResponse;

public interface SpaceAnalyzeService {
  void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);

  SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

  List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
      User loginUser);

  List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

  List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

  List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

  List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);

}
