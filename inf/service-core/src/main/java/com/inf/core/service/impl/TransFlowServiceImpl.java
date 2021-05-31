package com.inf.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inf.core.mapper.UserInfoMapper;
import com.inf.core.pojo.entites.TransFlow;
import com.inf.core.mapper.TransFlowMapper;
import com.inf.core.pojo.entites.UserInfo;
import com.inf.core.pojo.entites.bo.TransFlowBO;
import com.inf.core.service.TransFlowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 交易流水表 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Service
public class TransFlowServiceImpl extends ServiceImpl<TransFlowMapper, TransFlow> implements TransFlowService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public void saveTransFlow(TransFlowBO transFlowBO) {

        //获取用户基本信息 user_info
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("bind_code", transFlowBO.getBindCode()); // 根据bindCode查询
        UserInfo userInfo = userInfoMapper.selectOne(userInfoQueryWrapper);

        //存储交易流水数据
        TransFlow transFlow = new TransFlow();
        transFlow.setUserId(userInfo.getId()); // 关联用户id
        transFlow.setUserName(userInfo.getName()); // 设置用户名
        transFlow.setTransNo(transFlowBO.getAgentBillNo()); // 设置交易单号。
        transFlow.setTransType(transFlowBO.getTransTypeEnum().getTransType()); // 设置交易类型
        transFlow.setTransTypeName(transFlowBO.getTransTypeEnum().getTransTypeName()); // 设置交易名称
        transFlow.setTransAmount(transFlowBO.getAmount()); // 设置交易金额
        transFlow.setMemo(transFlowBO.getMemo()); // 设置备注参数
        baseMapper.insert(transFlow);
    }

    @Override
    public boolean isSaveTransFlow(String agentBillNo) {

        QueryWrapper<TransFlow> wrapper = new QueryWrapper<>();
        // 根据流水单号做查询。
        wrapper.eq("trans_no", agentBillNo);
        Integer count = baseMapper.selectCount(wrapper);
        if(count > 0) {// 如果大于0 返回true，否则就是false。
            return true;
        }
        return false;
    }

    @Override
    public List<TransFlow> selectByUserId(Long userId) {

        return baseMapper.selectList(new QueryWrapper<TransFlow>()
                // 根据user_id 字段查询，并且根据自身流水记录的id倒序排序，显示最新记录信息
                .eq("user_id", userId).orderByDesc("id"));
    }

}
