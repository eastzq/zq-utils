package com.zq.runnablejar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zq.runnablejar.intf.LocalCommandExecutor;

public class RuntimeUtils2 {

	private static Logger logger = LoggerFactory.getLogger(RuntimeUtils2.class);
	public static void main(String[] args) {
//		Runtime runtime = Runtime.getRuntime();
//		try {
//			Process process = runtime.exec("java -version", null, new File("D:/ABC/runnablejar/"));
//			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//			BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//			String line;
//			System.out.println("OUTPUT");
//			while ((line = stdoutReader.readLine()) != null) {
//				System.out.println(line);
//			}
//			System.out.println("ERROR");
//			while ((line = stderrReader.readLine()) != null) {
//				System.out.println(line);
//			}
//			int exitVal = process.waitFor();
//			System.out.println("process exit value is " + exitVal);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		BasicConfigurator.configure();

		logger.debug("heelo");

		LocalCommandExecutor ec = new LocalCommandExecutorImpl();
		ExecuteResult er = ec.executeCommand("cmd /c mvn -v", 3000);
		System.out.println(er.getExecuteOut());
	}

}