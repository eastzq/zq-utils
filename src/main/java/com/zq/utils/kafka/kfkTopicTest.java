package com.zq.utils.kafka;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.requests.IsolationLevel;

public class kfkTopicTest {

//    bootstrap.servers=localhost:9092
//    group.id=test
//    enable.auto.commit=true
//    auto.commit.interval.ms=1000
//    key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
//    value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
    public static void main(String[] args) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                OffsetResetStrategy.NONE.toString().toLowerCase(Locale.ROOT));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG,
                IsolationLevel.READ_COMMITTED.toString().toLowerCase(Locale.ROOT));
        
        AdminClient adminClient = AdminClient.create(props);
        NewTopic newTopic = new NewTopic("topicName", 1, (short) 1);
        List<NewTopic> newTopics = new ArrayList<NewTopic>();
        newTopics.add(newTopic);
        adminClient.createTopics(newTopics);
        adminClient.close();

    }
}
