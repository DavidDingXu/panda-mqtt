package com.panda.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 丁许
 * @date 2019-03-04 10:01
 */
@SpringBootApplication
@Slf4j
public class MqttApplication {
	public static void main(String[] args) {
		SpringApplication.run(MqttApplication.class, args);
	}
}
