package com.nekolr.support;

/**
 * 没有匹配的文件发现
 *
 * @author nekolr
 */
public class NoMatchFileFoundException extends RuntimeException {

    public NoMatchFileFoundException() {
        super();
    }

    public NoMatchFileFoundException(String message) {
        super(message);
    }

    public NoMatchFileFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMatchFileFoundException(Throwable cause) {
        super(cause);
    }

    protected NoMatchFileFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
