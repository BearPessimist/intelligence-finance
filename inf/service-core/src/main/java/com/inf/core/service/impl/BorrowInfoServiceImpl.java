package com.inf.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inf.core.enums.BorrowInfoStatusEnum;
import com.inf.core.enums.BorrowerStatusEnum;
import com.inf.core.enums.UserBindEnum;
import com.inf.core.mapper.BorrowerMapper;
import com.inf.core.mapper.IntegralGradeMapper;
import com.inf.core.mapper.UserInfoMapper;
import com.inf.core.pojo.entites.BorrowInfo;
import com.inf.core.mapper.BorrowInfoMapper;
import com.inf.core.pojo.entites.Borrower;
import com.inf.core.pojo.entites.IntegralGrade;
import com.inf.core.pojo.entites.UserInfo;
import com.inf.core.pojo.entites.vo.BorrowInfoApprovalVO;
import com.inf.core.pojo.entites.vo.BorrowerDetailVO;
import com.inf.core.service.BorrowInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inf.core.service.BorrowerService;
import com.inf.core.service.DictService;
import com.inf.core.service.LendService;
import com.inf.utils.Assert;
import com.inf.utils.ResponseEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private IntegralGradeMapper integralGradeMapper;


    @Autowired
    private DictService dictService;

    @Autowired
    private BorrowerMapper borrowerMapper;

    @Autowired
    private BorrowerService borrowerService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {

        // 获取用户的积分
        UserInfo userInfo = userInfoMapper.selectById(userId);
        // 如果用户信息为空则抛出用户不存在异常
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);
        Integer integral = userInfo.getIntegral(); // 获取用户积分
        // 根据用户积分查询额度
        var wrapper = new QueryWrapper<IntegralGrade>();
        wrapper.le("integral_start", integral) // 小于等于
                .ge("integral_end",integral); // 大于等于

        IntegralGrade integralGrade = integralGradeMapper.selectOne(wrapper);
        if (integral == null) {// 如果积分为空
            return new BigDecimal("0"); // 赋默认0值
        }
        return integralGrade.getBorrowAmount(); // 返回借款额度值。
    }

    // 提交保存借款申请信息。borrow_info表
    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {

        // 获取查询已登录的用户信息
        UserInfo userInfo = userInfoMapper.selectById(userId);
        // 判断借款人的绑定状态，是否绑定支付平台，如果没有绑定则抛出错误信息
        Assert.isTrue(userInfo.getBindStatus() == UserBindEnum.BIND_OK.getStatus().intValue(),ResponseEnum.USER_NO_BIND_ERROR);

        // 判断借款人额度申请状态，如果没有审核则抛出未审核的错误信息。
        Assert.isTrue(userInfo.getBorrowAuthStatus() == BorrowerStatusEnum.AUTH_OK.getStatus().intValue(), ResponseEnum.USER_NO_AMOUNT_ERROR);
        // 判断借款人额度是否充足
        BigDecimal borrowAmount = this.getBorrowAmount(userId); // 先根据已登录用户，调用获取额度信息的方法
        Assert.isTrue(borrowInfo.getAmount().doubleValue() <= borrowAmount.doubleValue(),
                ResponseEnum.USER_AMOUNT_LESS_ERROR); // 如果余额不足，则抛出借款额度不足错误信息

        // 存储数据到borrow_info表中
        borrowInfo.setUserId(userId); // 设置用户id的关联。

        // 百分比转换为小数
        borrowInfo.setBorrowYearRate(borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));

        // 修改借款申请为审核中，然后等待其它的操作。
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());
        // 插入数据到表中。
        baseMapper.insert(borrowInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        var wrapper = new QueryWrapper<BorrowInfo>();
        // 只查询status这个字段
        wrapper.select("status").eq("user_id",userId);
        // selectObjs方法：只返回一个字段的值。
        List<Object> objects = baseMapper.selectObjs(wrapper);
        if (objects.size() ==0) { // 如果值为0
            // 抛出未认证异常信息
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }

        return (Integer) objects.get(0); // 返回状态信息数据
    }


    // TODO: 2021-05-02 也可以用分页的形式去查询数据。
    @Override
    public List<BorrowInfo> selectList() {

        List<BorrowInfo> borrowInfoList = baseMapper.selectBorrowInfoList();

        List<BorrowInfo> collect = borrowInfoList.stream().peek(borrowInfo -> {

            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
            String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
            String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
            // 组装数据到map集合当中
            Map<String, Object> map = borrowInfo.getParam();
            map.put("returnMethod", returnMethod);
            map.put("moneyUse", moneyUse);
            map.put("status", status);
        }).collect(Collectors.toList());
        return collect;
    }

    @Override // 查询借款人借款信息详情页面
    public Map<String, Object> getBorrowInfoDetail(Long id) {

        // 查询借款对象 borrower_info 表。对应BorrowInfo对象
        BorrowInfo borrowInfo = baseMapper.selectById(id);
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
        String status = BorrowerStatusEnum.getMsgByStatus(borrowInfo.getStatus());

        // 组装数据到map集合当中
        Map<String, Object> map = borrowInfo.getParam();
        map.put("returnMethod", returnMethod); // 还款方式
        map.put("moneyUse", moneyUse); // 资金用途
        map.put("status", status); // 借款信息的状态

        // 查询借款人的对象 borrower 表。对应封装过的BorrowerDetailVo对象
        QueryWrapper<Borrower> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",borrowInfo.getUserId()); // 必须和关联的userId相等。
        Borrower borrower = borrowerMapper.selectOne(wrapper); // 先根据条件查询出借款人的数据。
        // 然后根据借款人id查询出对应封装的对象。
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        // 使用Map集合组装数据
        Map<String, Object> result = new HashMap<>();
        result.put("borrowInfo", borrowInfo);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    @Autowired
    private LendService lendService;

    /**
     *  审核借款信息的service方法
     *      1. 修改自身borrower_info 表中的status状态，通过，或者不通过
     *      2. 随着借款信息的审核通过，则创建lend表的标的信息数据。
     * @param borrowInfoApprovalVO 审批借款人信息的VO对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {

        // 先获取VO对象中的借款信息id
        Long borrowInfoId = borrowInfoApprovalVO.getId();
        // 然后根据id查询出BorrowInfo 对象信息
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfoId);

        Integer borrowerStatus = borrowerService.getStatusByUserId(borrowInfo.getUserId());

        // 如果借款人没有认证成功则抛出 借款人未认证通过异常信息。
        Assert.isTrue(borrowerStatus == BorrowerStatusEnum.AUTH_OK.getStatus().intValue(),
                ResponseEnum.BORROWER_UNAUTHORIZATION);

        // 然后设置 BorrowInfo 对象中的状态，使用borrowInfoApprovalVO 前端传的状态值进行赋值。
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());

        // 进行对 BorrowInfo 对象的更新操作。
        baseMapper.updateById(borrowInfo);

        // 如果借款信息被审核通过，则产生新的标的记录，lend 表
        if (borrowInfo.getStatus().intValue() == BorrowInfoStatusEnum.CHECK_OK.getStatus().intValue()) {

            // 创建新标的，调用lendService层编写过的方法。
            lendService.createLend(borrowInfoApprovalVO,borrowInfo);
        }
    }
}
