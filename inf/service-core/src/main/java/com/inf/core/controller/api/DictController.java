package com.inf.core.controller.api;


import com.inf.core.pojo.entites.Dict;
import com.inf.core.service.DictService;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * <p>
 * 数据字典 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Api(tags = "数据字典")
@Slf4j
@RestController
@RequestMapping("/api/core/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping(value = "/dictCode/{code}")
    public Result getDictByCode(
            @ApiParam(value = "字典编码", example = "1",required = true)
            @PathVariable(value = "code") String dictCode) {
        List<Dict> list = dictService.getDictByCode(dictCode);

        return Result.ok().data("dictList",list);
    }
}






