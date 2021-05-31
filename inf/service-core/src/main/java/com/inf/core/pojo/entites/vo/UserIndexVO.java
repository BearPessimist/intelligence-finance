package com.inf.core.pojo.entites.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "首页用户信息")
public class UserIndexVO {

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "用户姓名")
    private String name;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "1：出借人 2：借款人")
    private Integer userType;

    @ApiModelProperty(value = "用户头像")
    private String headImg;

    @ApiModelProperty(value = "绑定状态（0：未绑定，1：绑定成功 -1：绑定失败）")
    private Integer bindStatus;

    @ApiModelProperty(value = "帐户可用余额")
    private BigDecimal amount;

    @ApiModelProperty(value = "冻结金额")
    private BigDecimal freezeAmount;

    @ApiModelProperty(value = "上次登录时间")
    private LocalDateTime lastLoginTime;

//    rpm -ivh erlang-21.3.8.9-1.e17.x86_64.rpm
//    rpm -ivh socat-1.7.3.2-1.e16.lux.x86_64.rpm
//    rpm -ivh rabbitmq-server-3.8.1-1.e17.noarch.rpm
}
