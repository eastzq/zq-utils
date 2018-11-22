package com.zq.utils.httpclient;

public class HttpResult {
	private int statusCode;
	private String statusDesc;
	private String retValue;
	
	
	
	public HttpResult(int statusCode, String statusDesc, String retValue) {
		super();
		this.statusCode = statusCode;
		this.statusDesc = statusDesc;
		this.retValue = retValue;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getStatusDesc() {
		return statusDesc;
	}
	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
	public String getRetValue() {
		return retValue;
	}
	public void setRetValue(String retValue) {
		this.retValue = retValue;
	}
	
	@Override
	public String toString() {
		return "HttpResult [statusCode=" + statusCode + ", statusDesc=" + statusDesc + ", retValue=" + retValue + "]";
	}
	
	
}

