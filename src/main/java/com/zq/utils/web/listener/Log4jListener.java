package com.zq.utils.web.listener;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.xml.DOMConfigurator;


/**
 * 初始化log4j配置文件
 * @author zq
 *
 */
public class Log4jListener implements ServletContextListener {

	/**
	 * Initialize log4j when the application is being started
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		// initialize log4j here
		ServletContext context = event.getServletContext();
		String log4jConfigFile = context.getInitParameter("log4j-config-location");
		String fullPath = context.getRealPath("") + File.separator + log4jConfigFile;		
		DOMConfigurator.configure(fullPath);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}
}
