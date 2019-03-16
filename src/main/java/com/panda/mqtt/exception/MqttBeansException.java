package com.panda.mqtt.exception;

import org.springframework.beans.BeansException;

/**
 * @author 丁许
 * @date 2019-03-04 22:22
 */
public class MqttBeansException extends BeansException {

	public MqttBeansException(String msg) {
		super(msg);
	}

	public MqttBeansException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
