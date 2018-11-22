package com.zq.utils.httpclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class HttpClientUtil {
	public static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
	public static final String encoding = "UTF-8";
	private static CloseableHttpClient client = HttpClients.createDefault();
	
	public static HttpResult execAjax(String url,Map<String,Object> params) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		//StringEntity, ByteArrayEntity, InputStreamEntity, and FileEntity.
		String paramsString = JSON.toJSONString(params);
		StringEntity stringEntity = new StringEntity(paramsString,ContentType.APPLICATION_JSON);
		httpPost.setEntity(stringEntity);
		HttpResult result = null;
		logger.debug("开始发送ajax请求，url：{}，params：{}",url,paramsString);
		CloseableHttpResponse response = client.execute(httpPost);	
		try {
			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			String statusDesc = response.getStatusLine().getReasonPhrase();
			// 有可能没有返回值，有些验证请求是不会有返回结果的。
			String retValue = "";
		    if (entity != null) {
		        long len = entity.getContentLength();
		        retValue = EntityUtils.toString(entity,encoding);
		        logger.debug("返回请求体的大小：{}Bytes",len);
		    }
		    result = new HttpResult(statusCode,statusDesc,retValue);
			EntityUtils.consumeQuietly(entity);
		} finally {
			response.close();
		}
		logger.debug("返回结果为：{}",JSON.toJSONString(result));
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		execAjax("http://www.baidu.com/", new HashMap());
	}
	
}
