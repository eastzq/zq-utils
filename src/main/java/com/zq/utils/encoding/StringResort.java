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
		logger.debug(bytesToHexString(new byte[] {1,2,3,4,123,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}));
	}

	/**
	 * 字节数组转16进制输出！
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\n");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv+" ");
			if((i+1)%10 ==0) {
				stringBuilder.append("\n");
			}
		}
		return stringBuilder.toString();
	}

}
