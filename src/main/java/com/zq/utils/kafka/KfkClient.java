package com.zq.utils.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteConsumerGroupsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zq.utils.kafka.monitor.KfkMessageOffset;

/**
 * <pre>
 * </pre>
 * 
 * kfk客户端，提供创建主题，删除主题，查询主题等基本功能！
 * 
 * @author zq
 *
 */
public class KfkClient {

    private static final Logger logger = LoggerFactory.getLogger(KfkClient.class);

    private Properties props = new Properties();

    private AdminClient adminClient;

    private String bootServers;

    /**
     * @param bootServers 集群地址
     */
    public KfkClient(String bootServers) {
        if (StringUtils.isBlank(bootServers)) {
            throw new MqException("构造参数【bootServers】不能为空！");
        }
        this.bootServers = bootServers;
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootServers);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, "3");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "com.shine.tech.epmessagebus.kafka.serializer.KfkObjectSerializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        this.adminClient = AdminClient.create(props);
    }

    /**
     * 
     * @param topicName      主题名称。
     * @param partitionNum   分区数目，代表当前主题的消息有几个分区，需要结合集群环境调整。
     * @param replicationNum 副本数目，1个代表无副本无备份，通常设置2~3个。
     * @throws MqException 失败异常
     */
    public void createTopic(String topicName, int partitionNum, int replicationNum) {
        NewTopic newTopic = new NewTopic(topicName, partitionNum, (short) replicationNum);
        List<NewTopic> newTopics = new ArrayList<NewTopic>();
        newTopics.add(newTopic);
        CreateTopicsResult result = adminClient.createTopics(newTopics);
        Map<String, KafkaFuture<Void>> futures = result.values();
        KafkaFuture<Void> future = futures.get(topicName);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("创建topic出现异常，topicName：{}", topicName, e);
            throw new MqException("创建topic出现异常，原因:" + e.getMessage(), e);
        }
    }

    /**
     * @param topicName 主题名称。
     * @throws MqException 失败异常
     */
    public void deleteTopic(String topicName) {
        if (!this.existTopic(topicName)) {
            throw new MqException("删除topic出现异常，原因:当前主题不存在！主题名称：" + topicName);
        }
        List<String> topics = new ArrayList<>();
        topics.add(topicName);
        DeleteTopicsResult result = adminClient.deleteTopics(topics);
        Map<String, KafkaFuture<Void>> futures = result.values();
        KafkaFuture<Void> future = futures.get(topicName);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("删除topic出现异常，topicName：{}", topicName, e);
            throw new MqException("删除topic出现异常，原因:" + e.getMessage(), e);
        }
    }

    /**
     * @return List<String>
     * @throws MqException 失败异常
     */
    public List<String> listTopic() {
        List<String> topicNames = new ArrayList<String>();
        ListTopicsResult result = adminClient.listTopics();
        KafkaFuture<Collection<TopicListing>> futures = result.listings();
        Collection<TopicListing> topics = null;
        try {
            topics = futures.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("查询topic出现异常", e);
            throw new MqException("查询所有topic出现异常，原因:" + e.getMessage(), e);
        }
        for (TopicListing topicListing : topics) {
            topicNames.add(topicListing.name());
        }
        return topicNames;
    }

    /**
     * @param topicName 判断topic 是否存在
     * @throws MqException mq异常
     */
    public boolean existTopic(String topicName) {
        List<String> topics = this.listTopic();
        return topics.contains(topicName);
    }

    /**
     * 获取 指定topic offset用于监控。
     * 
     * @param groupId
     * @param topicName
     * @return
     */
    public Map<String, KfkMessageOffset> getLatestOffsetStatus(String groupId, String topicName) {
        Map<String, KfkMessageOffset> po = new HashMap<>();
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "com.shine.tech.epmessagebus.kafka.serializer.KfkObjectDeserializer");
        KafkaConsumer<String, Object> consumer = null;
        try {
            ListConsumerGroupOffsetsResult result = adminClient.listConsumerGroupOffsets(groupId);
            KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> future = result.partitionsToOffsetAndMetadata();
            Map<TopicPartition, OffsetAndMetadata> tm = future.get();
            List<TopicPartition> filterTopicPartitions = new ArrayList<>();
            for (TopicPartition tp : tm.keySet()) {
                if (tp.topic().equals(topicName)) {
                    filterTopicPartitions.add(tp);
                }
            }
            if(filterTopicPartitions.isEmpty()) {
                return null;
            }
            consumer = new KafkaConsumer<String, Object>(props);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(filterTopicPartitions, Duration.ofMillis(5000));
            for (Entry<TopicPartition, Long> entry : endOffsets.entrySet()) {
                TopicPartition tp = entry.getKey();
                String tpKey = tp.topic() + ":" + tp.partition();
                OffsetAndMetadata om = tm.get(tp);
                KfkMessageOffset msgOffset = new KfkMessageOffset(tpKey, om.offset(), entry.getValue());
                po.put(tpKey, msgOffset);
            }
            return po;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("取消费者offset出现异常！", e);
            throw new MqException("取消费者offset出现异常！" + e.getMessage(), e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }

    /**
     * 获取 offset用于监控。
     * 
     * @param groupId
     * @return
     */
    public Map<String, KfkMessageOffset> getLatestOffsetStatus(String groupId) {
        ListConsumerGroupOffsetsResult result = adminClient.listConsumerGroupOffsets(groupId);
        KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> future = result.partitionsToOffsetAndMetadata();
        Map<String, KfkMessageOffset> po = new HashMap<>();
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "com.shine.tech.epmessagebus.kafka.serializer.KfkObjectDeserializer");
        KafkaConsumer<String, Object> consumer = null;
        try {
            Map<TopicPartition, OffsetAndMetadata> tm = future.get();
            consumer = new KafkaConsumer<String, Object>(props);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(tm.keySet(), Duration.ofMillis(5000));
            for (Entry<TopicPartition, Long> entry : endOffsets.entrySet()) {
                TopicPartition tp = entry.getKey();
                String tpKey = tp.topic() + ":" + tp.partition();
                OffsetAndMetadata om = tm.get(tp);
                KfkMessageOffset msgOffset = new KfkMessageOffset(tpKey, om.offset(), entry.getValue());
                po.put(tpKey, msgOffset);
            }
            return po;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("取消费者offset出现异常！", e);
            throw new MqException("取消费者offset出现异常！" + e.getMessage(), e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }

    /**
     * 获取 offset用于监控。
     * 
     * @param groupId
     * @return
     */
    public boolean isFinishAllLog(String groupId) {
        Map<String, KfkMessageOffset> st = this.getLatestOffsetStatus(groupId);
        for (Entry<String, KfkMessageOffset> entry : st.entrySet()) {
            KfkMessageOffset value = entry.getValue();
            if (value.getOffset() != value.getTotal()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFinishAllLog(String groupId, String topicName) {
        Map<String, KfkMessageOffset> st = this.getLatestOffsetStatus(groupId, topicName);
        if(st==null) {
            return false;
        }
        for (Entry<String, KfkMessageOffset> entry : st.entrySet()) {
            KfkMessageOffset value = entry.getValue();
            if (value.getOffset() != value.getTotal()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param groupId 判断topic 是否存在
     * @throws MqException mq异常
     */
    public void deleteConsumerGroup(String groupId) {
        List<String> ll = new ArrayList<>();
        ll.add(groupId);
        DeleteConsumerGroupsResult result = adminClient.deleteConsumerGroups(ll);
        Map<String, KafkaFuture<Void>> futures = result.deletedGroups();
        KafkaFuture<Void> future = futures.get(groupId);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("删除groupId出现异常，groupId：{}", groupId, e);
            throw new MqException("删除groupId出现异常，原因:" + e.getMessage(), e);
        }
        return;
    }

    /**
     * @param 取消订阅主题
     * @throws MqException mq异常
     */
    public void unsubscribe(String groupId, String topicName) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "com.shine.tech.epmessagebus.kafka.serializer.KfkObjectDeserializer");
        KafkaConsumer<String, Object> consumer = null;
        try {
            consumer = new KafkaConsumer<String, Object>(props);
            consumer.unsubscribe();
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }
    /**
     * @param 订阅主题
     * @throws MqException mq异常
     */
    public void subscribe(String groupId, String topicName) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "com.shine.tech.epmessagebus.kafka.serializer.KfkObjectDeserializer");
        KafkaConsumer<String, Object> consumer = null;
        try {
            consumer = new KafkaConsumer<String, Object>(props);
            consumer.subscribe(Arrays.asList(topicName));
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }

    
    
    /**
     * 关闭客户端
     */
    public void close() {
        if (this.adminClient != null) {
            this.adminClient.close();
        }
    }

}
