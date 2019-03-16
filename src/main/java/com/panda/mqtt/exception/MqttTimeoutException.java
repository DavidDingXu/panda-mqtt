/*
 * *****************************************************
 * *****************************************************
 * Copyright (C), 2018-2020, panda-fa.com
 * FileName: com.panda.mqtt.exception.MqttTimeoutException
 * Author:   丁许
 * Date:     2019/3/11 20:09
 * *****************************************************
 * *****************************************************
 */
package com.panda.mqtt.exception;

/**
 * <Description>
 *
 * @author: 丁许
 * @date: 2019/3/11 20:09
 */
public class MqttTimeoutException extends MqttRemoteException {

	private static final long serialVersionUID = -4541083227944111553L;

	public MqttTimeoutException() {
		super();
	}

	public MqttTimeoutException(String message) {
		super(message);
	}
}
