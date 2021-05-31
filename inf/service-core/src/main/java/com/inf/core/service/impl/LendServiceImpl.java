package com.inf.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.inf.core.enums.LendStatusEnum;
import com.inf.core.enums.ReturnMethodEnum;
import com.inf.core.enums.TransTypeEnum;
import com.inf.core.hfb.HfbConst;
import com.inf.core.hfb.RequestHelper;
import com.inf.core.mapper.BorrowerMapper;
import com.inf.core.mapper.UserAccountMapper;
import com.inf.core.mapper.UserInfoMapper;
import com.inf.core.pojo.entites.*;
import com.inf.core.mapper.LendMapper;
import com.inf.core.pojo.entites.bo.TransFlowBO;
import com.inf.core.pojo.entites.query.LendQuery;
import com.inf.core.pojo.entites.vo.BorrowInfoApprovalVO;
import com.inf.core.pojo.entites.vo.BorrowerDetailVO;
import com.inf.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inf.core.utils.*;
import com.inf.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.Buffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */    @Slf4j

@Service
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {


    @Resource
    private DictService dictService;

    @Autowired
    private BorrowerMapper borrowerMapper;

    @Autowired
    private BorrowerService borrowerService;

    @Autowired
    private LendReturnService lendReturnService;

    @Autowired
    private LendItemReturnService lendItemReturnService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private LendItemService lendItemService;

    @Resource
    private TransFlowService transFlowService;


    @Override
    public void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {

        Lend lend = new Lend();
        lend.setUserId(borrowInfo.getUserId()); // 设置属于哪个用户的id
        lend.setBorrowInfoId(borrowInfo.getId()); // 设置借款信息id的关联
        lend.setLendNo(LendNoUtils.getLendNo()); // 设置标的编号
        lend.setTitle(borrowInfoApprovalVO.getTitle()); // 设置标的的标题名称

        lend.setAmount(borrowInfo.getAmount()); // 设置借款额度

        lend.setPeriod(borrowInfo.getPeriod()); // 投资时长

        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100))); // 从审批对象中获取设置年化利率。
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100))); // 从审批对象中获取设置服务费率
        lend.setReturnMethod(borrowInfo.getReturnMethod()); // 设置还款方式
        lend.setLowestAmount(new BigDecimal(100)); // 设置最低投资金额，将值转换为整数。
        lend.setInvestAmount(new BigDecimal(0)); // 设置已投资金额
        lend.setInvestNum(0); // 设置投资人数
        lend.setPublishDate(LocalDateTime.now()); // 设置发布日期

        // 设置起息日期
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), dateTimeFormatter);
        lend.setLendStartDate(lendStartDate); // 设置格式化后的日期

        // 设置结束起息日期，起息开始日期加上借款期限就是结束日期，使用plusMonths 方法。
        LocalDate lendEndDate = lendStartDate.plusMonths(borrowInfo.getPeriod());
        lend.setLendEndDate(lendEndDate); // 设置计算后的结束日期值

        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo()); // 设置标的描述说明
        // 设置平台预期收益率：年化 / 12 * 期数
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);// 小数点最多保留8位
        // 将标的金额 乘以 服务费率 和 投资期限。
        BigDecimal expectAmount = lend.getAmount().multiply(monthRate.multiply(new BigDecimal(lend.getPeriod())));
        // 设置平台预期收益率。
        lend.setExpectAmount(expectAmount);

        // 设置实际收益信息
        lend.setRealAmount(new BigDecimal(0));

        // 设置标的的状态为募资中
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());

        // 设置审核时间
        lend.setCheckTime(LocalDateTime.now());
        lend.setCheckAdminId(1L); // 设置审核人

        // 插入数据。
        baseMapper.insert(lend);

    }

    @Override
    public List<Lend> selectList() { // 查询标的数据列表。

        List<Lend> lendList = baseMapper.selectList(null);
        lendList.forEach(lend -> {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());

            String status = LendStatusEnum.getMsgByStatus(lend.getStatus()); // 设置状态值。
            lend.getParam().put("returnMethod", returnMethod); // 添加returnMethod属性字段。
            lend.getParam().put("status", status); // 添加status属性字段。
        });
        return lendList;
    }

    @Override
    public List<Lend> getLendByLendQuery(
            String title,
            BigDecimal yearRate,
            Integer period,
            String returnMethod) {

        LambdaQueryWrapper<Lend> wrapper = Wrappers.lambdaQuery();
        wrapper.like(Lend::getTitle,title)
                .or()
                .like(Lend::getLendYearRate,yearRate)
                .or()
                .lt(Lend::getLendYearRate, new BigDecimal(0.12))
                .or()
                .ge(Lend::getLendYearRate, new BigDecimal(0.16))
                .or()
                .like(Lend::getPeriod, period)
                .or()
                .le(Lend::getPeriod, 1)
                .or()
                .ge(Lend::getPeriod, 12)
                .or()
                .like(Lend::getReturnMethod, returnMethod)
        ;
        List<Lend> lendList = baseMapper.selectList(wrapper);
        lendList.forEach(lend -> {
            String returnMethods = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());

            String status = LendStatusEnum.getMsgByStatus(lend.getStatus()); // 设置状态值。
            lend.getParam().put("returnMethod", returnMethods); // 添加returnMethod属性字段。
            lend.getParam().put("status", status); // 添加status属性字段。
        });
        return lendList;
    }

    // TODO: 2021-05-02 也可以根据封装vo的方式做返回值
    /**
     * 根据标的id获取列表信息
     * 1. 首先根据标的id查询出标的对象的数据
     * 2. 获取字典数据的还款方式
     * 3. 获取标的状态信息
     * 4. 根据标的user_id查询出borrower的信息
     * 5. 最后统一将标的信息数据和 borrower借款人数据放到map集合中给前端调用。
     *
     * @param id 标的id
     * @return {@link Map<String, Object>}
     */
    @Override
    public Map<String, Object> getLendDetail(Long id) {

        // 首先根据id查询标的lend对象的数据。
        Lend lend = baseMapper.selectById(id);
        // 根据字符串查询出字典中指定的值
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus()); // 获取标的状态。

        // 将Lend对象中的map集合设置以下两个值。
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", status);

        // 查询借款人对象 Borrower（BorrowerDetailVO）
        var wrapper = new QueryWrapper<Borrower>();
        wrapper.eq("user_id", lend.getUserId()); // 必须和标的的userId一致

        Borrower borrower = borrowerMapper.selectOne(wrapper); // 先查询出借款人信息
        // 然后再查询出对应VO对象的值。
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        // 组装数据返回到前端。
        Map<String, Object> result = new HashMap<>();
        result.put("lend", lend);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    /**
     * 计算投资收益是多少钱。
     */
    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod) {

        BigDecimal interestCount;
        // 如果投资的类型是等额本息的方式
        if (returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod()) {
            // 按照等额本息公式计算
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalmonth);
            // 如果投资类型是等额本金
        } else if (returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod()) {
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalmonth);
            // 如果投资类型是 每月还息一次还本
        } else if (returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()) {
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalmonth);
        } else {
            // 否则就是最后一种计算方式：一次还本还息
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalmonth);
        }
        return interestCount; // 返回计算的结果。
    }

    /**
     *  放款操作。将投资给借款人的资金进行解冻，交给借款人。
     *      1. 对接汇付宝平台的参数，对汇付宝平台的项目数据表中进行数据的插入
     *          1.1 首先修改的是汇付宝项目的 user_account 表的amount和freeze_amount字段，
     *                  amount字段分别对应借款人和投资人的可用余额，freeze_amount对应冻结余额，
     *                      放款操作会将投资人投资的冻结金额进行解冻的操作，
     *                          然后转到借款人一分钱都没借到的amount字段当中，并且投资人的freeze_amount金额则会减去。
     *          1.2 修改汇付宝项目中的 user_invest表中的status字段，为1（已放款）
     *
     *      2. 更新lend 借款标的表的信息：
     *              首先是计算并且设置real_amount 字段 = 平台的实际收益，
     *              其次是修改标的表的status（状态）字段值为2（还款中），代表标的当前的状态
     *              payment_time：修改放款时间的字段
     *
     *      3. 修改user_account表：
     *         3.1 首先根据 lend对象获取到user_id，再用user_id 查询到userInfo对象的信息
     *         3.2 根据userInfo对象获取到用户的bind_code
     *         3.3 根据汇付宝平台提供的voteAmt键获取到放款的金额信息
     *         3.4 调用updateAmount方法同步更新 amount 金额字段和 freeze_amount 冻结金额字段
     *
     *      4. 新增借款人的 trans_flow 表，交易流水记录，记录借款人的流水
     *         4.1 调用saveTransFlow方法,设置BO对象的参数即可
     *              TransFlowBO transFlowBO = new TransFlowBO(
     *                        agentBillNo,
     *                        bindCode,
     *                        total,
     *                         TransTypeEnum.BORROW_BACK, // 设置枚举值为放款到账。
     *                         "借款放款到账，编号：" + lend.getLendNo());//项目编号
     *              transFlowService.saveTransFlow(transFlowBO);
     *
     *       5. 解冻投资人的冻结金额并且扣除。
     *          5.1 首先定义一个根据标的id和标的状态查询投资人列表的方法，并且调用遍历这个方法
     *          5.2 再获取到投资用户的id值，根据其id值查询到userInfo对象信息，
     *              然后根据userInfo对象获取bind_code绑定码，再根据userInfo对象获取投资的金额
     *          5.3 根据以上获取的三个参数：调用updateAmount方法进行user_amount表的数据更新操作
     *
     *
     *       6. 第六步是新增投资人的交易流水
     *          6.1 根据上一步获取到的参数，填充BO对象，调用saveTransFlow方法插入保存即可。
     *
     * @param lendId 标的信息id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void makeLoan(Long lendId) {

        // 根据标的id获取标的信息
        Lend lend = baseMapper.selectById(lendId);

        // 调用汇付宝的放款接口
        var paramMap = new HashMap<String, Object>();
        // 以下参数是汇付宝平台所需的参数。
        paramMap.put("agentId", HfbConst.AGENT_ID); // 设置商户唯一标识号
        paramMap.put("agentProjectCode", lend.getLendNo()); // 设置标的编号属性
        String agentBillNo = LendNoUtils.getLoanNo(); // 放款编号
        paramMap.put("agentBillNo", agentBillNo);

        // 计算平台的收益，放款扣除，借款人借款实际金额=借款金额-平台收益
        // 月年化
        BigDecimal monthRate = lend.getServiceRate()
                .divide(new BigDecimal(12),
                        8, BigDecimal.ROUND_DOWN);

        // 平台实际收益 = 已投金额 * 月年化 * 投资时长
        BigDecimal realAmount = lend.getInvestAmount()
                .multiply(monthRate)
                .multiply(new BigDecimal(lend.getPeriod()));

        paramMap.put("mchFee", realAmount); // 设置商户手续费（平台的实际收益）
        paramMap.put("timestamp", RequestHelper.getTimestamp()); // 设置时间戳
        paramMap.put("sign", RequestHelper.getSign(paramMap)); // 设置签名参数
        log.info("放款参数：" + JSONObject.toJSONString(paramMap));

        // 发送同步远程调用，带上组装的参数信息，和访问地址。
        JSONObject result = RequestHelper.sendRequest(paramMap, HfbConst.MAKE_LOAD_URL);
        log.info("放款结果：{}", result.toJSONString());

        // 判断结果码是否不等于0000，否则放款失败
        if (!"0000".equals(result.getString("resultCode"))) {
            // 抛出自定义异常。
            throw new CustomException(result.getString("resultMsg"));
        }

        // 放款成功步骤。在本项目数据库中同步放款的数据。
        // 1. 标的状态和标的平台收益：更新修改标的信息
        lend.setRealAmount(realAmount); // 设置平台实际收益
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus()); // 修改信息为还款中，因为投资人的钱已经发给借款人了
        lend.setPaymentTime(LocalDateTime.now());

        baseMapper.updateById(lend); // 更新标的信息表

        // 2. 给借款账号转入金额
        // 获取借款人信息
        Long userId = lend.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        // 根据用户获取绑定码
        String bindCode = userInfo.getBindCode();

        // 转入金额，更新user_account 数据表，同步汇付宝平台的数据。
        BigDecimal total = new BigDecimal(result.getString("voteAmt"));
        userAccountMapper.updateAccount(bindCode, total, new BigDecimal(0));

        // 3. 新增 ”借款人“ 账号的交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                total,
                TransTypeEnum.BORROW_BACK, // 设置枚举值为放款到账。
                "借款放款到账，编号：" + lend.getLendNo());//项目编号
        // 保存流水记录信息。
        transFlowService.saveTransFlow(transFlowBO);


        // 4. 解冻并扣除投资人的资金，
        // 获取投资列表信息，根据状态为1判断是投资人。
        List<LendItem> lendItemList = lendItemService.selectByLendId(lendId, 1); // 状态为1，已投资
        lendItemList.forEach(item -> {

            // 获取投资人的信息，id
            Long investUserId = item.getInvestUserId();
            UserInfo investUserIfo = userInfoMapper.selectById(investUserId);
            String investBindCode = investUserIfo.getBindCode();

            // 投资人账号冻结金额转出（减去）
            BigDecimal investAmount = item.getInvestAmount(); // 投资金额
            // 更新投资人账户信息，user_account 表。
            userAccountMapper.updateAccount(investBindCode,
                    new BigDecimal(0),
                    investAmount.negate()); // negate：取负数，减去冻结金额。


            // 5. 新增投资人交易流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtils.getTransNo(), // 重新生成编号。
                    investBindCode,
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "冻结资金转出，项目放款，项目编号：" + lend.getLendNo());//项目编号
            // 插入记录。
            transFlowService.saveTransFlow(investTransFlowBO);
        });

        // 6. 生成借款人还款计划和出借人回款计划。
        // 放款成功之后生成借款人的还款计划信息表和投资人回款计划信息表：分别对应 lend_return，lend_item_return
        this.repaymentPlan(lend);
    }


    /**
     * 还款计划：将还款计划的信息存入lend_return 表当中。
     *
     *       3期10个投资人，还3期，每期的还款拆分成十份
     *              1. 创建还款计划表
     *              2. 根据还款期限生成还款计划（for period）{
     *
     *                 创建还款计划对象
     *                 填充基本属性
     *                 判断是否是最后一期还款
     *                 设置还款的状态
     *                 将还款对象加入还款计划列表
     *              }
     *
     *              在循环外面执行批量保存还款计划表的数据。
     *
     * @param lend 借款标对象的所有信息
     */
    private void repaymentPlan(Lend lend) {

        // 1. 创建存储还款计划列表数据的List集合对象
        List<LendReturn> lendReturnList = new ArrayList<>();
        // 2. 获取投资期数，也是还款的期数
        int len = lend.getPeriod().intValue();

        // 按还款时间生成还款计划
        for (int i = 1; i <= len; i++) {
            //创建还款计划对象
            LendReturn lendReturn = new LendReturn();
            lendReturn.setReturnNo(LendNoUtils.getReturnNo()); // 设置还款编号属性
            lendReturn.setLendId(lend.getId()); // 和标的信息id进行关联
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId()); //和借款人借款信息id关联
            lendReturn.setUserId(lend.getUserId()); // 和标的关联的用户id进行关联
            lendReturn.setAmount(lend.getAmount()); // 设置借款的金额
            lendReturn.setBaseAmount(lend.getInvestAmount()); // 设置已投金额
            lendReturn.setLendYearRate(lend.getLendYearRate()); // 设置年化利率参数
            lendReturn.setCurrentPeriod(i); //当前期数，为循环的i变量。
            lendReturn.setReturnMethod(lend.getReturnMethod()); // 设置还款方式的参数

            //说明：还款计划中的这三项 = 回款计划中对应的这三项和：因此需要先生成对应的回款计划
            //			lendReturn.setPrincipal();
            //			lendReturn.setInterest();
            //			lendReturn.setTotal();

            lendReturn.setFee(new BigDecimal(0)); // 设置手续费为0，因为放款操作已经收取了手续费。
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i)); // 第二个月开始还款
            lendReturn.setOverdue(false); // 设置是否逾期，默认false

            if (i == len) { // 如果是最后一期
                lendReturn.setLast(true); // 设置为是最后一期还款
            } else {
                lendReturn.setLast(false); // 否则不是最后一期还款
            }

            lendReturn.setStatus(0); // 设置还款状态为0，默认未归还。

            lendReturnList.add(lendReturn); // 将还款计划添加到还款计划对象的集合当中。
        }
        // 批量保存
        lendReturnService.saveBatch(lendReturnList);


        // 获取lendReturnList中还款期数与还款计划id对应map，生成期数和还款记录id对应的键和值信息
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                        Collectors.toMap(LendReturn::getCurrentPeriod,
                                        LendReturn::getId));

        // =============获取所有投资者，生成回款计划=================== //

        // 创建所有投资的回款记录列表，用于存储汇款记录的数据
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();
        // 获取当前标的下所有已支付的投资，状态1为已支付
        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), 1);


        // 遍历投资信息的列表
        for (LendItem lendItem : lendItemList) {
            // 根据投资记录的id调用下方 回款计划生成的方法，得到当前投资的回款计划列表信息
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(), lendReturnMap, lend);


            // 将当前的投资回款计划列表信息，放入所有投资的所有回款记录列表中。
            lendItemReturnAllList.addAll(lendItemReturnList);
        }

        // 遍历还款记录列表
        for (LendReturn lendReturn : lendReturnList) {

            // 通过filter、map、reduce方法将相关期数的回款数据过滤出来
            // 将当前期数的所有投资人的数据相加，就是当前期数的所有投资人的回款数据（本金、利息、总金额）
            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
                    //过滤条件：当回款计划中的还款计划id == 当前还款计划id的时候
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    //将所有回款计划中计算的每月应收本金相加
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 过滤投资人回款信息的利息收益。
            BigDecimal sumInterest = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumTotal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 将计算出的数据填充入还款计划记录：设置本金、利息、总金额
            lendReturn.setPrincipal(sumPrincipal);
            lendReturn.setInterest(sumInterest);
            lendReturn.setTotal(sumTotal);
        }

        // 批量更新还款计划列表
        lendReturnService.updateBatchById(lendReturnList); // 将 lendReturnList 的三个值计算过后再进行更新的操作

    }


    /**
     * 回款计划
     *
     *     获取当前投资记录信息
     *
     *     调用工具类计算还款的本金和利息，存储为集合类型
     *     {期数：本金/利息}
     *
     *  创建回款计划列表 {
     *      创建回款计划的记录信息
     *
     *      根据当前期数，获取还款计划信息的id
     *          将还款记录关联到回款记录中
     *
     *      设置汇款记录的基本属性
     *      计算回款本金、利息、总额（注意最后一个月的计算方式）
     *
     *      设置回款状态是否逾期了
     *
     *      将回款记录放入回款列表。
     *  }
     *
     * @param lendItemId 回款对象的id值
     * @param lendReturnMap 还款期数与还款计划id对应map
     * @param lend 标的对象id
     * @return {@link List<LendItemReturn>}
     */
    public List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {

        // 获取当前投资记录信息
        LendItem lendItem = lendItemService.getById(lendItemId);

        //  调用工具类计算还款的本金和利息，存储为集合类型
        //    {期数：本金/利息}
        BigDecimal amount = lendItem.getInvestAmount(); // 获取投资金额
        BigDecimal yearRate = lendItem.getLendYearRate(); // 获取年化利率
        Integer totalMonth = lend.getPeriod(); // 获取投资期数

        Map<Integer, BigDecimal> mapInterest;  //还款期数 -> 利息

        Map<Integer, BigDecimal> mapPrincipal; //还款期数 -> 本金

        // 根据还款方式计算本金和利息有多少
        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
            //利息
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            //本金
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }

        //  创建回款计划列表{
        List<LendItemReturn> lendItemReturnList = new ArrayList<>();

        // 遍历利息的集合元素做处理
        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            // 获取到当前期数值
            Integer currentPeriod = entry.getKey();

            // 根据当前还款期数获取还款计划表的id值
            Long lendReturnId = lendReturnMap.get(currentPeriod);

            //  创建回款计划记录对象设置基本成员属性值
            LendItemReturn lendItemReturn = new LendItemReturn();
            // 将还款记录的id关联到回款记录表当中。
            lendItemReturn.setLendReturnId(lendReturnId); // 和还款记录表进行id关联
            //  x设置回款记录的基本属性
            lendItemReturn.setLendItemId(lendItemId); // 和投资记录信息表id进行关联
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId()); // 设置投资人的id值
            lendItemReturn.setLendId(lendItem.getLendId()); // 设置标的id
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount()); // 设置投资金额参数
            lendItemReturn.setLendYearRate(lend.getLendYearRate()); // 设置年化利率
            lendItemReturn.setCurrentPeriod(currentPeriod); // 设置当前期数值
            lendItemReturn.setReturnMethod(lend.getReturnMethod()); // 设置还款方式

           //    计算回款本金、利息、总额（注意最后一个月的计算方式）
            if (currentPeriod.intValue() == lend.getPeriod().intValue()) {

                //最后一期本金 = 本金 - 前几次之和
                BigDecimal sumPrincipal = lendItemReturnList.stream()
                        .map(LendItemReturn::getPrincipal) // 计算本金
                        .reduce(BigDecimal.ZERO, BigDecimal::add); // 从0 开始加

                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);

                lendItemReturn.setPrincipal(lastPrincipal); // 设置最后一期的本金收益

                BigDecimal sumInterest = lendItemReturnList.stream()
                        .map(LendItemReturn::getInterest)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 利用预期收益减去计算过的利息，就是最后一期的利息收益。
                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest);
                lendItemReturn.setInterest(lastInterest); // 设置最后一期的收益

            } else {
                // 本金
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                // 利息
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }

            // 设置回款总金额
            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));
            lendItemReturn.setFee(new BigDecimal("0")); // 设置手续费，为0
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod)); // 设置还款日期属性
            //  设置回款状态和是否逾期等其它的属性。
            lendItemReturn.setOverdue(false); // 默认未false
            lendItemReturn.setStatus(0); // 设置状态信息，默认为0：未归还状态
            // 将回款记录放入回款列表。
            lendItemReturnList.add(lendItemReturn);

        }

        lendItemReturnService.saveBatch(lendItemReturnList);
        return lendItemReturnList;
    }
















































