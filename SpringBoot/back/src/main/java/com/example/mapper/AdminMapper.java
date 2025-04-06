package com.example.mapper;

import com.example.entity.AdminEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import java.util.List;
public interface AdminMapper {//MyBatis的Mapper接口，封装数据库操作，通常对应于一个XMl映射文件或一个注解化的SQL语句
    List<AdminEntity> selectAll(AdminEntity adminEntity);

    /*
    * 通过注解实现查询*/
    @Select("select * from `admin` where id = #{id}")
    AdminEntity selectById(Integer id);

    @Select("select * from `admin` where id = #{id}")
    AdminEntity selectByIdd(String id);





    //返回值类型为void,不返回任何值。接受一个AdminEntity对象。
    void insert(AdminEntity adminEntity);






    void updateById(AdminEntity adminEntity);

    @Delete("delete from `admin` where id = #{id}")
    void deleteById(Integer id);






    @Select("select * from `admin` where username = #{username}")//注解化的SQL语句，查询admin表中username字段为#{username}的记录
    AdminEntity selectByusername(String username);//返回匹配的AdminEntity对象，若无结果则返回null.
}
