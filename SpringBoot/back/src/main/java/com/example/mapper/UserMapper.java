package com.example.mapper;

import com.example.entity.UserEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface UserMapper {
    List<UserEntity> selectAll(UserEntity userEntity);
    /*
    * 通过注解实现查询*/
    @Select("select * from `user` where id = #{id}")
    UserEntity selectById(Integer id);

    @Select("select * from `user` where id = #{id}")
    UserEntity selectByIdd(String id);


    void insert(UserEntity userEntity);

    void updateById(UserEntity userEntity);

    @Delete("delete from `user` where id = #{id}")
    void deleteById(Integer id);

    @Select("select * from `user` where username = #{username}")
    UserEntity selectByusername(String username);
}
