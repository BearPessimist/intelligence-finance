package com.inf.core.pojo.entites.query;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LendQuery {

    private String title;
    private BigDecimal yearRate;
    private byte period;
    private String returnMethod;
}







