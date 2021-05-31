package com.inf.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inf.core.pojo.entites.Borrower;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inf.core.pojo.entites.vo.BorrowerApprovalVO;
import com.inf.core.pojo.entites.vo.BorrowerDetailVO;
import com.inf.core.pojo.entites.vo.BorrowerVO;

/**
 * <p>
 * 借款人 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface BorrowerService extends IService<Borrower> {

    /**
     *  保存借款人信息
     * @param borrowerVO 提交的表单vo对象
     * @param userId 已登录用户id
     */
    void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId);

    /**
     *  根据已登录用户id获取借款人的状态
     * @param userId 已登录的用户id
     * @return {@link Integer}
     */
    Integer getStatusByUserId(Long userId);

    /**
     *  根据分页和关键字查询借款人信息列表
     * @param page 分页数据
     * @param keyword 查询关键字
     * @return {@link IPage<Borrower>}
     */
    IPage<Borrower> listPage(Page<Borrower> page, String keyword);

    /**
     *  根据借款人id获取对应封装的借款人VO对象
     * @param id 借款人id
     * @return {@link BorrowerDetailVO}
     */
    BorrowerDetailVO getBorrowerDetailVOById(Long id);

    /**
     *  借款额度审批
     * @param borrowerApprovalVO 封装VO对象
     */
    void approval(BorrowerApprovalVO borrowerApprovalVO);

}
