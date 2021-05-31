package com.inf.oss.service;

import java.io.InputStream;

public interface FileService {

    /**
     *  文件上传至阿里云
     * @param inputStream 输入流，进行数据的输入
     * @param module 自定义模块组
     * @param fileName 文件名称
     * @return {@link String}
     */
    String upload(InputStream inputStream, String module, String fileName);

    /**
     * 根据路径删除文件
     * @param url 文件地址
     */
    void removeFile(String url);
}
