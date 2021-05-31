package com.inf.core.service;

import com.inf.core.pojo.entites.BorrowerAttach;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inf.core.pojo.entites.vo.BorrowerAttachVO;

import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface BorrowerAttachService extends IService<BorrowerAttach> {

    /**
     *  根据借款人id查询出对应VO对象的借款人附件信息
     * @param borrowerId 借款人id
     * @return {@link List<BorrowerAttachVO>}
     */
    List<BorrowerAttachVO> selectBorrowerAttachVOList(Long borrowerId);
}
