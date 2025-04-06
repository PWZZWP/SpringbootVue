package com.example.exception;
import com.example.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
/**
 * 全局异常处理，处理所有controller抛出的异常**/
@ControllerAdvice("com.example.controller")
public class GlobalExceptionHandle {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandle.class);

    @ExceptionHandler(Exception.class)
//    @ExceptionHandler 注解的参数指定了要处理的异常类型，
//    例如 @ExceptionHandler(Exception.class) 表示处理所有类型的异常
    @ResponseBody // 将result对象转换成Json的格式
    public Result error(Exception e) {
        log.error("系统异常",e);
        return Result.error();
    }

    @ExceptionHandler(CustomException.class)
//    指定了该方法要处理的异常类型为 CustomException，这是一个自定义异常类
    @ResponseBody // 返回json串
    public Result error(CustomException e) {
//        log.error("自定义异常",e);
        //e.printStackTrace();
        return Result.error(e.getCode(), e.getMsg());
    }
}
