package com.inf.core.service;

import com.inf.core.pojo.entites.LendItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inf.core.pojo.entites.vo.InvestVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface LendItemService extends IService<LendItem> {

    String commitInvest(InvestVO investVO);

    /**
     *  投资回调方法，和汇付宝平台的冻结金额做同步
     * @param paramMap
     */
    void notify(Map<String, Object> paramMap);

    /**
     *  获取投资人投资记录的列表信息
     * @param lendId 投资人id
     * @param status 投资列表的状态
     * @return {@link List<LendItem> }
     */
    List<LendItem> selectByLendId(Long lendId, int status);

    /**
     *  后台管理系统中显示投资记录的列表信息
     * @param lendId 标的id
     * @return {@link List<LendItem>}
     */
    List<LendItem> selectByLendId(Long lendId);
}
