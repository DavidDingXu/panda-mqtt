package com.panda.mqtt.annotation;

import java.lang.annotation.*;

/**
 * 用户mqtt处理类中的方法注解，需要注明子topic值
 *
 * @author 丁许
 * @date 2019-03-05 8:56
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqttTopicMapping {

	/**
	 * 订阅的子topic，默认可以只订阅1级topic
	 *
	 * @return 订阅的子topic
	 */
	String subTopic() default "";
}
