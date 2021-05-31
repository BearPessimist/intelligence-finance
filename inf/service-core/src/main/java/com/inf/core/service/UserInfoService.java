package com.inf.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inf.core.pojo.entites.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inf.core.pojo.entites.query.UserInfoQuery;
import com.inf.core.pojo.entites.vo.LoginVO;
import com.inf.core.pojo.entites.vo.RegisterVO;
import com.inf.core.pojo.entites.vo.UserIndexVO;
import com.inf.core.pojo.entites.vo.UserInfoVO;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     *  注册用户信息
     * @param registerVO 注册用户的对象
     */
    void register(RegisterVO registerVO);


    /**
     *  用户登录认证
     * @param loginVO 登录对象
     * @param ip ip地址
     * @return {@link UserInfoVO}
     */
    UserInfoVO login(LoginVO loginVO, String ip);

    /**
     *  分页查询用户列表
     * @param userPageList 分页对象
     * @param userInfoQuery 封装的查询条件
     * @return {@link IPage}
     */
    IPage<UserInfo> listPage(Page<UserInfo> userPageList, UserInfoQuery userInfoQuery);

    /**
     *  锁定与解锁用户方法
     * @param id 用户id
     * @param status 状态
     */
    void lock(Long id, Integer status);

    /**
     *  检查手机号是否已注册
     * @param mobile 手机号
     * @return boolean
     */
    boolean checkMobile(String mobile);

    /**
     *  获取用户信息展示在前台网站首页面
     * @param userId
     * @return
     */
    UserIndexVO getIndexUserInfo(Long userId);


    /**
     *  根据bindCode获取手机号
     * @param bindCode
     * @return
     */
    String getMobileByBindCode(String bindCode);
}
