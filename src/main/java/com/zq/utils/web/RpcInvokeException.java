package com.zq.utils.web;

public class RpcInvokeException extends RuntimeException{
	
	public RpcInvokeException() {
		super();
	}
	public RpcInvokeException(String msg) {
		super(msg);
	}
	
	public RpcInvokeException(String msg,Throwable t) {
		super(msg,t);
	}
	
}
