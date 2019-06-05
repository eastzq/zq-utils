/**
 * 福建新意科技有限公司 1997-2012 版权所有.
 */
package com.zq.utils.web;

/**
 * 
 * 针对Rpc调用的调用结果结构.
 * 
 */
public class RpcResult {

    /**
     * 错误信息代码
     */
    public final static int CODE_ERROR = -1;

    /**
     * 正确信息代码
     */
    public final static int CODE_SUCCESS = 0;

    /**
     * 正确执行的提示信息
     */
    public final static String MSG_SUCCESS = "操作成功!";

    /**
     * 出错的提示信息
     */
    private static final String MSG_ERROR = "操作失败";

    /**
     * 操作状态
     */
    protected RpcStatus status;

    /**
     * 返回结果,所有需要返回到前端的数据须放到此对象中
     */
    private Object result;


    /**
     * 构造函数.
     */
    public RpcResult() {
    }

    /**
     * 
     * 出错的调用结果创建器.
     * @param errMsg 错误信息
     * @return 调用结果结构.
     * 
     */
    public static final RpcResult error(String errMsg) {
        RpcResult result = new RpcResult();
        RpcStatus rpcStatus = new RpcStatus(errMsg,CODE_ERROR);
        result.setStatus(rpcStatus);
        return result;
    }
    
    /**
     * 
     * 成功的调用结果创建器.
     * @return 调用结果结构.
     */
    public static final RpcResult success() {
        RpcResult result = new RpcResult();
        RpcStatus rpcStatus = new RpcStatus(MSG_SUCCESS, CODE_SUCCESS);
        result.setStatus(rpcStatus);
        return result;
    }
    
    
    /**
     * 类说明：操作状态. 要申明为public才可以输出信息.
     */
    public static class RpcStatus {
        /**
         * 信息内容
         */
        private String message;
        /**
         * 信息代码
         */
        private int code;

        /**
         * 构造器
         * 
         * @param text 信息内容
         * @param code 信息代码
         * 
         *            <pre>
         * 修改日期      修改人    修改原因
         * 2010-04-21　      陈建榕　　　新建
         * </pre>
         */
        public RpcStatus(String text, int code) {
            this.message = text;
            this.code = code;
        }

        /**
         * 构造器
         * 
         * @param text 信息内容
         * 
         *            <pre>
         * 修改日期      修改人    修改原因
         * 2010-04-21　      陈建榕　　　新建
         * </pre>
         */
        public RpcStatus(String text) {
            this.message = text;
            this.code = CODE_ERROR;
        }

        /**
         * 信息内容
         * 
         * @return the text
         */
        public String getMessage() {
            return message;
        }

        /**
         * 信息内容
         * 
         * @param text the text to set
         */
        public void setText(String text) {
            this.message = text;
        }

        /**
         * 信息代码
         * 
         * @return the code
         */
        public int getCode() {
            return code;
        }

        /**
         * 信息代码
         * 
         * @param code the code to set
         */
        public void setCode(int code) {
            this.code = code;
        }

    }

    /**
     * getResult.
     * 
     * @return the result
     */
    public Object getResult() {
        return result;
    }

    /**
     * setResult.
     * 
     * @param result the result to set
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * getStatus.
     * 
     * @return the msg
     */
    public RpcStatus getStatus() {
        return status;
    }

    /**
     * setStatus.
     * 
     * @param status the Status to set
     */
    public void setStatus(RpcStatus status) {
        this.status = status;
    }


}
