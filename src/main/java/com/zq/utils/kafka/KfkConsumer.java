package com.zq.utils.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.requests.IsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kafka事务消费者。 默认支持事务消费。需要手动提交事务，不提交则事务无效，下一次会收到重复消息。
 * 
 * @author zq
 *
 */
public class KfkConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KfkConsumer.class);

    private KafkaConsumer<String, Object> consumer;

    private String subject;

    private String subscriberName;

    private Properties props = new Properties();

    private boolean isTransacted = true;

    /**
     * 
     * @param topic 主题名称
     * @param subscriberName
     * 订阅者名称，如果订阅者名称相同，则名字相同的订阅者为一组，共同消费消息，类似队列，消息队列里有m1，m2两个消息，名称一致的A1，A2消费者，可能A1消费m1，A2消费m2，消费完成为止。
     * 如果消费者名称不一致，则每个消费者都能接受到所有的消息。如A消费m1和m2，B也会消费m1和m2。
     */
    public KfkConsumer(String bootServers, String topic, String subscriberName, Integer partition) {

        this.subject = topic;
        this.subscriberName = subscriberName;
        if (StringUtils.isBlank(subscriberName) || StringUtils.isBlank(subject)) {
            throw new MqException("构造参数【topic】和【subscriberName】不能为空！");
        }

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, subscriberName);
//		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
//				OffsetResetStrategy.NONE.toString().toLowerCase(Locale.ROOT));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "com.shine.tech.epmessagebus.kafka.serializer.KfkObjectDeserializer");
//		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 5000);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG,
                IsolationLevel.READ_COMMITTED.toString().toLowerCase(Locale.ROOT));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 2 * 1024 * 1024);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 30000);
        // props.put("group.id", subject);
        this.consumer = new KafkaConsumer<String, Object>(props);

        if (partition != null) {
            TopicPartition partion = new TopicPartition(topic, partition);
            consumer.assign(Arrays.asList(partion));
        } else {
            consumer.subscribe(Arrays.asList(subject));
        }

    }

    /**
     * 消息类型 T 获取消息，自动转型。
     * 
     * @param millis 如果有消息立刻返回，没有消息会尝试millis后返回。
     * @return 无消息时返回空的List<T>。
     */
    public <T> List<T> receiveObject(long millis) {

        List<T> list = new ArrayList<T>();
        ConsumerRecords<String, Object> records = this.consumer.poll(Duration.ofMillis(millis));
        Set<TopicPartition> partitions = consumer.assignment();
        StringBuilder buff = new StringBuilder();
        for (TopicPartition objinfo : partitions) {
            buff.append("{").append(objinfo.topic()).append(")[").append(objinfo.partition())
                    .append("]");
        }
        logger.info("consumer partition {}", buff);
        if (!records.isEmpty()) {
            for (ConsumerRecord<String, Object> record : records) {
                list.add((T) record.value());
            }
        }
        return list;
    }

    /**
     * 预留方法，如果以后有优化会新增代码
     */
    public void beginTransaction() {

    }

    /**
     * 关闭连接
     */
    public void close() {

        consumer.close();
    }

    /**
     * 提交事务
     */
    public void commit() {

        consumer.commitSync();
    }

    /**
     * 撤销
     */
    public void rollback() {

    }

}
