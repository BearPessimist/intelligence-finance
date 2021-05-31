package com.inf.core.controller.api;


import com.inf.core.pojo.entites.TransFlow;
import com.inf.core.service.TransFlowService;
import com.inf.utils.JwtUtils;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 交易流水表 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@RestController
@RequestMapping("/api/core/transFlow")
public class TransFlowController {

    @Autowired
    private TransFlowService transFlowService;

    /**
     *  判断流水号是否存在
     * @param no
     * @return
     */
    @GetMapping(value = "/exist/{no}")
    public Result isExist(@PathVariable(value = "no") String no) {
        boolean flow = transFlowService.isSaveTransFlow(no);
        return Result.ok().data("flow",flow);
    }

    /**
     *  获取流水记录列表，展示在前台网站用户中心页面
     * @param request
     * @return
     */
    @ApiOperation("获取列表")
    @GetMapping("/list")
    public Result list(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        List<TransFlow> list = transFlowService.selectByUserId(userId);
        return Result.ok().data("list", list);
    }
}

