package ds.together.pw.subscriptionmanajer.web;

public enum ResultCode {
    /*
    * 0~999 => success
    * */
    OK(0, "OK"),

    /*
     * 1000+ => error
     * */
    SERVER_ERROR(1000, "SERVER ERROR");


    ResultCode(int code, String value) {
        this.code = code;
        this.value = value;
    }

    private final int code;
    private final String value;

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public boolean isSuccess(){
        return this.code < 1000;
    }
}