package com.zq.utils.kafka;

/**
 * 获取kafka参数
 * @author zq
 * @note 消费者使用参数：serverClusters,topic,subscriberName
 * @note 生产者使用参数：serverClusters,topic
 * 
 */
public class KfkMqParam {
	/**
	 * kafka集群配置
	 */
	private String serverClusters;
	/**
	 * 主题名称
	 */
	private String topic;
	/**
	 * 获取消费者时使用，标识订阅者
	 * @note 如果多个消费者有相同标识，那么消费者将共同完成消息队列的消费。相当于队列模式
	 * @note 如果多个消费者有不同标识，那么每个消费者各自消费队列里的所有消息。相当于发布和订阅模式
	 */
	private String subscriberName;
	
	public KfkMqParam() {
	}
	
	public KfkMqParam(String serverClusters,String topic,String subscriberName) {
		this.serverClusters=serverClusters;
		this.topic=topic;
		this.subscriberName =subscriberName;
	}
	public KfkMqParam(String serverClusters,String topic) {
		this(serverClusters,topic,null);
	}
	
	public String getServerClusters() {
		return serverClusters;
	}
	public void setServerClusters(String serverClusters) {
		this.serverClusters = serverClusters;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getSubscriberName() {
		return subscriberName;
	}
	public void setSubscriberName(String subscriberName) {
		this.subscriberName = subscriberName;
	}
	
	
}
