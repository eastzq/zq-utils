package com.zq.utils.cli;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
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

public class RuntimeCommandExecutor extends BaseCommandExecutor {

    public static final Logger logger = LoggerFactory.getLogger(RuntimeCommandExecutor.class);

    public RuntimeCommandExecutor() {
    }

    public RuntimeCommandExecutor(String encoding) {
        super();
    }

    @Override
    public ExecuteResult executeCommand(String[] cmdarray, long timeout, String[] envp, File dir) {
        String command = StringUtils.join(cmdarray, " ");
        cmdarray = addWindowsCommandPrefix(cmdarray);
        Process process = null;
        InputStream pIn = null;
        InputStream pErr = null;
        StreamGobbler outputGobbler = null;
        StreamGobbler errorGobbler = null;
        Future<Integer> executeFuture = null;
        try {
            logger.info("开始执行命令：{}", command.toString());
            process = Runtime.getRuntime().exec(cmdarray, envp, dir);
            final Process p = process;

            // close process's output stream.
            p.getOutputStream().close();

            pIn = process.getInputStream();
            outputGobbler = new StreamGobbler(pIn, "OUTPUT", encoding);
            outputGobbler.start();

            pErr = process.getErrorStream();
            errorGobbler = new StreamGobbler(pErr, "ERROR", encoding);
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
            logger.info("执行结果为：{}", exitCode);
            if (exitCode == 0) {
                retMsg = outputGobbler.getContent();
            } else {
                retMsg = errorGobbler.getContent();
            }
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
                if (outputGobbler != null && !outputGobbler.isInterrupted()) {
                    outputGobbler.interrupt();
                }
            }
            if (pErr != null) {
                if (errorGobbler != null && !errorGobbler.isInterrupted()) {
                    errorGobbler.interrupt();
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