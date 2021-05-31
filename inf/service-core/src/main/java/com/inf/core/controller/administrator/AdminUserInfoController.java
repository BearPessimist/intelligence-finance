package com.inf.core.controller.administrator;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inf.core.pojo.entites.UserInfo;
import com.inf.core.pojo.entites.query.UserInfoQuery;
import com.inf.core.pojo.entites.vo.LoginVO;
import com.inf.core.pojo.entites.vo.RegisterVO;
import com.inf.core.pojo.entites.vo.UserInfoVO;
import com.inf.core.service.UserInfoService;
import com.inf.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Api(tags = "会员接口")
@Slf4j
@RestController
@RequestMapping("/admin/core/userInfo")
public class AdminUserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     *  分页获取用户信息
     * @param page 当前页
     * @param limit 每页多少条
     * @param userInfoQuery 查询了解对象
     * @return {@link Result}
     */
    @ApiOperation("获取会员分页列表")
    @GetMapping("/list/{page}/{limit}")
    public Result listPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(value = "每页记录数", required = true)
            @PathVariable Long limit,
            @ApiParam(value = "查询对象", required = false)
                    UserInfoQuery userInfoQuery) {

        var userPageList = new Page<UserInfo>(page,limit);
        IPage<UserInfo> pageModel = userInfoService.listPage(userPageList, userInfoQuery);
        long total = pageModel.getTotal();
        List<UserInfo> records = pageModel.getRecords();
        return Result.ok().data("total", total).data("records",records);
    }

    @ApiOperation(value = "用户锁定与解锁")
    @PutMapping(value = "/lock/{userId}/{status}")
    public Result lock(
            @ApiParam(value = "用户id", required = true)
            @PathVariable("userId") Long userId,
            @ApiParam(value = "锁定状态（0：锁定 1：解锁）", required = true)
            @PathVariable("status") Integer status) {
        userInfoService.lock(userId,status);

        return Result.ok().message(status == 1 ? "解锁成功" : "锁定成功");
    }
}

