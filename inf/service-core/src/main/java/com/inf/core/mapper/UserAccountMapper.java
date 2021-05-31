package com.inf.core.mapper;

import com.inf.core.pojo.entites.UserAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 用户账户 Mapper 接口
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    /**
     *  根据bindCode 去更新amount和freezeAmount 字段
     * @param bindCode 用户绑定汇付宝平台的代码
     * @param amount 金额
     * @param freezeAmount 冻结金额
     */
    void updateAccount(
            @Param("bindCode")String bindCode,
            @Param("amount") BigDecimal amount,
            @Param("freezeAmount")BigDecimal freezeAmount);
}
