package com.zq.utils.web.base;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.zq.utils.configuration.Configuration;

public class BaseService {

	protected Configuration params;
	
	protected boolean raw = false;
	
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

	protected HttpServletRequest getResponse() {
		return (HttpServletRequest) webContext.get("response");
	}
	
	public void setWebContext(Map<String,Object> webContext) {
		this.webContext = webContext;
	}

	public boolean isRaw() {
		return raw;
	}
}
