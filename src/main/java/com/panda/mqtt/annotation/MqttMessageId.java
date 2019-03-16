package com.panda.mqtt.annotation;

import java.lang.annotation.*;

/**
 * @author 丁许
 * @date 2019-03-05 22:45
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqttMessageId {

}
