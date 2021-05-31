package com.inf.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inf.core.mapper.UserAccountMapper;
import com.inf.core.mapper.UserLoginRecordMapper;
import com.inf.core.pojo.entites.UserAccount;
import com.inf.core.pojo.entites.UserInfo;
import com.inf.core.mapper.UserInfoMapper;
import com.inf.core.pojo.entites.UserLoginRecord;
import com.inf.core.pojo.entites.query.UserInfoQuery;
import com.inf.core.pojo.entites.vo.LoginVO;
import com.inf.core.pojo.entites.vo.RegisterVO;
import com.inf.core.pojo.entites.vo.UserIndexVO;
import com.inf.core.pojo.entites.vo.UserInfoVO;
import com.inf.core.service.UserAccountService;
import com.inf.core.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inf.utils.Assert;
import com.inf.utils.JwtUtils;
import com.inf.utils.MD5;
import com.inf.utils.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Slf4j
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private UserLoginRecordMapper userLoginRecordMapper;

    // 事务操作，要么都成功，要么都失败
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void register(RegisterVO registerVO) {

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile",registerVO.getMobile());

        Integer count = baseMapper.selectCount(wrapper);
        // 不等于0，表示已被注册，抛出异常
        Assert.isTrue(count == 0, ResponseEnum.MOBILE_EXIST_ERROR);

        // 插入用户基本信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserType(registerVO.getUserType());
        userInfo.setNickName(registerVO.getMobile());
        userInfo.setName(registerVO.getMobile());
        userInfo.setMobile(registerVO.getMobile());

        // 使用md5加密
        userInfo.setPassword(MD5.encrypt(registerVO.getPassword()));
        userInfo.setStatus(UserInfo.STATUS_NORMAL); //正常

        //设置一张静态资源服务器上的头像图片
        userInfo.setHeadImg("https://inf-files.oss-cn-beijing.aliyuncs.com/image.png");
        baseMapper.insert(userInfo);

        //创建会员账户
        UserAccount userAccount = new UserAccount();
        // 和用户信息做关联。
        userAccount.setUserId(userInfo.getId());
        userAccountMapper.insert(userAccount);

    }


    /**
     *  用户登录
     * @param loginVO 登录对象
     * @param ip ip地址
     * @return {@link UserInfoVO}
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public UserInfoVO login(LoginVO loginVO, String ip) {

        String mobile = loginVO.getMobile();
        String password = loginVO.getPassword();
        Integer userType = loginVO.getUserType();

        // 判断用户是否存在
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile",mobile).eq("user_type",userType);
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        Assert.notNull(userInfo,ResponseEnum.LOGIN_MOBILE_ERROR);
        // 判断密码是否正确
        Assert.equals(MD5.encrypt(password),userInfo.getPassword(),ResponseEnum.LOGIN_PASSWORD_ERROR);
        // 账户是否被禁用
        Assert.equals(userInfo.getStatus(), UserInfo.STATUS_NORMAL, ResponseEnum.LOGIN_LOKED_ERROR);

        // 插入记录登录日志表的信息数据
        UserLoginRecord record = new UserLoginRecord();
        // 关联用户信息的id值
        record.setUserId(userInfo.getId());
        // 将ip设置到登录日志表当中。
        record.setIp(ip);
        userLoginRecordMapper.insert(record);

        //生成token
        String token = JwtUtils.createToken(userInfo.getId(), userInfo.getName());

        // 赋值操作
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setToken(token);
        userInfoVO.setName(userInfo.getName());
        userInfoVO.setNickName(userInfo.getNickName());
        userInfoVO.setHeadImg(userInfo.getHeadImg());
        userInfoVO.setMobile(userInfo.getMobile());

        userInfoVO.setUserType(userType);

        return userInfoVO;
    }

    /**
     *  分页查询用户列表
     * @param userPageList 分页对象
     * @param userInfoQuery 封装的查询条件
     * @return {@link IPage}
     */
    @Override
    public IPage<UserInfo> listPage(Page<UserInfo> userPageList, UserInfoQuery userInfoQuery) {
        String mobile = userInfoQuery.getMobile();
        Integer status = userInfoQuery.getStatus();
        Integer userType = userInfoQuery.getUserType();

        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();

        // 不为空再组装条件
        userInfoQueryWrapper.eq(StringUtils.isNotBlank(mobile), "mobile", mobile)
                .eq(status != null, "status", userInfoQuery.getStatus())
                .eq(userType != null, "user_type", userType);

        return baseMapper.selectPage(userPageList, userInfoQueryWrapper);
    }

    /**
     *  根据id和status解锁和锁定账户，做更新操作
     * @param id 用户id
     * @param status 状态
     */
    @Override
    public void lock(Long id, Integer status) {

        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setStatus(status);

        int userId = baseMapper.updateById(userInfo);
        if (status == 0) {
            log.info("禁用id：=> {}",userId);
        } else {
            log.info("解锁id：=> {}",userId);
        }
    }

    @Override
    public boolean checkMobile(String mobile) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        // 组装条件为手机号相等
        queryWrapper.eq("mobile", mobile);
        // 调用查询总数方法
        Integer count = baseMapper.selectCount(queryWrapper);
        // 如果有手机号数据，则返回true，为已注册状态。
        return count > 0;
    }

    /**
     *  获取用户信息展示在前台网站个人中心页
     * @param userId 用户id
     * @return {@link UserIndexVO}
     */
    @Override
    public UserIndexVO getIndexUserInfo(Long userId) {

        //用户信息
        UserInfo userInfo = baseMapper.selectById(userId);

        //账户信息
        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id", userId);
        UserAccount userAccount = userAccountMapper.selectOne(userAccountQueryWrapper);

        //登录信息
        QueryWrapper<UserLoginRecord> userLoginRecordQueryWrapper = new QueryWrapper<>();
        userLoginRecordQueryWrapper
                .eq("user_id", userId)
                .orderByDesc("id")
                .last("limit 1");
        UserLoginRecord userLoginRecord = userLoginRecordMapper.selectOne(userLoginRecordQueryWrapper);
        HashMap<String, Object> result = new HashMap<>();
        result.put("userLoginRecord", userLoginRecord);

        //组装结果数据
        UserIndexVO userIndexVO = new UserIndexVO();
        userIndexVO.setUserId(userInfo.getId());
        userIndexVO.setUserType(userInfo.getUserType());
        userIndexVO.setName(userInfo.getName());
        userIndexVO.setNickName(userInfo.getNickName());
        userIndexVO.setHeadImg(userInfo.getHeadImg());
        userIndexVO.setBindStatus(userInfo.getBindStatus());
        userIndexVO.setAmount(userAccount.getAmount());
        userIndexVO.setFreezeAmount(userAccount.getFreezeAmount());
        userIndexVO.setLastLoginTime(userLoginRecord.getCreateTime());

        return userIndexVO;
    }

    @Override
    public String getMobileByBindCode(String bindCode) {
        LambdaQueryWrapper<UserInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserInfo::getBindCode,bindCode);

        UserInfo userInfo = baseMapper.selectOne(wrapper);

        return userInfo.getMobile();
    }

}
