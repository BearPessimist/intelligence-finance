package com.inf.core.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Api(value = "网站积分等级接口")
@RestController
@RequestMapping("/api/core/integralGrade")
public class IntegralGradeController {

    @ApiOperation(value = "测试接口")
    @GetMapping("/list")
    public void test() {
    }
}

