package com.zq.utils.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zq.utils.web.ResponseUtil;
import com.zq.utils.web.RpcResult;

/**
 * 作为自启动服务之一，当找不到对应服务时调用！
 */
public class EmptyServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(EmptyServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		RpcResult rpcResult = null;	
		rpcResult = RpcResult.error("未找到对应的服务！请检查url");
		ResponseUtil.outputJson(response, rpcResult);
	}
}
