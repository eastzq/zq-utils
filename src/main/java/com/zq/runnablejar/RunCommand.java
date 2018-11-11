package com.zq.runnablejar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

public class RunCommand {
	
	public static void main(String[] args) {
		String rs = runCommands("set");
		System.out.println(rs);
		
	}
	public static String runCommands(String cmds){
		 
		String str = "";
		String errStr = "";
		String charset = Charset.defaultCharset().toString();
		try {
			Process p = Runtime.getRuntime().exec(cmds);
			InputStream err = p.getErrorStream();
			InputStream in = p.getInputStream();
			str = processStdout(in, charset);
			errStr = processStdout(err, charset);
			if(!StringUtils.isEmpty(errStr)){
				return str+errStr;
			}
		} catch (IOException e) {
			errStr = e.getMessage();
			return errStr;
		}
		return str;
 
	}
	
	public static String runCommands(String[] cmd){
		 
		String str = "";
		String errStr = "";
		String charset = Charset.defaultCharset().toString();
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			InputStream err = p.getErrorStream();
			InputStream in = p.getInputStream();
			str = processStdout(in, charset);
			errStr = processStdout(err, charset);
			if(!StringUtils.isEmpty(errStr)){
				return str+errStr;
			}
		} catch (IOException e) {
			errStr = e.getMessage();
			return errStr;
		}
		return str;
 
	}
	public static String processStdout(InputStream in, String charset) throws IOException {
		String s = "";
		String str = "";
		InputStreamReader insr = new InputStreamReader(in);
		BufferedReader buffer = new BufferedReader(insr);
		try {
			while ((s=buffer.readLine())!= null){
				if(StringUtils.isEmpty(str)){
					str = str+s;
				}else{
					str = "\n"+str+s;
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			buffer.close();
			insr.close();
		}
		return str;
	}
}
