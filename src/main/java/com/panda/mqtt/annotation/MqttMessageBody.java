package com.panda.mqtt.annotation;

import java.lang.annotation.*;

/**
 * 该标注自动使得参数自动获得messageBody对象
 *
 * @author 丁许
 * @date 2019-03-05 9:22
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqttMessageBody {

}
