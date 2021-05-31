package com.inf.core.service;

import com.inf.core.pojo.entites.LendItemReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借回款记录表 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface LendItemReturnService extends IService<LendItemReturn> {

    /**
     *  根据标的id和登录用户id查看投资人回款计划信息
     * @param lendId 标的id
     * @param userId 登录用户id
     * @return {@link List<LendItemReturn>}
     */
    List<LendItemReturn> selectByLendId(Long lendId, Long userId);

    /**
     *  根据还款的id值，查询对应的回款记录数据，并且组装为Map集合
     * @param lendReturnId 还款id
     * @return {@link List<Map<String,Object>>}
     */
    List<Map<String, Object>> addReturnDetail(Long lendReturnId);

    /**
     *  根据还款计划id获取对应的回款计划列表
     * @param lendReturnId
     * @return
     */
    List<LendItemReturn> selectLendItemReturnList(Long lendReturnId);
}
