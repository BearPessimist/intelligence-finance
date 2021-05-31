package com.inf.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inf.core.enums.BorrowerStatusEnum;
import com.inf.core.enums.IntegralEnum;
import com.inf.core.mapper.BorrowerAttachMapper;
import com.inf.core.mapper.UserInfoMapper;
import com.inf.core.mapper.UserIntegralMapper;
import com.inf.core.pojo.entites.Borrower;
import com.inf.core.mapper.BorrowerMapper;
import com.inf.core.pojo.entites.BorrowerAttach;
import com.inf.core.pojo.entites.UserInfo;
import com.inf.core.pojo.entites.UserIntegral;
import com.inf.core.pojo.entites.vo.BorrowerApprovalVO;
import com.inf.core.pojo.entites.vo.BorrowerAttachVO;
import com.inf.core.pojo.entites.vo.BorrowerDetailVO;
import com.inf.core.pojo.entites.vo.BorrowerVO;
import com.inf.core.service.BorrowerAttachService;
import com.inf.core.service.BorrowerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inf.core.service.DictService;
import com.inf.core.service.UserIntegralService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Autowired
    private BorrowerAttachMapper borrowerAttachMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Autowired
    private DictService dictService;

    @Autowired
    private BorrowerAttachService borrowerAttachService;

    @Autowired
    private UserIntegralMapper userIntegralMapper;

    /**
     *  保存借款人信息
     * @param borrowerVO 提交的表单vo对象
     * @param userId 已登录用户id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId) {

        // 先根据已登录的用户id查询出用户的信息。
        UserInfo userInfo = userInfoMapper.selectById(userId);

        //保存借款人信息
        Borrower borrower = new Borrower();
        // 将vo对象的属性复制到Borrower对象中
        BeanUtils.copyProperties(borrowerVO, borrower);
        // 设置已登录的用户id，和Borrower对象进行关联。
        borrower.setUserId(userId);
        // 根据查询出的已登录用户信息中取出名称，存到Borrower对象当中。
        borrower.setName(userInfo.getName());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());

        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());//认证中
        baseMapper.insert(borrower);

        // 保存附件的信息
        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            // 将Borrower中的id赋值到BorrowerAttach对象关联的Borrower对象id中。进行数据的关联。
            borrowerAttach.setBorrowerId(borrower.getId());
            // 保存操作
            borrowerAttachMapper.insert(borrowerAttach);
        });

        // 这里也需要更新会员状态，更新为认证中，因为方便查询用户信息时也查询到借款人的信息状态。
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);
    }

    /**
     *  根据id查询借款人的状态
     * @param userId 已登录的用户id
     * @return {@link Integer}
     */
    @Override
    public Integer getStatusByUserId(Long userId) {

        QueryWrapper<Borrower> wrapper = new QueryWrapper<>();
        wrapper.select("status").eq("user_id",userId);
        // 只查询一个字段
        List<Object> objects = baseMapper.selectObjs(wrapper);
        // 如果一条记录都没有
        if (objects.size() == 0) {
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }

        return (Integer) objects.get(0); // 返回获取到的第一个值
    }




    @Override
    public IPage<Borrower> listPage(Page<Borrower> page, String keyword) {

        if (StringUtils.isBlank(keyword)) { // 如果查询条件为空则直接调用下面的普通分页方法。
            return baseMapper.selectPage(page,null);
        }
        var wrapper = new LambdaQueryWrapper<Borrower>();
        wrapper
                .like(Borrower::getName,keyword)
                .or()
                .like(Borrower::getIdCard,keyword)
                .or()
                .like(Borrower::getMobile,keyword)
                .orderByDesc(Borrower::getId);
        // 返回分页的数据信息
        return baseMapper.selectPage(page, wrapper);
    }


    @Override
    public BorrowerDetailVO getBorrowerDetailVOById(Long id) {

        Borrower borrower = baseMapper.selectById(id); // 先根据id获取到借款人的所有信息
        BorrowerDetailVO borrowerDetailVO = new BorrowerDetailVO();
        BeanUtils.copyProperties(borrower,borrowerDetailVO);
        // 是否结婚
        borrowerDetailVO.setMarry(borrower.getMarry() ? "是" : "否");
        // 判断是男还是女
        borrowerDetailVO.setSex(borrower.getSex() == 1 ? "男" : "女");

        //计算下拉列表选中内容
        String education = dictService.getNameByParentDictCodeAndValue("education", borrower.getEducation());
        String industry = dictService.getNameByParentDictCodeAndValue("moneyUse", borrower.getIndustry());
        String income = dictService.getNameByParentDictCodeAndValue("income", borrower.getIncome());
        String returnSource = dictService.getNameByParentDictCodeAndValue("returnSource", borrower.getReturnSource());
        String contactsRelation = dictService.getNameByParentDictCodeAndValue("relation", borrower.getContactsRelation());

        //设置下拉列表选中内容
        borrowerDetailVO.setEducation(education);
        borrowerDetailVO.setIndustry(industry);
        borrowerDetailVO.setIncome(income);
        borrowerDetailVO.setReturnSource(returnSource);
        borrowerDetailVO.setContactsRelation(contactsRelation);

        // 审批状态
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());
        borrowerDetailVO.setStatus(status);

        // 组装附件列表。调用转换好的附件对象方法
        List<BorrowerAttachVO> borrowerAttachVOList = borrowerAttachService.selectBorrowerAttachVOList(id);
        // 将附件信息设置进借款人VO对象当中
        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachVOList);
        return borrowerDetailVO;
    }


    /**
     *  借款人审核的service方法，
     *  1. 修改借款人（borrower表的status状态，通过或者不通过）
     *  2. 添加 user_integral 表的数据
     *  3. 在user_info表中的integral字段添加总积分
     *  4. 修改user_info表的冗余字段borrow_auth_status 和 integral 总积分记录信息。
     * @param borrowerApprovalVO 封装VO对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowerApprovalVO borrowerApprovalVO) {
        // 获取借款额度申请的借款人id
        Long borrowerId = borrowerApprovalVO.getBorrowerId();
        // 获取借款额度申请的借款人对象
        Borrower borrower = baseMapper.selectById(borrowerId);

        // 设置审核状态，通过或者不通过，根据前端传的值
        borrower.setStatus(borrowerApprovalVO.getStatus());
        // 修改借款人对象，做更新的操作。
        baseMapper.updateById(borrower);

        // 获取用户的id
        Long userId = borrower.getUserId();

        // 根据用户id查询用户对象，将下面计算的积分只存到用户表的积分字段当中
        UserInfo userInfo = userInfoMapper.selectById(userId);
        // 获取用户的原始积分
        Integer integral = userInfo.getIntegral();

        // 计算基本信息积分
        var userIntegral = new UserIntegral();
        userIntegral.setUserId(userId); // 和用户id进行关联。
        // 根据前端填写的值设置积分
        userIntegral.setIntegral(borrowerApprovalVO.getInfoIntegral());
        userIntegral.setContent("借款人基本信息"); // 设置内容
        userIntegralMapper.insert(userIntegral); // 做用户积分表添加的操作

        // 将前端添加的积分和用户原始积分想加，并且赋值给 currentIntegral 变量使用。
        int currentIntegral = integral + borrowerApprovalVO.getInfoIntegral();

        // 如果身份证信息正确，根据前端传来的值
        if (borrowerApprovalVO.getIsIdCardOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId); // 和用户id进行关联。
            userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral()); // 设置枚举常量
            userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
            userIntegralMapper.insert(userIntegral); // 做添加的操作

            currentIntegral += IntegralEnum.BORROWER_IDCARD.getIntegral();
        }

        // 如果房产信息正确，根据前端传来的值
        if (borrowerApprovalVO.getIsHouseOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId); // 和用户id进行关联。
            userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
            userIntegralMapper.insert(userIntegral); // 做添加的操作

            currentIntegral += IntegralEnum.BORROWER_HOUSE.getIntegral();
        }

        // 如果车辆信息正确，根据前端传来的值
        if (borrowerApprovalVO.getIsCarOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId); // 和用户id进行关联。
            userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
            userIntegralMapper.insert(userIntegral); // 做添加的操作

            // 加上车辆通过的积分。
            currentIntegral += IntegralEnum.BORROWER_CAR.getIntegral();
        }

        // 最后设置用户总积分
        userInfo.setIntegral(currentIntegral);

        // 最后修改审核状态，borrowerApprovalVO.getStatus()为前端传过来的值信息。
        userInfo.setBorrowAuthStatus(borrowerApprovalVO.getStatus());

        // 最后再更新UserInfo表
        userInfoMapper.updateById(userInfo);
    }
}
