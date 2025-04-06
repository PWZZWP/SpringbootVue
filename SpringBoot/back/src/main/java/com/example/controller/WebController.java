package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.UserEntity;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebController {

    @Resource
    private UserService userService;
    @Resource
    private AdminService adminService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    /*
    * 管理员和用户均可登录
    * */
    @PostMapping("/login")
    public Result login(@RequestBody Account account){
        Account dbAccount=null;
        if("ADMIN".equals(account.getRole())){//从account对象中获取角色信息，并与ADMIN进行比较
            dbAccount =  adminService.login(account);//当角色为ADMIN时，调用AdminService的login方法。
        }else if("USER".equals(account.getRole())){
            dbAccount = userService.login(account);
        }else{
            throw new CustomException("500","非法输入");//一旦抛出异常，当前方法会立即停止运行，后续的代码不会执行。
        }
        return Result.success(dbAccount);//正常情况下执行。
    }

    /*
    * 只能是用户注册，管理员不能注册*/
    @PostMapping("/register")
    public Result register(@RequestBody UserEntity userEntity){//@RequestBody注解用于将请求体中的Json数据绑定到UserEntity实体属性上。
        //JSON>>Java Object实体。JSON键名需要与UserEntity字段名一致。
        userService.register(userEntity);
        return Result.success();
    }
    /*
     * 管理员和用户均可修改密码*/
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account){
        if("ADMIN".equals(account.getRole())){
          adminService.updatePassword(account);
        }else if("USER".equals(account.getRole())){
          userService.updatePassword(account);
        }else{
            throw new CustomException("500","非法输入");
        }
        return Result.success();
    }
    }

