package com.inf.core.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inf.core.enums.UserBindEnum;
import com.inf.core.hfb.FormHelper;
import com.inf.core.hfb.HfbConst;
import com.inf.core.hfb.RequestHelper;
import com.inf.core.mapper.UserInfoMapper;
import com.inf.core.pojo.entites.UserBind;
import com.inf.core.mapper.UserBindMapper;
import com.inf.core.pojo.entites.UserInfo;
import com.inf.core.pojo.entites.vo.UserBindVO;
import com.inf.core.service.UserBindService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inf.utils.Assert;
import com.inf.utils.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Slf4j
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {


    /**
     *  用户绑定方法
     * @param userBindVO 绑定的对象
     * @param userId 用户的id
     * @return {@link String}
     */
    @Override
    public String commitBindUser(UserBindVO userBindVO, Long userId) {

        // 不同的user_id，有相同的身份证，如果存在的话，则不允许
        QueryWrapper<UserBind> wrapper = new QueryWrapper<>();

        // 数据库id_card 字段的身份证号和表单录入的vo字段一致
        wrapper.eq("id_card",userBindVO.getIdCard())
                // 用户是不同的，（not equals）
                .ne("user_id",userId);
        UserBind userBind = baseMapper.selectOne(wrapper);

        // 如果身份证号码不等于null则抛出异常： 身份证号码已存在。
        Assert.isNull(userBind, ResponseEnum.USER_BIND_IDCARD_EXIST_ERROR);

        // 判断用户是否已经绑定过汇付宝平台
        wrapper = new QueryWrapper<>();
        // 登录的用户id和表中的user_id 相等
        wrapper.eq("user_id",userId);
        userBind = baseMapper.selectOne(wrapper);
        // 如果绑定信息为null，没有那条记录，则再进行数据的插入操作
        if (userBind == null) {
            // 构建用户绑定记录
            userBind = new UserBind();
            // 将元数据中的内容复制到UserBind对象中，必须是同名的字段值
            BeanUtils.copyProperties(userBindVO, userBind);
            // 和参数中的 userId 进行值的关联。
            userBind.setUserId(userId);

            // 设置绑定状态，初始是未绑定状态
            userBind.setStatus(UserBindEnum.NO_BIND.getStatus());
            baseMapper.insert(userBind);
        } else { // 有绑定记录则做更新的操作。

            // 相同的user_id 如果存在，则取出数据做更新。
            BeanUtils.copyProperties(userBindVO, userBind);
            baseMapper.updateById(userBind);
        }

        // 设置绑定汇付宝平台所需的参数值。
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("agentId",HfbConst.AGENT_ID);
        paramMap.put("agentUserId",userId); // 注册的会员id，也就是用户的id

        paramMap.put("idCard",userBindVO.getIdCard()); // 绑定汇付宝的身份证号
        paramMap.put("personalName",userBindVO.getName()); // 用户的姓名
        paramMap.put("bankType",userBindVO.getBankNo()); // 银行卡类型
        paramMap.put("bankNo",userBindVO.getBankNo()); // 银行卡号
        paramMap.put("mobile",userBindVO.getMobile()); // 银行预留手机号
        // 同步回调，绑定后点击回到商户平台
        paramMap.put("returnUrl",HfbConst.USERBIND_RETURN_URL);
        // 异步回调，
        paramMap.put("notifyUrl", HfbConst.USERBIND_NOTIFY_URL);
        // 获取1970年的时间戳
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        // 利用上面map组装的数据来生成签名
        paramMap.put("sign", RequestHelper.getSign(paramMap));

        String fromStr = FormHelper.buildForm(HfbConst.USERBIND_URL,paramMap);
        // 返回结果值.
        return fromStr;
    }

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notify(Map<String, Object> paramMap) {

        var bindCode = (String)paramMap.get("bindCode"); // 根据异步回调的参数key。获取汇付宝端的绑定码
        var agentUserId = (String)paramMap.get("agentUserId"); // 获取汇付宝端的用户id值。
        // 根据user_id查询user_bind 的记录
        QueryWrapper<UserBind> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",agentUserId); // agentUserId是汇付宝端返回的id

        log.info("更新：user_bind && user_info 两张表的数据");

        // 然后更新用户的绑定表，根据组装的条件进行查询
        UserBind userBind = baseMapper.selectOne(wrapper);
        userBind.setBindCode(bindCode); // 和汇付宝平台的bindCode做关联,
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus()); // 设置绑定的状态为成功。
        // 更新操作
        baseMapper.updateById(userBind);

        // 更新用户表：==> user_info
        UserInfo userInfo = userInfoMapper.selectById(agentUserId);
        userInfo.setBindCode(bindCode); // 设置和汇付宝平台一致的bindCode码
        userInfo.setName(userBind.getName()); // 设置用户绑定表中的name属性
        userInfo.setIdCard(userBind.getIdCard()); // 绑定身份证号码

        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus()); // 设置绑定成功的状态
        userInfoMapper.updateById(userInfo); // 调用更新的方法
    }

    @Override
    public String getBindCodeByUserId(Long userId) {

        QueryWrapper<UserBind> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        UserBind userBind = baseMapper.selectOne(wrapper);
        return userBind.getBindCode();
    }
}
