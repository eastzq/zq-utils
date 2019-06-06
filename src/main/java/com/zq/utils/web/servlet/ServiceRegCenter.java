package com.zq.utils.web.servlet;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zq.utils.web.RpcInvokeException;
import com.zq.utils.web.base.BaseService;

public class ServiceRegCenter {
	private static Logger logger = LoggerFactory.getLogger(ServiceRegCenter.class);
	private static Map<String,Class<? extends BaseService>> service_class = new HashMap<String,Class<? extends BaseService>>();
	
	public static void register() {
		
	}

	private static void addService(String key,Class<? extends BaseService> clazz) {
		logger.info("正在注册服务{}--->{}",key,clazz.getName());
		service_class.put(key, clazz);
	}
	
	public static BaseService getService(String key) throws RpcInvokeException {
		logger.debug("正在查找服务{}",key);
		Class<? extends BaseService> clazz =  service_class.get(key);
		if(clazz==null) {
			throw new RpcInvokeException("目标服务不存在！serviceName="+key);
		}
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RpcInvokeException("初始化服务实例出现异常！",e);
		}
	}
}
