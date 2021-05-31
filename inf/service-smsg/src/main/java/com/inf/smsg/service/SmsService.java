package com.inf.smsg.service;

import java.util.Map;

public interface SmsService {

    /**
     *  发送验证码
     * @param mobile  手机号
     * @param templateCode  模板代码
     * @param param  参数信息
     */
    void send(String mobile, String templateCode, Map<String,Object> param);
}
