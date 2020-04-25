package com.sidneysimmons.plentifulports.kubernetes.exception;

/**
 * Exception for issues integrating with kubernetes.
 * 
 * @author Sidney Simmons
 */
public class KubernetesException extends Exception {

    private static final long serialVersionUID = 1L;

    public KubernetesException() {
        super();
    }

    public KubernetesException(String message) {
        super(message);
    }

    public KubernetesException(Throwable cause) {
        super(cause);
    }

    public KubernetesException(String message, Throwable cause) {
        super(message, cause);
    }

    public KubernetesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
