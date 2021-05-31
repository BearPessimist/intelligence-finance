package com.inf.core.pojo.entites.bo;

import com.inf.core.enums.TransTypeEnum;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransFlowBO {

    private String agentBillNo; // 交易流水单号。
    private String bindCode; // 绑定码
    private BigDecimal amount; // 金额
    private TransTypeEnum transTypeEnum; // 枚举值。操作的类型。
    private String memo; // 交易流水备注信息。
}
