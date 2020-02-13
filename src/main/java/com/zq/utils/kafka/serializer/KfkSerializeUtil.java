package com.zq.utils.kafka.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KfkSerializeUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(KfkSerializeUtil.class);

	public static byte[] serialize(Serializable object) {
		
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			// 序列化
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			byte[] bytes = baos.toByteArray();
			return bytes;
		} catch (Exception e) {
			logger.error("序列化对象出现异常！",e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> Object deserialize(byte[] bytes) {
		ByteArrayInputStream bais = null;
		T tmpObject = null;
		try {
			// 反序列化
			bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			tmpObject = (T) ois.readObject();
			return tmpObject;
		} catch (Exception e) {
			logger.error("序列化对象出现异常！",e);
		}
		return null;
	}
}
