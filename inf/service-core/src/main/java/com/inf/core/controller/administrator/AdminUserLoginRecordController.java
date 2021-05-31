package com.inf.core.controller.administrator;

import com.baomidou.mybatisplus.extension.api.R;
import com.inf.core.pojo.entites.UserLoginRecord;
import com.inf.core.service.UserLoginRecordService;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "会员登录日志接口")
@RestController
@RequestMapping("/admin/core/userLoginRecord")
@Slf4j
public class AdminUserLoginRecordController {

    @Autowired
    private UserLoginRecordService userLoginRecordService;

    @ApiOperation(value = "获取会员登录日志列表")
    @GetMapping(value = "/listTop50/{userId}")
    public Result listTop50(
            @ApiParam(value = "用户id", required = true)
            @PathVariable Long userId) {
        // 查询前五十条登录日志记录
        List<UserLoginRecord> userLoginRecordList = userLoginRecordService.listTop50(userId);
        return Result.ok().data("list", userLoginRecordList);
    }
}
