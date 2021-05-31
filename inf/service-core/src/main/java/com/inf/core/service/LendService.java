package com.inf.core.service;

import com.inf.core.pojo.entites.BorrowInfo;
import com.inf.core.pojo.entites.Lend;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inf.core.pojo.entites.query.LendQuery;
import com.inf.core.pojo.entites.vo.BorrowInfoApprovalVO;
import io.swagger.models.auth.In;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface LendService extends IService<Lend> {

    /**
     *  新增标的的数据
     * @param borrowInfoApprovalVO 审批借款信息封装的VO对象
     * @param borrowInfo 借款信息对象
     */
    void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo);

    /**
     *  查询标的列表信息
     * @return {@link List<Lend>}
     */
    List<Lend> selectList();
    List<Lend> getLendByLendQuery(
            String title,
            BigDecimal yearRate,
            Integer period,
            String returnMethod
    );


    Map<String, Object> getLendDetail(Long id);

    BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod);

    /**
     *  满标放款
     * @param lendId 标的id
     */
    void makeLoan(Long lendId);
}
