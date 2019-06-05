package com.zq.utils.encoding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对md5字符串进行处理，隐藏真实的md5信息
 * 
 * @author admin
 *
 */
public class StringResort {

	public static final Logger logger = LoggerFactory.getLogger(StringResort.class);

	public static String test = "{\"zhangssern\":\"111111\"}";

	public static String resortString(String origin) throws Exception {
		char[] array = origin.toCharArray();
		char[] result = new char[array.length];
		for (int i = 0; i < array.length; i++) {
			if (i % 2 == 0 && i < (array.length - 1)) {
				result[i] = array[i + 1];
				result[i + 1] = array[i];
			} else if (i % 2 == 0 && i == (array.length - 1)) {
				result[i] = array[i];
			}
		}
		return String.valueOf(result);
	}

	public static void main(String[] args) throws Exception {
		String md5String = MD5Util.MD5Encode(test);
		String t = resortString(md5String);
		logger.debug(md5String);
		logger.debug(t);
	}

}
