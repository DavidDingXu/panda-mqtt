# mqtt工具包的使用
## 1. 在yml配置中添加相关配置，配置示例如下，其中xxx改为自己使用的即可

```
ali:
  mqtt:
    accessKey: xxx
    secretKey: xxx
    groupId: xxx
    namesrvAddr: xxx
    sendMsgTimeoutMillis: 3000
    #消费者线程固定位50个
    consumeThreadNums: 50
#    用于同步调用返回发送的topic
    replyParentTopic: xxx
```
## 2. 添加工具包中的MqttConfig

```java
@Import({ MqttConfig.class})
@Configuration
public class MqttConfigure {

}
```

## 3. 自定义注解的使用

```java
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
```

## 4. 测试同步调用，模拟mqtt客户端消息返回代码

mqtt客户端实现代码示例参考阿里云官方demo [https://github.com/AliwareMQ/lmq-demo ](https://github.com/AliwareMQ/lmq-demo )

其中xxx的地方都改成自己的即可，下面代码中`mqttClient2.publish(replyTopic, message);`即将结果发送到replyTopic中

```java
public class MqttClientTest {

	public static void main(String[] args) throws Exception {
		String instanceId = "xxx";
		String endPoint = "xxx";
		String accessKey = "xxx";
		String secretKey = "xxx";
		String clientId = "xxx";
		final String parentTopic = "xxx";
		//这边需自定义mqtt客户端topic,
		final String mq4IotTopic = parentTopic + "/" + "xxx" + "/xxx";
		final int qosLevel = 0;
		ConnectionOptionWrapper connectionOptionWrapper = new ConnectionOptionWrapper(instanceId, accessKey, secretKey,
				clientId);
		final MemoryPersistence memoryPersistence = new MemoryPersistence();
		final MqttClient mqttClient = new MqttClient("tcp://" + endPoint + ":1883", clientId, memoryPersistence);
		final MqttClient mqttClient2 = new MqttClient("tcp://" + endPoint + ":1883", clientId, memoryPersistence);
		/**
		 * 客户端设置好发送超时时间，防止无限阻塞
		 */
		mqttClient.setTimeToWait(5000);
		final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
		mqttClient.setCallback(new MqttCallbackExtended() {

			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
				/**
				 * 客户端连接成功后就需要尽快订阅需要的 topic
				 */
				System.out.println("connect success");
				executorService.submit(new Runnable() {

					@Override
					public void run() {
						try {
							final String topicFilter[] = { mq4IotTopic };
							final int[] qos = { qosLevel };
							mqttClient.subscribe(topicFilter, qos);
						} catch (MqttException e) {
							e.printStackTrace();
						}
					}
				});
			}

			@Override
			public void connectionLost(Throwable throwable) {
				throwable.printStackTrace();
			}

			@Override
			public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
				System.out.println("receive msg from topic " + s + " , body is " + new String(mqttMessage.getPayload()));
				JSONObject jsonObject = JSON.parseObject(new String(mqttMessage.getPayload()));
				String mId = jsonObject.getString("mId");
				String replyTopic = jsonObject.getString("replyTopic");
				String result = mId + "回复啦";
				MqttMessage message = new MqttMessage(result.getBytes());
				message.setQos(qosLevel);
				//这边会将结果发送到replyTopic中
				mqttClient2.publish(replyTopic, message);
				System.out.println("发送啦");
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
				System.out.println("send msg succeed topic is : " + iMqttDeliveryToken.getTopics()[0]);
			}
		});
		mqttClient.connect(connectionOptionWrapper.getMqttConnectOptions());
		mqttClient2.connect(connectionOptionWrapper.getMqttConnectOptions());
		Thread.sleep(Long.MAX_VALUE);
	}
}
```