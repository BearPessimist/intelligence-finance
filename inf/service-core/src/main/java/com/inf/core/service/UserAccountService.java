package com.inf.core.service;

import com.inf.core.pojo.entites.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface UserAccountService extends IService<UserAccount> {

    /**
     *  用户充值操作
     * @param chargeAmt 充值的金额
     * @param userId 登录的用户id
     * @return {@link BigDecimal}
     */
    String commitCharge(BigDecimal chargeAmt, Long userId);

    /**
     *  汇付宝平台参数回调，同步到当前项目数据库当中
     * @param paramMap 各种回调回来的参数信息
     */
    String notify(Map<String, Object> paramMap);

    /**
     *  根据用户id获取账户余额
     * @param userId 用户id
     * @return
     */
    BigDecimal getAccount(Long userId);

    /**
     *  用户提现方法
     * @param fetchAmt
     * @param userId
     * @return
     */
    String commitWithdraw(BigDecimal fetchAmt, Long userId);

    /**
     *  用户提现回调方法，和项目数据库进行同步的操作。
     * @param paramMap 汇付宝平台所需的集合参数。
     */
    void notifyWithdraw(Map<String, Object> paramMap);

}
