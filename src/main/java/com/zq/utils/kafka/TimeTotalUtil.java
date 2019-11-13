package com.zq.utils.kafka;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeTotalUtil {

	private static final Logger logger = LoggerFactory.getLogger(TimeTotalUtil.class);

	private static Map<String, Long> totalCache = new ConcurrentHashMap<>();

	public static void addStartTimestamp(String key) {
		totalCache.put(key, System.currentTimeMillis());
	}

	public static void showIntevalTime(String key, String desc) {
		long startTime = totalCache.get(key);
		long interval = System.currentTimeMillis() - startTime;
		logger.debug(key+"_"+desc+"：[{}]",interval);
	}

	public static void clearKey(String key) {
		totalCache.remove(key);
	}

}
