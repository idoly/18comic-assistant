package xyz.idoly.comic.entity;

public class Result<T> {

    private int code;
    private String msg;
    private T data;

    private String type;

    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MSG = "success";

    public Result() {
        this.code = 200;
        this.msg = "success";
    }

    public Result(T data) {
        this();
        this.data = data;
    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>();
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(-1, msg);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg);
    }

    public static <T> Result<T> error(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
         this.code = code;
    }

    public String getMsg() {
         return msg;
    }

    public void setMsg(String msg) {
         this.msg = msg;
    }

    public T getData() {
         return data;
    }

    public void setData(T data) {
         this.data = data;
    }

    public Result<T> type(String type) {
        setType(type);
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Result [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }
}
