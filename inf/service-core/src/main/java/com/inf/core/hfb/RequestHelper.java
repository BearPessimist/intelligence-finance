package com.inf.core.hfb;

import com.alibaba.fastjson.JSONObject;

import com.inf.utils.HttpUtils;
import com.inf.utils.MD5;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class RequestHelper {

    public static void main(String[] args) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("d", "4");
        paramMap.put("b", "2");
        paramMap.put("c", "3");
        paramMap.put("a", "1");
    }

    /**
     * 请求数据获取签名
     * @param paramMap
     * @return
     */
    public static String getSign(Map<String, Object> paramMap) {
        if(paramMap.containsKey("sign")) {
            paramMap.remove("sign");
        }
        TreeMap<String, Object> sorted = new TreeMap<>(paramMap);

        StringBuilder str = new StringBuilder();

        for (Map.Entry<String, Object> param : sorted.entrySet()) {
            str.append(param.getValue()).append("|");
        }
        str.append(HfbConst.SIGN_KEY); // 追加加密的签名
        log.info("加密前：" + str.toString());

        String md5Str = MD5.encrypt(str.toString());
        log.info("加密后：" + md5Str);
        return md5Str;
    }

    /**
     * Map转换
     * @param paramMap
     * @return
     */
    public static Map<String, Object> switchMap(Map<String, String[]> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        // 遍历map集合
        for (Map.Entry<String, String[]> param : paramMap.entrySet()) {
            // 获取key和value，每次都0号索引值。
            resultMap.put(param.getKey(), param.getValue()[0]);
        }
        return resultMap;
    }

    /**
     *  签名校验
     * @param paramMap
     * @return
     */
    public static boolean isSignEquals(Map<String, Object> paramMap) {
        String sign = (String)paramMap.get("sign");
        String md5Str = getSign(paramMap);
        if(!sign.equals(md5Str)) {
            return false;
        }
        return true;
    }

    /**
     * 获取时间戳
     * @return
     */
    public static long getTimestamp() {
        return new Date().getTime();
    }

    /**
     * 封装同步请求
     * @param paramMap
     * @param url
     * @return
     */
    public static JSONObject sendRequest(Map<String, Object> paramMap, String url){
        String result = null;
        try {
            //封装post参数
            StringBuilder postdata = new StringBuilder();
            for (Map.Entry<String, Object> param : paramMap.entrySet()) {
                postdata.append(param.getKey()).append("=")
                        .append(param.getValue()).append("&");
            }
            log.info(String.format("--> 发送请求到汇付宝：post data %1s", postdata));
            // 进行字符集转换
            byte[] reqData = postdata.toString().getBytes(StandardCharsets.UTF_8);

            byte[] respdata = HttpUtils.doPost(url,reqData);

            result = new String(respdata);
            log.info(String.format("--> 汇付宝应答结果：result data %1s", result));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JSONObject.parseObject(result);
    }
}
