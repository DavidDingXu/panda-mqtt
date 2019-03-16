package com.panda.demo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 丁许
 * @date 2019-03-05 22:55
 */
@Data
public class AlarmVo implements Serializable {

	private static final long serialVersionUID = 1640096470239239486L;

	private Integer id;

	private String code;
}
