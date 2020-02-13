package com.zq.utils.kafka;

import java.io.Serializable;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kafka 消息发送者。 默认开启事务，需要手动提交
 * 
 * @author zq
 *
 */
public class KfkProducer {

	private static final Logger logger = LoggerFactory.getLogger(KfkProducer.class);
	private KafkaProducer<String, Object> producer;
	private String subject;
	private Properties props = new Properties();
	private boolean isTransacted = true;
	private String transactionId;

	/**
	 * @param topic         主题名称
	 * @param transactionId 事务id，kafka通过这个id实现事务控制，在beginTransaction方法会根据事务id初始化事务。当为空时默认设置UUID。
	 */
	public KfkProducer(String bootServers, String topic, String transactionId) {
		this.subject = topic;
		if (StringUtils.isBlank(topic)) {
			throw new MqException("构造参数【topic】不能为空！");
		}
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootServers);
		if (StringUtils.isBlank(transactionId)) {
			logger.debug("tansactionId为空，使用UUID");
			transactionId = UUID.randomUUID().toString();
		}
		logger.debug("使用producer事务id为：tansactionId：{}", transactionId);
		this.transactionId = transactionId;
		props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionId);
		props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000);
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.RETRIES_CONFIG, "3");
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"com.shine.tech.epmessagebus.kafka.serializer.KfkObjectSerializer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		this.producer = new KafkaProducer<String, Object>(props);
	}

	/**
	 * 
	 * @param topic 主题名称。
	 */
	public KfkProducer(String bootServers, String topic) {
		this(bootServers, topic, null);
	}

	/**
	 * 发送对象
	 * 异步
	 * 
	 * @param o 实现序列化接口的对象。
	 */
	public void sendObject(String msgKey, Serializable o) {
		producer.send(new ProducerRecord<>(subject, msgKey, o));
	}

	/**
	 * 发送对象
	 * 异步
	 * @param o 实现序列化接口的对象。
	 */
	public void sendObject(Serializable o) {
		producer.send(new ProducerRecord<>(subject, null, o));
	}

	
	/**
	 * 发送对象
	 * 同步
	 * @param o 实现序列化接口的对象。
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void sendObjectSync(String msgKey, Serializable o){
		try {
			producer.send(new ProducerRecord<>(subject, msgKey, o)).get();
		}catch (Exception e) {
			logger.error("同步发送消息到kafka失败，原因：{}",e.getMessage());
			throw new MqException("同步发送消息到kafka失败，原因："+e.getMessage(),e);
		}
	}

	/**
	 * 发送对象
	 * 同步
	 * @param o 实现序列化接口的对象。
	 */
	public void sendObjectSync(Serializable o) {
		try {
			producer.send(new ProducerRecord<>(subject, null, o)).get();
		}catch (Exception e) {
			logger.error("同步发送消息到kafka失败，原因：{}",e.getMessage());
			throw new MqException("同步发送消息到kafka失败，原因："+e.getMessage(),e);
		}
	}

	
	/**
	 * 开启事务
	 */
	public void beginTransaction() {
		producer.initTransactions();
		producer.beginTransaction();
	}

	/**
	 * 关闭连接
	 */
	public void close() {
		producer.close(Duration.ofMillis(500));
	}

	/**
	 * 提交事务
	 */
	public void commit() {
//		producer.flush();
		producer.commitTransaction();
	}

	/**
	 * 回退，撤销事务
	 */
	public void rollback() {
		producer.abortTransaction();
	}

	public String getTransactionId() {
		return transactionId;
	}

}
