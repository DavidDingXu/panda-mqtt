package com.panda.mqtt.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 用于mqtt处理类的注解，需要注明父topic值
 *
 * @author 丁许
 * @date 2019-03-05 8:53
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface MqttController {

	/**
	 * 监听的父topic
	 *
	 * @return 监听的父topic
	 */
	String parentTopic();
}
