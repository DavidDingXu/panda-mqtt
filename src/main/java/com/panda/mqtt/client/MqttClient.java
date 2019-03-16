package com.panda.mqtt.client;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.panda.mqtt.enums.QosEnum;
import com.panda.mqtt.exception.MqttRemoteException;
import com.panda.mqtt.remote.MqttFuture;
import com.panda.mqtt.remote.MqttMessage;
import lombok.Data;

import java.util.UUID;

/**
 * @author 丁许
 * @date 2019-03-06 13:41
 */
@Data
public class MqttClient {

	private Producer producer;

	private String replyParentTopic;

	public MqttClient(Producer producer) {
		this.producer = producer;
	}

	/**
	 * 同步发送一个mqtt普通消息
	 * 默认qos为1 cleanSessionFlag为true
	 * 只要不抛异常就表示成功
	 *
	 * @param topic   主题
	 * @param message 消息体
	 *
	 * @return messageId
	 */
	public String publish(String topic, String message) {
		return this.publish(topic, QosEnum.QOS_1.getValue(), true, message);
	}

	/**
	 * 同步发送一个mqtt普通消息，可以自己设置qos和cleanSessionFlag
	 * 只要不抛异常就表示成功
	 *
	 * @param topic            主题
	 * @param qos              qos
	 * @param cleanSessionFlag cleanSessionFlag
	 * @param message          消息体
	 *
	 * @return messageId
	 */
	public String publish(String topic, String qos, boolean cleanSessionFlag, String message) {
		String parentTopic = topic.substring(0, topic.indexOf("/"));
		String subTopic = topic.substring(topic.indexOf("/"));
		Message msg = new Message(parentTopic, "", message.getBytes());
		msg.putUserProperties(PropertyKeyConst.MqttSecondTopic, subTopic);
		msg.putUserProperties(PropertyKeyConst.MqttQOS, qos);
		msg.putUserProperties("cleansessionflag", "" + cleanSessionFlag);
		SendResult result = producer.send(msg);
		return result.getMessageId();
	}

	public String publishSync(String topic, Object data, int timeout) throws MqttRemoteException {
		return this.publishSync(topic, QosEnum.QOS_1.getValue(), true, data,timeout);
	}
	/**
	 * 同步发送一个mqtt普通消息并等待其返回结果
	 *
	 * @param topic            主题
	 * @param qos              qos
	 * @param cleanSessionFlag cleanSessionFlag
	 * @param data             消息体
	 * @param timeout          超时时间ms
	 *
	 * @return messageBody
	 */
	public String publishSync(String topic, String qos, boolean cleanSessionFlag, Object data, int timeout)
			throws MqttRemoteException {
		String parentTopic = topic.substring(0, topic.indexOf("/"));
		String subTopic = topic.substring(topic.indexOf("/"));
		String mId = UUID.randomUUID().toString().replaceAll("-", "");
		MqttMessage mqttMessage = new MqttMessage(mId, replyParentTopic + "/" + mId, data);
		Message msg = new Message(parentTopic, "", JSON.toJSONString(mqttMessage).getBytes());
		msg.putUserProperties(PropertyKeyConst.MqttSecondTopic, subTopic);
		msg.putUserProperties(PropertyKeyConst.MqttQOS, qos);
		msg.putUserProperties("cleansessionflag", "" + cleanSessionFlag);
		MqttFuture mqttFuture = new MqttFuture(mqttMessage, timeout);
		try {
			producer.send(msg);
		} catch (ONSClientException e) {
			mqttFuture.cancel();
		}
		return mqttFuture.get();
	}

	public void start() {
		this.producer.start();
	}

	public void shutdown() {
		this.producer.shutdown();
	}

}
