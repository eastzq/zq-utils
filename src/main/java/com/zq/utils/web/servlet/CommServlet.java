/**  
 * @Title: AppHessianServiceImpl.java
 * @Package com.shine.tech.epade.web.service.impl
 * @Description Hessian服务处理
 * @author bailixiang
 * @date 2019年5月28日 上午11:38:06
 * @Copyright
 */

package com.zq.utils.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zq.utils.configuration.Configuration;
import com.zq.utils.encoding.MD5Util;
import com.zq.utils.web.RequestUtil;
import com.zq.utils.web.ResponseUtil;
import com.zq.utils.web.RpcInvokeException;
import com.zq.utils.web.RpcResult;
import com.zq.utils.web.base.BaseService;

/**
 * 通用Servlet入口
 * 
 * @author zq
 *
 */
public class CommServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(CommServlet.class);

	@Override
	public void init() throws ServletException {
		super.init();
		logger.info("准备注册服务...");
		ServiceRegCenter.register();
		logger.info("服务注册完成...");
	}

	@Override
	public void service(ServletRequest request, ServletResponse response) throws IOException, ServletException {
		long start_time = System.currentTimeMillis();
		try {
			super.service(request, response);
		} finally {
			HttpServletRequest httpreq = (HttpServletRequest) request;
			logger.info("服务处理地址[{}]参数[{}]耗时[{}]秒", httpreq.getRequestURL(),httpreq.getRemoteAddr(),
					(double) (System.currentTimeMillis() - start_time) / 1000);
			
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		RpcResult rpcResult = null;
		Configuration params = null;
		Object result = null;
		BaseService service = null;
		String serviceIndex = request.getParameter("service");
		String contentType = RequestUtil.parseContentType2String(request);
		boolean isApplicationJson = RequestUtil.isApplicationJson(contentType);
		try {
			if (StringUtils.isBlank(serviceIndex)) {
				throw new RpcInvokeException("url缺少必要的参数service");
			}
			String[] args = serviceIndex.split("\\.");
			if (args == null || args.length != 2) {
				throw new RpcInvokeException("请求参数serviceIndex格式错误");
			}
			if (isApplicationJson) {
				String jsonStr = RequestUtil.parseRequestBody2String(request);
				params = Configuration.from(jsonStr);
			}
			service = ServiceRegCenter.getService(args[0]);
			result = serviceCall(service, args[1], params, request, response);
			rpcResult = RpcResult.success();
		} catch (RpcInvokeException e) {
			logger.error("RPC调用异常", e);
			rpcResult = RpcResult.error("RPC调用异常！原因："+e.toString());
		} catch (Exception e) {
			if (e instanceof InvocationTargetException) {
				Throwable ei = ((InvocationTargetException) e).getTargetException();
				logger.error("执行服务方法出现异常！", ei.toString());
				rpcResult = RpcResult.error("执行服务方法出现异常！原因：" + ei.toString());
				service.setRaw(false);
			} else {
				logger.error("RPC调用异常", e);
				rpcResult = RpcResult.error(e.toString());
			}
		} finally {
			rpcResult.setResult(result);
		}
		if(service==null || (service!=null && !service.isRaw())) {
			ResponseUtil.outputJson(response, rpcResult);
		}
	}

	public Object serviceCall(BaseService service, String methodName, Configuration params, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		logger.debug("准备调用服务名{}，方法名{}", service.getClass().getName(), methodName);
		Object retObj = null;
		service.setParams(params);
		Map<String,Object> cxt = new HashMap<String,Object>();
		cxt.put("request", request);
		cxt.put("response", response);
		service.setWebContext(cxt);
		Method method = service.getClass().getMethod(methodName, new Class[] {});

		if (method == null) {
			logger.error("当前服务不存在该方法，请检查方法名称是否正确。服务名{}，方法名{}",  service.getClass().getName(), methodName);
			throw new RpcInvokeException("当前服务不存在该方法，请检查方法名称是否正确。");
		}
		retObj = method.invoke(service);
		return retObj;
	}

	/**
	 * @Description 生成token信息
	 * @param i_salt_addtion
	 * @param i_timestamp
	 * @return String
	 * @throws BusiException
	 * @throws @author
	 *             bailixiang
	 * @date 2019年5月29日 下午2:29:21
	 * @see
	 */
	public static String createToken(String i_salt_addtion, long i_timestamp) throws Exception {
		String md5token = MD5Util.MD5Encode(i_salt_addtion + String.valueOf(i_timestamp));
		return md5token;
	}

	public static boolean verifyToken(String i_in_token) throws Exception {
		return true;
	}
}
