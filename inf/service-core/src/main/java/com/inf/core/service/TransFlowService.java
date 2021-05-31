package com.inf.core.service;

import com.inf.core.pojo.entites.TransFlow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inf.core.pojo.entites.bo.TransFlowBO;

import java.util.List;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface TransFlowService extends IService<TransFlow> {

    /**
     *  根据封装的BO 对象新增交易流水信息
     * @param transFlowBO BO对象
     */
    void saveTransFlow(TransFlowBO transFlowBO);

    /**
     *  判断流水号是否存在，解决幂等性的问题。
     * @param agentBillNo 流水单号
     * @return {@link Boolean}
     */
    boolean isSaveTransFlow(String agentBillNo);

    /**
     *  查询流水记录
     * @param userId
     * @return
     */
    List<TransFlow> selectByUserId(Long userId);
}
