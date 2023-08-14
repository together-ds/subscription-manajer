package ds.together.pw.subscriptionmanajer.exception;

/**
 * @author x
 * @version 1.0
 * @since 2023/7/16 21:52
 */
public class BeanCopyException extends RuntimeException{
    public BeanCopyException() {
        super();
    }

    public BeanCopyException(String message) {
        super(message);
    }

    public BeanCopyException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanCopyException(Throwable cause) {
        super(cause);
    }

    protected BeanCopyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
