package com.example.controller;

import com.example.common.Result;
import com.example.entity.AdminEntity;
import com.example.service.AdminService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController//声明当前类是一个控制器类
@RequestMapping("/admin")//声明当前类下所有方法的请求路径前缀
public class AdminController {
    @Resource//注入AdminService
    private AdminService adminService;

    /*
     * admin無法直接新增管理員，得在数据库添加
     * */
//    @PostMapping("/add")//声明当前方法为POST请求，请求路径为/admin/add
//    public Result add(@RequestBody AdminEntity adminEntity){//@RequestBody注解用于将请求体中的json数据绑定到AdminEntity实体类中
//        adminService.add(adminEntity);//调用AdminService的add方法添加数据
//        return Result.success();//返回成功信息
//    }
    /**更新数据
     * */
    @PutMapping("/update")
    public Result update(@RequestBody AdminEntity adminEntity){
        adminService.update(adminEntity);
        return Result.success();
    }

    /**删除单个数据
     * */
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id){
        adminService.deleteById(id);
        return Result.success();
    }

    /**删除多个数据
     * */
    @DeleteMapping("/deleteBatch/")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        adminService.deleteBatch(ids);
        return Result.success();
    }
//    /*
//     * 查询所有用户信息
//     */
    @GetMapping("/selectAll")
    public Result selectAll(AdminEntity adminEntity){
       List<AdminEntity> list = adminService.selectAll(adminEntity);
       return Result.success(list);
  }

/**
     * 根据id查询用户信息
     * */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable String id){
        AdminEntity adminEntity = adminService.selectByIdd(id);
        return Result.success(adminEntity);
    }

    /**
     * 根据分页查询用户信息
     * pageNum:当前页码
     * pageSize:每页显示条数
     * */
    @GetMapping("/selectPage")
    public Result selectPage(AdminEntity adminEntity,
            @RequestParam(defaultValue="1") Integer pageNum,
                             @RequestParam(defaultValue="10") Integer pageSize){
        PageInfo<AdminEntity> pageInfo = adminService.selectPage(adminEntity,pageNum,pageSize);
        return Result.success(pageInfo);
    }


}
