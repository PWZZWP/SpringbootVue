package com.example.mapper;

import com.example.entity.ArticleEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleMapper {
    List<ArticleEntity> selectAll(ArticleEntity articleEntity);

    /*
     * 通过注解实现查询*/
    @Select("select * from `article` where id = #{id}")
    ArticleEntity selectById(Integer id);


    void insert(ArticleEntity articleEntity);

    void updateById(ArticleEntity articleEntity);

    @Delete("delete from `article` where id = #{id}")
    void deleteById(Integer id);
}

