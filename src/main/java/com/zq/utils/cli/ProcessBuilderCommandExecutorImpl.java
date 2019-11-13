package com.zq.utils.cli;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
/**
* LocalCommandExecutorImpl.java
*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zq.utils.cli.intf.CommandExecutor;

public class ProcessBuilderCommandExecutorImpl implements CommandExecutor {

	public static final Logger logger = LoggerFactory.getLogger(ProcessBuilderCommandExecutorImpl.class);

	public static final String cmdPrefix = "cmd";

	public static boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os.toLowerCase().startsWith("win")) {
			return true;
		}
		return false;
	}

	public static ExecutorService pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 3L, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>());

	private String encoding = "UTF-8";

	public ProcessBuilderCommandExecutorImpl(String encoding) {
		if (StringUtils.isNoneBlank(encoding)) {
			this.encoding = encoding;
		}
	}

	@Override
	public ExecuteResult executeCommand(String[] cmdarray, long timeout, String[] envp, File dir) {
		String command = StringUtils.join(cmdarray, " ");
		if (isWindows() && !command.contains(cmdPrefix)) {
			command = cmdPrefix + " /c " + command;
			String[] f = new String[] { cmdPrefix, "/c", };
			cmdarray = RuntimeCommandExecutorImpl.concat(f, cmdarray);
		}
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
			return new ExecuteResult(-999, "IO异常，原因：" + ex.getMessage());
		} catch (TimeoutException ex) {
			String errorMessage = "The command [" + command + "] timed out.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(-999, "命令执行超时，原因：" + ex.getMessage());
		} catch (ExecutionException ex) {
			String errorMessage = "The command [" + command + "] did not complete due to an execution error.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(-999, "执行命令异常：原因：" + ex.getMessage());
		} catch (InterruptedException ex) {
			String errorMessage = "The command [" + command + "] did not complete due to an interrupted error.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(-999, "中断异常：原因：" + ex.getMessage());
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
				this.closeQuietly(pIn);
				if (outputGobbler != null && !outputGobbler.isInterrupted()) {
					outputGobbler.interrupt();
				}
			}
		}
	}

	private void closeQuietly(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException e) {
			logger.error("exception", e);
		}
	}

	@Override
	public ExecuteResult executeCommand(String command, long timeout) {
		return executeCommand(RuntimeCommandExecutorImpl.splitCommand(command), timeout, null, null);
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