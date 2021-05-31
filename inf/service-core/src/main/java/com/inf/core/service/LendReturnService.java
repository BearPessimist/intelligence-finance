package com.inf.core.service;

import com.inf.core.pojo.entites.LendReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface LendReturnService extends IService<LendReturn> {

    List<LendReturn> selectByLendId(Long lendId);

    String commitReturn(Long lendReturnId, Long userId); // 用户还款

    /**
     *  还款操作的回调方法，将汇付宝平台的参数同步到本项目当中的数据库中
     * @param paramMap 汇付宝平台发来的参数信息
     */
    void notify(Map<String, Object> paramMap);

}
