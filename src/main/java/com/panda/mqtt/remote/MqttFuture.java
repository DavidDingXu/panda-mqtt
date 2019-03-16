package com.panda.mqtt.remote;

import com.panda.mqtt.exception.MqttRemoteException;
import com.panda.mqtt.exception.MqttTimeoutException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 丁许
 */
@Slf4j
@Data
public class MqttFuture {

	public static final int DEFAULT_TIMEOUT = 1000;

	public static final Map<String, MqttFuture> FUTURES = new ConcurrentHashMap<>();

	private final Lock lock = new ReentrantLock();

	private final Condition done = lock.newCondition();



	/**
	 * 唯一id
	 */
	private final String mId;

	/**
	 * 发送的message消息体
	 */
	private final MqttMessage message;

	/**
	 * 设置的同步调用超时时间
	 */
	private final int timeout;

	/**
	 * 等待开始时间
	 */
	private final long start = System.currentTimeMillis();

	/**
	 * 返回结果
	 */
	private volatile MqttResponse response;

	public MqttFuture(MqttMessage message, int timeout) {
		this.message = message;
		this.timeout = timeout;
		this.mId = message.getMId();
		FUTURES.put(mId, this);
	}

	/**
	 * 判断是否有response结果
	 *
	 * @return 是否返回结果
	 */
	public boolean isDone() {
		return response != null;
	}

	private long getStartTimestamp() {
		return start;
	}

	public String get() throws MqttRemoteException {
		return this.get(timeout);
	}

	public String get(int timeout) throws MqttRemoteException {
		if (timeout <= 0) {
			timeout = DEFAULT_TIMEOUT;
		}
		if (!isDone()) {
			long start = System.currentTimeMillis();
			lock.lock();
			try {
				while (!isDone()) {
					done.await(timeout, TimeUnit.MILLISECONDS);
					if (isDone() || System.currentTimeMillis() - start > timeout) {
						break;
					}
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				lock.unlock();
			}
			if (!isDone()) {
				throw new MqttTimeoutException("Waiting client-side response timeout");
			}
		}
		if (response == null) {
			throw new IllegalStateException("response cannot be null");
		}
		if (response.getMStatus().equals(MqttResponse.OK)) {
			return response.getMessageResult();
		}
		if (response.getMStatus().equals(MqttResponse.TIMEOUT)) {
			throw new MqttTimeoutException("Waiting client-side response timeout");
		}
		throw new MqttRemoteException(response.getErrorMessage());
	}

	public void cancel() {
		MqttResponse errorResult = new MqttResponse(mId);
		errorResult.setMStatus(MqttResponse.CANCEL);
		errorResult.setErrorMessage("主动请求取消");
		response = errorResult;
		FUTURES.remove(mId);
	}

	private void doReceived(MqttResponse res) {
		lock.lock();
		try {
			response = res;
			done.signal();
		} finally {
			lock.unlock();
		}
	}

	public static void received(MqttResponse response) {
		MqttFuture future = FUTURES.remove(response.getMId());
		if (future != null) {
			future.doReceived(response);
		} else {
			log.warn("The timeout response finally returned at " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.format(new Date())) + ", response " + response);
		}
	}

	private static class RemotingInvocationTimeoutScan implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					for (MqttFuture future : FUTURES.values()) {

						if (future == null || future.isDone()) {
							continue;
						}
						if (System.currentTimeMillis() - future.getStartTimestamp() > future.getTimeout()) {
							//当前mqtt请求已超时
							MqttResponse timeoutResponse = new MqttResponse(future.getMId());
							timeoutResponse.setMStatus(MqttResponse.TIMEOUT);
							MqttFuture.received(timeoutResponse);
						}
					}
					//每30ms扫一次
					Thread.sleep(30);
				} catch (Throwable e) {
					log.error("Exception when scan the timeout invocation of remoting.", e);
				}
			}
		}
	}

	static {
		Thread th = new Thread(new RemotingInvocationTimeoutScan(), "MqttResponseTimeoutScanTimer");
		th.setDaemon(true);
		th.start();
	}
}
