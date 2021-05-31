package com.inf.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.inf.core.hfb.RequestHelper;
import com.inf.core.pojo.entites.vo.UserBindVO;
import com.inf.core.service.UserBindService;
import com.inf.utils.JwtUtils;
import com.inf.utils.Result;
import feign.Param;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Api(tags = "会员账号绑定")
@Slf4j
@RestController
@RequestMapping("/api/core/userBind")
public class UserBindController {

    @Autowired
    private UserBindService userBindService;

    /**
     *  绑定用户到汇付宝平台的接口
     * @param userBindVO 绑定对象
     * @param request 请求对象，用来获取请求头中的token值
     * @return {@link Result}
     */
    @ApiOperation(value = "账户绑定提交数据接口")
    @PostMapping(value = "/auth/bind")
    public Result bindUser(@RequestBody UserBindVO userBindVO, HttpServletRequest request) {

        // 从header中获取token令牌，并从token中取出id值，确保用户已经登陆，对token进行校验
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);

        // 根据userId做账户绑定，生成一个动态表单的字符串
        String data = userBindService.commitBindUser(userBindVO,userId);

        return Result.ok().data("formStr",data);
    }

    /**
     *  获取汇付宝平台的回调信息
     * @param request 请求对象
     * @return {@link String}
     */
    @ApiOperation(value = "账户绑定异步回调")
    @PostMapping(value = "/notify")
    public String notify(HttpServletRequest request) {

        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("账户绑定异步回调的参数：" + JSON.toJSONString(paramMap));

        // 校验签名信息。如果不想等则返回错误信息。
        if (!RequestHelper.isSignEquals(paramMap)) {
            log.error("用户账号绑定异步回调签名验证错误：{}",JSON.toJSONString(paramMap));
            return "fail"; // 汇付宝端只接受success，其他类型一律返回错误
        }
        log.info("验证成功，开始绑定账户。");
        userBindService.notify(paramMap);

        return "success";
    }
}

