package com.inf.core.controller.administrator;


import com.alibaba.excel.EasyExcel;
import com.inf.core.pojo.entites.Dict;
import com.inf.core.pojo.entites.dto.ExcelDictDTO;
import com.inf.core.service.DictService;
import com.inf.exception.CustomException;
import com.inf.utils.ResponseEnum;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 * 数据字典，前端的后台管理控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Api(tags = "数据字典管理")
@Slf4j
@RestController
@RequestMapping("/admin/core/dict")
public class AdminDictController {

    @Autowired
    private DictService dictService;

    /**
     *  导入Excel数据
     * @param file 上传的文件
     * @return {@link Result}
     */
    @ApiOperation(value = "Excel批量导入数据字典")
    @PostMapping("/import")
    public Result batchImport(
            @ApiParam(value = "Excel文件", required = true)
            @RequestParam("file") MultipartFile file) {

        try {
            // 获取输入流
            InputStream inputStream = file.getInputStream();
            // 调用service层方法
            dictService.importDictData(inputStream);
            // 导入成功的返回消息
            return Result.ok().message("批量导入成功");
        } catch (Exception e) {
            // 导入失败的返回消息，UPLOAD_ERROR(-103, "文件上传错误"),
            throw new CustomException(ResponseEnum.UPLOAD_ERROR, e);
        }
    }

    /**
     *  导出Excel数据
     * @param response 响应信息对象
     */
    @ApiOperation(value = "Excel数据的导出")
    @GetMapping(value = "/export")
    public void export(HttpServletResponse response){

        try {
            // 设置响应内容信息
            response.setContentType("application/vnd.ms-excel");

            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("mydict", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头信息。
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 调用EasyExcel写的操作
            EasyExcel.write(response.getOutputStream(), ExcelDictDTO.class).sheet("数据字典").doWrite(dictService.listDictData());

        } catch (IOException e) {
            // EXPORT_DATA_ERROR(104, "数据导出失败"),
            throw  new CustomException(ResponseEnum.EXPORT_DATA_ERROR, e);
        }
    }

    /**
     *  获取父节点下的子节点数据
     * @param parentId 父级id
     * @return {@link Result}
     */
    @ApiOperation(value = "根据上级id获取子节点数据列表")
    @GetMapping(value = "/listByParentId/{parentId}")
    public Result listByParentId(
            @ApiParam(value = "上级节点id", required = true)
            @PathVariable(value = "parentId") Long parentId) {

        return Result.ok().data("list", dictService.listByParentId(parentId));
    }
}
