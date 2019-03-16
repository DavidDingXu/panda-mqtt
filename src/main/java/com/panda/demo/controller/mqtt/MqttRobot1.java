package com.panda.demo.controller.mqtt;

import com.panda.demo.vo.AlarmVo;
import com.panda.mqtt.annotation.MqttController;
import com.panda.mqtt.annotation.MqttMessageBody;
import com.panda.mqtt.annotation.MqttMessageId;
import com.panda.mqtt.annotation.MqttTopicMapping;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 丁许
 * @date 2019-03-05 9:17
 */
@Slf4j
@MqttController(parentTopic = "robot1")
public class MqttRobot1 {

	@MqttTopicMapping
	public void dealFirstTopic() {
		log.info("MqttRobot1.dealAlarm 收到消息啦，只处理了一级topic");
	}

	@MqttTopicMapping(subTopic = "alarm")
	public void dealAlarm(@MqttMessageId String messageId, @MqttMessageBody AlarmVo alarmVo) {
		log.info("MqttRobot1.dealAlarm 收到消息啦");
		log.info("messageId:{}", messageId);
		log.info("alarmVo:{}", alarmVo);
	}

	@MqttTopicMapping(subTopic = "task")
	public void dealTask() {
		log.info("MqttRobot1.dealTask 收到消息啦");
	}
}
