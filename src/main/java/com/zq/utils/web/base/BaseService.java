package com.zq.utils.web.base;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zq.utils.configuration.Configuration;

public class BaseService {

	/**
	 * 服务参数
	 */
	protected Configuration params;
	
	/**
	 * 控制服务是否直接使用response返回数据，不经过框架！
	 */
	private boolean raw = false;
	
	/**
	 * web上下文
	 */
	private Map<String, Object> webContext = new HashMap<String, Object>();

	public BaseService() {
		this.params = Configuration.newDefault();
	}

	public BaseService(Configuration params) {
		this.params = params;
	}

	public void setParams(Configuration params) {
		this.params = params;
	}

	protected HttpServletRequest getRequest() {
		return (HttpServletRequest) webContext.get("request");
	}

	protected HttpServletResponse getResponse() {
		return (HttpServletResponse) webContext.get("response");
	}
	
	public void setWebContext(Map<String,Object> webContext) {
		this.webContext = webContext;
	}

	public boolean isRaw() {
		return raw;
	}

	public void setRaw(boolean raw) {
		this.raw = raw;
	}
}
