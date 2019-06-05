package com.zq.utils.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.alibaba.fastjson.JSON;

public class ResponseUtil {
	public static void outputJson(HttpServletResponse response,RpcResult result,String contentType,String encoding) throws IOException{
		response.setStatus(HttpStatus.SC_OK);
		response.setCharacterEncoding(encoding);
		response.setContentType(contentType);
		PrintWriter writer = response.getWriter();
		String jsonStr = JSON.toJSONString(result);
		writer.write(JSON.toJSONString(jsonStr));
	}
	
	public static void outputJson(HttpServletResponse response,RpcResult result) throws IOException{
		outputJson(response,result,"application/json;charset=utf-8","utf-8");
	}
	
}
