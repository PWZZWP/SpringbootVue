package com.example.controller;

import com.example.common.Result;
import com.example.entity.UserEntity;
import com.example.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**新增数据
     * */
    @PostMapping("/add")
    public Result add(@RequestBody UserEntity userEntity){
        userService.add(userEntity);
        return Result.success();
    }
    /**更新数据
     * */
    @PutMapping("/update")
    public Result update(@RequestBody UserEntity userEntity){
        userService.update(userEntity);
        return Result.success();
    }

    /**删除单个数据
     * */
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id){
        userService.deleteById(id);
        return Result.success();
    }

    /**删除多个数据
     * */
    @DeleteMapping("/deleteBatch/")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        userService.deleteBatch(ids);
        return Result.success();
    }
//    /*
//     * 查询所有用户信息
//     */
    @GetMapping("/selectAll")
    public Result selectAll(UserEntity userEntity){
       List<UserEntity> list = userService.selectAll(userEntity);
       return Result.success(list);
  }

/**
     * 根据id查询用户信息
     * */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        UserEntity userEntity = userService.selectById(id);
        return Result.success(userEntity);
    }
    @GetMapping("/selectByIdd/{id}")
    public Result selectByIdd(@PathVariable String id){
        UserEntity userEntity = userService.selectByIdd(id);
        return Result.success(userEntity);
    }

    /**
     * 根据分页查询用户信息
     * pageNum:当前页码
     * pageSize:每页显示条数
     * */
    @GetMapping("/selectPage")
    public Result selectPage(UserEntity userEntity,
            @RequestParam(defaultValue="1") Integer pageNum,
                             @RequestParam(defaultValue="10") Integer pageSize){
        PageInfo<UserEntity> pageInfo = userService.selectPage(userEntity,pageNum,pageSize);
        return Result.success(pageInfo);
    }


}
