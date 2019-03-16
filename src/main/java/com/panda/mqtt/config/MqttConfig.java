package com.panda.mqtt.config;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.*;
import com.panda.mqtt.annotation.MqttController;
import com.panda.mqtt.annotation.MqttMessageBody;
import com.panda.mqtt.annotation.MqttMessageId;
import com.panda.mqtt.annotation.MqttTopicMapping;
import com.panda.mqtt.client.MqttClient;
import com.panda.mqtt.exception.MqttBeansException;
import com.panda.mqtt.handler.MqttHandlerFactory;
import com.panda.mqtt.remote.MqttFuture;
import com.panda.mqtt.remote.MqttResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Properties;
import java.util.Set;

/**
 * @author 丁许
 * @date 2019-03-05 9:24
 */
@Slf4j
@Configuration
public class MqttConfig implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

	@Value("${ali.mqtt.accessKey}")
	private String accessKey;

	@Value("${ali.mqtt.secretKey}")
	private String secretKey;

	@Value("${ali.mqtt.groupId}")
	private String groupId;

	@Value("${ali.mqtt.namesrvAddr}")
	private String namesrvAddr;

	@Value("${ali.mqtt.sendMsgTimeoutMillis}")
	private String sendMsgTimeoutMillis;

	@Value("${ali.mqtt.consumeThreadNums}")
	private Integer comsumeThreadNums;

	@Value("${ali.mqtt.replyParentTopic}")
	private String replyParentTopic;


	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//根据已经注册的topic处理来订阅
		Properties properties = new Properties();
		properties.setProperty(PropertyKeyConst.GROUP_ID, this.groupId);
		// AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
		properties.put(PropertyKeyConst.AccessKey, this.accessKey);
		// SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
		properties.put(PropertyKeyConst.SecretKey, this.secretKey);
		// 设置 TCP 接入域名，到控制台的实例基本信息中查看
		properties.put(PropertyKeyConst.NAMESRV_ADDR, this.namesrvAddr);
		properties.put(PropertyKeyConst.ConsumeThreadNums, this.comsumeThreadNums);
		Consumer mqConsumer = ONSFactory.createConsumer(properties);
		Set<String> parentTopicSet = MqttHandlerFactory.getParentTopicSet();
		if (parentTopicSet.size() == 0) {
			log.warn("当前应用并未有任何topic订阅");
		}
		//根据parentTopic和subTopic订阅
		parentTopicSet.forEach(parentTopic -> {
			log.info("Add a new rocketMq subscription,topic:{}", parentTopic);
			mqConsumer.subscribe(parentTopic, "*", (message, context) -> {
				log.debug("MqReceive Message: " + message);
				//获得topic
				String mqttFirstTopic = message.getTopic();
				String mqttSecondTopic = message.getUserProperties(PropertyKeyConst.MqttSecondTopic);
				if (null == mqttSecondTopic) {
					//只有1级topic的情况
					mqttSecondTopic = "/";
				}
				if (!"/".equals(mqttSecondTopic.substring(mqttSecondTopic.length() - 1))) {
					mqttSecondTopic += "/";
				}
				String mqttTopic = mqttFirstTopic + mqttSecondTopic;

				Method method = MqttHandlerFactory.getMqttHandler(mqttTopic);
				if (null == method) {
					log.warn("当前没有处理该topic的handler,topic:{}", mqttTopic);
					return Action.CommitMessage;
				} else {
					//获得mqtt的一些数据
					String messageId = message.getUserProperties("UNIQ_KEY");
					String messageBody = new String(message.getBody());
					//处理入参
					Parameter[] parameters = method.getParameters();
					Object[] paramValues = new Object[parameters.length];
					for (int i = 0; i < parameters.length; i++) {
						if (parameters[i].isAnnotationPresent(MqttMessageId.class)) {
							//@MqttMessageId注解的参数
							paramValues[i] = messageId;
						} else if (parameters[i].isAnnotationPresent(MqttMessageBody.class)) {
							//@MqttMessageBody注解的参数
							Class parameterClazz = parameters[i].getType();
							try {
								paramValues[i] = JSONObject.parseObject(messageBody, parameterClazz);
							} catch (Exception e) {
								log.error("mqttMessageBody 格式错误，messageId:{},messageBody:{}", messageId, messageBody);
								//								return Action.ReconsumeLater;
								return Action.CommitMessage;
							}
						} else {
							//自己定义的一些参数就给null把
							paramValues[i] = null;
						}
					}
					try {
						method.invoke(MqttHandlerFactory.getMqttController(mqttTopic), paramValues);
					} catch (Exception e) {
						log.error("处理失败啦");
					}
				}
				return Action.CommitMessage;
			});
		});
		//订阅伪同步请求回复
		log.info("Add a new rocketMq subscription,topic:{}", replyParentTopic);
		mqConsumer.subscribe(replyParentTopic, "*", (message, context) -> {
			String mqttSecondTopic = message.getUserProperties(PropertyKeyConst.MqttSecondTopic);
			if (null == mqttSecondTopic) {
				mqttSecondTopic = "";
			}
			String mId = mqttSecondTopic.replaceAll("/", "");
			String messageBody = new String(message.getBody());
			MqttResponse response = new MqttResponse(mId);
			response.setMessageResult(messageBody);
			MqttFuture.received(response);
			return Action.CommitMessage;
		});
		mqConsumer.start();
		log.info("MqConsumer Started");
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class beanClazz = bean.getClass();
		if (beanClazz.isAnnotationPresent(MqttController.class)) {
			String parentTopic = ((MqttController) beanClazz.getAnnotation(MqttController.class)).parentTopic();
			MqttHandlerFactory.getParentTopicSet().add(parentTopic);
			for (Method method : beanClazz.getMethods()) {
				if (method.isAnnotationPresent(MqttTopicMapping.class)) {
					String subTopic = method.getAnnotation(MqttTopicMapping.class).subTopic();
					String realTopic;
					if ("".equals(subTopic)) {
						realTopic = parentTopic + "/";
					} else {
						realTopic = (parentTopic + "/" + subTopic + "/").replaceAll("/+", "/");
					}
					if (null != MqttHandlerFactory.getMqttHandler(realTopic)) {
						throw new MqttBeansException(bean.getClass().getSimpleName() + " topic 重复定义,值为" + realTopic);
					}
					MqttHandlerFactory.registerMqttHandler(realTopic, method);
					MqttHandlerFactory.registerMqttController(realTopic, bean);
					log.info("MqttHandler Mapped \"{}\" onto {}", realTopic, method.toString());
				}
			}
		}
		return bean;
	}

	@Bean(name = "mqttClient", initMethod = "start", destroyMethod = "shutdown")
	public MqttClient mqttClient() {
		Properties properties = new Properties();
		properties.setProperty(PropertyKeyConst.GROUP_ID, this.groupId);
		// AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
		properties.put(PropertyKeyConst.AccessKey, this.accessKey);
		// SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
		properties.put(PropertyKeyConst.SecretKey, this.secretKey);
		//设置发送超时时间，单位毫秒
		properties.setProperty(PropertyKeyConst.SendMsgTimeoutMillis, "3000");
		// 设置 TCP 接入域名，到控制台的实例基本信息中查看
		properties.put(PropertyKeyConst.NAMESRV_ADDR, this.namesrvAddr);
		Producer producer = ONSFactory.createProducer(properties);
		MqttClient mqttClient = new MqttClient(producer);
		mqttClient.setReplyParentTopic(replyParentTopic);
		return mqttClient;
	}


}
