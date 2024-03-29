package com.inf.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.inf.core.hfb.RequestHelper;
import com.inf.core.mapper.UserAccountMapper;
import com.inf.core.service.UserAccountService;
import com.inf.utils.JwtUtils;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Parameter;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */

@Api(tags = "会员账户")
@Slf4j
@RestController
@RequestMapping(path = "/api/core/userAccount")
public class UserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    @ApiOperation(value = "充值")
    @PostMapping("/auth/commitCharge/{chargeAmt}")
    public Result commitCharge(
            @ApiParam(value = "充值金额", required = true)
            @PathVariable BigDecimal chargeAmt, HttpServletRequest request) {

        String token = request.getHeader("token");
        // 根据token获取登录的用户id
        Long userId = JwtUtils.getUserId(token);
        // 组装表单字符串，用于远程提交数据。
        String formStr = userAccountService.commitCharge(chargeAmt, userId);
        return Result.ok().data("formStr", formStr);
    }


    @ApiOperation(value = "用户充值异步回调")
    @PostMapping(value = "/notify")
    public String notify(HttpServletRequest request) {
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户充值异步回调：" + JSON.toJSONString(paramMap));

        // 验证签名信息是否正确
        if (RequestHelper.isSignEquals(paramMap)) {
            // 判断业务是否成功，0001是充值成功的状态码
            if ("0001".equals(paramMap.get("resultCode"))){
                // 同步账户的数据
                return userAccountService.notify(paramMap);
            } else {
                return "success";
            }
        } else {
            return "fail";
        }
    }

    /**
     *  这个接口供前台网站查询lend标的详情功能使用
     * @param request 请求对象
     * @return {@link Result}
     */
    @ApiOperation(value = "根据已登录的用户id查询账户余额")
    @GetMapping("/auth/getAccount")
    public Result getAccount(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        BigDecimal account = userAccountService.getAccount(userId);
        return Result.ok().data("account", account);
    }

    @ApiOperation("用户提现")
    @PostMapping("/auth/commitWithdraw/{fetchAmt}")
    public Result commitWithdraw(
            @ApiParam(value = "金额", required = true)
            @PathVariable BigDecimal fetchAmt, HttpServletRequest request) {

        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = userAccountService.commitWithdraw(fetchAmt, userId);
        return Result.ok().data("formStr", formStr);
    }

    @ApiOperation(value = "用户提现异步回调接口")
    @PostMapping("/notifyWithdraw")
    public String notifyWithdraw(HttpServletRequest request) {
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("提现异步回调：" + JSON.toJSONString(paramMap));

        //校验签名
        if(RequestHelper.isSignEquals(paramMap)) {
            //提现成功交易
            if("0001".equals(paramMap.get("resultCode"))) {
                userAccountService.notifyWithdraw(paramMap);
            } else {
                log.info("提现异步回调充值失败：" + JSON.toJSONString(paramMap));
                return "fail";
            }
        } else {
            log.info("提现异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }
        return "success";
    }
}
