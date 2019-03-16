package com.panda.demo.controller.mqtt;

import com.panda.mqtt.annotation.MqttController;
import com.panda.mqtt.annotation.MqttTopicMapping;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 丁许
 * @date 2019-03-05 9:18
 */
@Slf4j
@MqttController(parentTopic = "robot2")
public class MqttRobot2 {

	@MqttTopicMapping(subTopic = "alarm")
	public void dealAlarm() {
		log.info("MqttRobot1.dealAlarm 收到消息啦");
	}

	@MqttTopicMapping(subTopic = "task")
	public void dealTask() {
		log.info("MqttRobot1.dealTask 收到消息啦");
	}
}
