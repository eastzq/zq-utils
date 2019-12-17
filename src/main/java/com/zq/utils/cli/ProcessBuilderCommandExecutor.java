package com.zq.utils.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
/**
* LocalCommandExecutorImpl.java
*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessBuilderCommandExecutor extends BaseCommandExecutor {

    public ProcessBuilderCommandExecutor() {
    }
    
	public ProcessBuilderCommandExecutor(String encoding) {
        super(encoding);
    }
    public static final Logger logger = LoggerFactory.getLogger(ProcessBuilderCommandExecutor.class);

	@Override
	public ExecuteResult executeCommand(String[] cmdarray, long timeout, String[] envp, File dir) {
	    String command = StringUtils.join(cmdarray, " ");
	    cmdarray = addWindowsCommandPrefix(cmdarray);
		Process process = null;
		InputStream pIn = null;
		StreamGobbler outputGobbler = null;
		Future<Integer> executeFuture = null;
		try {
			logger.info("开始执行命令：{}", command.toString());
			ProcessBuilder processBuilder = new ProcessBuilder(cmdarray);
			if (dir != null && dir.exists()) {
				processBuilder.directory(dir);
			}
			processBuilder.redirectErrorStream(true);

			process = processBuilder.start();

			final Process p = process;
			pIn = process.getInputStream();
			outputGobbler = new StreamGobbler(pIn, "OUTPUT", encoding);
			outputGobbler.start();

			Callable<Integer> call = new Callable<Integer>() {
				public Integer call() throws Exception {
					p.waitFor();
					return p.exitValue();
				}
			};
			// submit the command's call and get the result from a
			executeFuture = pool.submit(call);
			int exitCode = executeFuture.get(timeout, TimeUnit.MILLISECONDS);
			String retMsg = "";
			logger.info("执行结果为：{}", exitCode);
			retMsg = outputGobbler.getContent();
			return new ExecuteResult(exitCode, retMsg);
		} catch (IOException ex) {
			String errorMessage = "The command [" + command + "] execute failed.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(ExecuteResult.exErrorCode, "IO异常，原因：" + ex.getMessage());
		} catch (TimeoutException ex) {
			String errorMessage = "The command [" + command + "] timed out.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(ExecuteResult.exErrorCode, "命令执行超时，原因：" + ex.getMessage());
		} catch (ExecutionException ex) {
			String errorMessage = "The command [" + command + "] did not complete due to an execution error.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(ExecuteResult.exErrorCode, "执行命令异常：原因：" + ex.getMessage());
		} catch (InterruptedException ex) {
			String errorMessage = "The command [" + command + "] did not complete due to an interrupted error.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(ExecuteResult.exErrorCode, "中断异常：原因：" + ex.getMessage());
		} finally {
			if (process != null) {
				process.destroy();
			}
			if (executeFuture != null) {
				try {
					executeFuture.cancel(true);
				} catch (Exception ignore) {
					ignore.printStackTrace();
				}
			}
			if (pIn != null) {
			    //this.closeQuietly(pIn); 不用显示关闭，如果destroy自然会关闭，否则windows下可能会阻塞在close上
				if (outputGobbler != null && !outputGobbler.isInterrupted()) {
					outputGobbler.interrupt();
				}
			}
		}
	}



	@Override
	public ExecuteResult executeCommand(String command, long timeout) {
		return executeCommand(splitCommand(command), timeout, null, null);
	}

	@Override
	public ExecuteResult executeCommand(String command) {
		return executeCommand(command, Long.MAX_VALUE);
	}

	@Override
	public ExecuteResult executeCommand(String[] cmdarray, long timeout) {
		return executeCommand(cmdarray, timeout, null, null);
	}

	@Override
	public ExecuteResult executeCommand(String[] cmdarray) {
		return executeCommand(cmdarray, Long.MAX_VALUE, null, null);
	}

}