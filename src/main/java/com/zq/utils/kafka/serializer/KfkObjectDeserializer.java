package com.zq.utils.kafka.serializer;

import org.apache.kafka.common.serialization.Deserializer;

public class KfkObjectDeserializer implements Deserializer {

	@Override
	public Object deserialize(String topic, byte[] data) {
		return KfkSerializeUtil.deserialize(data);
	}

}
