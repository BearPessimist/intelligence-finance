package com.inf.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inf.core.pojo.entites.BorrowerAttach;
import com.inf.core.mapper.BorrowerAttachMapper;
import com.inf.core.pojo.entites.vo.BorrowerAttachVO;
import com.inf.core.pojo.entites.vo.BorrowerDetailVO;
import com.inf.core.service.BorrowerAttachService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 借款人上传资源表 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Service
public class BorrowerAttachServiceImpl extends ServiceImpl<BorrowerAttachMapper, BorrowerAttach> implements BorrowerAttachService {

    @Override
    public List<BorrowerAttachVO> selectBorrowerAttachVOList(Long borrowerId) {
        var wrapper = new QueryWrapper<BorrowerAttach>();
        wrapper.eq("borrower_id",borrowerId); // 条件为借款人id必须和参数的借款人id相等
        List<BorrowerAttach> borrowerAttachList = baseMapper.selectList(wrapper);
//        List<BorrowerAttachVO> borrowerAttachVOList = new ArrayList<>();

        var collect = borrowerAttachList.stream().map(borrowerAttach -> {
            var borrowerAttachVO = new BorrowerAttachVO();
            // 进行VO对象的赋值
            borrowerAttachVO.setImageType(borrowerAttach.getImageType());
            borrowerAttachVO.setImageUrl(borrowerAttach.getImageUrl());
            return borrowerAttachVO;
        }).collect(Collectors.toList());
//        borrowerAttachList.forEach(borrowerAttach -> {
//            BorrowerAttachVO borrowerAttachVO = new BorrowerAttachVO();
//            borrowerAttachVO.setImageType(borrowerAttach.getImageType());
//            borrowerAttachVO.setImageUrl(borrowerAttach.getImageUrl());
//
//            borrowerAttachVOList.add(borrowerAttachVO);
//        });
        return collect;
    }
}
