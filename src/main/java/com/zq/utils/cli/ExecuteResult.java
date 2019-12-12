package com.zq.utils.cli;

public class ExecuteResult {
    /**
     * 返回码 -999，代表命令执行过程出现异常！
     */
    public static final int exErrorCode = -999;
    
    /**
     * 命令行的返回码，如果时-999 代表执行出现异常！
     */
    private int exitCode;
    
    /**
     * 返回的控制台输出！
     */
    private String executeOut;

    public ExecuteResult(int exitCode, String executeOut) {
        this.exitCode = exitCode;
        this.executeOut = executeOut;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getExecuteOut() {
        return executeOut;
    }

    public void setExecuteOut(String executeOut) {
        this.executeOut = executeOut;
    }

    @Override
    public String toString() {
        return "ExecuteResult [exitCode=" + exitCode + ", executeOut=" + executeOut + "]";
    }
    
    public boolean isExceptionResult() {
        return exitCode==exErrorCode;
    }

}
