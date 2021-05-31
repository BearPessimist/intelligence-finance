package com.inf.core.pojo.entites.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "投标信息")
public class InvestVO {

    private Long lendId; // 标的id

    //投标金额
    private String investAmount;

    //用户id
    private Long investUserId;

    //用户姓名
    private String investName;
}
