package com.inf.core.controller.administrator;


import com.inf.core.pojo.entites.Lend;
import com.inf.core.service.LendService;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Api(tags = "标的管理")
@RestController
@RequestMapping("/admin/core/lend")
@Slf4j
public class AdminLendController {

    @Autowired
    private LendService lendService;

    @ApiOperation(value = "标的列表")
    @GetMapping(value = "/list")
    public Result list() {
        List<Lend> lendList = lendService.selectList();
        return Result.ok().data("list", lendList);
    }

    /**
     *  根据id查询标的详情页
     * @param id 标的id
     * @return {@link Result}
     */
    @ApiOperation("获取标的信息")
    @GetMapping(value = "/show/{id}")
    public Result show(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long id) {
        Map<String, Object> result = lendService.getLendDetail(id);
        return Result.ok().data("lendDetail", result);
    }

    /**
     *  管理平台放款操作
     * @param id 标的id值
     * @return {@link Result}
     */
    @ApiOperation(value = "放款")
    @GetMapping("/makeLoan/{id}")
    public Result makeLoan(
            @ApiParam(value = "标的id", required = true)
            @PathVariable("id") Long id) {
        lendService.makeLoan(id);
        return Result.ok().message("放款成功");
    }
}

