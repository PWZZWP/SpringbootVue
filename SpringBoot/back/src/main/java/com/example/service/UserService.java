package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.AdminEntity;
import com.example.entity.UserEntity;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public void add(UserEntity userEntity) {
        String username= userEntity.getUsername();
        UserEntity dbUser=userMapper.selectByusername(username);//查询数据库是否存在改账号并将记录返回给dbUser.
        if(dbUser!=null){//注册的账号已存在
            throw new CustomException("505","账号已存在");
        }
        if(StrUtil.isBlank(userEntity.getPassword())){//密码没填
            userEntity.setPassword("123");//默认密码
        }
        if(StrUtil.isBlank(userEntity.getName())){//密码没填
            userEntity.setName(userEntity.getUsername());//默认名字
        }
        //一定要设置角色
        userEntity.setRole("USER");//普通用户的角色
        userMapper.insert(userEntity);//调用插入方法将用户信息插入到数据库中。
    }

    public void update(UserEntity userEntity) {
        userMapper.updateById(userEntity);
    }

    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for(Integer id:ids){
            this.deleteById(id);
        }
    }
    public List<UserEntity> selectAll(UserEntity userEntity) {
      return  userMapper.selectAll(userEntity);
    }

    public UserEntity selectById(Integer id) {
        return userMapper.selectById(id);
    }
    public UserEntity selectByIdd(String id) {
        return userMapper.selectByIdd(id);
    }


    public PageInfo<UserEntity> selectPage(UserEntity userEntity, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserEntity> list=userMapper.selectAll(userEntity);
        return PageInfo.of(list);
    }

    /*
    * 普通用户登录*/
    public UserEntity login(Account account) {
        String username= account.getUsername();
        UserEntity dbUser=userMapper.selectByusername(username);
        if(dbUser==null){
            throw new CustomException("500","账号不存在");
        }
        //存在该用户
        String password= account.getPassword();
        if(!dbUser.getPassword().equals(password)){//密码错误
            throw new CustomException("500","账号或密码错误");
        }
        ////创建token并返回给前端
        String token = TokenUtils.createToken(dbUser.getId()+"-"+"USER",dbUser.getPassword());
        dbUser.setToken(token);
        return dbUser;
    }
    /*
    * 普通用户注册*/
    public void register(UserEntity userEntity) {
        this.add(userEntity);
    }

    public void updatePassword(Account account) {
        Integer id= account.getId();
        UserEntity userEntity=this.selectById(id);
        if(!userEntity.getPassword().equals(account.getPassword())){
            //页面传来的密码与数据库密码不匹配，就报错
            throw new CustomException("500","原密码错误");
        }
        userEntity.setPassword(account.getNewPassword());//设置新密码
        this.update(userEntity);
    }

}
