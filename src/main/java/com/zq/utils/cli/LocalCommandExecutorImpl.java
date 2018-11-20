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

/**
* LocalCommandExecutorImpl.java
*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zq.utils.cli.intf.LocalCommandExecutor;

public class LocalCommandExecutorImpl implements LocalCommandExecutor {

	public static final Logger logger = LoggerFactory.getLogger(LocalCommandExecutorImpl.class);
	public static final String cmdPrefix = "cmd";
	public static ExecutorService pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 3L, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>());

	@Override
	public ExecuteResult executeCommand(String command, long timeout, String[] envp, File dir) {
		if (!command.contains(cmdPrefix)) {
			command = cmdPrefix + " /c " +command;
		}
		Process process = null;
		InputStream pIn = null;
		InputStream pErr = null;
		StreamGobbler outputGobbler = null;
		StreamGobbler errorGobbler = null;
		Future<Integer> executeFuture = null;
		try {
			logger.info("开始执行命令：{}", command.toString());
			process = Runtime.getRuntime().exec(command,envp,dir);
			final Process p = process;

			// close process's output stream.
			p.getOutputStream().close();

			pIn = process.getInputStream();
			outputGobbler = new StreamGobbler(pIn, "OUTPUT");
			outputGobbler.start();

			pErr = process.getErrorStream();
			errorGobbler = new StreamGobbler(pErr, "ERROR");
			errorGobbler.start();

			// create a Callable for the command's Process which can be called by an
			// Executor
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

			if (exitCode == 0) {
				retMsg = outputGobbler.getContent();
			} else {
				retMsg = errorGobbler.getContent();
			}
			return new ExecuteResult(exitCode, retMsg);

		} catch (IOException ex) {
			String errorMessage = "The command [" + command + "] execute failed.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(-1, null);
		} catch (TimeoutException ex) {
			String errorMessage = "The command [" + command + "] timed out.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(-1, null);
		} catch (ExecutionException ex) {
			String errorMessage = "The command [" + command + "] did not complete due to an execution error.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(-1, null);
		} catch (InterruptedException ex) {
			String errorMessage = "The command [" + command + "] did not complete due to an interrupted error.";
			logger.error(errorMessage, ex);
			return new ExecuteResult(-1, null);
		} finally {
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
			if (pErr != null) {
				this.closeQuietly(pErr);
				if (errorGobbler != null && !errorGobbler.isInterrupted()) {
					errorGobbler.interrupt();
				}
			}
			if (process != null) {
				process.destroy();
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
		return executeCommand(command, timeout, null, null);
	}
}