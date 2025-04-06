package com.example.controller;

import com.example.common.Result;
import com.example.entity.ArticleEntity;
import com.example.service.ArticleService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {
    @Resource
    private ArticleService articleService;

    /**新增数据
     * */
    @PostMapping("/add")
    public Result add(@RequestBody ArticleEntity articleEntity){
        articleService.add(articleEntity);
        return Result.success();
    }
    /**更新数据
     * */
    @PutMapping("/update")
    public Result update(@RequestBody ArticleEntity articleEntity){
        articleService.update(articleEntity);
        return Result.success();
    }

    /**删除单个数据
     * */
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Integer id){
        articleService.deleteById(id);
        return Result.success();
    }

    /**删除多个数据
     * */
    @DeleteMapping("/deleteBatch/")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        articleService.deleteBatch(ids);
        return Result.success();
    }
//    /*
//     * 查询所有用户信息
//     */
    @GetMapping("/selectAll")
    public Result selectAll(ArticleEntity articleEntity){
       List<ArticleEntity> list = articleService.selectAll(articleEntity);
       return Result.success(list);
  }

/**
     * 根据id查询用户信息
     * */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        ArticleEntity articleEntity = articleService.selectById(id);
        return Result.success(articleEntity);
    }

    /**
     * 根据分页查询用户信息
     * pageNum:当前页码
     * pageSize:每页显示条数
     * */
    @GetMapping("/selectPage")
    public Result selectPage(ArticleEntity articleEntity,
            @RequestParam(defaultValue="1") Integer pageNum, @RequestParam(defaultValue="10") Integer pageSize){
        PageInfo<ArticleEntity> pageInfo = articleService.selectPage(articleEntity,pageNum,pageSize);
        return Result.success(pageInfo);
    }


}
