package com.inf.core.controller.api;


import com.inf.core.pojo.entites.BorrowInfo;
import com.inf.core.service.BorrowInfoService;
import com.inf.utils.JwtUtils;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * <p>
 * 借款信息表 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */

@Api(tags = "借款信息")
@RestController
@RequestMapping("/api/core/borrowInfo")
@Slf4j
public class BorrowInfoController {

    @Autowired
    private BorrowInfoService borrowInfoService;

    @ApiOperation(value = "获取借款额度")
    @GetMapping("/auth/getBorrowAmount")
    public Result getBorrowAmount(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        BigDecimal borrowAmount = borrowInfoService.getBorrowAmount(userId);
        return Result.ok().data("borrowAmount", borrowAmount);
    }

    @ApiOperation("提交借款申请")
    @PostMapping("/auth/save")
    public Result save(@RequestBody BorrowInfo borrowInfo, HttpServletRequest request) {

        // 拿到当前已登录的用户信息去做其它操作。
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        borrowInfoService.saveBorrowInfo(borrowInfo, userId); // 参数2为查询借款人id是属于哪个用户的。
        return Result.ok().message("提交成功");
    }

    @ApiOperation(value = "获取借款申请审批状态")
    @GetMapping(value = "/auth/getBorrowInfoStatus")
    public Result getBorrowerStatus(HttpServletRequest request){
        // 拿取当前已经登陆的用户去
        String token = request.getHeader("token"); // 获取token值
        Long userId = JwtUtils.getUserId(token); // 根据token值获取id
        Integer status = borrowInfoService.getStatusByUserId(userId);
        return Result.ok().data("borrowInfoStatus", status);
    }
}

