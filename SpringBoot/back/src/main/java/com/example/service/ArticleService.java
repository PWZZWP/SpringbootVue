package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.ArticleEntity;
import com.example.exception.CustomException;
import com.example.mapper.ArticleMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {
    @Resource
    private ArticleMapper articleMapper;

    public void add(ArticleEntity articleEntity) {
        Account currentUser = TokenUtils.getCurrentUser();
        articleEntity.setUserId(currentUser.getId());
        articleEntity.setTime(DateUtil.now());//获取当前最新的时间字符串
        articleEntity.setRole(currentUser.getRole());
        articleMapper.insert(articleEntity);
    }

    public void update(ArticleEntity articleEntity) {
        articleMapper.updateById(articleEntity);
    }

    public void deleteById(Integer id) {
        articleMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for(Integer id:ids){
            this.deleteById(id);
        }
    }
    public List<ArticleEntity> selectAll(ArticleEntity articleEntity) {
      return  articleMapper.selectAll(articleEntity);
    }

    public ArticleEntity selectById(Integer id) {
        return articleMapper.selectById(id);
    }


    public PageInfo<ArticleEntity> selectPage(ArticleEntity articleEntity, Integer pageNum, Integer pageSize) {
        //查之前要先给它条件
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole())){
            articleEntity.setUserId(currentUser.getId());
            articleEntity.setRole(currentUser.getRole());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<ArticleEntity> list=articleMapper.selectAll(articleEntity);
        return PageInfo.of(list);
    }

}
