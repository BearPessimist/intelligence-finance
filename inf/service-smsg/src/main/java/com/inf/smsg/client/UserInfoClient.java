package com.inf.smsg.client;


import com.inf.smsg.client.fallback.UserInfoClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *  远程调用service-core 中的校验手机号接口
 */
@FeignClient(value = "service-core",fallback = UserInfoClientFallback.class)
public interface UserInfoClient {

    @GetMapping("/api/core/userInfo/checkMobile/{mobile}")
    boolean checkMobile(@PathVariable(value = "mobile") String mobile);
}
