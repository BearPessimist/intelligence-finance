package com.inf.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inf.core.pojo.entites.UserLoginRecord;
import com.inf.core.mapper.UserLoginRecordMapper;
import com.inf.core.service.UserLoginRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户登录记录表 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Service
public class UserLoginRecordServiceImpl extends ServiceImpl<UserLoginRecordMapper, UserLoginRecord> implements UserLoginRecordService {

    @Override
    public List<UserLoginRecord> listTop50(Long userId) {

        QueryWrapper<UserLoginRecord> userLoginRecordQueryWrapper = new QueryWrapper<>();
        userLoginRecordQueryWrapper
                .eq("user_id", userId)
                // 根据倒序显示，最新的登录时间
                .orderByDesc("id")
                .last("limit 50"); // 在最后追加一段分页的sql语句

        return baseMapper.selectList(userLoginRecordQueryWrapper);
    }
}
