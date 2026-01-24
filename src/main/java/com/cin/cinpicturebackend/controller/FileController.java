package com.cin.cinpicturebackend.controller;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.qcloud.cos.utils.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cin.cinpicturebackend.annotation.AuthCheck;
import com.cin.cinpicturebackend.common.BaseResponse;
import com.cin.cinpicturebackend.common.ResultUtils;
import com.cin.cinpicturebackend.constant.UserConstant;
import com.cin.cinpicturebackend.exception.BusinessException;
import com.cin.cinpicturebackend.exception.ErrorCode;
import com.cin.cinpicturebackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // @RequestPart("file") ：表示接收 HTTP 请求（Content-Type 必须是 multipart/form-data ）中名为
        // file 的部分（part）
        // MultipartFile ：Spring 提供的接口，用来封装上传文件的所有信息（文件名、文件类型、文件流、大小等）。
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File file = null;
        try {
            file = File.createTempFile("cos_upload_", null);
            // File.createTempFile(prefix, suffix) 的作用是在操作系统的临时目录（比如 /tmp/ 或
            // C:\Users\...\AppData\Local\Temp\ ）下创建一个 随机命名的文件 。
            multipartFile.transferTo(file);
            // 把前端传来的流写入这个临时文件
            cosManager.putObject(filepath, file);
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete erro, filepath = {}", filepath);
                }
            }
        }
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath,HttpServletResponse response) throws IOException{
        COSObjectInputStream cosObjectInput = null;
        try{
            COSObject cosObject = cosManager.getObject(filepath);
            //cosObject 包括两部分 一部分是原数据 另一部分是content
             cosObjectInput = cosObject.getObjectContent();
             //建立起数据连接 但还没下载到内存中
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            //设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            // - 告诉浏览器“这是个二进制流文件”，浏览器通常会触发下载行为而不是直接在页面显示。
            response.setHeader("Content-Disposition","attachment; filename="+filepath);
//             - attachment ：强制触发“另存为”对话框。
// - filename=... ：指定下载下来的文件名。
            //写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"下载失败");
        }finally{
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }
}
