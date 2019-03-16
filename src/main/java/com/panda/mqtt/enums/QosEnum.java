package com.panda.mqtt.enums;

/**
 * @author 丁许
 * @date 2019-03-06 16:19
 */
public enum QosEnum {

	/**
	 * QOS0
	 */
	QOS_0("0", "cleanSession=true 无离线消息，在线消息只尝试推一次 cleanSession=false 无离线消息，在线消息只尝试推一次"),

	/**
	 * QOS1
	 */
	QOS_1("1", "cleanSession=true 无离线消息，在线消息保证可达 cleanSession=false 有离线消息，所有消息保证可达"),

	/**
	 * QOS2
	 */
	QOS_2("2", "cleanSession=true 无离线消息，在线消息保证只推一次 cleanSession=false 暂不支持");

	private String value;

	private String desc;

	QosEnum(String value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
