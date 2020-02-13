package com.zq.utils.kafka;

/**
 * 消息异常类。运行时异常
 **/

public class MqException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MqException() {
        super();
    }

    public MqException(String message) {
        super(message);
    }

    public MqException(String message, Throwable cause) {
        super(message, cause);
    }

    public MqException(Throwable cause) {
        super(cause);
    }

}
