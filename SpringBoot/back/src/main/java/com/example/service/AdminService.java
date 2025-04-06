package com.example.service;

import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.AdminEntity;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service//@Service注解表示这是一个服务类，会被Spring容器管理
public class AdminService {
    @Resource//@Resource注解用来注入依赖的对象AdminMapper
    private AdminMapper adminMapper;

//    public void add(AdminEntity adminEntity) {
//        String username= adminEntity.getUsername();//获取用户名，用户名为adminEntity的属性
//        AdminEntity dbAdmin=adminMapper.selectByusername(username);//通过adminMapper对象查询数据库中用户是否存在,并将对象返回和赋值给dbAdmin.
//
//        if(dbAdmin!=null){//对象不为空，说明该用户存在
//            throw new CustomException("505","账号已存在");
//        }
//        //形参adminEntity的属性username,password,role
//        if(StrUtil.isBlank(adminEntity.getPassword())){//密码没填
//            adminEntity.setPassword("adm");//默认密码
//        }
//
//
//        //形参adminEntity的属性name
//        if(StrUtil.isBlank(adminEntity.getName())){//密码没填
//            adminEntity.setName(adminEntity.getUsername());//默认名称为账号名
//        }
//        //默认设置角色
//        adminEntity.setRole("ADMIN");//管理员的角色
//        adminMapper.insert(adminEntity);//新增管理员时调用adminMapper的insert方法，将adminEntity对象插入到数据库中。
//    }




    public void update(AdminEntity adminEntity) {
        adminMapper.updateById(adminEntity);
    }

    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for(Integer id:ids){
            this.deleteById(id);
        }
    }
    public List<AdminEntity> selectAll(AdminEntity adminEntity) {
      return  adminMapper.selectAll(adminEntity);
    }

    public AdminEntity selectById(Integer id) {
        return adminMapper.selectById(id);
    }
    public AdminEntity selectByIdd(String id) {
        return adminMapper.selectByIdd(id);
    }


    public PageInfo<AdminEntity> selectPage(AdminEntity adminEntity, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AdminEntity> list=adminMapper.selectAll(adminEntity);
        return PageInfo.of(list);
    }

    /*
    * 普通用户登录*/
    public AdminEntity login(Account account) {
        String username= account.getUsername();
        AdminEntity dbAdmin=adminMapper.selectByusername(username);
        if(dbAdmin==null){
            throw new CustomException("500","账号或密码错误");
        }
        //存在该用户
        String password= account.getPassword();
        if(!dbAdmin.getPassword().equals(password)){//密码错误
            throw new CustomException("500","账号或密码错误");
        }
        //创建token并返回给前端
        String token = TokenUtils.createToken(dbAdmin.getId()+"-"+"ADMIN",dbAdmin.getPassword());
        dbAdmin.setToken(token);
        return dbAdmin;
    }

    public void updatePassword(Account account) {
        Integer id= account.getId();
        AdminEntity adminEntity=this.selectById(id);//this指向当前类实例adminService,并通过它调用selectById方法，id为前端传来的id值,查询出指定id的管理员实体数据。
        if(!adminEntity.getPassword().equals(account.getPassword())){
            //页面传来的密码与数据库密码不匹配，就报错
            throw new CustomException("500","您输入的原密码错误");
        }
        adminEntity.setPassword(account.getNewPassword());//设置新密码为前端account对象的密码
        this.update(adminEntity);//更新后台对象adminEntity到数据库中
    }


    /*
    * 管理员用户无法注册*/
//    public void register(AdminEntity adminEntity) {
//        this.add(adminEntity);
//    }

}
