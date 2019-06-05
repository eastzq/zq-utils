package com.zq.utils.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestUtil {

	private static Logger logger = LoggerFactory.getLogger(RequestUtil.class);

	/**
	 * 检查是否是json请求
	 * @param contentType
	 * @throws ServletException
	 */
	public static boolean isApplicationJson(String contentType){
		if ((contentType != null) && contentType.trim().equalsIgnoreCase("application/json")) {
			return true;
		}
		return false;
	}
	/**
	 * 获取contentType
	 * @param request
	 * @return
	 */
	public static final String parseContentType2String(HttpServletRequest request) {
        String contentType = request.getHeader("content-type");
        if (contentType != null) {
            int iSemicolonIdx;
            iSemicolonIdx = contentType.indexOf(';');
            if (iSemicolonIdx != -1) {
                contentType = contentType.substring(0, iSemicolonIdx);
            }
        }
        return contentType;
    }
	
	/**
	 * json请求转换成json字符串
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static final String parseRequestBody2String(HttpServletRequest request) throws IOException {
		String jsonStr = IOUtils.toString(request.getReader());
        return jsonStr;
    }

	

}
