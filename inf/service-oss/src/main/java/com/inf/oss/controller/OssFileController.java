package com.inf.oss.controller;

import com.inf.exception.CustomException;
import com.inf.oss.service.FileService;
import com.inf.utils.ResponseEnum;
import com.inf.utils.Result;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RequestMapping(path = "api/oss/file")
@RestController
public class OssFileController {

    @Autowired
    private FileService fileService;

    /**
     *  上传文件到阿里云OSS 云存储当中
     * @param file 文件上传组件
     * @param module 文件的自定义组
     * @return {@link Result}
     */
    @ApiOperation(value = "文件上传")
    @PostMapping(value = "/upload")
    public Result upload(
            @ApiParam(value = "文件", required = true)
            @RequestParam("file") MultipartFile file,

            @ApiParam(value = "模块", required = true)
            @RequestParam("module") String module)  {

        // 获取文件名称
        String filename = file.getOriginalFilename();
        try {
            String url = fileService.upload(file.getInputStream(), module, filename);

            return Result.ok().data("url",url).message("文件上传成功");
        } catch (IOException e) {
            throw new CustomException(ResponseEnum.UPLOAD_ERROR,e);
        }
    }

    /**
     *  根据文件URL 删除OSS上的文件
     * @param url 文件 URL 地址
     * @return {@link Result}
     */
    @ApiOperation(value = "删除OSS文件")
    @DeleteMapping(value = "/remove")
    public Result remove(
            @ApiParam(value = "要删除的文件路径", required = true)
            @RequestParam("url") String url) {
        // 调用service层删除方法
        fileService.removeFile(url);
        return Result.ok().message("删除文件成功");
    }
}
