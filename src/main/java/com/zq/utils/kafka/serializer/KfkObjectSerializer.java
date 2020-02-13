package com.zq.utils.kafka.serializer;

import java.io.Serializable;

import org.apache.kafka.common.serialization.Serializer;

public class KfkObjectSerializer implements Serializer{

	@Override
	public byte[] serialize(String topic, Object data) {
		return KfkSerializeUtil.serialize((Serializable)data);
	}

}
