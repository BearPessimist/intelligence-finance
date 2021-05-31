package com.inf.core.controller.api;


import com.inf.core.pojo.entites.vo.LoginVO;
import com.inf.core.pojo.entites.vo.RegisterVO;
import com.inf.core.pojo.entites.vo.UserIndexVO;
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
@RequestMapping("/api/core/userInfo")
public class UserInfoController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserInfoService userInfoService;

    @PostMapping(value = "/register")
    public Result register(@RequestBody RegisterVO registerVO) {

        String mobile = registerVO.getMobile();
        String code = registerVO.getCode();
        String password = registerVO.getPassword();

        //MOBILE_NULL_ERROR(-202, "手机号不能为空"),
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);

        //MOBILE_ERROR(-203, "手机号不正确"),
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);
        //PASSWORD_NULL_ERROR(-204, "密码不能为空"),
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);
        //CODE_NULL_ERROR(-205, "验证码不能为空"),
        Assert.notEmpty(code, ResponseEnum.CODE_NULL_ERROR);


        // CODE_ERROR(-206, "验证码不正确")，抛出异常
        // 从 redis 中获取验证码来判断，注册时的验证码是否和redis中存的一致。
        String codeGen = (String)redisTemplate.opsForValue().get("inf:smsg:code" + mobile);
        Assert.equals(code, codeGen, ResponseEnum.CODE_ERROR);

        // 注册操作
        userInfoService.register(registerVO);

        return Result.ok().message("registration success!");
    }

    /**
     *  用户登录接口
     * @param loginVO 登录对象
     * @param request 请求对象
     * @return {@link Result}
     */
    @PostMapping(value = "/login")
    public Result login(@RequestBody LoginVO loginVO, HttpServletRequest request) {
        // 手机号码不能为空，否则抛出断言异常
        Assert.notEmpty(loginVO.getMobile(),ResponseEnum.MOBILE_NULL_ERROR);
        // 密码不能为空，否则抛异常
        Assert.notEmpty(loginVO.getPassword(),ResponseEnum.PASSWORD_NULL_ERROR);
        // 获取登录用户的ip地址
        String ipAddress = request.getRemoteAddr();
        UserInfoVO userInfoVO = userInfoService.login(loginVO, ipAddress);

        return Result.ok().data("userInfo",userInfoVO);
    }


    @ApiOperation(value = "校验令牌")
    @GetMapping(value = "/checkToken")
    public Result checkToken(HttpServletRequest request) {

        String token = request.getHeader("token");
        boolean result = JwtUtils.checkToken(token);

        if(result){
            return Result.ok();
        }else{
            return Result.setResult(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }

    @ApiOperation("校验手机号是否注册")
    @GetMapping("/checkMobile/{mobile}")
    public boolean checkMobile(@PathVariable String mobile){
        return userInfoService.checkMobile(mobile);
    }

    @ApiOperation("获取个人空间用户信息")
    @GetMapping("/auth/getIndexUserInfo")
    public Result getIndexUserInfo(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        UserIndexVO userIndexVO = userInfoService.getIndexUserInfo(userId);
        return Result.ok().data("userIndexVO", userIndexVO);
    }
}

