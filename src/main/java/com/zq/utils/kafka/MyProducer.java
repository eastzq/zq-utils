package com.zq.utils.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.StringSerializer;

public class MyProducer {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
	        Properties props = new Properties();
	        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
	        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-transactional-id");
	        props.put(ProducerConfig.ACKS_CONFIG, "all");
	        props.put(ProducerConfig.RETRIES_CONFIG, "3");
	        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,true); 
	        Producer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());

	        producer.initTransactions();

	        try {
	            producer.beginTransaction();
	            for (int i = 0; i < 5; i++) {
	                Future<RecordMetadata> send = producer
	                        .send(new ProducerRecord<>("my-topic", Integer.toString(i), Integer.toString(i)));
	                System.out.println(send.get().offset());
	                TimeUnit.MILLISECONDS.sleep(1000L);
	            }
	            producer.commitTransaction();
	        } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
	            // We can't recover from these exceptions, so our only option is to close the producer and exit.
	            producer.close();
	        } catch (KafkaException e) {
	            // For all other exceptions, just abort the transaction and try again.
	            producer.abortTransaction();
	        }
	        producer.close();
	    }

}
