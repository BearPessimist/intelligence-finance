package com.inf.core.service;

import com.inf.core.pojo.entites.BorrowInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inf.core.pojo.entites.vo.BorrowInfoApprovalVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface BorrowInfoService extends IService<BorrowInfo> {

    /**
     *  根据已经登录用户id获取借款额度
     * @param userId 已登录用户的id
     * @return {@link BigDecimal}
     */
    BigDecimal getBorrowAmount(Long userId);

    /**
     *  提交借款申请的信息，保存到borrow_info 表当中
     * @param borrowInfo 保存的对象
     * @param userId 已登陆的用户id
     */
    void saveBorrowInfo(BorrowInfo borrowInfo, Long userId);

    /**
     *  获取借款申请的状态，认证中、认证通过
     * @param userId 已登录的用户id
     * @return {@link Integer}
     */
    Integer getStatusByUserId(Long userId);

    /**
     *  展示借款人信息列表，联表查询
     * @return {@link List<BorrowInfo>}
     */
    List<BorrowInfo> selectList();

    /**
     *  获取借款人信息详情的数据，使用Map集合返回的方式。
     * @param id id值
     * @return {@link Map<String, Object>}
     */
    Map<String, Object> getBorrowInfoDetail(Long id);

    /**
     *  审批借款人的借款请求信息
     * @param borrowInfoApprovalVO 审批借款人信息的VO对象
     */
    void approval(BorrowInfoApprovalVO borrowInfoApprovalVO);

}
