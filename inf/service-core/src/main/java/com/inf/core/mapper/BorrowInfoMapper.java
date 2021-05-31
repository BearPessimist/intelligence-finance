package com.inf.core.mapper;

import com.inf.core.pojo.entites.BorrowInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 借款信息表 Mapper 接口
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface BorrowInfoMapper extends BaseMapper<BorrowInfo> {

    /**
     *  关联查询，borrower表和borrower_info 表
     * @return
     */
    List<BorrowInfo> selectBorrowInfoList();
}
