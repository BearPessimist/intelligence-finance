package com.inf.core.controller.administrator;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inf.core.pojo.entites.IntegralGrade;
import com.inf.core.service.IntegralGradeService;
import com.inf.exception.CustomException;
import com.inf.utils.Assert;
import com.inf.utils.ResponseEnum;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Api(tags = "积分等级列表")
@RestController
@RequestMapping("/admin/core/integralGrade")
public class AdminIntegralGradeController {

    @Autowired
    private IntegralGradeService integralGradeService;

    /**
     *  查询所有积分等级列表数据
     * @return {@link Result}
     */
    @ApiOperation(value = "积分等级列表")
    @GetMapping(value = "/list")
    public Result queryAll() {
        var list = integralGradeService.list();
        return Result.ok().data("integralGrade",list);
    }

    /**
     *  分页查询积分等级列表
     * @param current 当前页
     * @param limit 每页多少条
     * @return {@link Result}
     */
    @ApiOperation(value = "分页显示积分列表")
    @GetMapping(value = "/page/{current}/{limit}")
    public Result pageAll(@PathVariable(value = "current") Long current,
                          @PathVariable(value = "limit") Long limit) {

        var page = new Page<IntegralGrade>(current,limit);
        Page<IntegralGrade> pages = integralGradeService.page(page);

        long total = pages.getTotal();
        List<IntegralGrade> records = pages.getRecords();

        return Result.ok().data("total",total).data("records",records);
    }

    /**
     *  根据积分等级id查询积分等级
     * @param id 积分等级的id值
     * @return {@link Result}
     */
    @GetMapping(value = "/get/{id}")
    public Result getIntegralById(@PathVariable(value = "id") Long id) {

        var integralGrade = integralGradeService.getById(id);

        if (integralGrade != null) {
            return Result.ok().data("record", integralGrade);
        }else{
            return Result.error().message("Data doesn't exist");
        }
    }

    @ApiOperation(value = "delete integral grade by id")
    @DeleteMapping("/remove/{id}")
    public Result removeById(@ApiParam(value = "被删除的id",example = "1") @PathVariable(value = "id") Long id) {

        var result = integralGradeService.removeById(id);
        if (result) {
            return Result.ok().message("Deleted");
        } else {
            return Result.error().message("Not deleted");
        }
    }

    /**
     *  新增积分等级数据
     * @param integralGrade 积分等级对象
     * @return {@link Result}
     */
    @ApiOperation("add integral grade")
    @PostMapping(value = "/add")
    public Result add(@ApiParam(value = "积分等级对象", required = true) @RequestBody IntegralGrade integralGrade){

        Assert.notNull(integralGrade.getBorrowAmount(),ResponseEnum.BORROW_AMOUNT_NULL_ERROR);

        var result = integralGradeService.save(integralGrade);
        if (result) {
            return Result.ok().message("Added");
        } else {
            return Result.error().message("Not Added");
        }
    }

    /**
     *  修改积分等级数据
     * @param integralGrade 积分等级对象
     * @return {@link Result}
     */
    @ApiOperation("modify integral grade")
    @PutMapping(value = "/modify")
    public Result update(@ApiParam(value = "积分等级对象", required = true) @RequestBody IntegralGrade integralGrade){
        var result = integralGradeService.updateById(integralGrade);
        if (result) {
            return Result.ok().message("Updated");
        } else {
            return Result.error().message("Not Updated");
        }
    }

}

