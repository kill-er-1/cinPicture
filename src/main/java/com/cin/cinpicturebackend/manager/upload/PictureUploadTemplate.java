package com.cin.cinpicturebackend.manager.upload;

import java.io.File;
import java.util.Date;

import javax.annotation.Resource;

import com.cin.cinpicturebackend.config.CosClientConfig;
import com.cin.cinpicturebackend.exception.BusinessException;
import com.cin.cinpicturebackend.exception.ErrorCode;
import com.cin.cinpicturebackend.manager.CosManager;
import com.cin.cinpicturebackend.model.dto.picture.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosManager cosManager;
    @Resource
    private CosClientConfig cosClientConfig;

    public final UploadPictureResult  uploadPicture(Object inputSource,String uploadPathPrefix) {
        //final是禁止子类重写

        //1.验证图片
        validPicture(inputSource);

        //2.图片重命名
        String uuid = RandomUtil.randomString(16);
        String orginName = getOriginFilename(inputSource);
        String fileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,FileUtil.getSuffix(orginName));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix,fileName);

        //3.上传图片到内存
        File file = null;
        try{
            file = File.createTempFile(uploadPathPrefix, null);
            processFile(inputSource,file);
        //4.上传图片去cos

            PutObjectResult putObjectResult = cosManager.putObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();  

            return buildResult(orginName, file, uploadPath, imageInfo);

         }catch (Exception e) {  
            log.error("图片上传到对象存储失败", e);  
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");  
        } finally {  
            // 5. 清理临时文件  
            deleteTempFile(file);  
        } 
    }

    protected abstract void validPicture(Object inputSource);

    protected abstract String getOriginFilename(Object inputSource);

    protected abstract void processFile(Object inputSource,File file) throws Exception; 

    private UploadPictureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo) {
         UploadPictureResult uploadPictureResult = new UploadPictureResult();  
        int picWidth = imageInfo.getWidth();  
        int picHeight = imageInfo.getHeight();  
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();  
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));  
        uploadPictureResult.setPicWidth(picWidth);  
        uploadPictureResult.setPicHeight(picHeight);  
        uploadPictureResult.setPicScale(picScale);  
        uploadPictureResult.setPicFormat(imageInfo.getFormat());  
        uploadPictureResult.setPicSize(FileUtil.size(file));  
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);  
        return uploadPictureResult;  
    }

    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean result = file.delete();
        if (!result) {
            log.error("file not delete error {}",file.getAbsolutePath());
        }
    }


}
