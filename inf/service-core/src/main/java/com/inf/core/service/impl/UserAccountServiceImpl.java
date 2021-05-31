package com.inf.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inf.core.enums.TransTypeEnum;
import com.inf.core.hfb.FormHelper;
import com.inf.core.hfb.HfbConst;
import com.inf.core.hfb.RequestHelper;
import com.inf.core.mapper.UserInfoMapper;
import com.inf.core.pojo.entites.UserAccount;
import com.inf.core.mapper.UserAccountMapper;
import com.inf.core.pojo.entites.UserInfo;
import com.inf.core.pojo.entites.bo.TransFlowBO;
import com.inf.core.service.TransFlowService;
import com.inf.core.service.UserAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inf.core.service.UserBindService;
import com.inf.core.utils.LendNoUtils;
import com.inf.utils.Assert;
import com.inf.utils.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Service
@Slf4j
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Autowired
    private TransFlowService transFlowService;

    /**
     *  组装汇付宝平台所需的参数进行用户的充值操作
     * @param chargeAmt 充值的金额
     * @param userId 登录的用户id
     * @return {@link String}
     */
    @Override
    public String commitCharge(BigDecimal chargeAmt, Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        // 获取充值人的绑定协议号
        String bindCode = userInfo.getBindCode();
        //判断账户绑定状态
        Assert.notEmpty(bindCode, ResponseEnum.USER_NO_BIND_ERROR);

        HashMap<String, Object> map = new HashMap<>();
        map.put("agentId", HfbConst.AGENT_ID); //  添加商户唯一标识号参数
        map.put("agentBillNo", LendNoUtils.getChargeNo()); // 生成充值单号
        map.put("bindCode", bindCode); // 设置绑定协议号参数
        map.put("chargeAmt", chargeAmt); // 设置充值金额参数
        map.put("feeAmt", new BigDecimal("0")); // 设置商户收取用户手续费的参数
        map.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL); // 设置回调请求地址参数，汇付宝平台发给商户端的回调参数信息。

        map.put("returnUrl", HfbConst.RECHARGE_RETURN_URL); // 设置充值完成后点击返回的页面地址参数。
        map.put("timestamp", RequestHelper.getTimestamp()); // 设置时间戳
        map.put("sign", RequestHelper.getSign(map)); // 将上面所有的参数设置进去，生成签名信息。
        // 构建表单字符串信息，目标充值URL地址和上面组装的参数信息。
        String formStr = FormHelper.buildForm(HfbConst.RECHARGE_URL, map);
        return formStr;
    }


    /**
     *  接收汇付宝平台发来的回调信息，
     *      1. 修改项目中user_account 表中的amount字段，和汇付宝平台的金额做同步
     *      2. 在trans_flow 表中添加交易流水的记录，代表商户平台和资金托管平台的资金记录。
     *
     * @param paramMap 各种回调回来的参数信息
     * @return {@link String}
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String  notify(Map<String, Object> paramMap) {

        // 记录账户的流水信息，先取出流水单号。
        String agentBillNo = (String)paramMap.get("agentBillNo");

        // TODO: 2021-05-04 接口幂等性测试未完成
        // 幂等性判断，判断交易流水是否存在。
        boolean count = transFlowService.isSaveTransFlow(agentBillNo);
        if (count) { // 如果流水单号存在则直接返回success 信息。
            log.warn("幂等性返回");
            return "success"; // 直接返回success，不会执行下方的代码了。
        }

        // 账户处理
        String bindCode = (String) paramMap.get("bindCode"); // 获取绑定码
        String chargeAmt = (String) paramMap.get("chargeAmt"); // 获取金额
        // 设置绑定代码、金额、和冻结金额
        baseMapper.updateAccount(bindCode,new BigDecimal(chargeAmt), new BigDecimal(0));

        // 通过全参构造传值，生成交易流水。
        TransFlowBO transFlowBO = new TransFlowBO(
            agentBillNo,
            bindCode,
            new BigDecimal(chargeAmt),
            TransTypeEnum.RECHARGE,
            "用户充值"
        );
        // 将构造好的Bo对象传入保存方法当中。
        transFlowService.saveTransFlow(transFlowBO);

        // TODO: 2021-05-09  集成rabbitMQ发消息，未完成。
//        log.info("发消息");
//        String mobile = userInfoService.getMobileByBindCode(bindCode);
//        SmsDTO smsDTO = new SmsDTO();
//        smsDTO.setMobile(mobile);
//        smsDTO.setMessage("充值成功");
//
//        mqService.sendMessage(MQConst.EXCHANGE_TOPIC_SMS,
//                MQConst.ROUTING_SMS_ITEM, smsDTO);

        return "success";
    }

    @Override
    public BigDecimal getAccount(Long userId) {

        // 根据 userId 查找用户账户的余额
        UserAccount userAccount = baseMapper.selectOne(new QueryWrapper<UserAccount>()
                .eq("user_id", userId));

        return userAccount.getAmount();
    }

    @Autowired
    private UserBindService userBindService;

    @Autowired
    private UserAccountService userAccountService;

    // 提现方法
    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {

        // 判断当前用户余额是否大于提现余额
        BigDecimal amount = userAccountService.getAccount(userId);//获取当前用户的账户余额
        Assert.isTrue(amount.doubleValue() >= fetchAmt.doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        // 根据用户id查询bind_code
        String bindCode = userBindService.getBindCodeByUserId(userId);
        // 设置汇付宝平台所需的参数值
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getWithdrawNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("fetchAmt", fetchAmt);
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);
        return formStr;

    }
    // 提现业务方法
    @Override
    public void notifyWithdraw(Map<String, Object> paramMap) {

        String agentBillNo = (String) paramMap.get("agentBillNo");

        boolean result = transFlowService.isSaveTransFlow(agentBillNo);
        if(result){
            log.warn("幂等性返回");
            return;
        }

        String bindCode = (String)paramMap.get("bindCode");
        String fetchAmt = (String)paramMap.get("fetchAmt");

        // 根据用户账户修改账户金额，就是提现的操作。
        baseMapper.updateAccount(bindCode, new BigDecimal("-" + fetchAmt), new BigDecimal(0));

        //增加交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(fetchAmt),
                TransTypeEnum.WITHDRAW, // 提现
                "用户提现操作");
        transFlowService.saveTransFlow(transFlowBO);

    }
}
