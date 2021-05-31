package com.inf.utils;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Result {

    private int code; // 返回状态码
    private String message; // 返回信息

    private Map<String,Object> data = new HashMap<>(); // 返回结果封装

    private Result(){} // 私有构造，防止外界new

    /**
     *  返回结果成功的方法
     * @return {@link Result}
     */
    public static Result ok() {
        var result = new Result();
        result.setCode(ResponseEnum.OK.getCode());
        result.setMessage(ResponseEnum.OK.getMessage());
        return result;
    }

    /**
     *  返回结果失败的方法
     * @return {@link Result}
     */
    public static Result error() {
        var result = new Result();
        result.setCode(ResponseEnum.ERROR.getCode());
        result.setMessage(ResponseEnum.ERROR.getMessage());
        return result;
    }

    /**
     *  返回的数据封装方法
     * @param key 键
     * @param value 值
     * @return {@link Result}
     */
    public Result data(String key, Object value) {
        this.data.put(key,value);
        return this;
    }

    /**
     *  返回数据封装结果
     * @param map Map集合
     * @return {@link Result}
     */
    public Result data(Map<String, Object> map){
        this.setData(map);
        return this;
    }

    /**
     *  根据枚举类返回特定的结果
     */
    public static Result setResult(ResponseEnum responseEnum){
        var result = new Result();
        result.setCode(responseEnum.getCode());
        result.setMessage(responseEnum.getMessage());
        return result;
    }

    /**
     *  返回特定的信息
     * @param message 返回的信息
     * @return {@link Result}
     */
    public Result message(String message){
        this.setMessage(message);
        return this;
    }

    /**
     *  返回特定的响应码
     * @param code 响应码
     * @return {@link Result}
     */
    public Result code(Integer code){
        this.setCode(code);
        return this;
    }
}
