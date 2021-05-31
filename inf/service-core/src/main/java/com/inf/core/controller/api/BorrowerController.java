package com.inf.core.controller.api;


import com.inf.core.pojo.entites.vo.BorrowerVO;
import com.inf.core.service.BorrowerService;
import com.inf.utils.JwtUtils;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 借款人 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Api(tags = "借款人")
@RestController
@RequestMapping("/api/core/borrower")
@Slf4j
public class BorrowerController {

    @Resource
    private BorrowerService borrowerService;

    @ApiOperation(value = "保存借款人信息")
    @PostMapping(value = "/auth/save")
    public Result save(@RequestBody BorrowerVO borrowerVO, HttpServletRequest request) {
        // 获取到token，表示用户已经登录
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        // 带上登录用户的id。
        borrowerService.saveBorrowerVOByUserId(borrowerVO, userId);
        return Result.ok().message("信息提交成功");
    }


    /**
     *  根据已登录用户的id获取借款人的状态信息。
     * @param request 请求对象
     * @return {@link Result}
     */
    @ApiOperation(value = "获取借款人认证状态")
    @GetMapping(value = "/auth/getBorrowerStatus")
    public Result getBorrowerStatus(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        Integer status = borrowerService.getStatusByUserId(userId);
        return Result.ok().data("borrowerStatus", status);
    }
}

