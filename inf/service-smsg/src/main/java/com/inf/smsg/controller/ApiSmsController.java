package com.inf.smsg.controller;

import com.inf.smsg.client.UserInfoClient;
import com.inf.smsg.service.SmsService;
import com.inf.smsg.utils.SmsProperties;
import com.inf.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


@Api(tags = "短信管理")
@Slf4j
@RestController
@RequestMapping("/api/smsg")
public class ApiSmsController {

    @Resource
    private SmsService smsService;

//    @Qualifier("service-core")
    @Resource
    private UserInfoClient userInfoClient;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     *  向客户端发送验证码
     * @param phone 发送信息的手机号码
     * @return {@link Result}
     */
    @ApiOperation(value = "获取验证码")
    @GetMapping(value = "/send/{phone}")
    public Result sendCode(@ApiParam(value = "手机号码", required = true)
                               @PathVariable String phone) {
        // 判断手机号是否为空MOBILE_NULL_ERROR(-202, "手机号不能为空"),
        Assert.notEmpty(phone, ResponseEnum.MOBILE_NULL_ERROR);

        // 校验手机号是否合法
        Assert.isTrue(RegexValidateUtils.checkCellphone(phone),ResponseEnum.MOBILE_ERROR);

        // 如果手机号为true，也就是手机号存在，则抛出异常
        boolean result = userInfoClient.checkMobile(phone);
        log.info("result：==>{}", result);

        // 如果等于false，继续往下执行
        Assert.isTrue(!result,ResponseEnum.MOBILE_EXIST_ERROR);

        // 调用RandomUtils工具类 生成四位随机的验证码
        String code = RandomUtils.getFourBitRandom();
        var map = new HashMap<String, Object>();
        // 将生成的四位验证码放到map集合当中
        map.put("code",code);

        // 调用service发送短信方法
        smsService.send(phone, SmsProperties.TEMPLATE_CODE,map);

        // 将验证码存到redis，过期时间为5分钟
        redisTemplate.opsForValue().set("inf:smsg:code" + phone, code,5, TimeUnit.MINUTES);
        // 返回成功的消息
        return Result.ok().message("短信发送成功");
    }

}