//
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public void makeLoan(Long lendId) {
//        //获取标的信息
//        Lend lend = baseMapper.selectById(lendId);
//
//        //放款接口调用
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("agentId", HfbConst.AGENT_ID);
//        paramMap.put("agentProjectCode", lend.getLendNo());//标的编号
//        String agentBillNo = LendNoUtils.getLoanNo();//放款编号
//        paramMap.put("agentBillNo", agentBillNo);
//
//        //平台收益，放款扣除，借款人借款实际金额=借款金额-平台收益
//        //月年化
//        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
//        //平台实际收益 = 已投金额 * 月年化 * 标的期数
//        BigDecimal realAmount = lend.getInvestAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));
//
//
//        paramMap.put("mchFee", realAmount); //商户手续费(平台实际收益)
//        paramMap.put("timestamp", RequestHelper.getTimestamp());
//        String sign = RequestHelper.getSign(paramMap);
//        paramMap.put("sign", sign);
//
//        log.info("放款参数：" + JSONObject.toJSONString(paramMap));
//        //发送同步远程调用
//        JSONObject result = RequestHelper.sendRequest(paramMap, HfbConst.MAKE_LOAD_URL);
//        log.info("放款结果：" + result.toJSONString());
//
//        //放款失败
//        if (!"0000".equals(result.getString("resultCode"))) {
//            throw new CustomException(result.getString("resultMsg"));
//        }
//
//        //更新标的信息
//        lend.setRealAmount(realAmount);
//        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
//        lend.setPaymentTime(LocalDateTime.now());
//        baseMapper.updateById(lend);
//
//        //获取借款人信息
//        Long userId = lend.getUserId();
//        UserInfo userInfo = userInfoMapper.selectById(userId);
//        String bindCode = userInfo.getBindCode();
//
//        //给借款账号转入金额
//        BigDecimal total = new BigDecimal(result.getString("voteAmt"));
//        userAccountMapper.updateAccount(bindCode, total, new BigDecimal(0));
//
//        //新增借款人交易流水
//        TransFlowBO transFlowBO = new TransFlowBO(
//                agentBillNo,
//                bindCode,
//                total,
//                TransTypeEnum.BORROW_BACK,
//                "借款放款到账，编号：" + lend.getLendNo());//项目编号
//        transFlowService.saveTransFlow(transFlowBO);
//
//        //获取投资列表信息
//        List<LendItem> lendItemList = lendItemService.selectByLendId(lendId, 1);
//        lendItemList.stream().forEach(item -> {
//
//            //获取投资人信息
//            Long investUserId = item.getInvestUserId();
//            UserInfo investUserInfo = userInfoMapper.selectById(investUserId);
//            String investBindCode = investUserInfo.getBindCode();
//
//            //投资人账号冻结金额转出
//            BigDecimal investAmount = item.getInvestAmount(); //投资金额
//            userAccountMapper.updateAccount(investBindCode, new BigDecimal(0), investAmount.negate());
//
//            //新增投资人交易流水
//            TransFlowBO investTransFlowBO = new TransFlowBO(
//                    LendNoUtils.getTransNo(),
//                    investBindCode,
//                    investAmount,
//                    TransTypeEnum.INVEST_UNLOCK,
//                    "冻结资金转出，出借放款，编号：" + lend.getLendNo());//项目编号
//            transFlowService.saveTransFlow(investTransFlowBO);
//        });
//
//        //放款成功生成借款人还款计划和投资人回款计划
//        // TODO
//        this.repaymentPlan(lend);
//    }
//
//    /**
//     * 还款计划
//     *
//     * @param lend
//     */
//    private void repaymentPlan(Lend lend) {
//
//        //还款计划列表
//        List<LendReturn> lendReturnList = new ArrayList<>();
//
//        //按还款时间生成还款计划
//        int len = lend.getPeriod().intValue();
//        for (int i = 1; i <= len; i++) {
//
//            //创建还款计划对象
//            LendReturn lendReturn = new LendReturn();
//            lendReturn.setReturnNo(LendNoUtils.getReturnNo());
//            lendReturn.setLendId(lend.getId());
//            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());
//            lendReturn.setUserId(lend.getUserId());
//            lendReturn.setAmount(lend.getAmount());
//            lendReturn.setBaseAmount(lend.getInvestAmount());
//            lendReturn.setLendYearRate(lend.getLendYearRate());
//            lendReturn.setCurrentPeriod(i);//当前期数
//            lendReturn.setReturnMethod(lend.getReturnMethod());
//
//            //说明：还款计划中的这三项 = 回款计划中对应的这三项和：因此需要先生成对应的回款计划
//            //			lendReturn.setPrincipal();
//            //			lendReturn.setInterest();
//            //			lendReturn.setTotal();
//
//            lendReturn.setFee(new BigDecimal(0));
//            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i)); //第二个月开始还款
//            lendReturn.setOverdue(false);
//            if (i == len) { //最后一个月
//                //标识为最后一次还款
//                lendReturn.setLast(true);
//            } else {
//                lendReturn.setLast(false);
//            }
//            lendReturn.setStatus(0);
//            lendReturnList.add(lendReturn);
//        }
//        //批量保存
//        lendReturnService.saveBatch(lendReturnList);
//
//        //获取lendReturnList中还款期数与还款计划id对应map
//        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
//                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
//        );
//
//        //======================================================
//        //=============获取所有投资者，生成回款计划===================
//        //======================================================
//        //回款计划列表
//        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();
//        //获取投资成功的投资记录
//        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), 1);
//        for (LendItem lendItem : lendItemList) {
//
//            //创建回款计划列表
//            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(), lendReturnMap, lend);
//            lendItemReturnAllList.addAll(lendItemReturnList);
//        }
//
//        //更新还款计划中的相关金额数据
//        for (LendReturn lendReturn : lendReturnList) {
//
//            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
//                    //过滤条件：当回款计划中的还款计划id == 当前还款计划id的时候
//                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
//                    //将所有回款计划中计算的每月应收本金相加
//                    .map(LendItemReturn::getPrincipal)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//            BigDecimal sumInterest = lendItemReturnAllList.stream()
//                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
//                    .map(LendItemReturn::getInterest)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//            BigDecimal sumTotal = lendItemReturnAllList.stream()
//                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
//                    .map(LendItemReturn::getTotal)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//            lendReturn.setPrincipal(sumPrincipal); //每期还款本金
//            lendReturn.setInterest(sumInterest); //每期还款利息
//            lendReturn.setTotal(sumTotal); //每期还款本息
//        }
//        lendReturnService.updateBatchById(lendReturnList);
//    }
//
//
//    /**
//     * 回款计划
//     *
//     * @param lendItemId
//     * @param lendReturnMap 还款期数与还款计划id对应map
//     * @param lend
//     * @return
//     */
//    public List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {
//
//        //投标信息
//        LendItem lendItem = lendItemService.getById(lendItemId);
//
//        //投资金额
//        BigDecimal amount = lendItem.getInvestAmount();
//        //年化利率
//        BigDecimal yearRate = lendItem.getLendYearRate();
//        //投资期数
//        int totalMonth = lend.getPeriod();
//
//        Map<Integer, BigDecimal> mapInterest = null;  //还款期数 -> 利息
//        Map<Integer, BigDecimal> mapPrincipal = null; //还款期数 -> 本金
//
//        //根据还款方式计算本金和利息
//        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
//            //利息
//            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
//            //本金
//            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
//        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
//            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
//            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
//        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
//            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
//            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
//        } else {
//            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
//            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
//        }
//
//        //创建回款计划列表
//        List<LendItemReturn> lendItemReturnList = new ArrayList<>();
//        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
//            Integer currentPeriod = entry.getKey();
//            //根据还款期数获取还款计划的id
//            Long lendReturnId = lendReturnMap.get(currentPeriod);
//
//            LendItemReturn lendItemReturn = new LendItemReturn();
//            lendItemReturn.setLendReturnId(lendReturnId);
//            lendItemReturn.setLendItemId(lendItemId);
//            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
//            lendItemReturn.setLendId(lendItem.getLendId());
//            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
//            lendItemReturn.setLendYearRate(lend.getLendYearRate());
//            lendItemReturn.setCurrentPeriod(currentPeriod);
//            lendItemReturn.setReturnMethod(lend.getReturnMethod());
//
//
//
//            //    计算回款本金、利息、总额（注意最后一个月的计算方式）
//            if (currentPeriod.intValue() == lend.getPeriod().intValue()) {
//                //最后一期本金 = 本金 - 前几次之和
//                BigDecimal sumPrincipal = lendItemReturnList.stream()
//                        .map(LendItemReturn::getPrincipal) // 计算本金
//                        .reduce(BigDecimal.ZERO, BigDecimal::add); // 从0 开始加
//
//                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);
//
//                lendItemReturn.setPrincipal(lastPrincipal); // 设置最后一期的本金收益
//
//                BigDecimal sumInterest = lendItemReturnList.stream()
//                        .map(LendItemReturn::getInterest)
//                        .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//                // 利用预期收益减去计算过的利息，就是最后一期的利息收益。
//                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest);
//                lendItemReturn.setInterest(lastInterest); // 设置最后一期的收益
//            } else {
//                // 本金
//                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
//                // 利息
//                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
//            }
//
//
//
//            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));
//            lendItemReturn.setFee(new BigDecimal("0"));
//            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
//            //是否逾期，默认未逾期
//            lendItemReturn.setOverdue(false);
//            lendItemReturn.setStatus(0);
//
//            lendItemReturnList.add(lendItemReturn);
//        }
//        lendItemReturnService.saveBatch(lendItemReturnList);
//
//        return lendItemReturnList;
//    }






 }
