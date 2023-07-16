package ds.together.pw.subscriptionmanajer.web;

/**
 * 2020/9/11 21:46
 * @author kunqiang.zhou
 * @version 1.0
 */
public class Result<T> {

    private String message;
    private T data;
    private int code;
    private boolean success;
    private long timestamp;

    public static <T> Result<T> success() {
        return new Result<>(null, null, ResultCode.OK);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(null, data, ResultCode.OK);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(message, data, ResultCode.OK);
    }

    public static <T> Result<T> successMsg(String message) {
        return new Result<>(message, null, ResultCode.OK);
    }

    public static <T> Result<T> message(ResultCode code, String message) {
        return new Result<>(message, null, code);
    }

    public static <T> Result<T> failed(ResultCode code, String message, T data) {
        return new Result<>(message, data, code);
    }

    public static <T> Result<T> failed(ResultCode code, T data) {
        return new Result<>(null, data, code);
    }


    public Result(String message, T data, ResultCode code) {
        this.message = message == null ? code.getValue() : message;
        this.data = data;
        this.code = code.getCode();
        this.success = code.isSuccess();
        this.timestamp = System.currentTimeMillis();
    }

    public Result() {
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public int getCode() {
        return code;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
