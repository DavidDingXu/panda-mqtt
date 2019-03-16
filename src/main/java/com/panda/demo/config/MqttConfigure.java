package com.panda.demo.config;

import com.panda.mqtt.config.MqttConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author 丁许
 * @date 2019-03-06 11:02
 */
@Import({ MqttConfig.class})
@Configuration
public class MqttConfigure {

}
