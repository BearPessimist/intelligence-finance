package com.inf.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inf.core.mapper.*;
import com.inf.core.pojo.entites.Lend;
import com.inf.core.pojo.entites.LendItem;
import com.inf.core.pojo.entites.LendItemReturn;
import com.inf.core.pojo.entites.LendReturn;
import com.inf.core.service.LendItemReturnService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inf.core.service.UserBindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的出借回款记录表 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Service
public class LendItemReturnServiceImpl extends ServiceImpl<LendItemReturnMapper, LendItemReturn> implements LendItemReturnService {


    @Autowired
    private LendReturnMapper lendReturnMapper;

    @Autowired
    private LendMapper lendMapper; // 标的信息查询对象

    @Autowired
    private LendItemMapper lendItemMapper;

    @Autowired
    private UserBindService userBindService;


    @Override
    public List<LendItemReturn> selectByLendId(Long lendId, Long userId) {

        QueryWrapper<LendItemReturn> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("lend_id", lendId) // 根据标的id查询
                .eq("invest_user_id", userId) // 根据投资人id
                .orderByAsc("current_period"); // 再根据当前期数。
        return baseMapper.selectList(queryWrapper); // 查询列表
    }

    /**
     *  根据还款id查询到回款信息的列表
     * @param lendReturnId 还款id
     * @return {@link List<Map<String,Object>>}
     */
    @Override
    public List<Map<String, Object>> addReturnDetail(Long lendReturnId) {

        // 获取还款记录
        LendReturn lendReturn = lendReturnMapper.selectById(lendReturnId);
        // 获取标的信息
        Lend lend = lendMapper.selectById(lendReturn.getLendId());

        // 根据还款id获取回款列表
        List<LendItemReturn> lendItemReturnList = this.selectLendItemReturnList(lendReturnId);
        List<Map<String, Object>> collect = lendItemReturnList.stream().map(lendItemReturn -> {
            LendItem lendItem = lendItemMapper.selectById(lendItemReturn.getLendItemId());
            // 获取投资人的绑定码
            String bindCode = userBindService.getBindCodeByUserId(lendItem.getInvestUserId());

            Map<String, Object> map = new HashMap<>();
            //项目编号
            map.put("agentProjectCode", lend.getLendNo());
            //出借编号
            map.put("voteBillNo", lendItem.getLendItemNo());
            //收款人（出借人）
            map.put("toBindCode", bindCode);
            //还款金额
            map.put("transitAmt", lendItemReturn.getTotal());
            //还款本金
            map.put("baseAmt", lendItemReturn.getPrincipal());
            //还款利息
            map.put("benifitAmt", lendItemReturn.getInterest());
            //商户手续费
            map.put("feeAmt", new BigDecimal("0"));
            return map;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<LendItemReturn> selectLendItemReturnList(Long lendReturnId) {
        QueryWrapper<LendItemReturn> queryWrapper = new QueryWrapper<>();
        // 根据还款id进行查询操作。
        queryWrapper.eq("lend_return_id", lendReturnId);
        return baseMapper.selectList(queryWrapper);
    }

}
