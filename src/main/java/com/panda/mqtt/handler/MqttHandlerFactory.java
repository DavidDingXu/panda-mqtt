package com.panda.mqtt.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author 丁许
 */
public class MqttHandlerFactory {

	/**
	 * 用于存储mqtt处理类的bean,key为parentTopic/subTopic
	 */
	private static Map<String, Object> mqttControllers = new HashMap<>();

	/**
	 * 用于存储mqtt处理方法,key为parentTopic/subTopic
	 */
	private static Map<String, Method> mqttHandlers = new HashMap<>();

	/**
	 * 存储parentTopic列表
	 */
	private static Set<String> parentTopicSet = new HashSet<>();

	/**
	 * 注册一个mqtt处理类bean
	 *
	 * @param topic topic全称 parentTopic/subTopic
	 * @param bean  mqtt处理类bean
	 */
	public static void registerMqttController(String topic, Object bean) {
		mqttControllers.put(topic, bean);
	}

	/**
	 * 注册一个mqtt处理方法
	 *
	 * @param topic  topic全称 parentTopic/subTopic
	 * @param method mqtt处理方法
	 */
	public static void registerMqttHandler(String topic, Method method) {
		mqttHandlers.put(topic, method);
	}

	/**
	 * 根据topic获得对应的bean
	 *
	 * @param topic parentTopic/subTopic
	 *
	 * @return mqtt处理类bean
	 */
	public static Object getMqttController(String topic) {
		return mqttControllers.get(topic);
	}

	/**
	 * 根据topic获得对应的Method
	 *
	 * @param topic parentTopic/subTopic
	 *
	 * @return Method
	 */
	public static Method getMqttHandler(String topic) {
		return mqttHandlers.get(topic);
	}

	public static Set<String> getParentTopicSet() {
		return parentTopicSet;
	}
}
