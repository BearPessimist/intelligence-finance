package com.inf.utils;

import com.inf.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class Assert {

    /**
     *  断言对象不为空
     * 如果对象obj为空，则抛出异常
     * @param obj 判断对象参数
     */
    public static void notNull(Object obj, ResponseEnum responseEnum) {
        if (obj == null) {
            log.info("obj is null...............");
            throw new CustomException(responseEnum);
        }
    }

    /**
     * 断言对象为空
     * 如果对象obj不为空，则抛出异常
     * @param object 判断对象参数
     * @param responseEnum 异常枚举
     */
    public static void isNull(Object object, ResponseEnum responseEnum) {
        if (object != null) {
            log.info("obj is not null......");
            throw new CustomException(responseEnum);
        }
    }

    /**
     * 断言表达式为真
     * 如果不为真，则抛出异常
     * @param expression 是否成功
     */
    public static void isTrue(boolean expression, ResponseEnum responseEnum) {
        if (!expression) {
            log.error("fail...............");
            throw new CustomException(responseEnum);
        }
    }

    /**
     * 断言两个对象是否相等，相等则抛出异常
     * 如果相等，则抛出异常
     * @param m1 参数一
     * @param m2 参数二
     * @param responseEnum 异常枚举
     */
    public static void notEquals(Object m1, Object m2,  ResponseEnum responseEnum) {
        if (m1.equals(m2)) {
            log.info("equals...............");
            throw new CustomException(responseEnum);
        }
    }

    /**
     * 断言两个对象是否不想等，不想等则抛出异常
     * 如果不相等，则抛出异常
     * @param m1 参数一
     * @param m2 参数二
     * @param responseEnum 异常枚举
     */
    public static void equals(Object m1, Object m2,  ResponseEnum responseEnum) {
        if (!m1.equals(m2)) {
            log.info("not equals...............");
            throw new CustomException(responseEnum);
        }
    }

    /**
     * 断言参数不为空
     * 如果为空，则抛出异常
     * @param key 判断的参数
     * @param responseEnum 枚举值
     */
    public static void notEmpty(String key, ResponseEnum responseEnum) {
        if (StringUtils.isEmpty(key)) {
            log.info("is empty...............");
            throw new CustomException(responseEnum);
        }
    }
}
