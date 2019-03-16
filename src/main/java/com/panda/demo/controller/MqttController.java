package com.panda.demo.controller;

import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.panda.mqtt.client.MqttClient;
import com.panda.mqtt.exception.MqttRemoteException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created With ems-parent
 *
 * @author ChenHao
 * @date 2019/1/25
 * Target
 */
@RestController
@RequestMapping("/mqtt")
public class MqttController {

	@Resource
	private MqttClient mqttClient;

	@GetMapping("/publish")
	public String doPublish(@RequestParam("topic") String topic, @RequestParam("message") String message) {
		try {
			return mqttClient.publish(topic, message);
		} catch (ONSClientException e) {
			return e.getMessage();
		}
	}

	@GetMapping("/publish-sync")
	public String publishSync(@RequestParam("topic") String topic, @RequestParam("message") String message) {
		try {
			return mqttClient.publishSync(topic, message, 5000);
		} catch (MqttRemoteException e) {
			return e.getMessage();
		}
	}

}
