package com.lyndonding.projecttemplate.framework;

/**
 * SD卡不存在异常
 */

public class ExternalStorageNotReadyException extends RuntimeException {

	private static final long serialVersionUID = 7287893177828417752L;

    public ExternalStorageNotReadyException() {
    }

    public ExternalStorageNotReadyException(String detailMessage) {
        super(detailMessage);
    }

    public ExternalStorageNotReadyException(Throwable cause) {
        super(cause);
    }
}
