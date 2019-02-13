package com.zq.utils.concurrent.base;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base{
	public static final Logger logger = LoggerFactory.getLogger(Base.class);
	public static class R1 implements Runnable{
		@Override
		public void run() {
			logger.debug("实现Runnable接口！");
		}
	}
	
	public static class C1 implements Callable<Boolean>{
		@Override
		public Boolean call() throws Exception{
			logger.debug("实现Callable接口！");
			Thread.sleep(5000);
			return true;
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		for(int i =0;i<10;i++) {
			test2();			
		}
		logger.debug("main 线程结束");
	}
	
	// 测试Runnable接口！
	public static void test1() throws Exception{
		Thread t1 = new  Thread() {
			@Override
			public void run() {
				logger.debug("重写thread run方法！");
			}
		};
		Thread t2 = new Thread(new R1());
		t1.start();
		t2.start();
	}
	
	// 测试Callable接口
	public static void test2() throws Exception{
        FutureTask<Boolean> ft = new FutureTask<Boolean>(new C1());
        Thread t2 = new Thread(ft);
        t2.start();
        try {
            Boolean sum = ft.get(500,TimeUnit.MILLISECONDS); //FutureTask 可用于闭锁
            System.out.println(sum);
        } catch (Exception e) {
			// TODO: handle exception
        	e.printStackTrace();
		}
	}
}
