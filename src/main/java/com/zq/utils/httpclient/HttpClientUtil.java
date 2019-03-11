package com.zq.utils.httpclient;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
	
	/**
	 * 请求ajax json格式
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static HttpResult execAjaxRequest(String url,Map<String,Object> params) throws IOException {
		HttpResult result = null;
		HttpPost httpPost = new HttpPost(url);
		//StringEntity, ByteArrayEntity, InputStreamEntity, and FileEntity.
		String paramsString = JSON.toJSONString(params);
		StringEntity stringEntity = new StringEntity(paramsString,ContentType.APPLICATION_JSON);
		httpPost.setEntity(stringEntity);
		logger.info("准备发送ajax请求，url：{}，params：{}",url,paramsString);
		CloseableHttpResponse response = client.execute(httpPost);	
		try {
			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			String statusDesc = response.getStatusLine().getReasonPhrase();
			String retValue = "";
			if (entity != null) {
				long len = entity.getContentLength();
				retValue = EntityUtils.toString(entity,encoding);
				logger.debug("返回请求体的大小：{}Bytes",len);
				EntityUtils.consumeQuietly(entity);
			}
		    result = new HttpResult(statusCode,statusDesc,retValue);
		} finally {
			response.close();
		}
		logger.debug("返回结果为：{}",JSON.toJSONString(result));
		return result;
	}
	
	/**
	 * Get请求
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static HttpResult execGetRequest(String url) throws IOException {
		HttpResult result = null;
		HttpGet httpGet = new HttpGet(url);
		logger.info("准备发送Get请求，url：{}",url);
		CloseableHttpResponse response = client.execute(httpGet);	
		try {
			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			String statusDesc = response.getStatusLine().getReasonPhrase();
			String retValue = "";
			if (entity != null) {
				long len = entity.getContentLength();
				retValue = EntityUtils.toString(entity,encoding);
				logger.debug("返回实体大小：{}Bytes",len);
				EntityUtils.consumeQuietly(entity);
			}
		    result = new HttpResult(statusCode,statusDesc,retValue);
		} finally {
			response.close();
		}
		logger.debug("返回结果为：{}",JSON.toJSONString(result));
		return result;
	}
	
}
