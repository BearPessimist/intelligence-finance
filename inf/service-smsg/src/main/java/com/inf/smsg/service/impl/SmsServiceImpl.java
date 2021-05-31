package com.inf.smsg.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.inf.exception.CustomException;
import com.inf.smsg.service.SmsService;
import com.inf.smsg.utils.SmsProperties;
import com.inf.utils.Assert;
import com.inf.utils.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    /**
     *  发送短信验证码的service层代码实现
     * @param mobile phone number
     * @param templateCode  模板代码
     * @param param  Parameter information
     */
    @Override
    public void send(String mobile, String templateCode, Map<String, Object> param) {

        // 创建远程连接客户端对象
        DefaultProfile profile = DefaultProfile.getProfile(
                SmsProperties.REGION_Id,
                SmsProperties.KEY_ID,
                SmsProperties.KEY_SECRET);
        IAcsClient client = new DefaultAcsClient(profile);

        //创建远程连接的请求参数
        CommonRequest request = new CommonRequest();

        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        // 地域id
        request.putQueryParameter("RegionId", SmsProperties.REGION_Id);
        // 手机号码
        request.putQueryParameter("PhoneNumbers", mobile);
        // 短息签名信息
        request.putQueryParameter("SignName", SmsProperties.SIGN_NAME);
        // 模板代码
        request.putQueryParameter("TemplateCode", templateCode);

        Gson gson = new Gson();
        // 将Map集合中的数据转换为json格式
        String json = gson.toJson(param);
        // 设置map集合中的数据
        request.putQueryParameter("TemplateParam", json);

        try {
            // 使用客户端对象携带请求对象发送请求并得到响应结果，将request中封装的数据响应到客户端
            CommonResponse response = client.getCommonResponse(request);
            // 响应失败的处理
            boolean success = response.getHttpResponse().isSuccess();

            // ALIYUN_RESPONSE_FAIL(-501, "阿里云响应失败"),
            Assert.isTrue(success, ResponseEnum.ALIYUN_RESPONSE_ERROR);

            String data = response.getData();
            // 将获取的Data信息转换为json格式
            Map<String, String> resultMap = gson.fromJson(data, HashMap.class);

            String code = resultMap.get("Code"); // 获取响应信息中的Code 键
            String message = resultMap.get("Message");

//            log.info("阿里云短信发送响应结果：");
            log.info("code：" + code,"message：" + message);

            // ALIYUN_SMS_LIMIT_CONTROL_ERROR(-502, "短信发送过于频繁"),//业务限流
            Assert.notEquals("isv.BUSINESS_LIMIT_CONTROL", code, ResponseEnum.ALIYUN_SMS_LIMIT_CONTROL_ERROR);

            // ALIYUN_SMS_ERROR(-503, "短信发送失败"),//其他失败
            Assert.equals("OK", code, ResponseEnum.ALIYUN_SMS_ERROR);

        } catch (ClientException e) {
            log.error("阿里云短信发送SDK调用失败：");
            log.error("ErrorCode=" + e.getErrCode(),"ErrorMessage=" + e.getErrMsg());
            throw new CustomException(ResponseEnum.ALIYUN_SMS_ERROR , e);
        }
    }
}
