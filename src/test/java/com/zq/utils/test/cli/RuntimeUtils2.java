package com.zq.utils.test.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.zq.utils.cli.ExecuteResult;
import com.zq.utils.cli.ProcessBuilderCommandExecutor;
import com.zq.utils.cli.intf.CommandExecutor;

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
		CommandExecutor ec = new ProcessBuilderCommandExecutor("GBK");
		ExecuteResult er = ec.executeCommand("git st", 1000);
		logger.debug(er.getExecuteOut());
		logger.debug("执行结果打印：{}", JSON.toJSONString(er));
	}

}