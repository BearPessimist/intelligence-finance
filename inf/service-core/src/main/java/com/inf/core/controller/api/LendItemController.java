package com.inf.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.inf.core.hfb.RequestHelper;
import com.inf.core.pojo.entites.LendItem;
import com.inf.core.pojo.entites.vo.InvestVO;
import com.inf.core.service.LendItemService;
import com.inf.utils.JwtUtils;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Api(tags = "标的的投资")
@RestController
@RequestMapping("/api/core/lendItem")
@Slf4j
public class LendItemController {

    @Resource
    LendItemService lendItemService;

    /**
     *  投资人投资接口，存数据到lend_item 表中，里面记录投资的记录信息。
     * @param investVO 提交对象
     * @param request 请求对象，获取token
     * @return {@link Result}
     */
    @ApiOperation(value = "会员投资提交数据")
    @PostMapping("/auth/commitInvest")
    public Result commitInvest(@RequestBody InvestVO investVO, HttpServletRequest request) {

        String token = request.getHeader("token"); // 从请求头中获取token
        Long userId = JwtUtils.getUserId(token); // 根据token值获取登录用户id
        String userName = JwtUtils.getUserName(token); // 根据token获取用户名称
        investVO.setInvestUserId(userId); // 设置投资用户ID
        investVO.setInvestName(userName); // 设置投资用户名称

        //构建充值自动提交表单
        String formStr = lendItemService.commitInvest(investVO);
        return Result.ok().data("formStr", formStr);
    }

    @ApiOperation(value = "会员投资异步回调")
    @PostMapping(value = "/notify")
    public String notify(HttpServletRequest request) {

        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户投资异步回调：" + JSON.toJSONString(paramMap));

        //校验签名 P2pInvestNotifyVo
        if(RequestHelper.isSignEquals(paramMap)) {
            if("0001".equals(paramMap.get("resultCode"))) {
                lendItemService.notify(paramMap);
            } else {
                log.info("用户投资异步回调失败：" + JSON.toJSONString(paramMap));
                return "fail";
            }
        } else {
            log.info("用户投资异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }
        return "success";
    }


    @ApiOperation("获取列表")
    @GetMapping(value = "/list/{lendId}")
    public Result list(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long lendId) {
        List<LendItem> list = lendItemService.selectByLendId(lendId);
        return Result.ok().data("list", list);
    }
}

