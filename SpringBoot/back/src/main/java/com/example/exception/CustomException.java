package com.example.exception;

public class CustomException extends RuntimeException {
    private String code;
    private String msg;

    public CustomException(String code,String msg){//有参构造函数
        this.code = code;
        this.msg = msg;
    }
    public CustomException(){//无参构造函数

    }
    public String getCode() {//获取值时，返回值类型为String，直接用return返回。不需要形参
        return code;
    }

    public void setCode(String code) {//赋值时，返回值类型为void,所以可以省略，但需要传递形参
        this.code = code;//把形参的值赋给当前对象的成员变量
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

