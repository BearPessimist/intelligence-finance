package com.inf.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inf.core.enums.LendStatusEnum;
import com.inf.core.enums.TransTypeEnum;
import com.inf.core.hfb.FormHelper;
import com.inf.core.hfb.HfbConst;
import com.inf.core.hfb.RequestHelper;
import com.inf.core.mapper.*;
import com.inf.core.pojo.entites.Lend;
import com.inf.core.pojo.entites.LendItem;
import com.inf.core.pojo.entites.LendItemReturn;
import com.inf.core.pojo.entites.LendReturn;
import com.inf.core.pojo.entites.bo.TransFlowBO;
import com.inf.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inf.core.utils.LendNoUtils;
import com.inf.utils.Assert;
import com.inf.utils.ResponseEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* <p>
* 还款记录表 服务实现类
* </p>
*
* @author Bear
* @since 2021-04-14
*/
@Service
public class LendReturnServiceImpl extends ServiceImpl<LendReturnMapper, LendReturn> implements LendReturnService {


    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private TransFlowService transFlowService;
    @Autowired
    private LendItemReturnMapper lendItemReturnMapper;
    @Autowired
    private LendItemMapper lendItemMapper;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserBindService userBindService;

    @Resource
    private LendItemReturnService lendItemReturnService;


    @Override
    public List<LendReturn> selectByLendId(Long lendId) {
        QueryWrapper<LendReturn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lend_id", lendId);
        return baseMapper.selectList(queryWrapper);
    }


    /**
     *  还款操作方法
     *      更新的数据表：
     *          1. 汇付宝平台方的user_account表中的amount字段，投资人用户增加还款人还的金额
     *          2. 汇付宝平台的user_item_return 加两条回款记录。
     *
     *          3. 更新lend_return 表中的status、fee、real_return_time 字段。
     *          4. 更新lend 标的表的status信息，如果是最后一次还款才更新
     *          5. 更新项目中user_account 表的amount 字段，投资人新增金额，借款人减去还款的金额
     *          6. 更新trans_flow 表，还款人的还款流水记录
     *
     *          7. 更新回款记录表：lend_item_return表中的status字段
     *          8. lend_item 出借信息表中的real_amount字段
     *          9. 修改回款记录的流水表，在trans_flow 表
     *
     * @param lendReturnId 还款记录id
     * @param userId 用户id
     * @return {@link String}
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String commitReturn(Long lendReturnId, Long userId) {

        //获取还款记录
        LendReturn lendReturn = baseMapper.selectById(lendReturnId);

        //判断账号余额是否充足
        BigDecimal amount = userAccountService.getAccount(userId);
        Assert.isTrue(amount.doubleValue() >= lendReturn.getTotal().doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        //获取借款人code
        String bindCode = userBindService.getBindCodeByUserId(userId);

        //获取lend
        Long lendId = lendReturn.getLendId();
        Lend lend = lendMapper.selectById(lendId);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        //商户商品名称
        paramMap.put("agentGoodsName", lend.getTitle());
        //批次号
        paramMap.put("agentBatchNo",lendReturn.getReturnNo());
        //还款人绑定协议号
        paramMap.put("fromBindCode", bindCode);
        //还款总额
        paramMap.put("totalAmt", lendReturn.getTotal());
        paramMap.put("note", "");

        //还款明细
        List<Map<String, Object>> lendItemReturnDetailList = lendItemReturnService.addReturnDetail(lendReturnId);

        paramMap.put("data", JSONObject.toJSONString(lendItemReturnDetailList));
        paramMap.put("voteFeeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.BORROW_RETURN_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.BORROW_RETURN_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建自动提交表单
    return FormHelper.buildForm(HfbConst.BORROW_RETURN_URL, paramMap);
    }


    /**
     *  还款功能异步回调，同步汇付宝数据到项目的表中。
     * @param paramMap 汇付宝平台发来的参数信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notify(Map<String, Object> paramMap) {

        // 根据key获取还款编号
        String agentBatchNo = (String)paramMap.get("agentBatchNo");

        // 1.幂等性判断
        boolean result = transFlowService.isSaveTransFlow(agentBatchNo);
        if(result){
            log.warn("幂等性返回");
            return;
        }
        //获取还款数据
        String voteFeeAmt = (String)paramMap.get("voteFeeAmt");

        LendReturn lendReturn = baseMapper.selectOne(new QueryWrapper<LendReturn>()
                .eq("return_no", agentBatchNo));;

        // 2. 更新还款状态
        lendReturn.setStatus(1); // 1表示已归还。
        lendReturn.setFee(new BigDecimal(voteFeeAmt));
        lendReturn.setRealReturnTime(LocalDateTime.now());
        baseMapper.updateById(lendReturn);

        // 3. 更新标的信息，根据还款记录信息id查出标的信息
        Lend lend = lendMapper.selectById(lendReturn.getLendId());

        //最后一次还款的时候，更新标的状态为已结清（已还款状态）
        if(lendReturn.getLast()) {
            lend.setStatus(LendStatusEnum.PAY_OK.getStatus());
            lendMapper.updateById(lend); // 更新操作
        }

        // 4. 还款账号转出金额，借款账号转出金额
        BigDecimal totalAmt = new BigDecimal((String)paramMap.get("totalAmt"));//还款金额
        String bindCode = userBindService.getBindCodeByUserId(lend.getUserId());
        // negate方法是减去的意思。
        userAccountMapper.updateAccount(bindCode, totalAmt.negate(), new BigDecimal(0));

        // 5. 还款流水，新增借款人交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBatchNo,
                bindCode,
                totalAmt,
                TransTypeEnum.RETURN_DOWN,
                "借款人还款扣减，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
        transFlowService.saveTransFlow(transFlowBO);


        // 获取回款明细
        List<LendItemReturn> lendItemReturnList = lendItemReturnService.selectLendItemReturnList(lendReturn.getId());
        lendItemReturnList.forEach(item -> {

            // 更新回款状态，0-未归还 1-已归还）
            item.setStatus(1);
            item.setRealReturnTime(LocalDateTime.now()); // 更新时间
            lendItemReturnMapper.updateById(item);

            // 更新出借信息记录表
            LendItem lendItem = lendItemMapper.selectById(item.getLendItemId());
            lendItem.setRealAmount(item.getInterest()); // 动态的实际收益金额
            lendItemMapper.updateById(lendItem);

            //投资账号转入金额
            String investBindCode = userBindService.getBindCodeByUserId(item.getInvestUserId());
            userAccountMapper.updateAccount(investBindCode, item.getTotal(), new BigDecimal(0));

            //投资账号交易流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtils.getReturnItemNo(),
                    investBindCode,
                    item.getTotal(),
                    TransTypeEnum.INVEST_BACK, // 出借的还款到账流水。
                    "还款到账，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
            transFlowService.saveTransFlow(investTransFlowBO);

        });

        /*
            6. 回款明细的获取
            {
                更新回款状态
                投资账号转入金额
                回款流水。
            }
         */

    }
}
