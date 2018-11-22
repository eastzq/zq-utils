package com.zq.utils.httpclient.demo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zq.utils.cli.LocalCommandExecutorImpl;

public class QuickStart {
	public static final Logger logger = LoggerFactory.getLogger(LocalCommandExecutorImpl.class);

	public static void main(String[] args) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("https://www.baidu.com/");
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		// The underlying HTTP connection is still held by the response object
		// to allow the response content to be streamed directly from the network
		// socket.
		// In order to ensure correct deallocation of system resources
		// the user MUST call CloseableHttpResponse#close() from a finally clause.
		// Please note that if response content is not fully consumed the underlying
		// connection cannot be safely re-used and will be shut down and discarded
		// by the connection manager.

		// response状态
		System.out.println(response1.getProtocolVersion());
		System.out.println(response1.getStatusLine().getStatusCode());
		System.out.println(response1.getStatusLine().getReasonPhrase());
		System.out.println(response1.getStatusLine().toString());
		
		// 返回头
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		response.addHeader("Set-Cookie", "c1=a; path=/; domain=localhost");
		response.addHeader("Set-Cookie", "c2=b; path=\"/\", c3=c; domain=\"localhost\"");
		Header h1 = response.getFirstHeader("Set-Cookie");
		System.out.println(h1);
		Header h2 = response.getLastHeader("Set-Cookie");
		System.out.println(h2);
		Header[] hs = response.getHeaders("Set-Cookie");
		System.out.println(hs.length);
		HeaderIterator it = response.headerIterator("Set-Cookie");

		StringEntity myEntity = new StringEntity("important message", ContentType.create("text/plain", "UTF-8"));

		System.out.println(myEntity.getContentType());
		System.out.println(myEntity.getContentLength());
		System.out.println(EntityUtils.toString(myEntity));
		System.out.println(EntityUtils.toByteArray(myEntity).length);

		while (it.hasNext()) {
			System.out.println(it.next());
		}

		try {
			logger.info("{}", response1.getStatusLine());
			HttpEntity entity = response1.getEntity();
			// do something useful with the response body
		    if (entity != null) {
		        long len = entity.getContentLength();
		        System.out.println(EntityUtils.toString(entity,"UTF-8"));
		        if (len != -1 && len < 2048) {
		        } else {
		            // Stream content out
		        }
		    }
			// and ensure it is fully consumed
			EntityUtils.consume(entity);
		} finally {
			response1.close();
		}

	}

	private static void post() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("https://hc.apache.org/httpcomponents-client-ga/quickstart.html");
		File file = new File("somefile.txt");
		FileEntity entity = new FileEntity(file, 
		    ContentType.create("text/plain", "UTF-8"));        
		//StringEntity, ByteArrayEntity, InputStreamEntity, and FileEntity.
		HttpPost httppost = new HttpPost("http://localhost/action.do");
		httppost.setEntity(entity);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", "vip"));
		nvps.add(new BasicNameValuePair("password", "secret"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps,Consts.UTF_8));
		CloseableHttpResponse response2 = httpclient.execute(httpPost);

		try {
			System.out.println(response2.getStatusLine());
			HttpEntity entity2 = response2.getEntity();
			// do something useful with the response body
			// and ensure it is fully consumed
			EntityUtils.consume(entity2);

		} finally {
			response2.close();
		}
	}

}
