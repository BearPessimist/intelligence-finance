package com.heepay.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * UserBind
 * </p>
 *
 *  封装汇付宝平台向商户平台发起的具体请求地址和参数信息。
 * @author qy
 */
@Data
public class NotifyVo implements Serializable {

	private static final long serialVersionUID = 1L;


	private String notifyUrl;

	private Map<String, Object> paramMap;

	public NotifyVo() {}

	public NotifyVo(String notifyUrl, Map<String, Object> paramMap) {
		this.notifyUrl = notifyUrl;
		this.paramMap = paramMap;
	}

    public static void main(String[] args) {
        HashMap<String, Object> map = new HashMap<>();
    }
}

