package com.inf.core.service;

import com.inf.core.pojo.entites.UserBind;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inf.core.pojo.entites.vo.UserBindVO;

import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface UserBindService extends IService<UserBind> {

    /**
     *  账户绑定方法
     * @param userBindVO 绑定的对象
     * @param userId 用户的id
     * @return {@link String}
     */
    String commitBindUser(UserBindVO userBindVO, Long userId);

    /**
     *  账户绑定发送请求到汇付宝端，异步回调信息的方法
     * @param paramMap 参数集合
     */
    void notify(Map<String, Object> paramMap);

    // 分别获取投资人和借款人的绑定id
    String getBindCodeByUserId(Long investUserId);

}
