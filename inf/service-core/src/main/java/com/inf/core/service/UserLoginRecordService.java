package com.inf.core.service;

import com.inf.core.pojo.entites.UserLoginRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户登录记录表 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface UserLoginRecordService extends IService<UserLoginRecord> {

    /**
     *  查询显示五十条登录日志
     * @param userId 用户id
     * @return {@link List<UserLoginRecord>}
     */
    List<UserLoginRecord> listTop50(Long userId);
}
