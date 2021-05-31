package com.inf.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inf.core.enums.LendStatusEnum;
import com.inf.core.enums.TransTypeEnum;
import com.inf.core.hfb.FormHelper;
import com.inf.core.hfb.HfbConst;
import com.inf.core.hfb.RequestHelper;
import com.inf.core.mapper.LendMapper;
import com.inf.core.mapper.UserAccountMapper;
import com.inf.core.pojo.entites.Lend;
import com.inf.core.pojo.entites.LendItem;
import com.inf.core.mapper.LendItemMapper;
import com.inf.core.pojo.entites.bo.TransFlowBO;
import com.inf.core.pojo.entites.vo.InvestVO;
import com.inf.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inf.core.utils.LendNoUtils;
import com.inf.utils.Assert;
import com.inf.utils.ResponseEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Service
public class LendItemServiceImpl extends ServiceImpl<LendItemMapper, LendItem> implements LendItemService {

    @Autowired
    private LendMapper lendMapper;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserBindService userBindService;

    @Autowired
    private LendService lendService;

    @Autowired
    private TransFlowService transFlowService;

    @Autowired
    private UserAccountMapper userAccountMapper;

    /**
     *  投资人投资方法
     *      1. 首先获取标的id，根据id查询出所有标的信息，进行校验操作。
     *      2. 将投资记录插入到lend_item 表当中
     *      3.
     * @param investVO 提交对象
     * @return {@link String}
     */
    @Override
    public String commitInvest(InvestVO investVO) {

        // 校验
//        Long lendId = investVO.getLendId();
//        Lend lend = lendMapper.selectById(lendId);
//
//        // 判断标的状态为募资中。如果不是募资中则抛出异常。
//        Assert.isTrue(lend.getStatus().intValue() == LendStatusEnum.INVEST_RUN.getStatus(),
//                ResponseEnum.LEND_INVEST_ERROR);
//        /*
//            判断是否超卖：
//                1. 已投金额 + 当前投资金额 <= 标的金额（正常）
//                2. 已投金额 + 当前投资金额 > 标的（借款）金额（超卖）
//         */
//        BigDecimal sum = lend.getInvestAmount().add(new BigDecimal(investVO.getInvestAmount()));
//        // 如果超卖则抛出已满标异常。
//        Assert.isTrue(sum.doubleValue() <= lend.getAmount().doubleValue(),
//                ResponseEnum.LEND_FULL_SCALE_ERROR);
//
//        // 判断用户余额：当前用户余额 >= 当前投资的金额
//        Long investUserId = investVO.getInvestUserId(); // 首先获取投资用户的id
//        BigDecimal account = userAccountService.getAccount(investUserId); // 然后根据id查询账户的余额
//        Assert.isTrue(account.doubleValue() >=
//                Double.parseDouble(investVO.getInvestAmount()),
//                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR); // 抛出余额不足异常
//
//
//        // 获取 paramMap中需要的参数
//        // 生成标的下的投资记录，也就是向lend_item 表中插入数据
//        //在商户平台中生成投资信息==========================================
//        LendItem lendItem = new LendItem();
//        lendItem.setInvestUserId(investUserId);// 投资人id
//        lendItem.setInvestName(investVO.getInvestName());//投资人名字
//        lendItem.setLendItemNo(LendNoUtils.getLendItemNo()); // 投资条目编号（一个Lend对应一个或多个LendItem）
//        lendItem.setLendId(investVO.getLendId());// 对应的标的id
//        lendItem.setInvestAmount(new BigDecimal(investVO.getInvestAmount())); // 投资金额
//        lendItem.setLendYearRate(lend.getLendYearRate());// 年化
//        lendItem.setInvestTime(LocalDateTime.now()); // 投资时间，指定为投资当前的时间。
//        lendItem.setLendStartDate(lend.getLendStartDate()); // 开始时间，为标的生成的时间
//        lendItem.setLendEndDate(lend.getLendEndDate()); // 结束时间，为标的结束的时间。
//
//        // 设置预期收益参数
//        BigDecimal expectAmount = lendService
//                .getInterestCount(lendItem.getInvestAmount(),
//                lendItem.getLendYearRate(),
//                lend.getPeriod(),
//                lend.getReturnMethod()
//        );
//        lendItem.setExpectAmount(expectAmount);
//
//        // 设置实际收益
//        lendItem.setRealAmount(new BigDecimal(0)); // 0
//
//        // 设置投资记录状态参数
//        lendItem.setStatus(0); // 0为默认，刚刚创建投资记录，账户尚未修改。
//
//        // 生成投资投标记录存入数据库
//        baseMapper.insert(lendItem);
//
//        // 获取投资人的bindCode
//        String bindCode = userBindService.getBindCodeByUserId(investUserId);
//        // 获取借款人的bindCode
//        String benefitBindCode = userBindService.getBindCodeByUserId(lend.getUserId());
//
//        //封装提交至汇付宝的参数
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("agentId", HfbConst.AGENT_ID); // 设置商户唯一标识id
//        paramMap.put("voteBindCode", bindCode); // 设置投资人的绑定码
//        paramMap.put("benefitBindCode",benefitBindCode); // 设置借款人的绑定代码
//        paramMap.put("agentProjectCode", lend.getLendNo()); //项目标的流水号标号
//        paramMap.put("agentProjectName", lend.getTitle()); // 标的名称
//
//        //在资金托管平台上的投资订单的唯一编号，要和lendItemNo保持一致。
//        paramMap.put("agentBillNo", LendNoUtils.getLendItemNo()); // 投资订单编号
//        paramMap.put("voteAmt", investVO.getInvestAmount()); // 投资金额
//        paramMap.put("votePrizeAmt", "0");
//        paramMap.put("voteFeeAmt", "0"); // 冻结金额
//        paramMap.put("projectAmt", lend.getAmount()); // 标的总金额
//        paramMap.put("note", ""); // 备注
//        paramMap.put("notifyUrl", HfbConst.INVEST_NOTIFY_URL); //检查常量是否正确
//        paramMap.put("returnUrl", HfbConst.INVEST_RETURN_URL); // 回调地址
//        paramMap.put("timestamp", RequestHelper.getTimestamp());
//        String sign = RequestHelper.getSign(paramMap); // 签名校验。
//        paramMap.put("sign", sign);
//
//        //构建充值自动提交表单
//        String formStr = FormHelper.buildForm(HfbConst.INVEST_URL, paramMap);
//        return formStr;

        //输入校验==========================================

        Long lendId = investVO.getLendId();
        //获取标的信息
        Lend lend = lendMapper.selectById(lendId);

        //标的状态必须为募资中
        Assert.isTrue(
                lend.getStatus().intValue() == LendStatusEnum.INVEST_RUN.getStatus().intValue(),
                ResponseEnum.LEND_INVEST_ERROR);

        //标的不能超卖：(已投金额 + 本次投资金额 )>=标的金额（超卖）
        BigDecimal sum = lend.getInvestAmount().add(new BigDecimal(investVO.getInvestAmount()));
        Assert.isTrue(sum.doubleValue() <= lend.getAmount().doubleValue(),
                ResponseEnum.LEND_FULL_SCALE_ERROR);

        //账户可用余额充足：当前用户的余额 >= 当前用户的投资金额（可以投资）
        Long investUserId = investVO.getInvestUserId();
        BigDecimal amount = userAccountService.getAccount(investUserId);//获取当前用户的账户余额
        Assert.isTrue(amount.doubleValue() >= Double.parseDouble(investVO.getInvestAmount()),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        //在商户平台中生成投资信息==========================================
        //标的下的投资信息
        LendItem lendItem = new LendItem();
        lendItem.setInvestUserId(investUserId);//投资人id
        lendItem.setInvestName(investVO.getInvestName());//投资人名字
        String lendItemNo = LendNoUtils.getLendItemNo();
        lendItem.setLendItemNo(lendItemNo); //投资条目编号（一个Lend对应一个或多个LendItem）
        lendItem.setLendId(investVO.getLendId());//对应的标的id
        lendItem.setInvestAmount(new BigDecimal(investVO.getInvestAmount())); //此笔投资金额
        lendItem.setLendYearRate(lend.getLendYearRate());//年化
        lendItem.setInvestTime(LocalDateTime.now()); //投资时间
        lendItem.setLendStartDate(lend.getLendStartDate()); //开始时间
        lendItem.setLendEndDate(lend.getLendEndDate()); //结束时间

        //预期收益
        BigDecimal expectAmount = lendService.getInterestCount(
                lendItem.getInvestAmount(),
                lendItem.getLendYearRate(),
                lend.getPeriod(),
                lend.getReturnMethod());
        lendItem.setExpectAmount(expectAmount);

        //实际收益
        lendItem.setRealAmount(new BigDecimal(0));

        lendItem.setStatus(0);//默认状态：刚刚创建
        baseMapper.insert(lendItem);


        //组装投资相关的参数，提交到汇付宝资金托管平台==========================================
        //在托管平台同步用户的投资信息，修改用户的账户资金信息==========================================
        //获取投资人的绑定协议号
        String bindCode = userBindService.getBindCodeByUserId(investUserId);
        //获取借款人的绑定协议号
        String benefitBindCode = userBindService.getBindCodeByUserId(lend.getUserId());

        //封装提交至汇付宝的参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("voteBindCode", bindCode);
        paramMap.put("benefitBindCode",benefitBindCode);
        paramMap.put("agentProjectCode", lend.getLendNo());//项目标号
        paramMap.put("agentProjectName", lend.getTitle());

        //在资金托管平台上的投资订单的唯一编号，要和lendItemNo保持一致。
        paramMap.put("agentBillNo", lendItemNo);//订单编号
        paramMap.put("voteAmt", investVO.getInvestAmount());
        paramMap.put("votePrizeAmt", "0");
        paramMap.put("voteFeeAmt", "0");
        paramMap.put("projectAmt", lend.getAmount()); //标的总金额
        paramMap.put("note", "");
        paramMap.put("notifyUrl", HfbConst.INVEST_NOTIFY_URL); //检查常量是否正确
        paramMap.put("returnUrl", HfbConst.INVEST_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建充值自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.INVEST_URL, paramMap);
        return formStr;

    }

    /**
     *   同步汇付宝平台的数据到数据库中
     *      1. 获取汇付宝平台绑定投资的投资编号
     *      2. 判断幂等性，如果有订单号直接返回结果。不作下面的操作。
     *      3. 更新user_account表的amount和freeze_amount 字段，在投资时减少金额总数，将投资的金额添加到冻结金额中，代表投资的金额数
     *      4. 更新lend_item 表中的status 字段为1，表示已支付（已经投资）
     *
     *      5. 更新lend表中的 invest_num（投资人数）和 invest_amount（投资人已投资的金额）
     *      6. 新增交易流水，trans_flow 表中的记录信息。
     *
     * @param paramMap 汇付宝平台回调过来的参数信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notify(Map<String, Object> paramMap) {

        //获取投资编号
        String agentBillNo = (String)paramMap.get("agentBillNo");
        // 判断幂等性。
        boolean result = transFlowService.isSaveTransFlow(agentBillNo);
        if (result) { // 如果有订单号，则只执行下面的日志
            log.warn("幂等性返回");
            return;
        }
        // 修改账户金额：从余额中减去投资金额，在冻结金额中增加投资金额
        String voteBindCode = (String) paramMap.get("voteBindCode");
        String voteAmt = (String) paramMap.get("voteAmt"); // 获取金额

        userAccountMapper.updateAccount(
                voteBindCode,
                new BigDecimal("-" + voteAmt), // 减去冻结金额
                new BigDecimal(voteAmt) // 添加冻结金额。
        );

        // 修改投资记录的状态，根据汇付宝平台发的回调参数获取流水号
        LendItem lendItem = this.getByLendItemNo(agentBillNo);
        lendItem.setStatus(1); // 设置状态为1 已支付（已经投资了。）
        baseMapper.updateById(lendItem); // 修改投资状态

        // 修改标的记录：投资人数、已投金额
        Long lendId = lendItem.getLendId();
        Lend lend = lendMapper.selectById(lendId);
        lend.setInvestNum(lend.getInvestNum() + 1); // 更新投资人数，加1即可。
        // 添加已投金额的值，添加上投资人投资的金额即可。
        lend.setInvestAmount(lend.getInvestAmount().add(lendItem.getInvestAmount()));
        lendMapper.updateById(lend);

        // 新增交易流水。
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                voteBindCode,
                new BigDecimal(voteAmt),
                TransTypeEnum.INVEST_LOCK,
                "项目编号：" + lend.getLendNo() + ", 项目名称：" + lend.getTitle()
        );

        transFlowService.saveTransFlow(transFlowBO); // 记录投资人账户流水。


    }

    @Override
    public List<LendItem> selectByLendId(Long lendId, int status) {
        var queryWrapper = new QueryWrapper<LendItem>();
        // 根据借款人id和状态查询。
        queryWrapper.eq("lend_id", lendId);
        queryWrapper.eq("status", status);
        return baseMapper.selectList(queryWrapper);

    }

    /**
     *  根据标的
     * @param lendId 标的id
     * @return {@link List<LendItem>}
     */
    @Override
    public List<LendItem> selectByLendId(Long lendId) {

        QueryWrapper<LendItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lend_id", lendId);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     *  根据流水号获取投资记录
     * @param lendItemNo 流水号
     * @return {@link LendItem}
     */
    private LendItem getByLendItemNo(String lendItemNo) {
        QueryWrapper<LendItem> queryWrapper = new QueryWrapper();
        queryWrapper.eq("lend_item_no", lendItemNo);
        return baseMapper.selectOne(queryWrapper);
    }
}
